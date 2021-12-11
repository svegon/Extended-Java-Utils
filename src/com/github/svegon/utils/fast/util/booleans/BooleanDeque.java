package com.github.svegon.utils.fast.util.booleans;

import java.util.Deque;

public interface BooleanDeque extends ImprovedBooleanCollection, Deque<Boolean> {
    @Override
    default boolean add(Boolean key) {
        return ImprovedBooleanCollection.super.add(key);
    }

    @Override
    default boolean remove(Object key) {
        return ImprovedBooleanCollection.super.remove(key);
    }

    @Override
    default boolean contains(Object key) {
        return ImprovedBooleanCollection.super.contains(key);
    }
}
