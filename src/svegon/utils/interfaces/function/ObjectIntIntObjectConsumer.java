package svegon.utils.interfaces.function;

@FunctionalInterface
public interface ObjectIntIntObjectConsumer<A, D> {
	 void accept(A arg0, int arg1, int arg2, D arg3);
}