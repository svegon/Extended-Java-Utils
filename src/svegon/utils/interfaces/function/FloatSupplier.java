package svegon.utils.interfaces.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface FloatSupplier extends Supplier<Float> {
    float getAsFloat();

    @Override
    default Float get() {
        return getAsFloat();
    }
}
