package com.github.svegon.utils.hash;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;
import com.github.svegon.utils.collections.ListUtil;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public final class ObjectBinaryHashtable<K, V, E extends ObjectBinaryHashtable.Entry<K, V, E>>
        extends AbstractBinaryHashtable<K, V, E> implements Object2ObjectMap<K, E> {
    private final ObjectSet<K> keySet = new KeySet<>(this);
    private final Hash.Strategy<? super K> strategy;

    public ObjectBinaryHashtable(int branchFactor, Hash.Strategy<? super K> strategy) {
        super(branchFactor);
        this.strategy = strategy != null ? strategy : HashUtil.defaultStrategy();
    }

    public ObjectBinaryHashtable(Hash.Strategy<? super K> strategy) {
        this(DEFAULT_BRANCH_FACTOR, strategy);
    }

    public ObjectBinaryHashtable() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public List<E> getAll(K key) {
        Object e = tree;
        int hash = strategy.hashCode(key);

        for (int i = 0; i < depth && e != null; i++) {
            e = ((Object[]) e)[hash & branchIndexMask];
            hash >>>= branchCoveredBits;
        }

        if (e != null) {
            return (List<E>) e;
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(Object key) {
        Object e = tree;
        K k;
        int hash;

        try {
            k = (K) key;
            hash = strategy.hashCode(k);
        } catch (ClassCastException notKeyClass) {
            return null;
        }

        for (int i = 0; i < depth && e != null; i++) {
            e = ((Object[]) e)[hash & branchIndexMask];
            hash >>>= branchCoveredBits;
        }

        if (e != null) {
            for (E entry : (List<E>) e) {
                if (strategy.equals(k, entry.getKey())) {
                    return entry;
                }
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E remove(Object key) {
        Object[] e = tree;
        K k;
        int hash;

        try {
            k = (K) key;
            hash = strategy.hashCode(k);
        } catch (ClassCastException invalidClass) {
            return defaultReturnValue();
        }

        for (int i = 0; i < depth - 1; i++) {
            int temp = hash & branchIndexMask;

            if (e[temp] == null) {
                return defaultReturnValue();
            }

            e = (Object[]) e[temp];
            hash >>>= branchCoveredBits;
        }

        if (e[hash] == null) {
            return defaultReturnValue();
        }

        ListIterator<E> itr = ((List<E>) e[hash]).listIterator();

        while (itr.hasNext()) {
            E entry = itr.next();

            if (strategy.equals(k, entry.getKey())) {
                itr.remove();
                return entry;
            }
        }

        return defaultReturnValue();
    }

    @Override
    protected Object2EntryEntry<K, V, E> newK2EEntry(E entry) {
        return new Object2EntryEntry<>(this, entry);
    }

    @Override
    public void defaultReturnValue(E rv) {
        if (rv != defaultReturnValue()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public E defaultReturnValue() {
        return null;
    }

    @Override
    public ObjectSet<K> keySet() {
        return keySet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ObjectSet<Object2ObjectMap.Entry<K, E>> object2ObjectEntrySet() {
        return (ObjectSet<Object2ObjectMap.Entry<K, E>>) (Object) entrySet();
    }

    @Override
    public E put(final @NotNull E entry) {
        K key = entry.getKey();
        Object[] e = tree;
        int hash = strategy.hashCode(key);
        int depthMinusOne = depth - 1;

        for (int i = 0; i != depthMinusOne; i++) {
            int index = hash & branchIndexMask;

            if (e[index] == null) {
                e[index] = new Object[branching];
            }

            e = (Object[]) e[index];
            hash >>>= branchCoveredBits;
        }

        if (e[hash] == null) {
            e[hash] = ListUtil.newSyncedList();
        }

        List<E> list = (List<E>) e[hash];
        ListIterator<E> itr = list.listIterator();

        while (itr.hasNext()) {
            E entry0 = itr.next();

            if (strategy.equals(key, entry0.getKey())) {
                itr.set(entry);
                return entry0;
            }
        }

        list.add(entry);
        return null;
    }

    public Hash.Strategy<? super K> getStrategy() {
        return strategy;
    }

    public static abstract class Entry<K, V, E extends ObjectBinaryHashtable.Entry<K, V, E>>
            implements Object2ObjectMap.Entry<K, V> {
        private final ObjectBinaryHashtable<K, V, E> table;
        private final K key;

        public Entry(ObjectBinaryHashtable<K, V, E> table, K key) {
            this.table = table;
            this.key = key;
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry entry)) {
                return false;
            }

            try {
                return table.strategy.equals(getKey(), (K) entry.getKey())
                        && Objects.equals(getValue(), entry.getValue());
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return table.strategy.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public final ObjectBinaryHashtable<K, V, E> getTable() {
            return table;
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    public static final class Object2EntryEntry<K, V, E extends ObjectBinaryHashtable.Entry<K, V, E>>
            extends Key2EntryEntry<K, V, E> implements Object2ObjectMap.Entry<K, E> {
        private final ObjectBinaryHashtable<K, V, E> ref;

        public Object2EntryEntry(ObjectBinaryHashtable<K, V, E> ref, E entry) {
            super(entry);
            this.ref = ref;
        }

        @Override
        protected boolean keysEqual(Map.Entry<K, ?> value) {
            return ref.strategy.equals(getKey(), value.getKey());
        }

        @Override
        protected int keyHashCode() {
            return ref.strategy.hashCode(getKey());
        }

        @Override
        public K getKey() {
            return entry.getKey();
        }
    }

    private static final class KeySet<K, V, E extends ObjectBinaryHashtable.Entry<K, V, E>> extends AbstractObjectSet<K> {
        private final ObjectBinaryHashtable<K, V, E> ref;

        private KeySet(ObjectBinaryHashtable<K, V, E> ref) {
            this.ref = ref;
        }

        public ObjectIterator<K> iterator() {
            return new ObjectIterator<>() {
                private ObjectIterator<Map.Entry<K, E>> i = ref.entrySet().iterator();

                public boolean hasNext() {
                    return i.hasNext();
                }

                public K next() {
                    return i.next().getKey();
                }

                public void remove() {
                    i.remove();
                }
            };
        }

        public int size() {
            return ref.size();
        }

        public boolean isEmpty() {
            return ref.isEmpty();
        }

        public void clear() {
            ref.clear();
        }

        public boolean contains(Object k) {
            return ref.containsKey(k);
        }
    }
}
