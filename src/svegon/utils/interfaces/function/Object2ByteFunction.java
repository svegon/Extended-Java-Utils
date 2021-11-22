package svegon.utils.interfaces.function;

import java.util.function.Function;

@FunctionalInterface
public interface Object2ByteFunction<T> extends Function<T, Byte> {
    byte applyToByte(T o);

    @Override
    default Byte apply(T t) {
        return applyToByte(t);
    }
}
