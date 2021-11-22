package svegon.utils.multithreading;

import it.unimi.dsi.fastutil.Arrays;

public abstract class ReentrantThreadExecutor<R extends Runnable> extends ThreadExecutor<R> {
    private int runningTasks;

    public ReentrantThreadExecutor(String name) {
        super(name);
    }

    @Override
    protected boolean canExecute(R task) {
        return task != null && runningTasks < Arrays.MAX_ARRAY_SIZE;
    }

    public boolean shouldExecuteAsync() {
        return this.hasRunningTasks() || super.shouldExecuteAsync();
    }

    protected boolean hasRunningTasks() {
        return this.runningTasks != 0;
    }

    public void executeTask(R task) {
        ++this.runningTasks;

        try {
            super.execute(task);
        } finally {
            --this.runningTasks;
        }

    }
}
