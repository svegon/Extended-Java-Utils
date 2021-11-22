package svegon.utils.interfaces.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface ShortSupplier extends Supplier<Short> {
    short getAsShort();

    @Override
    default Short get() {
        return getAsShort();
    }
}
