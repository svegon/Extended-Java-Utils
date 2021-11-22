package svegon.utils.multithreading;

import com.google.common.collect.Queues;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public abstract class ThreadExecutor<R extends Runnable> implements Executor {
    private final Queue<R> tasks = Queues.newConcurrentLinkedQueue();
    private final String name;
    private int executionsInProgress;

    protected ThreadExecutor(String name) {
        this.name = name;
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        if (this.shouldExecuteAsync()) {
            this.send(this.createTask(runnable));
        } else {
            runnable.run();
        }
    }

    public abstract Thread getThread();

    protected abstract R createTask(Runnable runnable);

    protected abstract boolean canExecute(R task);

    public boolean isOnThread() {
        return Thread.currentThread() == getThread();
    }

    protected boolean shouldExecuteAsync() {
        return !isOnThread();
    }

    public int getTaskCount() {
        return this.tasks.size();
    }

    public final String getName() {
        return this.name;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> task) {
        return this.shouldExecuteAsync() ? CompletableFuture.supplyAsync(task, this) : CompletableFuture.completedFuture(task.get());
    }

    public final CompletableFuture<Void> submitAsync(final Runnable runnable) {
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return null;
        }, this);
    }

    public CompletableFuture<Void> submit(Runnable task) {
        if (this.shouldExecuteAsync()) {
            return this.submitAsync(task);
        } else {
            task.run();
            return CompletableFuture.completedFuture(null);
        }
    }

    public void submitAndJoin(Runnable runnable) {
        if (!this.isOnThread()) {
            this.submitAsync(runnable).join();
        } else {
            runnable.run();
        }
    }

    public void send(R runnable) {
        this.tasks.add(runnable);
        LockSupport.unpark(this.getThread());
    }

    protected void cancelTasks() {
        this.tasks.clear();
    }

    protected final void runTasks() {
        while(this.runTask());
    }

    public boolean runTask() {
        R runnable = (R) this.tasks.peek();

        if (runnable == null) {
            return false;
        } else if (this.executionsInProgress == 0 && !this.canExecute(runnable)) {
            return false;
        } else {
            this.execute((Runnable)this.tasks.remove());
            return true;
        }
    }

    public void runTasks(BooleanSupplier stopCondition) {
        ++this.executionsInProgress;

        try {
            while(!stopCondition.getAsBoolean()) {
                if (!this.runTask()) {
                    this.waitForTasks();
                }
            }
        } finally {
            --this.executionsInProgress;
        }

    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }
}
