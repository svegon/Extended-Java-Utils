package com.github.svegon.utils.interfaces.function;

@FunctionalInterface
public interface IntInt2ObjectFunction<R> {
    R apply(int i, int j);
}
