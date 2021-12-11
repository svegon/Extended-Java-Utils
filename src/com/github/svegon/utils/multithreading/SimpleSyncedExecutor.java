package com.github.svegon.utils.multithreading;

public final class SimpleSyncedExecutor extends SyncedExecutor<Runnable> {
    public SimpleSyncedExecutor(String name, Thread thread) {
        super(name, thread);
    }

    public SimpleSyncedExecutor(String name) {
        super(name);
    }

    @Override
    protected Runnable createTask(Runnable runnable) {
        return runnable;
    }
}
