package com.github.svegon.utils.interfaces.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectCharConsumer<T> extends BiConsumer<T, Character> {
    void accept(T o, char chr);

    @Override
    default void accept(T o, Character character) {
        accept(o, character.charValue());
    }

    @NotNull
    default ObjectCharConsumer<T> andThen(@NotNull final ObjectCharConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (o, chr) -> {
            accept(o, chr);
            after.accept(o, chr);
        };
    }

    @NotNull
    @Override
    default ObjectCharConsumer<T> andThen(@NotNull final BiConsumer<? super T, ? super Character> after) {
        return andThen(after instanceof ObjectCharConsumer ? (ObjectCharConsumer) after : after::accept);
    }
}
