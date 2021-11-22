/*
 * Copyright (c) 2021-2021 Svegon and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package svegon.utils.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MutableCombinedMap<K, V> extends AbstractCombinedMap<K, V> implements CombinedMap<K, V> {
    private final LinkedHashSet<Map<K, V>> cache;

    public MutableCombinedMap(final LinkedHashSet<Map<K, V>> cache) {
        this.cache = Preconditions.checkNotNull(cache);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public MutableCombinedMap(Map<? extends K, ? extends V>... initialMaps) {
        this(Sets.newLinkedHashSet(Arrays.asList((Map<K, V>[]) initialMaps)));
    }

    @SuppressWarnings("unchecked")
    public MutableCombinedMap(Collection<? extends Map<? extends K, ? extends V>> initialMaps) {
        this(Sets.newLinkedHashSet((Collection<Map<K, V>>) initialMaps));
    }

    @Override
    public boolean addMap(Map<K, V> map) {
        return getCache().add(map);
    }

    @Override
    public final @NotNull LinkedHashSet<Map<K, V>> getCache() {
        return cache;
    }

    @Override
    protected V putEntry(final K key, V value) {
        Optional<Map<K, V>> unusedMap = getCache().stream().filter(m -> !m.containsKey(key)).findAny();

        if (unusedMap.isPresent()) {
            unusedMap.get().put(key, value);
            return getCache().stream().filter(m -> m.containsKey(key)).map(m -> m.get(key)).findAny().orElse(null);
        }

        getCache().add(Collections.singletonMap(key, value));
        return null;
    }
}
