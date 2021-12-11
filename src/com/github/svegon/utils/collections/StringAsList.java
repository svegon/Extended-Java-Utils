package com.github.svegon.utils.collections;

import com.github.svegon.utils.fast.util.chars.ImprovedCharCollection;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.AbstractCharList;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.RandomAccess;

@Immutable
public final class StringAsList extends AbstractCharList implements ImprovedCharCollection, RandomAccess, CharSequence {
    private final String string;

    StringAsList(final String string) {
        this.string = string;
    }

    @Override
    public char getChar(final int index) {
        return string.charAt(index);
    }

    @Override
    public int size() {
        return string.length();
    }

    @Override
    public @NotNull StringAsList subList(final int from, final int to) {
        return new StringAsList(string.substring(from, to));
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public void getElements(final int from, final char[] a, final int offset, final int length) {
        string.getChars(from, from + length, a, offset);
    }

    @Override
    public @NotNull String toString() {
        return string;
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
    public StringAsList subSequence(final int start, final int end) {
        return subList(start, end);
    }

    public static StringAsList of(final String string, final int startingIndex, final int endingIndex) {
        Preconditions.checkPositionIndexes(startingIndex, endingIndex, string.length());
        return new StringAsList(string.substring(startingIndex, endingIndex));
    }

    public static StringAsList of(final String string, final int startingIndex) {
        return of(string, startingIndex, string.length());
    }

    public static StringAsList of(final String string) {
        return new StringAsList(Preconditions.checkNotNull(string));
    }
}
