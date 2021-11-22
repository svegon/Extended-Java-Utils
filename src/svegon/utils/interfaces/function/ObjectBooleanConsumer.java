package svegon.utils.interfaces.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectBooleanConsumer<T> extends BiConsumer<T, Boolean> {
    void accept(T o, boolean chr);

    @Override
    default void accept(T o, Boolean character) {
        accept(o, character.booleanValue());
    }

    @NotNull
    default ObjectBooleanConsumer<T> andThen(@NotNull final ObjectBooleanConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (o, bl) -> {
            accept(o, bl);
            after.accept(o, bl);
        };
    }

    @NotNull
    @Override
    default ObjectBooleanConsumer<T> andThen(@NotNull final BiConsumer<? super T, ? super Boolean> after) {
        return andThen(after instanceof ObjectBooleanConsumer ? (ObjectBooleanConsumer) after : after::accept);
    }
}
