package svegon.utils.multithreading;

public abstract class SyncedExecutor<R extends Runnable> extends ReentrantThreadExecutor<R> {
    private final Thread thread;

    public SyncedExecutor(final String name, final Thread thread) {
        super(name);
        this.thread = thread;
    }

    public SyncedExecutor(final String name) {
        this(name, new Thread(name + "-executing-thread"));
    }

    @Override
    public Thread getThread() {
        return thread;
    }
}
