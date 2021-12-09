package svegon.utils.fast.util.booleans.immutable;

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import net.jcip.annotations.Immutable;

import java.util.Arrays;
import java.util.RandomAccess;

@Immutable
public final class RegularImmutableBooleanList extends ImmutableBooleanList implements RandomAccess {
    public static final ImmutableBooleanList EMPTY = new RegularImmutableBooleanList(BooleanArrays.EMPTY_ARRAY);

    final boolean[] values;
    private final int hashCode;

    RegularImmutableBooleanList(boolean[] values) {
        this.values = values;
        this.hashCode = Arrays.hashCode(values);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean getBoolean(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, boolean[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
