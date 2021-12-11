package com.github.svegon.utils.interfaces.function;

@FunctionalInterface
public interface HeptaFunction<T, U, V, W, X, Y, Z, R> {
    R apply(T o1, U o2, V o3, W o4, X o5, Y o6, Z o7);
}
