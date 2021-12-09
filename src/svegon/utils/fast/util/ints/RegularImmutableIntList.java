package svegon.utils.fast.util.ints;

import it.unimi.dsi.fastutil.ints.IntArrays;
import net.jcip.annotations.Immutable;

import java.util.RandomAccess;

@Immutable
final class RegularImmutableIntList extends ImmutableIntList implements RandomAccess {
    public static final ImmutableIntList EMPTY = new RegularImmutableIntList(IntArrays.EMPTY_ARRAY);

    final int[] values;

    RegularImmutableIntList(int[] values) {
        this.values = values;
    }

    @Override
    public int getInt(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, int[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
