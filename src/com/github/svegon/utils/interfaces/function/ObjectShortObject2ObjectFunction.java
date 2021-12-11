package com.github.svegon.utils.interfaces.function;

@FunctionalInterface
public interface ObjectShortObject2ObjectFunction<T, U, R> {
    R apply(T o1, short s, U o2);
}
