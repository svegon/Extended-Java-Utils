package svegon.utils.reflect;

import org.jetbrains.annotations.Nullable;
import svegon.utils.collections.ArrayUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import jdk.internal.reflect.ConstructorAccessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ReflectionUtil {
    private ReflectionUtil() {
        throw new AssertionError();
    }

    public static final Field FIELD_MODIFIERS_FIELD;
    public static final Field FIELD_DECLARED_ANNOTATIONS_FIELD;
    public static final Field METHOD_MODIFIERS_FIELD;
    public static final Field CONSTRUCTOR_ACCESSOR_FIELD;
    public static final Method ACQUIRE_CONSTRUCTOR_ACCESSOR_METHOD;

    public static final Class<?>[] EMPTY_CLASS_ARRAY = {};

    /**
     * Parses a string address into a class in a way that is similar to the one used by spongepowered mixins
     * @param name the string address identifying the specified class
     * @return an annotated element matching the address
     * or null if and only if {@param name}.isEmpty() || {@param name}.equals("null")
     * @throws NullPointerException if {@param name} == null
     * @throws IllegalArgumentException if the specified name doesn't match any class
     * @throws SecurityException if getting the class is denied by the SecurityManager
     */
    public static Class<?> classByName(@NotNull String name) {
        // first eliminate primitives
        return switch (name) {
            case "", "null" -> throw new IllegalArgumentException("null object has no class.");
            case "Z", "boolean" -> boolean.class;
            case "B", "byte" -> byte.class;
            case "S", "short" -> short.class;
            case "I", "int" -> int.class;
            case "J", "long" -> long.class;
            case "C", "char" -> char.class;
            case "F", "float" -> float.class;
            case "D", "double" -> double.class;
            case "V", "void" -> void.class;
            case "L", "java.lang.Object", "Ljava.lang.Object", "Ljava.lang.Object;" -> Object.class;
            default -> classByName0(name);
        };
    }

    private static Class<?> classByName0(@NotNull String name) {
        if (name.startsWith("L")) {
            if (name.endsWith(";")) {
                name = name.substring(1, name.length() - 1);
            } else {
                name = name.substring(1);
            }
        }

        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            // most likely we're looking for a class described differently from what Class.forName would find
        }

        // check for an array annotated with [] at the end
        if (name.endsWith("[]")) {
            int i;

            for (i = name.length() - 2; i >= 0 && name.charAt(i) == '[' && name.charAt(i + 1) == ']'; i -= 2);

            return Array.newInstance(classByName(name.substring(0, i + 2)),
                    new int[(name.length() - i) / 2]).getClass();
        }

        // lambda specified by an enclosing method/constructor
        String[] var0 = name.split(";");

        if (var0.length == 2) {
            String[] var1 = var0[0].split("\\$");

            if (var1.length == 2) {
                if (var1[1].startsWith("$")) {
                    var1[1] = var1[1].substring(1);
                }

                String[] var2 = var1[1].split("\\$");

                if (var2.length == 1 || var2.length == 2) {
                    Class<?> declaringClass = classByName(var0[0]);
                    Class<?> superclass = classByName(var2[0]);
                    int oridinal = var2.length == 2 ? Integer.parseInt(var2[1]) : 0;

                    if (oridinal < 0) {
                        throw new IllegalArgumentException(name + " doesn't match any class.");
                    }

                    if (var1[0].startsWith("<init>")) {
                        Constructor<?> enclosingConstructor = constructorByName(declaringClass, var1[0]);

                        if (superclass.isInterface()) {
                            for (Class<?> declaredClass : declaringClass.getDeclaredClasses()) {
                                if (!declaredClass.isAnonymousClass()) {
                                    continue;
                                }

                                if (declaredClass.getEnclosingConstructor() != enclosingConstructor) {
                                    continue;
                                }

                                if (!Arrays.asList(declaredClass.getInterfaces()).contains(superclass)) {
                                    continue;
                                }

                                if (oridinal-- == 0) {
                                    return declaredClass;
                                }
                            }
                        } else {
                            for (Class<?> declaredClass : declaringClass.getDeclaredClasses()) {
                                if (!declaredClass.isAnonymousClass()) {
                                    continue;
                                }

                                if (declaredClass.getEnclosingConstructor() != enclosingConstructor) {
                                    continue;
                                }

                                if (declaredClass.getSuperclass() != superclass) {
                                    continue;
                                }

                                if (oridinal-- == 0) {
                                    return declaredClass;
                                }
                            }
                        }
                    } else {
                        Method enclosingMethod = methodByName(declaringClass, var1[0]);

                        if (superclass.isInterface()) {
                            for (Class<?> declaredClass : declaringClass.getDeclaredClasses()) {
                                if (!declaredClass.isAnonymousClass()) {
                                    continue;
                                }

                                if (declaredClass.getEnclosingMethod() != enclosingMethod) {
                                    continue;
                                }

                                if (!Arrays.asList(declaredClass.getInterfaces()).contains(superclass)) {
                                    continue;
                                }

                                if (oridinal-- == 0) {
                                    return declaredClass;
                                }
                            }
                        } else {
                            for (Class<?> declaredClass : declaringClass.getDeclaredClasses()) {
                                if (!declaredClass.isAnonymousClass()) {
                                    continue;
                                }

                                if (declaredClass.getEnclosingMethod() != enclosingMethod) {
                                    continue;
                                }

                                if (declaredClass.getSuperclass() != superclass) {
                                    continue;
                                }

                                if (oridinal-- == 0) {
                                    return declaredClass;
                                }
                            }
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException(name + " doesn't match any class.");
    }

    public static Field fieldByName(@NotNull Class<?> enclosingClass, @NotNull String fieldSpecifier) {
        String[] nameType = fieldSpecifier.split(":");

        if (nameType.length != 2) {
            throw new IllegalArgumentException("Illegal syntax: " + fieldSpecifier);
        }

        String fieldName = nameType[0];
        Class<?> fieldType = classByName(nameType[1]);
        Field field;

        try {
            field = enclosingClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }

        if (field.getType().equals(fieldType)) {
            return field;
        }

        throw new IllegalArgumentException("Found a field named \"" + fieldName + "\" in "
                + enclosingClass.getName() + " but it's type is " + field.getType().getName() + " not "
                + fieldType + "!");
    }

    public static Field fieldByName(@NotNull String name) {
        String[] var = name.split(";", 2);

        if (var.length != 2) {
            throw new IllegalArgumentException("There is a syntax error in " + name + "!");
        }

        return fieldByName(classByName(var[0]), var[1]);
    }

    public static Method methodByName(@NotNull Class<?> enclosingClass, @NotNull String methodSpecifier) {
        String[] var0 = methodSpecifier.split("\\(");

        if (var0.length != 2) {
            throw methodNotFound(enclosingClass, methodSpecifier);
        }

        Pair<Class<?>[], Class<?>> methodSignature = parseExecutableSignature(var0[1]);
        String methodName = var0[0];
        Class<?>[] args = methodSignature.left();
        Class<?> returnClass = methodSignature.right();

        if (returnClass == null) {
            throw methodNotFound(enclosingClass, methodSpecifier);
        }

        try {
            Method method = enclosingClass.getDeclaredMethod(methodName, args);

            if (!method.getReturnType().equals(returnClass)) {
                throw new IllegalArgumentException("missmatching return type for method "
                        + method.getDeclaringClass().getName() + method.getName());
            }

            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Contract("_, _ -> new")
    public static @NotNull IllegalArgumentException methodNotFound(@NotNull Class<?> enclosingClass,
                                                                   @NotNull String methodSpecifier) {
        return new IllegalArgumentException(methodSpecifier + " doesn't match any method at class "
                + enclosingClass.getName() + ".");
    }

    @Contract("_ -> new")
    private static @NotNull Pair<Class<?>[], Class<?>> parseExecutableSignature(@NotNull String signatureString) {
        int length = signatureString.length();
        List<Class<?>> args = Lists.newArrayList();
        int index = 0;
        Class<?> returnClass = null;

        while (index < length) {
            char chr = signatureString.charAt(index);
            String argClassName;

            if (chr == 'L') {
                int start = index + 1;
                // using anonymous classes in method arguments is impossible at compile time
                index = signatureString.indexOf(';');

                if (index < 0) {
                    throw new IllegalArgumentException("unended arg class path at " + (start > 20
                            ? signatureString.substring(length - 20) : signatureString) + "<");
                }

                argClassName = signatureString.substring(start, index);
            } else if (chr == ')') {
                if (index < length - 1) {
                    returnClass = classByName(signatureString.substring(index + 1));
                }

                break;
            } else {
                argClassName = String.valueOf(chr);
            }

            args.add(classByName(argClassName));
        }

        return new ObjectObjectImmutablePair<>(args.toArray(EMPTY_CLASS_ARRAY), returnClass);
    }

    /**
     * Parses a string address into a method in a way that accepts the one used by spongepowered mixins
     * @param name the string address identifying the specified method
     * @return the method matching the identifier
     * @throws NullPointerException if {@param name} == null
     * @throws IllegalArgumentException if the specified name doesn't match any name
     * @throws SecurityException if getting the method is denied by the SecurityManager
     */
    public static Method methodByName(@NotNull String name) {
        String[] var = name.split(";", 2);

        if (var.length != 2) {
            throw new IllegalArgumentException("There is a syntax error in \"" + name + "\"!");
        }

        return methodByName(classByName(var[0]), var[1]);
    }

    public static Constructor<?> constructorByName(@NotNull Class<?> declaringClass,
                                                   @NotNull String constructorSpecifier) {
        if (!constructorSpecifier.startsWith("<init>")) {
            throw new IllegalArgumentException(constructorSpecifier + " doesn't specify a constructor!");
        }

        Pair<Class<?>[], Class<?>> signature = parseExecutableSignature(constructorSpecifier.substring(6));

        if (signature.right() != null) {
            throw new IllegalArgumentException("Cannot return in a constructor!");
        }

        try {
            return declaringClass.getConstructor(signature.first());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Constructor<?> constructorByName(@NotNull String name) {
        String[] var = name.split(";", 2);

        if (var.length != 2) {
            throw new IllegalArgumentException("There is a syntax error in " + name + "!");
        }

        return constructorByName(classByName(var[0]), var[1]);
    }

    public static Executable executableByName(@NotNull Class<?> declaringClass,
                                                   @NotNull String specifier) {
        return specifier.startsWith("<init>") ? constructorByName(declaringClass, specifier)
                : methodByName(declaringClass, specifier);
    }

    public static Executable executableByName(@NotNull String name) {
        String[] var = name.split(";", 2);

        if (var.length != 2) {
            throw new IllegalArgumentException("There is a syntax error in " + name + "!");
        }

        return executableByName(classByName(var[0]), var[1]);
    }

    /**
     * Parses a string address into a class in a way that is similar to the one used by spongepowered mixins
     * @param name the string address identifying the specified element
     * @return an annotated element matching the address
     */
    public static AnnotatedElement forName(@NotNull String name) {
        try {
            return classByName(name);
        } catch (IllegalArgumentException e) {
            try {
                return fieldByName(name);
            } catch (IllegalArgumentException ignored) {
                return executableByName(name);
            }
        }
    }

    @Contract("_ -> param1")
    @SuppressWarnings("unchecked")
    public static @NotNull Field makeAccessible(Field field) {
        try {
            field.setAccessible(true);
            field.getDeclaredAnnotations(); // just to make sure the declared annotations initialize
            FIELD_MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            Map<Class<? extends Annotation>, Annotation> declaredAnnotations =
                    (Map<Class<? extends Annotation>, Annotation>) FIELD_DECLARED_ANNOTATIONS_FIELD.get(field);

            if (declaredAnnotations != Collections.EMPTY_MAP) {
                declaredAnnotations.remove(jdk.internal.vm.annotation.Stable.class);
            }

            return field;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Contract("_ -> param1")
    public static @NotNull Method makeAccessible(Method method) {
        method.setAccessible(true);
        return method;
    }

    @Contract("_ -> param1")
    public static <T> @NotNull Constructor<T> makeAccessible(Constructor<T> constructor) {
        constructor.setAccessible(true);
        return constructor;
    }

    public static <T> ConstructorAccessor getAccessor(Constructor<T> constructor) {
        try {
            ConstructorAccessor ca = (ConstructorAccessor) CONSTRUCTOR_ACCESSOR_FIELD.get(makeAccessible(constructor));

            if (ca != null) {
                return ca;
            }

            return (ConstructorAccessor) ACQUIRE_CONSTRUCTOR_ACCESSOR_METHOD.invoke(constructor);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new instance using the specified constructor and arguments.
     * @param constructor the constructor of the instance
     * @param args arguments to be passed to the constructor
     * @param <T> type of the resulting instance
     * @return a new instance of {@param <T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Constructor<?> constructor, Object... args) {
        try {
            return (T) getAccessor(constructor).newInstance(args);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getCause());
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a new instance of the specified class using a constructor using
     * the specified argument classed passing the specified arguments.
     * @param clazz class to be instantiated
     * @param argClasses classes of the arguments specified in the constructor
     * @param args arguments to be passed to the constructor
     * @param <T> type of the resulting instance
     * @return a new instance of {@param clazz}
     * @throws IllegalArgumentException if the clazz couldn't be instantiated either due to mismatch of arguments
     * or due to being impossible to instantiate
     */
    public static <T> T instantiate(Class<T> clazz, Class<?>[] argClasses, Object... args) {
        if (argClasses.length != args.length) {
            throw new IllegalArgumentException("argClasses.length and args.length don't match!");
        }

        try {
            return instantiate(clazz.getConstructor(argClasses), args);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns a list of all non-static fields declared on the given class and all of
     * its superclasses rdered from the top most class (Object) to the current class.
     *
     * @param clazz the class object
     * @return a list of non-static fields declared on the given class and all
     * of its superclasses
     */
    public static List<Field> getAllFields(@Nullable Class<?> clazz) {
        List<Field> list = Lists.newArrayList();

        while (clazz != null) {
            list.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        list.removeIf((field) -> Modifier.isStatic(field.getModifiers()));

        return list;
    }

    /**
     * Creates a new instance of {@param enumClass} at the position following position
     * note: this implementation doesn't work for abstract enums
     * @param enumClass a class for which Class.isEnum() returns true
     * @param constantName a name of the constant, should be different from all other names of constant on the class
     * @param argClasses classes of arguments, this may differ classes of each argument
     *                  if superclasses or fast classes are sued
     * @param args arguments used by the class'es constructor
     * @param <E> type of the instance to be instantiated
     * @return a new instance instantiated with the given arguments
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E newEnumConstant(Class<E> enumClass, String constantName,
                                                        Class<?>[] argClasses, Object... args) {
        if (argClasses.length != args.length) {
            throw new IllegalArgumentException("argClasses.length and args.length don't match!");
        }

        synchronized (Preconditions.checkNotNull(enumClass)) {
            // modify arguments since the enum's constructor actually takes the constant's name and ordinal as well
            argClasses = ArrayUtil.copyAtEnd(argClasses, argClasses.length + 2);
            argClasses[0] = String.class;
            argClasses[1] = int.class;

            args = ArrayUtil.copyAtEnd(args, args.length + 2);
            args[0] = constantName;

            try {
                // retrieve current values since we need their length to determine the ordinal
                Field valuesField = makeAccessible(enumClass.getDeclaredField("$VALUES"));
                E[] oldValues = (E[]) valuesField.get(null);
                args[1] = oldValues.length;

                // instantiate the new enum value
                E enumValue = instantiate(enumClass, argClasses, args);

                // modify the enum's values to include the new value as well
                E[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
                newValues[oldValues.length] = enumValue;
                valuesField.set(null, newValues);

                // update enumClass.getEnumConstants()
                Field enumConstants = makeAccessible(enumClass.getDeclaredField("enumConstants"));
                Field enumConstantDirectory = makeAccessible(enumClass.getDeclaredField("enumConstantDirectory"));
                // the values can be set to null, they will regenerate next time then
                enumConstants.set(null, null);
                enumConstantDirectory.set(null, null);

                return enumValue;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public static <E extends Enum<E>> E newEnumConstant(Class<E> enumClass, String constantName, Object... args) {
        return newEnumConstant(enumClass, constantName,
                Arrays.stream(args).map(Object::getClass).toArray(Class[]::new), args);
    }

    static {
        try {
            FIELD_MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
            FIELD_MODIFIERS_FIELD.setAccessible(true);
            FIELD_DECLARED_ANNOTATIONS_FIELD = Field.class.getDeclaredField("declaredAnnotations");
            FIELD_DECLARED_ANNOTATIONS_FIELD.setAccessible(true);
            METHOD_MODIFIERS_FIELD = makeAccessible(Method.class.getDeclaredField("modifiers"));
            CONSTRUCTOR_ACCESSOR_FIELD =
                    makeAccessible(Constructor.class.getDeclaredField("constructorAccessor"));
            ACQUIRE_CONSTRUCTOR_ACCESSOR_METHOD
                    = makeAccessible(Constructor.class.getDeclaredMethod("acquireConstructorAccessor"));
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}