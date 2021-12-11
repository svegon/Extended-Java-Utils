package com.github.svegon.utils.interfaces.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectShortConsumer<T> extends BiConsumer<T, Short> {
    void accept(T o, short chr);

    @Override
    default void accept(T o, Short character) {
        accept(o, character.shortValue());
    }

    @NotNull
    default ObjectShortConsumer<T> andThen(@NotNull final ObjectShortConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (o, bl) -> {
            accept(o, bl);
            after.accept(o, bl);
        };
    }

    @NotNull
    @Override
    default ObjectShortConsumer<T> andThen(@NotNull final BiConsumer<? super T, ? super Short> after) {
        return andThen(after instanceof ObjectShortConsumer ? (ObjectShortConsumer) after : after::accept);
    }
}
