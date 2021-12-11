package com.github.svegon.utils.interfaces.function;

@FunctionalInterface
public interface PentaFunction<T, U, V, W, X, R> {
    R apply(T o1, U o2, V o3, W o4, X o5);
}
