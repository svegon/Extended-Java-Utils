package com.github.svegon.utils.interfaces.function;

import java.util.function.Function;

@FunctionalInterface
public interface Object2CharFunction<T> extends Function<T, Character> {
    char applyToChar(T o);

    @Deprecated
    @Override
    default Character apply(T t) {
        return applyToChar(t);
    }
}
