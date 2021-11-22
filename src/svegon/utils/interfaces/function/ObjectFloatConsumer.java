package svegon.utils.interfaces.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectFloatConsumer<T> extends BiConsumer<T, Float> {
    void accept(T o, float chr);

    @Override
    default void accept(T o,Float character) {
        accept(o, character.floatValue());
    }

    @NotNull
    default ObjectFloatConsumer<T> andThen(@NotNull final ObjectFloatConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (o, chr) -> {
            accept(o, chr);
            after.accept(o, chr);
        };
    }

    @NotNull
    @Override
    default ObjectFloatConsumer<T> andThen(@NotNull final BiConsumer<? super T, ? super Float> after) {
        return andThen(after instanceof ObjectFloatConsumer ? (ObjectFloatConsumer) after : after::accept);
    }
}
