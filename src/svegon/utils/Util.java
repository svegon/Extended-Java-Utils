package svegon.utils;

import svegon.utils.collections.ArrayUtil;
import svegon.utils.interfaces.DeepClonable;
import svegon.utils.interfaces.IdentityComparable;
import svegon.utils.reflect.CommonReflections;
import svegon.utils.reflect.ReflectionUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Util {
    private Util() {
        throw new AssertionError();
    }

    private static final Map<Class<?>, DeepCopyMethod> DEEP_COPY_METHOD_CACHE =
            new Object2ObjectOpenCustomHashMap<>(HashUtil.identityStrategy());
    private static final Map<DeepCopyMethod, ArrayDeepCopy> ARRAY_DEEP_COPY_CACHE =
            new Object2ObjectOpenCustomHashMap<>(HashUtil.identityStrategy());
    private static final DeepCopyMethod IDENTITY_COPY = new DeepCopyMethod() {
        @Override
        public <E> E copy(E original) throws CloneNotSupportedException {
            return original;
        }
    };
    private static final DeepCopyMethod PURE_CLONE = new DeepCopyMethod() {
        @Override
        @SuppressWarnings("unchecked")
        public <E> E copy(E original) throws CloneNotSupportedException {
            try {
                return (E) CommonReflections.CLONE_METHOD.invoke(original, (Object[]) null);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new CloneNotSupportedException();
            }
        }
    };
    private static final DeepCopyMethod DEEP_CLONE = new DeepCopyMethod() {
        @Override
        @SuppressWarnings("unchecked")
        public <E> E copy(E original) throws CloneNotSupportedException {
            return (E) ((DeepClonable) original).deepClone();
        }
    };
    private static final DeepCopyMethod OBJECT_ARRAY_COPY = new DeepCopyMethod() {
        @Override
        @SuppressWarnings("unchecked")
        public <E> E copy(E original) throws CloneNotSupportedException {
            Object[] array = (Object[]) original;
            Object[] copy = ArrayUtil.newArray(array);

            for (int i = 0; i < copy.length; i++) {
                copy[i] = deepCopy(array[i]);
            }

            return (E) copy;
        }
    };


    public static <E> E make(Supplier<E> creatingFunction) {
        return creatingFunction.get();
    }

    public static <E> E make(E obj, Consumer<E> initializer) {
        initializer.accept(obj);
        return obj;
    }

    /**
     * Creates a deep copy of the given object.
     * It is possible that the result will be the original itself if its deeply immutable or a similar reason.
     * If original == null then return null.
     * @param original the object who will be deeply copied
     * @return a deep copy o
     * @throws CloneNotSupportedException if the deep copy can't be calculated
     */
    @SuppressWarnings("unchecked")
    public static <E> E deepCopy(@Nullable E original) throws CloneNotSupportedException {
        // first let's filter some built-in deeply immutable object
        if (original == null || original instanceof Enum) {
            return original;
        }

        Class<?> clazz = original.getClass();
        if (DEEP_COPY_METHOD_CACHE.containsKey(clazz)) {
            return DEEP_COPY_METHOD_CACHE.get(clazz).copy(original);
        }

        if (original instanceof DeepClonable) {
            DEEP_COPY_METHOD_CACHE.put(clazz, DEEP_CLONE);
            return DEEP_CLONE.copy(original);
        }

        if (original instanceof IdentityComparable) {
            DeepCopyMethod copyMethod = PURE_CLONE;
            E copy;

            try {
                copy = copyMethod.copy(original);
            } catch (CloneNotSupportedException e) {
                copyMethod = IDENTITY_COPY;
                copy = copyMethod.copy(original);
            }

            DEEP_COPY_METHOD_CACHE.put(clazz, copyMethod);
            return copy;
        }

        if (clazz.isArray()) {
            Class<?> componentType = clazz.componentType();

            if (Modifier.isFinal(componentType.getModifiers())) {
                Object[] array = (Object[]) original;

                if (DEEP_COPY_METHOD_CACHE.containsKey(componentType)) {
                    DeepCopyMethod copyMethod = ARRAY_DEEP_COPY_CACHE.computeIfAbsent(DEEP_COPY_METHOD_CACHE
                                    .get(componentType), ArrayDeepCopy::new);
                    DEEP_COPY_METHOD_CACHE.put(clazz, copyMethod);
                    return copyMethod.copy(original);
                }

                // well we weren't able to generate a deep copy method, so we're just try next time
                return OBJECT_ARRAY_COPY.copy(original);
            } else if (Enum.class.isAssignableFrom(componentType)) {
                DeepCopyMethod identityComponentArrayCopy = ARRAY_DEEP_COPY_CACHE.get(IDENTITY_COPY);
                DEEP_COPY_METHOD_CACHE.put(clazz, identityComponentArrayCopy);
                return identityComponentArrayCopy.copy(original);
            }

            DEEP_COPY_METHOD_CACHE.put(clazz, OBJECT_ARRAY_COPY);
            return OBJECT_ARRAY_COPY.copy(original);
        }

        if (clazz.getAnnotation(Immutable.class) != null) {
            DEEP_COPY_METHOD_CACHE.put(clazz, IDENTITY_COPY);
            return original;
        }

        List<Field> allFields = Lists.newArrayList();

        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            allFields.addAll(Arrays.asList(superClass.getDeclaredFields()));
        }

        allFields.removeIf((f) -> Modifier.isStatic(f.getModifiers()));

        if (allFields.isEmpty()) {
            DEEP_COPY_METHOD_CACHE.put(clazz, IDENTITY_COPY);
            return original; // It's probably a functional object or an Object mask
        }

        DeepCopyMethod copyMethod = new ReflectiveFieldDeepCopy(ImmutableList.copyOf(allFields));
        DEEP_COPY_METHOD_CACHE.put(clazz, copyMethod);
        return copyMethod.copy(original);
    }

    @FunctionalInterface
    private interface DeepCopyMethod {
        <E> E copy(E original) throws CloneNotSupportedException;
    }

    // this only differs in that it doesn't assert original != null
    private static final class FinalClassDeepCopy implements DeepCopyMethod {
        private final DeepCopyMethod matchingMethod;

        private FinalClassDeepCopy(DeepCopyMethod matchingMethod) {
            this.matchingMethod = matchingMethod;
        }

        @Override
        public <E> E copy(E original) throws CloneNotSupportedException {
            if (original == null) {
                return null;
            }

            return matchingMethod.copy(original);
        }
    }

    private static final class ArrayDeepCopy implements DeepCopyMethod {
        private final DeepCopyMethod componentMethod;

        private ArrayDeepCopy(DeepCopyMethod matchingMethod) {
            this.componentMethod = matchingMethod;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> E copy(E original) throws CloneNotSupportedException {
            Object[] array = (Object[]) original;
            Object[] copy = ArrayUtil.newArray(array);
            int length = copy.length;

            for (int i = 0; i < length; i++) {
                copy[i] = componentMethod.copy(array[i]);
            }

            return (E) copy;
        }
    }

    private static final class ReflectiveFieldDeepCopy implements DeepCopyMethod {
        private final ImmutableList<Field> fields;

        private ReflectiveFieldDeepCopy(ImmutableList<Field> fields) {
            this.fields = fields;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> E copy(E original) throws CloneNotSupportedException {
            // we can still make a new instance and deeply copy all fields
            try {
                E copy = (E) CommonReflections.CLONE_METHOD.invoke(original, (Object[]) null);

                for (Field field : fields) {
                    field.set(copy, ReflectionUtil.makeAccessible(field).get(original));
                }

                return copy;
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new CloneNotSupportedException();
            }
        }
    }

    static {
        DEEP_COPY_METHOD_CACHE.put(Class.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Boolean.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Byte.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Short.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Integer.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Long.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Float.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(Double.class, IDENTITY_COPY);
        DEEP_COPY_METHOD_CACHE.put(boolean[].class, PURE_CLONE);
        DEEP_COPY_METHOD_CACHE.put(byte[].class, PURE_CLONE);
        DEEP_COPY_METHOD_CACHE.put(short[].class, PURE_CLONE);
        DEEP_COPY_METHOD_CACHE.put(int[].class, PURE_CLONE);
        DEEP_COPY_METHOD_CACHE.put(long[].class, PURE_CLONE);
        DEEP_COPY_METHOD_CACHE.put(float[].class, PURE_CLONE);
        DEEP_COPY_METHOD_CACHE.put(double[].class, PURE_CLONE);
        ARRAY_DEEP_COPY_CACHE.put(IDENTITY_COPY, new ArrayDeepCopy(IDENTITY_COPY));
    }
}
