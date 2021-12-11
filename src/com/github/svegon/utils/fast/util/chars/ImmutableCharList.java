package com.github.svegon.utils.fast.util.chars;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

@Immutable
public abstract class ImmutableCharList extends ImmutableCharCollection implements CharList {
    ImmutableCharList() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        try {
            return compareTo((List) obj) == 0;
        } catch (ClassCastException ignored) {
            return false;
        }
    }

    @Override
    public final @NotNull CharListIterator iterator() {
        return listIterator();
    }

    @Override
    public boolean contains(char key) {
        return indexOf(key) >= 0;
    }

    @Override
    public char[] toArray(char[] a) {
        if (a.length < size()) {
            a = new char[size()];
        }

        getElements(0, a, 0, size());
        return a;
    }

    @Override
    public CharSpliterator spliterator() {
        if (this instanceof RandomAccess) {
            return new IndexBasedImmutableSpliterator(this);
        } else {
            return CharSpliterators.asSpliterator(iterator(), sizeOf(this),
                    CharSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public final void addElements(int index, char[] a, int offset, int length) {
        if (length != 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends Character> c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final @NotNull CharListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public CharListIterator listIterator(int index) {
        return new RandomAccessImmutableListIterator(Preconditions.checkPositionIndex(index, size()));
    }

    @Override
    public ImmutableCharList subList(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, size());
        return this instanceof RandomAccess ? new RandomAccessSubList(this, from, to)
                : new SubList(this, from, to);
    }

    @Override
    public final void size(int size) {
        if (size != size()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void getElements(int from, char[] a, int offset, int length) {
        Preconditions.checkPositionIndexes(from, from + length, size());
        CharArrays.ensureOffsetLength(a, offset, length);

        CharListIterator it = listIterator(from);

        while (it.hasNext()) {
            a[it.nextIndex()] = it.nextChar();
        }
    }

    @Override
    public final void removeElements(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, size());

        if (from != to) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void addElements(int index, char[] a) {
        Preconditions.checkPositionIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, char key) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, CharCollection c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final char set(int index, char k) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(char k) {
        CharListIterator it = listIterator();

        while (it.hasNext()) {
            if (it.nextChar() == k) {
                return it.previousIndex();
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(char k) {
        CharListIterator it = listIterator(size() - 1);

        while (it.hasPrevious()) {
            if (it.previousChar() == k) {
                return it.nextIndex();
            }
        }

        return -1;
    }

    @Override
    public final char removeChar(int index) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void sort(CharComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void unstableSort(CharComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int compareTo(@NotNull List<? extends Character> o) {
        int c = size() - o.size();

        if (c != 0) {
            return c;
        }

        CharListIterator itr = listIterator();
        ListIterator<? extends Character> oItr = o.listIterator();

        while (itr.hasNext()) {
            if (!oItr.hasNext()) {
                return 1;
            }

            c = Character.compare(itr.nextChar(), oItr.next());

            if (c != 0) {
                return c;
            }
        }

        return oItr.hasNext() ? -1 : 0;
    }

    @Override
    protected int initHashCode() {
        int h = 0;
        CharIterator it = iterator();

        while (it.hasNext()) {
            h = 31 * h + Character.hashCode(it.nextChar());
        }

        return h;
    }

    public static ImmutableCharList of(char... values) {
        return values.length == 0 ? RegularImmutableCharList.EMPTY : new RegularImmutableCharList(values.clone());
    }

    public static ImmutableCharList of(CharIterator it) {
        return copyOf(new CharArrayList(it));
    }

    public static ImmutableCharList copyOf(CharIterable iterable) {
        if (iterable.getClass() == CharArrayList.class) {
            char[] array = ((CharArrayList) iterable).toCharArray();
            return array.length == 0 ? RegularImmutableCharList.EMPTY : new RegularImmutableCharList(array);
        }

        return iterable instanceof CharCollection ? of(((CharCollection) iterable).toCharArray())
                : of(iterable.iterator());
    }

    protected class RandomAccessImmutableListIterator implements CharListIterator {
        protected int index;

        public RandomAccessImmutableListIterator(int index) {
            this.index = index;
        }

        @Override
        public char nextChar() {
            return ImmutableCharList.this.getChar(index++);
        }

        @Override
        public boolean hasNext() {
            return index < ImmutableCharList.this.size();
        }

        @Override
        public char previousChar() {
            return ImmutableCharList.this.getChar(--index);
        }

        @Override
        public boolean hasPrevious() {
            return index >= 0;
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }
    }

    protected static class IndexBasedImmutableSpliterator implements CharSpliterator {
        private final ImmutableCharList list;
        private final int start;
        private final int fence;
        private int index;

        public IndexBasedImmutableSpliterator(ImmutableCharList list, int index, int fence) {
            this.list = list;
            this.start = index;
            this.fence = fence;
            this.index = index;
        }

        public IndexBasedImmutableSpliterator(ImmutableCharList list) {
            this(list, 0, list.size());
        }

        @Override
        public long skip(long n) {
            int i = index;
            index = (int) Math.min(index + n, fence);
            return index - i;
        }

        @Override
        public CharSpliterator trySplit() {
            return new IndexBasedImmutableSpliterator(list, start, index);
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public long getExactSizeIfKnown() {
            return estimateSize();
        }

        @Override
        public int characteristics() {
            return CharSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }

        @Override
        public boolean tryAdvance(CharConsumer action) {
            if (index >= fence) {
                return false;
            }

            action.accept(list.getChar(index++));
            return true;
        }
    }

    protected static class SubList extends ImmutableCharList {
        protected final ImmutableCharList list;
        protected final int from;
        protected final int size;

        public SubList(ImmutableCharList list, int from, int to) {
            this.list = list;
            this.from = from;
            this.size = to - from;
        }

        @Override
        public char getChar(int index) {
            return list.getChar(index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public CharListIterator listIterator(int index) {
            return list.listIterator(from + index);
        }

        @Override
        public ImmutableCharList subList(int from, int to) {
            Preconditions.checkPositionIndexes(from, to, size());
            return new SubList(list, this.from + from, this.from + to);
        }
    }

    protected static class RandomAccessSubList extends SubList implements RandomAccess {
        public RandomAccessSubList(ImmutableCharList list, int from, int to) {
            super(list, from, to);
        }

        @Override
        public CharSpliterator spliterator() {
            return new IndexBasedImmutableSpliterator(list, from, from + size);
        }
    }
}
