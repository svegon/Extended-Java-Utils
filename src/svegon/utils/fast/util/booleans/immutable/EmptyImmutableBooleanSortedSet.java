package svegon.utils.fast.util.booleans.immutable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.booleans.BooleanComparator;
import org.jetbrains.annotations.Nullable;
import svegon.utils.fast.util.booleans.BooleanSortedSet;

import java.util.NoSuchElementException;

public final class EmptyImmutableBooleanSortedSet extends ImmutableBooleanSortedSet {
    EmptyImmutableBooleanSortedSet(@Nullable BooleanComparator comparator) {
        super(comparator);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean contains(boolean key) {
        return false;
    }

    @Override
    public ImmutableBooleanSortedSet subList(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, size());
        return this;
    }

    @Override
    public boolean getBoolean(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(Strings.lenientFormat("index (%s) must not be negative",
                    index));
        } else {
            throw new IndexOutOfBoundsException(Strings.lenientFormat("index (%s) must be less than size (0)",
                    index));
        }
    }

    @Override
    public BooleanSortedSet subSet(boolean fromElement, boolean toElement) {
        return this;
    }

    @Override
    public boolean firstBoolean() {
        throw new NoSuchElementException();
    }

    @Override
    public boolean lastBoolean() {
        throw new NoSuchElementException();
    }

    @Override
    public int size() {
        return 0;
    }
}
