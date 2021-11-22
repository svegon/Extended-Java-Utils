package svegon.utils.interfaces.function;

@FunctionalInterface
public interface TetraFunction<T, U, V, W, R> {
    R apply(T o1, U o2, V o3, W o4);
}
