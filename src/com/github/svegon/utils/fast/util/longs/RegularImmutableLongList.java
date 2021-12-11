package com.github.svegon.utils.fast.util.longs;

import it.unimi.dsi.fastutil.longs.LongArrays;
import net.jcip.annotations.Immutable;

import java.util.RandomAccess;

@Immutable
final class RegularImmutableLongList extends ImmutableLongList implements RandomAccess {
    public static final ImmutableLongList EMPTY = new RegularImmutableLongList(LongArrays.EMPTY_ARRAY);

    final long[] values;

    RegularImmutableLongList(long[] values) {
        this.values = values;
    }

    @Override
    public long getLong(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, long[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
