package com.github.svegon.utils.fast.util.booleans.immutable;

import it.unimi.dsi.fastutil.booleans.*;
import org.jetbrains.annotations.NotNull;

import java.util.RandomAccess;
import java.util.Spliterator;

public abstract class ImmutableBooleanSet extends ImmutableBooleanList implements BooleanSet, RandomAccess {
    @Override
    public boolean contains(Object key) {
        return key instanceof Boolean && contains((boolean) key);
    }

    @Deprecated
    @Override
    public final boolean add(Boolean k) {
        return super.add(k);
    }

    @Deprecated
    @Override
    public final boolean remove(Object key) {
        return key instanceof Boolean && remove((boolean) key);
    }

    @Override
    public BooleanSpliterator spliterator() {
        return new ImmutableBooleanSetSpliterator(this);
    }

    @Override
    public abstract ImmutableBooleanSet subList(int from, int to);

    @Override
    public final boolean remove(boolean k) {
        throw new UnsupportedOperationException();
    }

    public static ImmutableBooleanSet of(final boolean @NotNull ... values) {
        return ImmutableBooleanSortedSet.of(null, values);
    }

    public static ImmutableBooleanSet copyOf(final @NotNull BooleanIterable iterable) {
        return ImmutableBooleanSortedSet.copyOf(null, iterable);
    }

    public static ImmutableBooleanSet copyOf(final @NotNull BooleanIterator iterator) {
        return ImmutableBooleanSortedSet.copyOf(null, iterator);
    }

    protected static class ImmutableBooleanSetSpliterator extends IndexBasedImmutableSpliterator {
        public ImmutableBooleanSetSpliterator(ImmutableBooleanSet list, int index, int fence) {
            super(list, index, fence);
        }

        public ImmutableBooleanSetSpliterator(ImmutableBooleanSet list) {
            super(list);
        }

        @Override
        public int characteristics() {
            return BooleanSpliterators.LIST_SPLITERATOR_CHARACTERISTICS
                    | BooleanSpliterators.SET_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }
    }
}
