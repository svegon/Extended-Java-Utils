package svegon.utils.reflect;

import java.lang.reflect.Method;

public final class CommonReflections {
    private CommonReflections(CommonReflections none) {
        throw new AssertionError();
    }

    public static final Method CLONE_METHOD;

    static {
        try {
            CLONE_METHOD = ReflectionUtil.makeAccessible(Object.class.getDeclaredMethod("clone"));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
