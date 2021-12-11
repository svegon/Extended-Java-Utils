package com.github.svegon.utils.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class CommonReflections {
    private CommonReflections(CommonReflections none) {
        throw new AssertionError();
    }

    public static final Method CLONE_METHOD;
    public static final Field BIG_INTEGER_MAG_FIELD;
    public static final Field BIG_DECIMAL_INT_VAL_FIELD;

    static {
        try {
            CLONE_METHOD = ReflectionUtil.makeAccessible(Object.class.getDeclaredMethod("clone"));
            BIG_INTEGER_MAG_FIELD = ReflectionUtil.makeAccessible(BigInteger.class.getDeclaredField("mag"));
            BIG_DECIMAL_INT_VAL_FIELD =
                    ReflectionUtil.makeAccessible(BigDecimal.class.getDeclaredField("intVal"));
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
