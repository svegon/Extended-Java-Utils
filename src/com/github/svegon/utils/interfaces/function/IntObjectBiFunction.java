package com.github.svegon.utils.interfaces.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface IntObjectBiFunction<T, R> extends BiFunction<Integer, T, R> {
    R apply(int i, T o);

    @Override
    default R apply(Integer integer, T t) {
        return apply(integer.intValue(), t);
    }

    @NotNull
    @Override
    default <S> IntObjectBiFunction<T, S> andThen(@NotNull final Function<? super R, ? extends S> after) {
        Objects.requireNonNull(after);
        return (int i, T o) -> after.apply(apply(i, o));
    }
}
