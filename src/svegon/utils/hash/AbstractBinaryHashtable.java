package svegon.utils.hash;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svegon.utils.collections.ArrayUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public abstract class AbstractBinaryHashtable<K, V, E extends Map.Entry<K, V>> extends AbstractMap<K, E>
        implements Size64 {
    /**
     * !!!the most important number of all!!!
     * (approximately) the maximum amount of entries
     * where it's still more effective than {@code Hashtable}
     */
    public static final int MAX_EFFECTIVE_ENTRY_COUNT = 980;
    public static final int BRANCH_FACTOR_LIMIT = 5;
    public static final int DEFAULT_BRANCH_FACTOR = 3;
    public static final Key2EntryEntry<?, ?, ?>[] EMPTY_ENTRY_ENTRY_ARRAY = new Key2EntryEntry<?, ?, ?>[0];

    private final ObjectCollection<E> values = initValues();
    private final ObjectSet<Map.Entry<K, E>> entrySet = initEntrySet();
    private final int branchFactor;
    protected final Object[] tree;
    protected final int depth;
    protected final int depthMinusOne;
    protected final int branchCoveredBits;
    protected final int branching;
    protected final int branchIndexMask;

    AbstractBinaryHashtable(int branchFactor) {
        Preconditions.checkArgument(0 <= branchFactor && branchFactor < BRANCH_FACTOR_LIMIT);

        this.branchFactor = branchFactor;
        this.branchCoveredBits = 1 << branchFactor;
        this.branching = 1 << branchCoveredBits;
        this.branchIndexMask = branching - 1;
        this.depth = 1 << (BRANCH_FACTOR_LIMIT - branchFactor);
        this.depthMinusOne = depth - 1;
        this.tree = new Object[branching];
    }

    @Override
    public final boolean remove(Object key, Object value) {
        E e = AbstractBinaryHashtable.this.get(key);

        if (e != null && e.equals(value)) {
            AbstractBinaryHashtable.this.remove(key);
            return true;
        }

        return false;
    }

    @Deprecated
    @Override
    public final int size() {
        return Size64.super.size();
    }

    /**
     * The size may be up to 2 ** 32
     *
     * @return the size as long
     */
    @Override
    public long size64() {
        Stream<Object> stream = Arrays.stream(tree);

        for (int i = 0; i < depthMinusOne; i++) {
            stream = stream.flatMap((o) -> o != null ? Arrays.stream((Object[]) o) : null);
        }

        return stream.mapToLong((o) -> o != null ? ((List<?>) o).size() : 0).sum();
    }

    @Override
    public final boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public abstract E get(final @Nullable Object key);

    @Override
    public final E put(@Nullable K key, final @NotNull E value) {
        return value.getKey() == key ? put(value) : null;
    }

    @Override
    public abstract E remove(final @Nullable Object key);

    @Override
    public final void clear() {
        ArrayUtil.fill(tree, null);
    }

    @NotNull
    @Override
    public ObjectCollection<E> values() {
        return values;
    }

    @Override
    public ObjectSet<Map.Entry<K, E>> entrySet() {
        return entrySet;
    }

    @Override
    public final E getOrDefault(Object key, E defaultValue) {
        E value = get(key);

        return value != null ? value : defaultValue;
    }

    @Override
    public final void replaceAll(final @NotNull BiFunction<? super K, ? super E, ? extends E> function) {
        Preconditions.checkNotNull(function);

        for (E entry : values()) {
            entry.setValue(function.apply(entry.getKey(), entry).getValue());
        }
    }

    @Nullable
    @Override
    public final E putIfAbsent(final @Nullable K key, final @NotNull E value) {
        return computeIfAbsent(key, (k) -> value);
    }

    @Override
    public final boolean replace(K key, E oldValue, E newValue) {
        E curValue = get(key);

        if (!Objects.equals(curValue, oldValue)) {
            return false;
        }

        put(key, newValue);
        return true;
    }

    @Nullable
    @Override
    public final E replace(K key, E value) {
        E curValue = null;

        if (containsKey(key)) {
            curValue = put(key, value);
        }

        return curValue;
    }

    protected abstract Key2EntryEntry<K, V, E> newK2EEntry(E entry);

    protected ObjectCollection<E> initValues() {
        return new AbstractObjectCollection<E>() {
            public ObjectIterator<E> iterator() {
                return new ObjectIterator<>() {
                    private Iterator<Map.Entry<K, E>> i = entrySet().iterator();

                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    public E next() {
                        return i.next().getValue();
                    }

                    public void remove() {
                        i.remove();
                    }
                };
            }

            public int size() {
                return AbstractBinaryHashtable.this.size();
            }

            public boolean isEmpty() {
                return AbstractBinaryHashtable.this.isEmpty();
            }

            @Override
            public boolean add(E e) {
                return !Objects.equals(e, AbstractBinaryHashtable.this.put(e.getKey(), e));
            }

            @Override
            public boolean remove(Object o) {
                return o instanceof Map.Entry entry
                        && AbstractBinaryHashtable.this.remove(entry.getKey(), entry.getValue());
            }

            public void clear() {
                AbstractBinaryHashtable.this.clear();
            }

            public boolean contains(Object v) {
                return AbstractBinaryHashtable.this.containsValue(v);
            }
        };
    }

    protected ObjectSet<Map.Entry<K, E>> initEntrySet() {
        return new AbstractObjectSet<>() {
            @Override
            public int size() {
                return AbstractBinaryHashtable.this.size();
            }

            @Override
            public boolean isEmpty() {
                return AbstractBinaryHashtable.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry<?, ?> entry)) {
                    return false;
                }

                return Objects.equals(AbstractBinaryHashtable.this.get(entry), entry.getValue());
            }

            @Override
            @SuppressWarnings("unchecked")
            public ObjectIterator<Map.Entry<K, E>> iterator() {
                Stream<Object> stream = Arrays.stream(tree);

                for (int i = 0; i < depthMinusOne; i++) {
                    stream = stream.flatMap((o) -> o != null ? Arrays.stream((Object[]) o) : null);
                }

                return ObjectIterators.asObjectIterator(Iterators.concat(stream.filter(Objects::nonNull)
                        .map((o) -> Iterators.transform(((List<E>) o).listIterator(),
                                (e) -> newK2EEntry(e))).iterator()));
            }

            @Override
            public boolean remove(Object o) {
                return o instanceof Entry<?, ?> entry
                        && AbstractBinaryHashtable.this.remove(entry.getKey(), entry.getValue());
            }

            @Override
            public void clear() {
                AbstractBinaryHashtable.this.clear();
            }
        };
    }

    public final int branchFactor() {
        return branchFactor;
    }

    @SuppressWarnings("unchecked")
    public final synchronized void rehash() {
        Key2EntryEntry<K, V, E>[] entries = (Key2EntryEntry<K, V, E>[]) entrySet().toArray(EMPTY_ENTRY_ENTRY_ARRAY);

        clear();

        for (Key2EntryEntry<K, V, E> entry : entries) {
            put((E) entry);
        }
    }

    public abstract E put(E entry);

    public static abstract class Key2EntryEntry<K, V, E extends Map.Entry<K, V>>
            implements Object2ObjectMap.Entry<K, E> {
        protected final E entry;

        public Key2EntryEntry(E entry) {
            this.entry = entry;
        }

        @Override
        public final E getValue() {
            return entry;
        }

        /**
         * Doesn't technically return the old value since the value
         * doesn't change and is only modified.
         *
         * @param value
         * @return
         */
        @Override
        public final E setValue(E value) {
            if (keysEqual(value)) {
                entry.setValue(value.getValue());
            }

            return entry;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry<?, ?> entry)) {
                return false;
            }

            try {
                return keysEqual((Entry<K, ?>) entry) && Objects.equals(getValue(), entry.getValue());
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public final int hashCode() {
            return keyHashCode() ^ Objects.hashCode(getValue());
        }

        @Override
        public final String toString() {
            return getValue().toString();
        }

        protected abstract boolean keysEqual(Map.Entry<K, ?> value);

        protected abstract int keyHashCode();
    }
}
