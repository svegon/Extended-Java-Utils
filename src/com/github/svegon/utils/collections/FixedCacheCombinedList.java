/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class FixedCacheCombinedList<E> extends AbstractList<E> implements CombinedList<E> {
    private final ImmutableList<List<E>> cachedLists;

    public FixedCacheCombinedList(ImmutableList<List<E>> cache) {
        cachedLists = cache;
    }

    @SafeVarargs
    public FixedCacheCombinedList(List<? extends E>... collections) {
        this(ImmutableList.copyOf((List<E>[]) collections));
    }

    public FixedCacheCombinedList(Collection<? extends Collection<? extends E>> collections) {
        this(ImmutableList.copyOf((Collection<? extends List<E>>) collections));
    }

    public @NotNull ImmutableList<List<E>> getCache() {
        return cachedLists;
    }

    @Override
    public boolean add(E e) {
        cachedLists.get(cachedLists.size() - 1).add(e);
        return true;
    }

    @Override
    public E get(int index) {
        int passedIndexes = 0;

        for (List<E> list : cachedLists) {
            passedIndexes += list.size();

            if (passedIndexes > index) {
                return list.get(passedIndexes - list.size());
            }
        }

        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public E set(int index, E element) {
        int passedIndexes = 0;

        for (List<E> list : cachedLists) {
            passedIndexes += list.size();

            if (passedIndexes > index) {
                return list.set(passedIndexes - list.size(), element);
            }
        }

        if (passedIndexes == index) {
            add(element);
            return null;
        }

        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public E remove(int index) {
        int passedIndexes = 0;

        for (List<E> list : cachedLists) {
            passedIndexes += list.size();

            if (passedIndexes > index) {
                return list.remove(passedIndexes - list.size());
            }
        }

        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public void clear() {
        cachedLists.forEach(List::clear);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return cachedLists.get(cachedLists.size() - 1).addAll(c);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean modified = false;

        for (Collection<E> col : cachedLists) {
            modified |= col.removeIf(filter);
        }

        return modified;
    }

    @Override
    public int size() {
        return cachedLists.stream().mapToInt(Collection::size).sum();
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.concat(cachedLists.stream().map(List::iterator).toArray(Iterator[]::new));
    }
}
