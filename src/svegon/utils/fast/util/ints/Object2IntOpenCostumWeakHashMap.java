package svegon.utils.fast.util.ints;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
import it.unimi.dsi.fastutil.objects.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class Object2IntOpenCostumWeakHashMap<K> extends AbstractObject2IntMap<K> {

    /**
     * The default initial capacity -- MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    Entry<K>[] table;

    /**
     * The number of key-value mappings contained in this weak hash map.
     */
    private int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     */
    private int threshold;

    /**
     * The load factor for the hash table.
     */
    private final float loadFactor;

    private final Hash.Strategy<? super K> strategy;

    /**
     * Reference queue for cleared WeakEntries
     */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    /**
     * The number of times this Object2IntOpenCostumWeakHashMap has been structurally modified.
     * Structural modifications are those that change the number of
     * mappings in the map or otherwise modify its internal structure
     * (e.g., rehash).  This field is used to make iterators on
     * Collection-views of the map fail-fast.
     *
     * @see ConcurrentModificationException
     */
    int modCount;

    @SuppressWarnings("unchecked")
    private Entry<K>[] newTable(int n) {
        return new Entry[n];
    }

    /**
     * Constructs a new, empty {@code Object2IntOpenCostumWeakHashMap} with the given initial
     * capacity and the given load factor.
     *
     * @param  initialCapacity The initial capacity of the {@code Object2IntOpenCostumWeakHashMap}
     * @param  loadFactor      The load factor of the {@code Object2IntOpenCostumWeakHashMap}
     * @throws IllegalArgumentException if the initial capacity is negative,
     *         or if the load factor is nonpositive.
     */
    public Object2IntOpenCostumWeakHashMap(Hash.Strategy<? super K> strategy, int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Initial Capacity: "+
                    initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load factor: "+
                    loadFactor);
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;
        table = newTable(capacity);
        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
        this.strategy = strategy;
    }

    /**
     * Constructs a new, empty {@code Object2IntOpenCostumWeakHashMap} with the given initial
     * capacity and the default load factor (0.75).
     *
     * @param  initialCapacity The initial capacity of the {@code Object2IntOpenCostumWeakHashMap}
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public Object2IntOpenCostumWeakHashMap(Hash.Strategy<? super K> strategy, int initialCapacity) {
        this(strategy, initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty {@code Object2IntOpenCostumWeakHashMap} with the default initial
     * capacity (16) and load factor (0.75).
     */
    public Object2IntOpenCostumWeakHashMap(Hash.Strategy<? super K> strategy) {
        this(strategy, DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code Object2IntOpenCostumWeakHashMap} with the same mappings as the
     * specified map.  The {@code Object2IntOpenCostumWeakHashMap} is created with the default
     * load factor (0.75) and an initial capacity sufficient to hold the
     * mappings in the specified map.
     *
     * @param   m the map whose mappings are to be placed in this map
     * @throws  NullPointerException if the specified map is null
     * @since   1.3
     */
    public Object2IntOpenCostumWeakHashMap(Hash.Strategy<? super K> strategy, Object2IntMap<? extends K> m) {
        this(strategy, Math.max((int) ((float)m.size() / DEFAULT_LOAD_FACTOR + 1.0F),
                        DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    // internal utilities

    /**
     * Value representing null keys inside tables.
     */
    private static final Object NULL_KEY = new Object();

    /**
     * Use NULL_KEY for key if it is null.
     */
    private static Object maskNull(Object key) {
        return (key == null) ? NULL_KEY : key;
    }

    /**
     * Returns internal representation of null key back to caller as null.
     */
    static Object unmaskNull(Object key) {
        return (key == NULL_KEY) ? null : key;
    }

    /**
     * Checks for equality of non-null reference x and possibly-null y.  By
     * default uses Object.equals.
     */
    private boolean matchesKey(Entry<K> e, K key) {
        // check if the given entry refers to the given key without
        // keeping a strong reference to the entry's referent
        if (e.refersTo(key))
            return true;

        // then check for equality if the referent is not cleared
        K k = e.get();
        return k != null && strategy.equals(key, k);
    }

    /**
     * Retrieve object hash code and applies a supplemental hash function to the
     * result hash, which defends against poor quality hash functions.  This is
     * critical because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits.
     */
    final int hash(K k) {
        int h = strategy.hashCode(k);

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    private static int indexFor(int h, int length) {
        return h & (length-1);
    }

    /**
     * Expunges stale entries from the table.
     */
    private void expungeStaleEntries() {
        for (Object x; (x = queue.poll()) != null; ) {
            synchronized (queue) {
                @SuppressWarnings("unchecked")
                Entry<K> e = (Entry<K>) x;
                int i = indexFor(e.hash, table.length);

                Entry<K> prev = table[i];
                Entry<K> p = prev;
                while (p != null) {
                    Entry<K> next = p.next;
                    if (p == e) {
                        if (prev == e)
                            table[i] = next;
                        else
                            prev.next = next;
                        // Must not null out e.next;
                        // stale entries may be in use by a HashIterator
                        e.value = defaultReturnValue();
                        size--;
                        break;
                    }
                    prev = p;
                    p = next;
                }
            }
        }
    }

    /**
     * Returns the table after first expunging stale entries.
     */
    private Entry<K>[] getTable() {
        expungeStaleEntries();
        return table;
    }

    /**
     * Returns the number of key-value mappings in this map.
     * This result is a snapshot, and may not reflect unprocessed
     * entries that will be removed before next attempted access
     * because they are no longer referenced.
     */
    public int size() {
        if (size == 0)
            return 0;

        expungeStaleEntries();
        return size;
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     * This result is a snapshot, and may not reflect unprocessed
     * entries that will be removed before next attempted access
     * because they are no longer referenced.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that
     * {@code Objects.equals(key, k)},
     * then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(K, int)
     */
    @SuppressWarnings("unchecked")
    public int getInt(Object key) {
        try {
            K k = (K) maskNull(key);
            int h = hash(k);
            Entry<K>[] tab = getTable();
            int index = indexFor(h, tab.length);
            Entry<K> e = tab[index];
            while (e != null) {
                if (e.hash == h && matchesKey(e, k))
                    return e.value;
                e = e.next;
            }
        } catch (ClassCastException ignored) {
        }

        return defaultReturnValue();
    }

    /**
     * Returns {@code true} if this map contains a mapping for the
     * specified key.
     *
     * @param  key   The key whose presence in this map is to be tested
     * @return {@code true} if there is a mapping for {@code key};
     *         {@code false} otherwise
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in this map.
     * Returns null if the map contains no mapping for this key.
     */
    @SuppressWarnings("unchecked")
    Entry<K> getEntry(Object key) {
        try {
            K k = (K) maskNull(key);
            int h = hash(k);
            Entry<K>[] tab = getTable();
            int index = indexFor(h, tab.length);
            Entry<K> e = tab[index];
            while (e != null && !(e.hash == h && matchesKey(e, k)))
                e = e.next;
            return e;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     */
    @SuppressWarnings("unchecked")
    public int put(K key, int value) {
        try {
            K k = (K) maskNull(key);
            int h = hash(k);
            Entry<K>[] tab = getTable();
            int i = indexFor(h, tab.length);

            for (Entry<K> e = tab[i]; e != null; e = e.next) {
                if (h == e.hash && matchesKey(e, k)) {
                    int oldValue = e.value;
                    if (value != oldValue)
                        e.value = value;
                    return oldValue;
                }
            }

            modCount++;
            Entry<K> e = tab[i];
            tab[i] = new Entry<>(k, value, this, h, e);
            if (++size >= threshold)
                resize(tab.length * 2);
            return defaultReturnValue();
        } catch (ClassCastException ignored) {
        }

        return defaultReturnValue();
    }

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    void resize(int newCapacity) {
        Entry<K>[] oldTable = getTable();
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry<K>[] newTable = newTable(newCapacity);
        transfer(oldTable, newTable);
        table = newTable;

        /*
         * If ignoring null elements and processing ref queue caused massive
         * shrinkage, then restore old table.  This should be rare, but avoids
         * unbounded expansion of garbage-filled tables.
         */
        if (size >= threshold / 2) {
            threshold = (int)(newCapacity * loadFactor);
        } else {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            table = oldTable;
        }
    }

    /** Transfers all entries from src to dest tables */
    private void transfer(Entry<K>[] src, Entry<K>[] dest) {
        for (int j = 0; j < src.length; ++j) {
            Entry<K> e = src[j];
            src[j] = null;
            while (e != null) {
                Entry<K> next = e.next;
                if (e.refersTo(null)) {
                    e.next = null;  // Help GC
                    e.value = defaultReturnValue(); //  "   "
                    size--;
                } else {
                    int i = indexFor(e.hash, dest.length);
                    e.next = dest[i];
                    dest[i] = e;
                }
                e = next;
            }
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for any
     * of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map.
     * @throws  NullPointerException if the specified map is null.
     */
    public void putAll(Object2IntMap<? extends K> m) {
        int numKeysToBeAdded = m.size();

        if (numKeysToBeAdded == 0)
            return;

        /*
         * Expand the map if the map if the number of mappings to be added
         * is greater than or equal to threshold.  This is conservative; the
         * obvious condition is (m.size() + size) >= threshold, but this
         * condition could result in a map with twice the appropriate capacity,
         * if the keys to be added overlap with the keys already in this map.
         * By using the conservative calculation, we subject ourself
         * to at most one extra resize.
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (Object2IntMap.Entry<? extends K> e : m.object2IntEntrySet())
            put(e.getKey(), e.getIntValue());
    }

    /**
     * Removes the mapping for a key from this weak hash map if it is present.
     * More formally, if this map contains a mapping from key {@code k} to
     * value {@code v} such that <code>(key==null ?  k==null :
     * key.equals(k))</code>, that mapping is removed.  (The map can contain
     * at most one such mapping.)
     *
     * <p>Returns the value to which this map previously associated the key,
     * or {@code null} if the map contained no mapping for the key.  A
     * return value of {@code null} does not <i>necessarily</i> indicate
     * that the map contained no mapping for the key; it's also possible
     * that the map explicitly mapped the key to {@code null}.
     *
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}
     */
    @SuppressWarnings("unchecked")
    public int removeInt(Object key) {
        try {
            K k = (K) maskNull(key);
            int h = hash(k);
            Entry<K>[] tab = getTable();
            int i = indexFor(h, tab.length);
            Entry<K> prev = tab[i];
            Entry<K> e = prev;

            while (e != null) {
                Entry<K> next = e.next;
                if (h == e.hash && matchesKey(e, k)) {
                    modCount++;
                    size--;
                    if (prev == e)
                        tab[i] = next;
                    else
                        prev.next = next;
                    return e.value;
                }
                prev = e;
                e = next;
            }
        } catch (ClassCastException ignored) {

        }

        return defaultReturnValue();
    }

    /** Special version of remove needed by Entry set */
    @SuppressWarnings("unchecked")
    boolean removeMapping(Object o) {
        if (!(o instanceof Map.Entry))
            return false;

        try {
            Entry<K>[] tab = getTable();
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            K k = (K) maskNull(entry.getKey());
            int h = hash(k);
            int i = indexFor(h, tab.length);
            Entry<K> prev = tab[i];
            Entry<K> e = prev;

            while (e != null) {
                Entry<K> next = e.next;
                if (h == e.hash && e.equals(entry)) {
                    modCount++;
                    size--;
                    if (prev == e)
                        tab[i] = next;
                    else
                        prev.next = next;
                    return true;
                }
                prev = e;
                e = next;
            }
        } catch (ClassCastException ignored) {

        }

        return false;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        // clear out ref queue. We don't need to expunge entries
        // since table is getting cleared.
        while (queue.poll() != null)
            ;

        modCount++;
        Arrays.fill(table, null);
        size = 0;

        // Allocation of array may have caused GC, which may have caused
        // additional entries to go stale.  Removing these entries from the
        // reference queue will make them eligible for reclamation.
        while (queue.poll() != null)
            ;
    }


    /**
     * Returns {@code true} if this map maps one or more keys to the
     * specified value.
     *
     * @param v value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the
     *         specified value
     */
    @Override
    public boolean containsValue(int v) {
        Entry<K>[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
            for (Entry<K> e = tab[i]; e != null; e = e.next)
                if (v == e.value)
                    return true;
        return false;
    }

    /**
     * The entries in this hash table extend WeakReference, using its main ref
     * field as the key.
     */
    private static class Entry<K> extends WeakReference<K> implements Object2IntMap.Entry<K> {
        int value;
        final Object2IntOpenCostumWeakHashMap<K> map;
        final int hash;
        Entry<K> next;

        /**
         * Creates new entry.
         */
        Entry(K key, int value, Object2IntOpenCostumWeakHashMap<K> map, int hash, Entry<K> next) {
            super(key, map.queue);
            this.value = value;
            this.map = map;
            this.hash  = hash;
            this.next  = next;
        }

        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) Object2IntOpenCostumWeakHashMap.unmaskNull(get());
        }

        public int getIntValue() {
            return value;
        }

        public int setValue(int newValue) {
            int oldValue = value;
            value = newValue;
            return oldValue;
        }

        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;

            try {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                K k1 = getKey();
                Object k2 = e.getKey();
                if (map.strategy.equals(k1, (K) k2)) {
                    int v1 = getIntValue();
                    Object v2 = e.getValue();

                    return v2 instanceof Integer && v1 == (int) v2;
                }
            } catch (ClassCastException ignored) {

            }

            return false;
        }

        public int hashCode() {
            K k = getKey();
            int v = getIntValue();
            return map.strategy.hashCode(k) ^ Integer.hashCode(v);
        }

        public String toString() {
            return getKey() + "=" + getIntValue();
        }
    }

    private abstract class HashIterator<T> implements ObjectIterator<T> {
        private int index;
        private Entry<K> entry;
        private Entry<K> lastReturned;
        private int expectedModCount = modCount;

        /**
         * Strong reference needed to avoid disappearance of key
         * between hasNext and next
         */
        private Object nextKey;

        /**
         * Strong reference needed to avoid disappearance of key
         * between nextEntry() and any use of the entry
         */
        private Object currentKey;

        HashIterator() {
            index = isEmpty() ? 0 : table.length;
        }

        public boolean hasNext() {
            Entry<K>[] t = table;

            while (nextKey == null) {
                Entry<K> e = entry;
                int i = index;
                while (e == null && i > 0)
                    e = t[--i];
                entry = e;
                index = i;
                if (e == null) {
                    currentKey = null;
                    return false;
                }
                nextKey = e.get(); // hold on to key in strong ref
                if (nextKey == null)
                    entry = entry.next;
            }
            return true;
        }

        /** The common parts of next() across different types of iterators */
        protected Entry<K> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextKey == null && !hasNext())
                throw new NoSuchElementException();

            lastReturned = entry;
            entry = entry.next;
            currentKey = nextKey;
            nextKey = null;
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            Object2IntOpenCostumWeakHashMap.this.remove(currentKey);
            expectedModCount = modCount;
            lastReturned = null;
            currentKey = null;
        }

    }

    private class ValueIterator implements IntIterator {
        private int index;
        private Entry<K> entry;
        private Entry<K> lastReturned;
        private int expectedModCount = modCount;

        /**
         * Strong reference needed to avoid disappearance of key
         * between hasNext and next
         */
        private Object nextKey;

        /**
         * Strong reference needed to avoid disappearance of key
         * between nextEntry() and any use of the entry
         */
        private Object currentKey;

        ValueIterator() {
            index = isEmpty() ? 0 : table.length;
        }

        public boolean hasNext() {
            Entry<K>[] t = table;

            while (nextKey == null) {
                Entry<K> e = entry;
                int i = index;
                while (e == null && i > 0)
                    e = t[--i];
                entry = e;
                index = i;
                if (e == null) {
                    currentKey = null;
                    return false;
                }
                nextKey = e.get(); // hold on to key in strong ref
                if (nextKey == null)
                    entry = entry.next;
            }
            return true;
        }

        /** The common parts of next() across different types of iterators */
        protected Entry<K> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextKey == null && !hasNext())
                throw new NoSuchElementException();

            lastReturned = entry;
            entry = entry.next;
            currentKey = nextKey;
            nextKey = null;
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            Object2IntOpenCostumWeakHashMap.this.remove(currentKey);
            expectedModCount = modCount;
            lastReturned = null;
            currentKey = null;
        }

        public int nextInt() {
            return nextEntry().value;
        }
    }

    private class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private class EntryIterator extends HashIterator<Object2IntMap.Entry<K>> {
        public Object2IntMap.Entry<K> next() {
            return nextEntry();
        }
    }

    // Views

    private final transient ObjectSet<Object2IntMap.Entry<K>> entrySet = new EntrySet();

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear}
     * operations.  It does not support the {@code add} or {@code addAll}
     * operations.
     */
    public ObjectSet<K> keySet() {
        return new KeySet();
    }

    private class KeySet extends AbstractObjectSet<K> {
        public ObjectIterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return Object2IntOpenCostumWeakHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            if (containsKey(o)) {
                Object2IntOpenCostumWeakHashMap.this.remove(o);
                return true;
            }
            else
                return false;
        }

        public void clear() {
            Object2IntOpenCostumWeakHashMap.this.clear();
        }

        public ObjectSpliterator<K> spliterator() {
            return new Object2IntOpenCostumWeakHashMap.KeySpliterator<>(Object2IntOpenCostumWeakHashMap.this,
                    0, -1, 0, 0);
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own {@code remove} operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Collection.remove}, {@code removeAll},
     * {@code retainAll} and {@code clear} operations.  It does not
     * support the {@code add} or {@code addAll} operations.
     */
    @Override
    public IntCollection values() {
        return new Values();
    }

    private class Values extends AbstractIntCollection {
        public IntIterator iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return Object2IntOpenCostumWeakHashMap.this.size();
        }

        @Override
        public boolean contains(int o) {
            return containsValue(o);
        }

        @Override
        public void clear() {
            Object2IntOpenCostumWeakHashMap.this.clear();
        }

        @Override
        public IntSpliterator spliterator() {
            return new ValueSpliterator<>(Object2IntOpenCostumWeakHashMap.this, 0, -1, 0,
                    0);
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation, or through the
     * {@code setValue} operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll} and
     * {@code clear} operations.  It does not support the
     * {@code add} or {@code addAll} operations.
     * @return
     */
    @Override
    public ObjectSet<Object2IntMap.Entry<K>> object2IntEntrySet() {
        return entrySet;
    }

    private class EntrySet extends AbstractObjectSet<Object2IntMap.Entry<K>> {
        public ObjectIterator<Object2IntMap.Entry<K>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            Entry<K> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            return removeMapping(o);
        }

        public int size() {
            return Object2IntOpenCostumWeakHashMap.this.size();
        }

        public void clear() {
            Object2IntOpenCostumWeakHashMap.this.clear();
        }

        private List<Object2IntMap.Entry<K>> deepCopy() {
            List<Object2IntMap.Entry<K>> list = new ArrayList<>(size());
            for (Object2IntMap.Entry<K> e : this)
                list.add(new AbstractObject2IntMap.BasicEntry<>(e.getKey(), e.getIntValue()));
            return list;
        }

        public Object[] toArray() {
            return deepCopy().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return deepCopy().toArray(a);
        }

        public ObjectSpliterator<Object2IntMap.Entry<K>> spliterator() {
            return new EntrySpliterator<>(Object2IntOpenCostumWeakHashMap.this, 0, -1, 0,
                    0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super Integer> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;

        Entry<K>[] tab = getTable();
        for (Entry<K> entry : tab) {
            while (entry != null) {
                Object key = entry.get();
                if (key != null) {
                    action.accept((K)Object2IntOpenCostumWeakHashMap.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceAll(BiFunction<? super K, ? super Integer, ? extends Integer> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        Entry<K>[] tab = getTable();;
        for (Entry<K> entry : tab) {
            while (entry != null) {
                Object key = entry.get();
                if (key != null) {
                    entry.value = function.apply((K)Object2IntOpenCostumWeakHashMap.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /**
     * Similar form as other hash Spliterators, but skips dead
     * elements.
     */
    public static class Object2IntOpenCostumWeakHashMapSpliterator<K> {
        final Object2IntOpenCostumWeakHashMap<K> map;
        Entry<K> current; // current node
        int index;             // current index, modified on advance/split
        int fence;             // -1 until first use; then one past last index
        int est;               // size estimate
        int expectedModCount;  // for comodification checks

        Object2IntOpenCostumWeakHashMapSpliterator(Object2IntOpenCostumWeakHashMap<K> m, int origin,
                               int fence, int est,
                               int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                Object2IntOpenCostumWeakHashMap<K> m = map;
                est = m.size();
                expectedModCount = m.modCount;
                hi = fence = m.table.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return est;
        }
    }

    static final class KeySpliterator<K,V>
            extends Object2IntOpenCostumWeakHashMapSpliterator<K>
            implements ObjectSpliterator<K> {
        KeySpliterator(Object2IntOpenCostumWeakHashMap<K> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            Object2IntOpenCostumWeakHashMap<K> m = map;
            Entry<K>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                    (i < (index = hi) || current != null)) {
                Entry<K> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        p = p.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) Object2IntOpenCostumWeakHashMap.unmaskNull(x);
                            action.accept(k);
                        }
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<K>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        current = current.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) Object2IntOpenCostumWeakHashMap.unmaskNull(x);
                            action.accept(k);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT;
        }
    }

    public static final class ValueSpliterator<K>
            extends Object2IntOpenCostumWeakHashMapSpliterator<K>
            implements IntSpliterator {
        ValueSpliterator(Object2IntOpenCostumWeakHashMap<K> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new Object2IntOpenCostumWeakHashMap.ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(IntConsumer action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            Object2IntOpenCostumWeakHashMap<K> m = map;
            Entry<K>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                    (i < (index = hi) || current != null)) {
                Entry<K> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        int v = p.value;
                        p = p.next;
                        if (x != null)
                            action.accept(v);
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(IntConsumer action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<K>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        int v = current.value;
                        current = current.next;
                        if (x != null) {
                            action.accept(v);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 0;
        }
    }

    static final class EntrySpliterator<K> extends Object2IntOpenCostumWeakHashMapSpliterator<K>
            implements ObjectSpliterator<Object2IntMap.Entry<K>> {
        EntrySpliterator(Object2IntOpenCostumWeakHashMap<K> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new Object2IntOpenCostumWeakHashMap.EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }


        public void forEachRemaining(Consumer<? super Object2IntMap.Entry<K>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            Object2IntOpenCostumWeakHashMap<K> m = map;
            Entry<K>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                    (i < (index = hi) || current != null)) {
                Entry<K> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        int v = p.value;
                        p = p.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) Object2IntOpenCostumWeakHashMap.unmaskNull(x);
                            action.accept
                                    (new AbstractObject2IntMap.BasicEntry<>(k, v));
                        }
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super Object2IntMap.Entry<K>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<K>[] tab = map.table;

            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        int v = current.value;
                        current = current.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) Object2IntOpenCostumWeakHashMap.unmaskNull(x);
                            action.accept
                                    (new AbstractObject2IntMap.BasicEntry<>(k, v));
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT;
        }
    }
}
