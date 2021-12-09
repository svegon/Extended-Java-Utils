package svegon.utils.collections;

import com.google.common.collect.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMultimap<K, V> extends AbstractObjectCollection<Map.Entry<K, V>>
        implements Multimap<K, V> {
    private final Map<K, Collection<V>> mapView = initMapView();
    private final Set<K> keySet = initKeySet();
    private final Multiset<K> keys = initKeys();
    private final Collection<V> values = initValues();
    private final AbstractMultimap<V, K> invertedView = initInvertedView();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Multimap<?, ?> multimap)) {
            return false;
        }

        return asMap().equals(multimap.asMap());
    }

    @Override
    public int hashCode() {
        return asMap().hashCode();
    }

    @Override
    public abstract ObjectIterator<Map.Entry<K, V>> iterator();

    @Override
    public abstract int size();

    @Override
    public boolean add(Map.Entry<K, V> entry) {
        return put(entry.getKey(), entry.getValue());
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Map.Entry<?, ?> entry && remove(entry.getKey(), entry.getValue());
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        return keySet().contains(o);
    }

    @Override
    public boolean containsValue(@Nullable Object o) {
        if (o == null) {
            for (Map.Entry<K, V> entry : entries()) {
                if (entry.getValue() == null) {
                    return true;
                }
            }
        } else {
            for (Map.Entry<K, V> entry : entries()) {
                if (o.equals(entry.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean containsEntry(@Nullable Object o, @Nullable Object o1) {
        final Map.Entry<?, ?> entry = MapUtil.immutableEntry(o, o1);
        return entries().parallelStream().anyMatch(entry::equals);
    }

    @Override
    public boolean put(@Nullable K k, @Nullable V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        Iterator<Map.Entry<K, V>> it = iterator();
        Map.Entry<?, ?> entry = MapUtil.immutableEntry(key, value);

        while (it.hasNext()) {
            if (entry.equals(it.next())) {
                it.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean putAll(@Nullable K k, Iterable<? extends V> iterable) {
        boolean modified = false;

        for (V v : iterable) {
            modified |= put(k, v);
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
    public @Nullable Collection<V> replaceValues(@Nullable K k, Iterable<? extends V> iterable) {
        if (iterable == null) {
            return null;
        }

        Collection<V> ret = removeAll(k);

        for (V v : iterable) {
            put(k, v);
        }

        return ret;
    }

    @Override
    public Collection<V> removeAll(@Nullable Object o) {
        List<V> ret = Lists.newArrayList();
        Iterator<Map.Entry<K, V>> it = entries().iterator();

        if (o == null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();

                if (entry.getKey() == null) {
                    ret.add(entry.getValue());
                    it.remove();
                }
            }
        } else {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();

                if (o.equals(entry.getKey())) {
                    ret.add(entry.getValue());
                    it.remove();
                }
            }
        }

        return ret;
    }

    @Override
    public Collection<V> get(@Nullable K k) {
        List<V> ret = Lists.newArrayList();

        if (k == null) {
            for (Map.Entry<K, V> entry : entries()) {
                if (entry.getKey() == null) {
                    ret.add(entry.getValue());
                }
            }
        } else {
            for (Map.Entry<K, V> entry : entries()) {
                if (k.equals(entry.getKey())) {
                    ret.add(entry.getValue());
                }
            }
        }

        return ret;
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public Multiset<K> keys() {
        return keys;
    }

    @Override
    public Collection<V> values() {
        return values;
    }

    @Override
    public ObjectCollection<Map.Entry<K, V>> entries() {
        return this;
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return mapView;
    }

    public AbstractMultimap<V, K> inverse() {
        return invertedView;
    }

    protected Set<K> initKeySet() {
        return new AbstractSet<>() {
            @Override
            public int size() {
                return (int) keys().parallelStream().distinct().count();
            }

            @Override
            public boolean isEmpty() {
                return AbstractMultimap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return containsKey(o);
            }

            @NotNull
            @Override
            public Iterator<K> iterator() {
                return keys().parallelStream().distinct().iterator();
            }

            @Override
            public boolean remove(Object o) {
                Collection<V> ret = AbstractMultimap.this.removeAll(o);
                return ret != null && !ret.isEmpty();
            }

            @Override
            public void clear() {
                AbstractMultimap.this.clear();
            }
        };
    }

    protected Multiset<K> initKeys() {
        return new AbstractMultiset<>() {
            @Override
            public int size() {
                return AbstractMultimap.this.size();
            }

            @Override
            public int count(@Nullable Object o) {
                Collection<V> values = AbstractMultimap.this.asMap().get(o);
                return values != null ? values.size() : 0;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return AbstractMultimap.this.asMap().remove(o) != null;
            }

            @Override
            public Set<K> initElementSet() {
                return keySet();
            }

            @Override
            protected Set<Entry<K>> initEntrySet() {
                return new AbstractSet<>() {
                    @Override
                    public int size() {
                        return AbstractMultimap.this.asMap().size();
                    }

                    @Override
                    public boolean isEmpty() {
                        return AbstractMultimap.this.isEmpty();
                    }

                    @Override
                    public boolean contains(Object o) {
                        if (!(o instanceof Entry<?> entry)) {
                            return false;
                        }

                        Collection<V> values = asMap().get(entry.getElement());
                        return values != null && values.size() == entry.getCount();
                    }

                    @NotNull
                    @Override
                    public Iterator<Entry<K>> iterator() {
                        return new Iterator<>() {
                            private final Iterator<Map.Entry<K, Collection<V>>> it = asMap().entrySet().iterator();

                            @Override
                            public boolean hasNext() {
                                return it.hasNext();
                            }

                            @Override
                            public Entry<K> next() {
                                return new Entry<>() {
                                    private final Map.Entry<K, Collection<V>> backingEntry = it.next();

                                    @Override
                                    public K getElement() {
                                        return backingEntry.getKey();
                                    }

                                    @Override
                                    public int getCount() {
                                        return backingEntry.getValue().size();
                                    }
                                };
                            }

                            @Override
                            public void remove() {
                                it.remove();
                            }
                        };
                    }

                    @Override
                    public boolean remove(Object o) {
                        if (!(o instanceof Entry<?> entry)) {
                            return false;
                        }

                        Collection<V> values = asMap().get(entry.getElement());

                        if (values != null && values.size() == entry.getCount()) {
                            asMap().remove(entry.getElement());
                            return true;
                        }

                        return false;
                    }

                    @Override
                    public void clear() {
                        AbstractMultimap.this.clear();
                    }
                };
            }

            @Override
            public Iterator<K> iterator() {
                return new Iterator<>() {
                    private final Iterator<Map.Entry<K, V>> it = AbstractMultimap.this.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public K next() {
                        return it.next().getKey();
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return AbstractMultimap.this.containsKey(o);
            }

            @Override
            public boolean isEmpty() {
                return AbstractMultimap.this.isEmpty();
            }

            @Override
            public void clear() {
                AbstractMultimap.this.clear();
            }
        };
    }

    protected Collection<V> initValues() {
        return new AbstractCollection<>() {
            @Override
            public Iterator<V> iterator() {
                return Iterators.concat(asMap().values().stream().map(Collection::iterator).iterator());
            }

            @Override
            public boolean remove(Object o) {
                Iterator<Map.Entry<K, V>> it = AbstractMultimap.this.iterator();

                if (o == null) {
                    while (it.hasNext()){
                        if (it.next().getValue() == null) {
                            it.remove();
                            return true;
                        }
                    }
                } else {
                    while (it.hasNext()) {
                        if (o.equals(it.next().getValue())) {
                            it.remove();
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public void clear() {
                AbstractMultimap.this.clear();
            }

            @Override
            public int size() {
                return AbstractMultimap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return AbstractMultimap.this.isEmpty();
            }
        };
    }

    protected Map<K, Collection<V>> initMapView() {
        return new AbstractMap<>() {
            @Override
            public int size() {
                return AbstractMultimap.this.keySet().size();
            }

            @Override
            public boolean isEmpty() {
                return AbstractMultimap.this.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return AbstractMultimap.this.containsKey(key);
            }

            @Override
            @SuppressWarnings("unchecked")
            public Collection<V> get(final Object key) {
                try {
                    return AbstractMultimap.this.get((K) key);
                } catch (ClassCastException e) {
                    return null;
                }
            }

            @Nullable
            @Override
            public Collection<V> put(K key, Collection<V> value) {
                return AbstractMultimap.this.replaceValues(key, value);
            }

            @Override
            public Collection<V> remove(Object key) {
                return AbstractMultimap.this.removeAll(key);
            }

            @Override
            public void clear() {
                AbstractMultimap.this.clear();
            }

            @NotNull
            @Override
            public Set<Entry<K, Collection<V>>> entrySet() {
                return new AbstractSet<>() {
                    @Override
                    public int size() {
                        return AbstractMultimap.this.keySet().size();
                    }

                    @Override
                    public boolean isEmpty() {
                        return AbstractMultimap.this.isEmpty();
                    }

                    @Override
                    public boolean contains(Object o) {
                        return o instanceof Entry<?, ?> that && Objects.equals(that.getValue(),
                                AbstractMultimap.this.asMap().get(that.getKey()));
                    }

                    @NotNull
                    @Override
                    public Iterator<Entry<K, Collection<V>>> iterator() {
                        return new Iterator<>() {
                            private final Iterator<K> keyIt = AbstractMultimap.this.keySet().iterator();

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
                                        return AbstractMultimap.this.get(getKey());
                                    }

                                    @Override
                                    public Collection<V> setValue(@NotNull Collection<V> value) {
                                        return AbstractMultimap.this.replaceValues(getKey(), value);
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
                                        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
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
                        return AbstractMultimap.this.putAll(kCollectionEntry.getKey(),
                                kCollectionEntry.getValue());
                    }

                    @Override
                    public boolean remove(Object o) {
                        if (o instanceof Entry<?, ?> that) {
                            Collection<V> value = AbstractMultimap.this.asMap().get(that.getKey());

                            if (Objects.equals(value, that.getValue())) {
                                AbstractMultimap.this.removeAll(that.getKey());
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    public void clear() {
                        AbstractMultimap.this.clear();
                    }
                };
            }
        };
    }

    protected AbstractMultimap<V, K> initInvertedView() {
        return new AbstractMultimap<>() {
            @Override
            public ObjectIterator<Map.Entry<V, K>> iterator() {
                return new ObjectIterator<>() {
                    final ObjectIterator<Map.Entry<K, V>> itr = AbstractMultimap.this.iterator();

                    @Override
                    public boolean hasNext() {
                        return itr.hasNext();
                    }

                    @Override
                    public Map.Entry<V, K> next() {
                        return new Map.Entry<>() {
                            Map.Entry<K, V> backingEntry = itr.next();

                            @Override
                            public boolean equals(Object o) {
                                if (this == o)
                                    return true;

                                if (!(o instanceof Map.Entry<?, ?> entry))
                                    return false;

                                return Objects.equals(getKey(), entry.getKey())
                                        && Objects.equals(getValue(), entry.getValue());
                            }

                            @Override
                            public int hashCode() {
                                return backingEntry.hashCode();
                            }

                            @Override
                            public V getKey() {
                                return backingEntry.getValue();
                            }

                            @Override
                            public K getValue() {
                                return backingEntry.getKey();
                            }

                            @Override
                            public K setValue(K value) {
                                K v = backingEntry.getKey();
                                AbstractMultimap.this.remove(v, backingEntry.getValue());
                                AbstractMultimap.this.put(value, backingEntry.getValue());
                                backingEntry = MapUtil.immutableEntry(v, backingEntry.getValue());
                                return v;
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        itr.remove();
                    }
                };
            }

            @Override
            public int size() {
                return AbstractMultimap.this.size();
            }

            @Override
            public boolean containsKey(@Nullable Object o) {
                return AbstractMultimap.this.containsValue(o);
            }

            @Override
            public boolean containsValue(@Nullable Object o) {
                return AbstractMultimap.this.containsKey(o);
            }

            @Override
            public boolean containsEntry(@Nullable Object k, @Nullable Object v) {
                return AbstractMultimap.this.containsEntry(v, k);
            }

            @Override
            public boolean put(@Nullable V v, @Nullable K k) {
                return AbstractMultimap.this.put(k, v);
            }

            @Override
            public boolean remove(@Nullable Object key, @Nullable Object value) {
                return AbstractMultimap.this.remove(value, key);
            }

            @Override
            protected AbstractMultimap<K, V> initInvertedView() {
                return AbstractMultimap.this;
            }
        };
    }
}
