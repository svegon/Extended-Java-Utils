/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package svegon.utils.collections;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractCombinedMap<K, V> extends AbstractMultimap<K, V> implements CombinedMap<K, V> {
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return getCache().stream().flatMap((m) -> m.entrySet().stream()).iterator();
    }

    @Override
    public int size() {
        return getCache().stream().mapToInt(Map::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return getCache().stream().allMatch(Map::isEmpty);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        return getCache().stream().anyMatch((m) -> m.containsKey(o));
    }

    @Override
    public boolean containsValue(final @Nullable Object o) {
        return getCache().stream().anyMatch((m) -> m.containsValue(o));
    }

    @Override
    public boolean containsEntry(@Nullable Object o, @Nullable Object o1) {
        final Map.Entry<Object, Object> entry = MapUtil.immutableEntry(o, o1);
        return getCache().stream().map(Map::entrySet).anyMatch((es) -> es.contains(entry));
    }

    @Override
    public boolean put(@Nullable K k, @Nullable V v) {
        return putEntry(k, v) != v;
    }

    @Override
    public boolean remove(final @Nullable Object o, final @Nullable Object o1) {
        return getCache().stream().anyMatch((m) -> m.remove(o, o1));
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
    public boolean putAll(@NotNull Multimap<? extends K, ? extends V> multimap) {
        boolean modified = false;

        for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry : multimap.asMap().entrySet()) {
            modified |= putAll(entry.getKey(), entry.getValue());
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
        return asMap().get(k);
    }

    @Override
    public Map<K, Collection<V>> initMapView() {
        return new AbstractMap<>() {
            @Override
            public int size() {
                return (int) getCache().stream().flatMap(m -> m.keySet().stream()).distinct().count();
            }

            @Override
            public boolean isEmpty() {
                return AbstractCombinedMap.this.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return AbstractCombinedMap.this.containsKey(key);
            }

            @Override
            public Collection<V> get(final Object key) {
                return new AbstractCollection<>() {
                    @Override
                    public int size() {
                        return (int) getCache().stream().filter((m) -> m.containsKey(key)).count();
                    }

                    @Override
                    public boolean isEmpty() {
                        return getCache().stream().noneMatch((m) -> m.containsKey(key));
                    }

                    @Override
                    public boolean contains(final Object o) {
                        return o == null ? getCache().stream().anyMatch(m -> m.get(key) == null)
                                : getCache().stream().anyMatch(m -> o.equals(m.get(key)));
                    }

                    @NotNull
                    @Override
                    public Iterator<V> iterator() {
                        return getCache().stream().filter((m) -> m.containsKey(key)).map((m) -> m.get(key)).iterator();
                    }
                };
            }

            @Nullable
            @Override
            public Collection<V> put(K key, Collection<V> value) {
                return AbstractCombinedMap.this.replaceValues(key, value);
            }

            @Override
            public Collection<V> remove(Object key) {
                return AbstractCombinedMap.this.removeAll(key);
            }

            @Override
            public void clear() {
                AbstractCombinedMap.this.clear();
            }

            @NotNull
            @Override
            public Set<Entry<K, Collection<V>>> entrySet() {
                return new AbstractSet<>() {
                    @Override
                    public int size() {
                        return (int) getCache().stream().flatMap(m -> m.keySet().stream()).distinct().count();
                    }

                    @Override
                    public boolean isEmpty() {
                        return AbstractCombinedMap.this.isEmpty();
                    }

                    @Override
                    public boolean contains(Object o) {
                        return o instanceof Entry<?, ?> that && Objects.equals(that.getValue(),
                                AbstractCombinedMap.this.asMap().get(that.getKey()));
                    }

                    @NotNull
                    @Override
                    public Iterator<Entry<K, Collection<V>>> iterator() {
                        return new Iterator<>() {
                            private final Iterator<K> keyIt =
                                    getCache().stream().flatMap(m -> m.keySet().stream()).distinct().iterator();

                            @Override
                            public boolean hasNext() {
                                return keyIt.hasNext();
                            }

                            @Override
                            public Entry<K, Collection<V>> next() {
                                return new Entry<>() {
                                    private final K key = keyIt.next();

                                    @Override
                                    public K getKey() {
                                        return key;
                                    }

                                    @Override
                                    public Collection<V> getValue() {
                                        return AbstractCombinedMap.this.get(getKey());
                                    }

                                    @Override
                                    public Collection<V> setValue(@NotNull Collection<V> value) {
                                        return AbstractCombinedMap.this.replaceValues(getKey(), value);
                                    }

                                    @Override
                                    public boolean equals(Object o) {
                                        if (this == o) {
                                            return true;
                                        }

                                        if (!(o instanceof Entry<?, ?> that)) {
                                            return false;
                                        }

                                        return Objects.equals(getKey(), that.getKey())
                                                && Objects.equals(getValue(), that.getValue());
                                    }

                                    @Override
                                    public int hashCode() {
                                        return 31 * Objects.hashCode(getKey()) + Objects.hashCode(getValue());
                                    }

                                    @Override
                                    public String toString() {
                                        return getKey() + "=" + getValue();
                                    }
                                };
                            }
                        };
                    }

                    @Override
                    public boolean add(@NotNull Entry<K, Collection<V>> kCollectionEntry) {
                        return AbstractCombinedMap.this.putAll(kCollectionEntry.getKey(),
                                kCollectionEntry.getValue());
                    }

                    @Override
                    public boolean remove(Object o) {
                        if (o instanceof Entry<?, ?> that) {
                            Collection<V> value = AbstractCombinedMap.this.asMap().get(that.getKey());

                            if (Objects.equals(value, that.getValue())) {
                                AbstractCombinedMap.this.removeAll(that.getKey());
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    public void clear() {
                        AbstractCombinedMap.this.clear();
                    }
                };
            }
        };
    }

    @Override
    public abstract @NotNull Collection<Map<K, V>> getCache();

    protected abstract V putEntry(K key, V value);
}
