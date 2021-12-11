package com.github.svegon.utils.collections;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public final class CollectionUtil {
    private CollectionUtil() {
        throw new AssertionError();
    }

    public static <E> MutableCombinedCollection<E> newMutableCombinedCollection(
            @NotNull final Collection<? extends E>... collections) {
        return new MutableCombinedCollection<>(collections);
    }

    @SuppressWarnings("unchecked")
    public static <E> MutableCombinedCollection<E> newMutableCombinedCollection(
            @NotNull final Iterable<? extends Collection<? extends E>> collections) {
        return collections instanceof Collection ? new MutableCombinedCollection<>(
                (Collection<? extends E>) collections) : newMutableCombinedCollection(collections.iterator());
    }

    public static <E> MutableCombinedCollection<E> newMutableCombinedCollection(
            @NotNull final Iterator<? extends Collection<? extends E>> collections) {
        MutableCombinedCollection<E> c = new MutableCombinedCollection<>();

        collections.forEachRemaining(c::addAll);

        return c;
    }

    public static <E> Collection<E> collectionView(final @NotNull Iterable<E> iterable) {
        return new AbstractCollection<>() {
            @Override
            public @NotNull Iterator<E> iterator() {
                return iterable.iterator();
            }

            @Override
            public int size() {
                return Iterables.size(iterable);
            }

            @Override
            public boolean isEmpty() {
                return iterator().hasNext();
            }
        };
    }
}
