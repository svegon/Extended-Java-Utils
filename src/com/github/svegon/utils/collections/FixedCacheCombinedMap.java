/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.github.svegon.utils.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FixedCacheCombinedMap<K, V> extends AbstractCombinedMap<K, V> implements CombinedMap<K, V> {
    private final ImmutableList<Map<K, V>> cache;

    public FixedCacheCombinedMap(final ImmutableList<Map<K, V>> cache) {
        this.cache = Preconditions.checkNotNull(cache);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public FixedCacheCombinedMap(Map<? extends K, ? extends V>... initialMaps) {
        this(ImmutableList.copyOf((Map<K, V>[]) initialMaps));
    }

    @SuppressWarnings("unchecked")
    public FixedCacheCombinedMap(Collection<? extends Map<? extends K, ? extends V>> initialMaps) {
        this(ImmutableList.copyOf((Collection<Map<K, V>>) initialMaps));
    }

    @Override
    public final @NotNull ImmutableList<Map<K, V>> getCache() {
        return cache;
    }

    @Override
    protected V putEntry(K key, V value) {
        if (getCache().isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return getCache().get(getCache().size() - 1).put(key, value);
    }
}
