package com.github.svegon.utils.interfaces.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ObjectByteConsumer<T> extends BiConsumer<T, Byte> {
    void accept(T o, byte chr);

    @Override
    default void accept(T o, Byte character) {
        accept(o, character.byteValue());
    }

    @NotNull
    default ObjectByteConsumer<T> andThen(@NotNull final ObjectByteConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (o, bl) -> {
            accept(o, bl);
            after.accept(o, bl);
        };
    }

    @NotNull
    @Override
    default ObjectByteConsumer<T> andThen(@NotNull final BiConsumer<? super T, ? super Byte> after) {
        return andThen(after instanceof ObjectByteConsumer ? (ObjectByteConsumer) after : after::accept);
    }
}
