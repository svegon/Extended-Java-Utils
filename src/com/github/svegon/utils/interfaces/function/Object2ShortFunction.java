package com.github.svegon.utils.interfaces.function;

import java.util.function.Function;

@FunctionalInterface
public interface Object2ShortFunction<T> extends Function<T, Short> {
    short applyToShort(T o);

    @Deprecated
    @Override
    default Short apply(T t) {
        return applyToShort(t);
    }
}
