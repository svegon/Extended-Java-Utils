package svegon.utils.fast.util.shorts;

import it.unimi.dsi.fastutil.shorts.ShortArrays;
import net.jcip.annotations.Immutable;

import java.util.RandomAccess;

@Immutable
final class RegularImmutableShortList extends ImmutableShortList implements RandomAccess {
    public static final ImmutableShortList EMPTY = new RegularImmutableShortList(ShortArrays.EMPTY_ARRAY);

    final short[] values;

    RegularImmutableShortList(short[] values) {
        this.values = values;
    }

    @Override
    public short getShort(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, short[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
