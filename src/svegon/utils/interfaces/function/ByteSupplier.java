package svegon.utils.interfaces.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface ByteSupplier extends Supplier<Byte> {
    byte getAsByte();

    @Override
    default Byte get() {
        return getAsByte();
    }
}
