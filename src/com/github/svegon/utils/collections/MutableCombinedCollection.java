/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils.collections;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class MutableCombinedCollection<E> extends AbstractCollection<E> implements CombinedCollection<E> {
    private final List<Collection<E>> cache;

    private MutableCombinedCollection(@NotNull final List<Collection<E>> cache) {
        this.cache = cache;
    }

    @SafeVarargs
    public MutableCombinedCollection(Collection<? extends E>... collections) {
        this((List<Collection<E>>) (Object) Lists.newArrayList(collections));
    }

    public MutableCombinedCollection(Collection<? extends Collection<? extends E>> collections) {
        this((List<Collection<E>>) Lists.newArrayList(collections));
    }

    public @NotNull List<Collection<E>> getCache() {
        return cache;
    }

    @Override
    public boolean add(E e) {
        cache.add(Collections.singleton(e));
        return true;
    }

    @Override
    public void clear() {
        cache.forEach(Collection::clear);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return cache.add((Collection<E>) c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean modified = false;

        for (Collection<E> col : cache) {
            modified |= col.removeIf(filter);
        }

        return modified;
    }

    @Override
    public int size() {
        return cache.stream().mapToInt(Collection::size).sum();
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.concat(cache.stream().map(Collection::iterator).iterator());
    }
}
