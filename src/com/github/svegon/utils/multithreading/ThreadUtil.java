package com.github.svegon.utils.multithreading;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public final class ThreadUtil {
    private ThreadUtil() {
        throw new AssertionError();
    }

    public static ForkJoinPool getPool() {
        ForkJoinPool current = ForkJoinTask.getPool();
        return current == null ? ForkJoinPool.commonPool() : current;
    }
}
