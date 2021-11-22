package svegon.utils.interfaces.function;

@FunctionalInterface
public interface HexaFunction<T, U, V, W, X, Y, R> {
    R apply(T o1, U o2, V o3, W o4, X o5, Y o6);
}
