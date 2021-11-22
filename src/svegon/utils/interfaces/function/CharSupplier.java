package svegon.utils.interfaces.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface CharSupplier extends Supplier<Character> {
    char getAsChar();

    @Override
    default Character get() {
        return getAsChar();
    }
}
