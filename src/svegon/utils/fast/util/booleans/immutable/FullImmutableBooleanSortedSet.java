package svegon.utils.fast.util.booleans.immutable;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.booleans.BooleanComparator;
import org.jetbrains.annotations.NotNull;
import svegon.utils.fast.util.booleans.BooleanSortedSet;

public final class FullImmutableBooleanSortedSet extends ImmutableBooleanSortedSet {
    public static final FullImmutableBooleanSortedSet DEFAULT_SORTING = new FullImmutableBooleanSortedSet();

    private final BooleanComparator actualComparator;

    private FullImmutableBooleanSortedSet() {
        super(null);
        this.actualComparator = Boolean::compare;
    }

    FullImmutableBooleanSortedSet(@NotNull BooleanComparator comparator) {
        super(comparator);
        this.actualComparator = comparator;
    }

    @Override
    public int hashCode() {
        return 2468;
    }

    @Override
    public boolean contains(boolean key) {
        return true;
    }

    @Override
    public ImmutableBooleanSortedSet subList(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, size());

        if (from == to) {
            return ImmutableBooleanSortedSet.empty(comparator());
        }

        if (from == 0 && to == size()) {
            return this;
        }

        return ImmutableBooleanSortedSet.of(comparator(), getBoolean(from));
    }

    @Override
    public boolean getBoolean(int index) {
        return index == 0 == actualComparator.compare(false, true) > 0;
    }

    @Override
    public BooleanSortedSet subSet(boolean fromElement, boolean toElement) {
        return actualComparator.compare(fromElement, toElement) < 0 ? ImmutableBooleanSortedSet.of(comparator(),
                fromElement) : ImmutableBooleanSortedSet.empty(comparator());
    }

    @Override
    public boolean lastBoolean() {
        return getBoolean(1);
    }

    @Override
    public int size() {
        return 2;
    }
}
