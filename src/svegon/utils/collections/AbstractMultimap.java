package svegon.utils.collections;

import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMultimap<K, V> extends AbstractCollection<Map.Entry<K, V>> implements Multimap<K, V> {
    private final Map<K, Collection<V>> mapView = initMapView();
    private final Set<K> keySet = initKeySet();
    private final Multiset<K> keys = initKeys();
    private final Collection<V> values = initValues();

    @Override
    public boolean containsKey(@Nullable Object o) {
        if (o == null) {
            for (Map.Entry<K, V> entry : this) {
                if (entry.getKey() == null) {
                    return true;
                }
            }
        } else {
            for (Map.Entry<K, V> entry : this) {
                if (o.equals(entry.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean containsValue(@Nullable Object o) {
        if (o == null) {
            for (Map.Entry<K, V> entry : this) {
                if (entry.getValue() == null) {
                    return true;
                }
            }
        } else {
            for (Map.Entry<K, V> entry : this) {
                if (o.equals(entry.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean containsEntry(@Nullable Object o, @Nullable Object o1) {
        Map.Entry<?, ?> entry = MapUtil.immutableEntry(o, o1);

        for (Map.Entry<K, V> e : this) {
            if (entry.equals(e)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean put(@Nullable K k, @Nullable V v) {
        return add(MapUtil.mutableEntry(k, v));
    }

    @Override
    public boolean remove(@Nullable Object o, @Nullable Object o1) {
        return remove(MapUtil.immutableEntry(o, o1));
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
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        boolean modified = false;

        for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
            modified |= put(entry.getKey(), entry.getValue());
        }

        return modified;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> replaceValues(@Nullable K k, Iterable<? extends V> iterable) {
        return asMap().put(k, iterable instanceof Collection ? (Collection<V>) iterable
                : (Collection<V>) CollectionUtil.collectionView(iterable));
    }

    @Override
    public Collection<V> removeAll(@Nullable Object o) {
        return asMap().remove(o);
    }

    @Override
    public Collection<V> get(@Nullable K k) {
        return asMap().get(k);
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
    public Collection<Map.Entry<K, V>> entries() {
        return this;
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return mapView;
    }

    protected Set<K> initKeySet() {
        return asMap().keySet();
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
            public Set<Entry<K>> entrySet() {
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

    protected abstract Map<K, Collection<V>> initMapView();
}
