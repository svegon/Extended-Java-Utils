package com.github.svegon.utils.fast.util.booleans;

import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.booleans.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface BooleanMultiset extends Multiset<Boolean>, ImprovedBooleanCollection {
    @Deprecated
    @Override
    default void forEach(Consumer<? super Boolean> action) {
        ImprovedBooleanCollection.super.forEach(action);
    }

    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @Deprecated
    @Override
    default int add(@Nullable Boolean aBoolean, int i) {
        return add(aBoolean != null && aBoolean, i);
    }

    int add(boolean aBoolean, int i);

    @Override
    default BooleanSpliterator spliterator() {
        return ImprovedBooleanCollection.super.spliterator();
    }

    @Deprecated
    @Override
    default boolean add(Boolean aBoolean) {
        return ImprovedBooleanCollection.super.add(aBoolean);
    }

    @Deprecated
    @Override
    default int remove(@Nullable Object o, int i) {
        return o instanceof Boolean ? remove(((boolean) o), i) : 0;
    }

    int remove(boolean bl, int i);

    @Deprecated
    @Override
    default boolean remove(@Nullable Object o) {
        return ImprovedBooleanCollection.super.remove(o);
    }

    @Override
    boolean addAll(final @NotNull BooleanCollection c);

    @Override
    boolean containsAll(final @NotNull BooleanCollection c);

    @Override
    default boolean removeAll(final @NotNull BooleanCollection c) {
        return removeIf(c::contains);
    }

    @Override
    default boolean retainAll(final @NotNull BooleanCollection c) {
        return removeIf((bl) -> !c.contains(bl));
    }

    @Deprecated
    @Override
    default int setCount(Boolean aBoolean, int i) {
        return setCount(aBoolean != null && aBoolean, i);
    }

    int setCount(boolean aBoolean, int i);

    @Override
    default boolean setCount(Boolean aBoolean, int i, int i1) {
        return false;
    }

    @Override
    BooleanSet elementSet();

    @Deprecated
    @Override
    @SuppressWarnings("unchecked")
    default Set<Multiset.Entry<Boolean>> entrySet() {
        return (Set<Multiset.Entry<Boolean>>) (Object) booleanEntrySet();
    }

    Set<BooleanMultiset.Entry> booleanEntrySet();

    @Deprecated
    @Override
    default boolean contains(@Nullable Object o) {
        return ImprovedBooleanCollection.super.contains(o);
    }

    @Deprecated
    @Override
    boolean containsAll(final @NotNull Collection<?> collection);

    @Deprecated
    @Override
    boolean addAll(@NotNull Collection<? extends Boolean> c);

    @Deprecated
    @Override
    default boolean removeAll(final @NotNull Collection<?> collection) {
        return collection instanceof BooleanCollection ? removeAll((BooleanCollection) collection)
                : removeIf(collection::contains);
    }

    @Deprecated
    @Override
    default boolean retainAll(final @NotNull Collection<?> collection) {
        return collection instanceof BooleanCollection ? retainAll((BooleanCollection) collection)
                : removeIf((bl) -> !collection.contains(bl));
    }

    interface Entry extends Multiset.Entry<Boolean> {
        @Deprecated
        @Override
        default Boolean getElement() {
            return getBoolean();
        }

        boolean getBoolean();
    }
}
