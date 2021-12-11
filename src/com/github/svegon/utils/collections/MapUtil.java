package com.github.svegon.utils.collections;

import com.google.common.collect.ImmutableList;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class MapUtil {
    private MapUtil() {
        throw new AssertionError();
    }


    public static <K, V> FixedCacheCombinedMap<K, V> newFixedCacheCombinedMap(final ImmutableList<Map<K, V>> cache) {
        return new FixedCacheCombinedMap<>(cache);
    }

    public static <K, V> FixedCacheCombinedMap<K, V> newFixedCacheCombinedMap(
            final Map<? extends K, ? extends V>... initialMaps) {
        return new FixedCacheCombinedMap<>(initialMaps);
    }


    public static <K, V> FixedCacheCombinedMap<K, V> newFixedCacheCombinedMap(final Iterable<? extends Map<? extends K,
            ? extends V>> initialMaps) {
        return initialMaps instanceof Collection ? new FixedCacheCombinedMap<>((Collection<? extends Map<? extends K,
                ? extends V>>) initialMaps) : newFixedCacheCombinedMap(initialMaps.iterator());
    }


    public static <K, V> FixedCacheCombinedMap<K, V> newFixedCacheCombinedMap(final Iterator<? extends Map<? extends K,
            ? extends V>> initialMaps) {
        return newFixedCacheCombinedMap((ImmutableList<Map<K, V>>) ImmutableList.copyOf(initialMaps));
    }

    public static <K, V> Map.Entry<K, V> mutableEntry(K key) {
        return key == null ? new MutableNullKeyEntry<>() : new MutableNotNullKeyEntry<>(key);
    }

    public static <K, V> Map.Entry<K, V> mutableEntry(K key, V value) {
        return key == null ? new MutableNullKeyEntry<>(value) : new MutableNotNullKeyEntry<>(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return key == null ? value == null ? (Map.Entry<K, V>) ImmutableNull2NullEntry.INSTANCE
                : new ImmutableNullKeyEntry<>(value) : value == null ? new ImmutableNullValueEntry<>(key)
                : new ImmutableNotNullEntry<>(key, value);
    }

    public abstract static class MutableEntry<K, V> implements Map.Entry<K, V> {
        private V value;

        public MutableEntry(V value) {
            this.value = value;
        }

        public MutableEntry() {
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V v = this.value;
            this.value = value;
            return v;
        }

        @Override
        public abstract boolean equals(Object o);

        @Override
        public abstract int hashCode();

        @Override
        public abstract String toString();
    }

    public static class MutableNullKeyEntry<K, V> extends MutableEntry<K, V> {
        public MutableNullKeyEntry() {
        }

        public MutableNullKeyEntry(V value) {
            super(value);
        }

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry<?, ?> that)) {
                return false;
            }

            return that.getKey() == null && Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return "null=" + getValue();
        }
    }

    public static class MutableNotNullKeyEntry<K, V> extends MutableEntry<K, V> {
        private final K key;

        public MutableNotNullKeyEntry(K key) {
            this.key = key;
        }

        public MutableNotNullKeyEntry(K key, V value) {
            super(value);
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry<?, ?> that)) {
                return false;
            }

            return getKey().equals(that.getKey()) && Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return 31 * getKey().hashCode() + Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    @Immutable
    private record ImmutableNotNullEntry<K, V>(@NotNull K key, @NotNull V value) implements Map.Entry<K, V> {
        private ImmutableNotNullEntry(@NotNull K key, @NotNull V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof Map.Entry<?, ?> that))
                return false;

            return getKey().equals(that.getKey()) && getValue().equals(that.getValue());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode() ^ getValue().hashCode();
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    @Immutable
    private static final class ImmutableNullKeyEntry<K, V> implements Map.Entry<K, V> {
        @NotNull
        private final V value;

        public ImmutableNullKeyEntry(@NotNull V value) {
            this.value = value;
        }

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry<?, ?> that)) {
                return false;
            }

            return that.getKey() == null && getValue().equals(that.getValue());
        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }

        @Override
        public String toString() {
            return "null=" + value;
        }
    }

    @Immutable
    private static final class ImmutableNullValueEntry<K, V> implements Map.Entry<K, V> {
        @NotNull
        private final K key;

        public ImmutableNullValueEntry(@NotNull K key) {
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry<?, ?> that)) {
                return false;
            }

            return getKey().equals(that.getKey()) && that.getValue() == null;
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }

        @Override
        public String toString() {
            return key + "=null";
        }
    }

    @Immutable
    private static final class ImmutableNull2NullEntry<K, V> implements Map.Entry<K, V> {
        private static final ImmutableNull2NullEntry<?, ?> INSTANCE = new ImmutableNull2NullEntry<>();

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Map.Entry<?, ?> that)) {
                return false;
            }

            return that.getKey() == that.getValue() && that.getValue() == null;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "null=null";
        }
    }
}
