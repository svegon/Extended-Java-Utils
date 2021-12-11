package com.github.svegon.utils.collections;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public interface CombinedMap<K, V> extends Multimap<K, V> {
    @NotNull Collection<? extends Map<K, V>> getCache();

    default boolean addMap(Map<K, V> map) {
        throw new UnsupportedOperationException();
    }
}
