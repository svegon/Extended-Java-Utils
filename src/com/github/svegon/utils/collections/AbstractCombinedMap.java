/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils.collections;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractCombinedMap<K, V> extends AbstractMultimap<K, V> implements CombinedMap<K, V> {
    @Override
    public ObjectIterator<Map.Entry<K, V>> iterator() {
        return ObjectIterators.asObjectIterator(getCache().stream().flatMap((m) -> m.entrySet().stream()).iterator());
    }

    @Override
    public int size() {
        return getCache().parallelStream().mapToInt(Map::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return getCache().parallelStream().allMatch(Map::isEmpty);
    }

    @Override
    public boolean containsValue(final @Nullable Object o) {
        return getCache().parallelStream().anyMatch((m) -> m.containsValue(o));
    }

    @Override
    public boolean containsEntry(@Nullable Object o, @Nullable Object o1) {
        final Map.Entry<Object, Object> entry = MapUtil.immutableEntry(o, o1);
        return getCache().stream().map(Map::entrySet).anyMatch((es) -> es.contains(entry));
    }

    @Override
    public boolean add(Map.Entry<K, V> entry) {
        if (entry == null) {
            return false;
        }

        return putEntry(entry.getKey(), entry.getValue()) != entry.getValue();
    }

    @Override
    public boolean remove(final @Nullable Object key, final @Nullable Object value) {
        return getCache().stream().anyMatch((m) -> m.remove(key, value));
    }

    @Override
    public boolean putAll(@Nullable K k, Iterable<? extends V> iterable) {
        Iterator<? extends V> it = iterable.iterator();
        Iterator<Map<K, V>> cacheItr = getCache().iterator();
        boolean modified = false;

        while (it.hasNext() && cacheItr.hasNext()) {
            V value = it.next();
            modified |= cacheItr.next().put(k, value) != value;
        }

        if (it.hasNext()) {
            V value = it.next();

            while (it.hasNext()) {
                value = it.next();
            }

            modified |= put(k, value);
        }

        return modified;
    }

    @Override
    public Collection<V> replaceValues(@Nullable K k, @NotNull Iterable<? extends V> iterable) {
        Iterator<? extends V> it = iterable.iterator();
        Iterator<Map<K, V>> cacheItr = getCache().iterator();
        List<V> modified = Lists.newArrayList();

        while (it.hasNext() && cacheItr.hasNext()) {
            V value = it.next();
            modified.add(cacheItr.next().put(k, value));
        }

        if (it.hasNext()) {
            V value = it.next();

            while (it.hasNext()) {
                value = it.next();
            }

            modified.add(putEntry(k, value));
        }

        return modified;
    }

    @Override
    public Collection<V> removeAll(@Nullable Object o) {
        List<V> modified = Lists.newArrayListWithCapacity(getCache().size());

        for (Map<K, V> map : getCache()) {
            modified.add(map.remove(o));
        }

        return modified;
    }

    @Override
    public void clear() {
        getCache().forEach(Map::clear);
    }

    @Override
    public Collection<V> get(final @Nullable K k) {
        return new AbstractCollection<>() {
            @Override
            public int size() {
                return (int) getCache().stream().filter((m) -> m.containsKey(k)).count();
            }

            @Override
            public boolean isEmpty() {
                return getCache().stream().noneMatch((m) -> m.containsKey(k));
            }

            @Override
            public boolean contains(final Object o) {
                return o == null ? getCache().stream().anyMatch(m -> m.get(k) == null)
                        : getCache().stream().anyMatch(m -> o.equals(m.get(k)));
            }

            @NotNull
            @Override
            public Iterator<V> iterator() {
                return getCache().stream().filter((m) -> m.containsKey(k)).map((m) -> m.get(k)).iterator();
            }
        };
    }

    @Override
    protected Set<K> initKeySet() {
        return new AbstractSet<>() {
            @Override
            public int size() {
                return (int) getCache().parallelStream().flatMap(m -> m.keySet().stream()).distinct().count();
            }

            @Override
            public boolean isEmpty() {
                return AbstractCombinedMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return getCache().parallelStream().anyMatch(m -> m.containsKey(o));
            }

            @NotNull
            @Override
            public Iterator<K> iterator() {
                return getCache().parallelStream().flatMap(m -> m.keySet().stream()).distinct().iterator();
            }

            @Override
            public boolean remove(Object o) {
                Collection<V> ret = AbstractCombinedMap.this.removeAll(o);
                return ret != null && !ret.isEmpty();
            }

            @Override
            public void clear() {
                AbstractCombinedMap.this.clear();
            }
        };
    }

    @Override
    public abstract @NotNull Collection<Map<K, V>> getCache();

    protected abstract V putEntry(K key, V value);
}
