package svegon.utils.interfaces.function;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T o1, U o2, V o3);
}
