package com.github.svegon.utils.fast.util.booleans.immutable;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.booleans.BooleanComparator;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import com.github.svegon.utils.fast.util.booleans.BooleanSortedSet;

@Immutable
public final class SingletonImmutableBooleanSortedSet extends ImmutableBooleanSortedSet {
    public static final SingletonImmutableBooleanSortedSet FALSE = new SingletonImmutableBooleanSortedSet(false);
    public static final SingletonImmutableBooleanSortedSet TRUE = new SingletonImmutableBooleanSortedSet(true);

    private final BooleanComparator actualComparator;
    private final boolean value;

    private SingletonImmutableBooleanSortedSet(boolean value) {
        super(null);
        this.actualComparator = Boolean::compare;
        this.value = value;
    }

    SingletonImmutableBooleanSortedSet(@NotNull BooleanComparator comparator, boolean value) {
        super(comparator);
        this.actualComparator = comparator;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public boolean contains(boolean key) {
        return key == value;
    }

    @Override
    public ImmutableBooleanSortedSet subList(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, size());
        return from == to ? ImmutableBooleanSortedSet.empty(comparator()) : this;
    }

    @Override
    public boolean getBoolean(int index) {
        Preconditions.checkElementIndex(index, size());
        return value;
    }

    @Override
    public BooleanSortedSet subSet(boolean fromElement, boolean toElement) {
        return actualComparator.compare(fromElement, toElement) == 0
                ? ImmutableBooleanSortedSet.empty(comparator()) : this;
    }

    @Override
    public boolean lastBoolean() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}
