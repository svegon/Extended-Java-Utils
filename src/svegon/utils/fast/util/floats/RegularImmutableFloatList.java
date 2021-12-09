package svegon.utils.fast.util.floats;

import it.unimi.dsi.fastutil.floats.FloatArrays;
import net.jcip.annotations.Immutable;

import java.util.RandomAccess;

@Immutable
final class RegularImmutableFloatList extends ImmutableFloatList implements RandomAccess {
    public static final ImmutableFloatList EMPTY = new RegularImmutableFloatList(FloatArrays.EMPTY_ARRAY);

    final float[] values;

    RegularImmutableFloatList(float[] values) {
        this.values = values;
    }

    @Override
    public float getFloat(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, float[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
