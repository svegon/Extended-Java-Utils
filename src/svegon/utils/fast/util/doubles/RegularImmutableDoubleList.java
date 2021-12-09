package svegon.utils.fast.util.doubles;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import net.jcip.annotations.Immutable;

import java.util.RandomAccess;

@Immutable
final class RegularImmutableDoubleList extends ImmutableDoubleList implements RandomAccess {
    public static final ImmutableDoubleList EMPTY = new RegularImmutableDoubleList(DoubleArrays.EMPTY_ARRAY);

    final double[] values;

    RegularImmutableDoubleList(double[] values) {
        this.values = values;
    }

    @Override
    public double getDouble(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, double[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
