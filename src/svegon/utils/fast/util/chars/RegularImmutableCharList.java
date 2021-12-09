package svegon.utils.fast.util.chars;

import it.unimi.dsi.fastutil.chars.CharArrays;
import net.jcip.annotations.Immutable;

import java.util.RandomAccess;

@Immutable
final class RegularImmutableCharList extends ImmutableCharList implements RandomAccess {
    public static final ImmutableCharList EMPTY = new RegularImmutableCharList(CharArrays.EMPTY_ARRAY);

    final char[] values;

    RegularImmutableCharList(char[] values) {
        this.values = values;
    }

    @Override
    public char getChar(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void getElements(int from, char[] a, int offset, int length) {
        System.arraycopy(values, from, a, offset, length);
    }
}
