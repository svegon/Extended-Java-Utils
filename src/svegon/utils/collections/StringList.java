package svegon.utils.collections;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.AbstractCharList;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.chars.CharSpliterators;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.RandomAccess;

@Immutable
public final class StringList extends AbstractCharList implements RandomAccess, CharSequence {
    private final String string;
    private final int startingIndex;
    private final int endingIndex;

    StringList(final String string, final int startingIndex, final int endingIndex) {
        this.string = string;
        this.startingIndex = startingIndex;
        this.endingIndex = endingIndex;
    }

    @Override
    public char getChar(final int index) {
        return string.charAt(Preconditions.checkElementIndex(startingIndex + index, size()));
    }

    @Override
    public int size() {
        return endingIndex - startingIndex;
    }

    @Contract(value = " -> new", pure = true)
    @Override
    public @NotNull CharSpliterator spliterator() {
        return new CharSpliterators.AbstractIndexBasedSpliterator(startingIndex) {
            @Override
            protected char get(int location) {
                return string.charAt(location);
            }

            @Override
            protected int getMaxPos() {
                return endingIndex;
            }

            @Override
            protected CharSpliterator makeForSplit(final int pos, final int maxPos) {
                return subList(pos, maxPos).spliterator();
            }
        };
    }

    @Override
    public @NotNull StringList subList(final int from, final int to) {
        Preconditions.checkPositionIndexes(from, to, size());

        return new StringList(string, startingIndex + from,
                Preconditions.checkElementIndex(startingIndex + to, size()));
    }

    @Override
    public int hashCode() {
        return string.substring(startingIndex, endingIndex).hashCode();
    }

    @Override
    public void getElements(final int from, final char[] a, final int offset, final int length) {
        string.getChars(startingIndex + from, startingIndex + from + length, a, offset);
    }

    @Override
    public @NotNull String toString() {
        return string.substring(startingIndex, endingIndex);
    }

    @Override
    public int length() {
        return size();
    }

    @Override
    public char charAt(int index) {
        return getChar(index);
    }

    @NotNull
    @Override
    public StringList subSequence(final int start, final int end) {
        return subList(start, end);
    }

    public String getOriginalString() {
        return string;
    }

    public int getStartingIndex() {
        return startingIndex;
    }

    public int getEndingIndex() {
        return endingIndex;
    }

    public static StringList of(final String string, final int startingIndex, final int endingIndex) {
        Preconditions.checkPositionIndexes(startingIndex, endingIndex, string.length());
        return new StringList(string, startingIndex, endingIndex);
    }

    public static StringList of(final String string, final int startingIndex) {
        return of(string, startingIndex, string.length());
    }

    public static StringList of(final String string) {
        return of(string, 0);
    }
}
