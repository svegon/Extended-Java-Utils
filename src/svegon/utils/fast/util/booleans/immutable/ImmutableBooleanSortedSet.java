package svegon.utils.fast.util.booleans.immutable;

import it.unimi.dsi.fastutil.booleans.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import svegon.utils.collections.ArrayUtil;
import svegon.utils.fast.util.booleans.BooleanSortedSet;

import java.util.Spliterator;

@Immutable
public abstract class ImmutableBooleanSortedSet extends ImmutableBooleanSet implements BooleanSortedSet {
    private final BooleanComparator comparator;

    ImmutableBooleanSortedSet(final @Nullable BooleanComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public abstract boolean contains(boolean key);

    @Override
    public abstract ImmutableBooleanSortedSet subList(int from, int to);

    @Override
    public abstract boolean getBoolean(int index);

    @Override
    public final BooleanBidirectionalIterator iterator(boolean fromElement) {
        int index = indexOf(fromElement);
        return index < 0 ? listIterator() : listIterator(index);
    }

    @Override
    public final BooleanSortedSet headSet(boolean toElement) {
        int index = indexOf(toElement);
        return index < 0 ? empty(comparator()) : subList(0, index);
    }

    @Override
    public final BooleanSortedSet tailSet(boolean fromElement) {
        int index = indexOf(fromElement);
        return index < 0 ? this : subList(index, size());
    }

    @Override
    public final BooleanComparator comparator() {
        return comparator;
    }

    @Override
    public boolean firstBoolean() {
        return getBoolean(0);
    }

    @Override
    public abstract boolean lastBoolean();

    @Override
    public abstract int size();

    public static ImmutableBooleanSortedSet empty(final @Nullable BooleanComparator comparator) {
        return new EmptyImmutableBooleanSortedSet(comparator);
    }

    public static ImmutableBooleanSortedSet of(final @Nullable BooleanComparator comparator, boolean bl) {
        if (comparator == null) {
            return bl ? SingletonImmutableBooleanSortedSet.TRUE : SingletonImmutableBooleanSortedSet.FALSE;
        }

        return new SingletonImmutableBooleanSortedSet(comparator, bl);
    }

    public static ImmutableBooleanSortedSet both(final @Nullable BooleanComparator comparator) {
        return comparator != null ? new FullImmutableBooleanSortedSet(comparator)
                : FullImmutableBooleanSortedSet.DEFAULT_SORTING;
    }

    public static ImmutableBooleanSortedSet of(final @Nullable BooleanComparator comparator,
                                               final boolean @NotNull ... values) {
        return copyOf(comparator, ArrayUtil.asList(values).iterator());
    }

    public static ImmutableBooleanSortedSet copyOf(final @Nullable BooleanComparator comparator,
                                                   final @NotNull BooleanIterable iterable) {
        if (iterable instanceof BooleanCollection c) {
            if (c.contains(false)) {
                return c.contains(true) ? both(comparator) : of(comparator, false);
            } else {
                return c.contains(true) ? of(comparator, true) : empty(comparator);
            }
        }

        return copyOf(comparator, iterable.iterator());
    }

    public static ImmutableBooleanSortedSet copyOf(final @Nullable BooleanComparator comparator,
                                                   final @NotNull BooleanIterator itr) {
        boolean true_present = false;
        boolean false_present = false;

        while (itr.hasNext() && !(true_present && false_present)) {
            if (itr.nextBoolean()) {
                true_present = true;
            } else {
                false_present = true;
            }
        }

        if (false_present) {
            return true_present ? both(comparator) : of(comparator, false);
        } else {
            return true_present ? of(comparator, true) : empty(comparator);
        }
    }

    public static ImmutableBooleanSortedSet copyOf(final @NotNull BooleanSortedSet set) {
        return copyOf(set.comparator(), set.iterator());
    }

    protected static class ImmutableBooleanSortedSetSpliterator extends IndexBasedImmutableSpliterator {
        public ImmutableBooleanSortedSetSpliterator(ImmutableBooleanSortedSet set, int index, int fence) {
            super(set, index, fence);
        }

        public ImmutableBooleanSortedSetSpliterator(ImmutableBooleanSortedSet set) {
            super(set);
        }

        @Override
        public int characteristics() {
            return BooleanSpliterators.LIST_SPLITERATOR_CHARACTERISTICS
                    | BooleanSpliterators.SORTED_SET_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }
    }
}
