package com.github.svegon.utils.fast.util.booleans.immutable;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.booleans.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

@Immutable
public abstract class ImmutableBooleanList extends ImmutableBooleanCollection implements BooleanList {
    ImmutableBooleanList() {

    }

    @Override
    public final boolean equals(Object obj) {
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
    public final @NotNull BooleanListIterator iterator() {
        return listIterator();
    }

    @Override
    public boolean contains(boolean key) {
        return indexOf(key) >= 0;
    }

    @Override
    public boolean[] toArray(boolean[] a) {
        if (a.length < size()) {
            a = new boolean[size()];
        }

        getElements(0, a, 0, size());
        return a;
    }

    @Override
    public BooleanSpliterator spliterator() {
        if (this instanceof RandomAccess) {
            return new IndexBasedImmutableSpliterator(this);
        } else {
            return BooleanSpliterators.asSpliterator(iterator(), sizeOf(this),
                    BooleanSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public final void addElements(int index, boolean[] a, int offset, int length) {
        if (length != 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends Boolean> c) {
        Preconditions.checkPositionIndex(index, size());

        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final @NotNull BooleanListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public BooleanListIterator listIterator(int index) {
        return new RandomAccessImmutableListIterator(this, Preconditions.checkPositionIndex(index, size()));
    }

    @Override
    public ImmutableBooleanList subList(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, size());

        if (from == 0 && to == size()) {
            return this;
        }

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
    public void getElements(int from, boolean[] a, int offset, int length) {
        Preconditions.checkPositionIndexes(from, from + length, size());
        BooleanArrays.ensureOffsetLength(a, offset, length);

        BooleanListIterator it = listIterator(from);

        while (it.hasNext()) {
            a[it.nextIndex()] = it.nextBoolean();
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
    public final void addElements(int index, boolean[] a) {
        Preconditions.checkPositionIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, boolean key) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, BooleanCollection c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean set(int index, boolean k) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(boolean k) {
        BooleanListIterator it = listIterator();

        while (it.hasNext()) {
            if (it.nextBoolean() == k) {
                return it.previousIndex();
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(boolean k) {
        BooleanListIterator it = listIterator(size() - 1);

        while (it.hasPrevious()) {
            if (it.previousBoolean() == k) {
                return it.nextIndex();
            }
        }

        return -1;
    }

    @Override
    public final boolean removeBoolean(int index) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void sort(BooleanComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void unstableSort(BooleanComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int compareTo(@NotNull List<? extends Boolean> o) {
        int c = size() - o.size();

        if (c != 0) {
            return c;
        }

        BooleanListIterator itr = listIterator();
        ListIterator<? extends Boolean> oItr = o.listIterator();

        while (itr.hasNext()) {
            if (!oItr.hasNext()) {
                return 1;
            }

            if (itr.nextBoolean() != oItr.next()) {
                return itr.previousBoolean() ? 1 : -1;
            }
        }

        return oItr.hasNext() ? -1 : 0;
    }

    public static ImmutableBooleanList of(boolean... values) {
        return values.length == 0 ? RegularImmutableBooleanList.EMPTY : new RegularImmutableBooleanList(values.clone());
    }

    public static ImmutableBooleanList of(BooleanIterator it) {
        return copyOf(new BooleanArrayList(it));
    }

    public static ImmutableBooleanList copyOf(BooleanIterable iterable) {
        if (iterable.getClass() == BooleanArrayList.class) {
            boolean[] array = ((BooleanArrayList) iterable).toBooleanArray();
            return array.length == 0 ? RegularImmutableBooleanList.EMPTY : new RegularImmutableBooleanList(array);
        }

        return iterable instanceof BooleanCollection ? of(((BooleanCollection) iterable).toBooleanArray())
                : of(iterable.iterator());
    }

    protected static class RandomAccessImmutableListIterator implements BooleanListIterator,
            BooleanBidirectionalIterator {
        private final ImmutableBooleanList list;
        private int index;

        public RandomAccessImmutableListIterator(ImmutableBooleanList list, int index) {
            this.list = list;
            this.index = index;
        }

        @Override
        public boolean nextBoolean() {
            return list.getBoolean(index++);
        }

        @Override
        public boolean hasNext() {
            return index < list.size();
        }

        @Override
        public boolean previousBoolean() {
            return list.getBoolean(--index);
        }

        @Override
        public int back(int n) {
            int i = index;
            index = Math.max(index - n, 0);
            return index - i;
        }

        @Override
        public int skip(int n) {
            int i = index;
            index = Math.min(index + n, list.size());
            return index - i;
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

    protected static class IndexBasedImmutableSpliterator implements BooleanSpliterator {
        private final ImmutableBooleanList list;
        private final int start;
        private final int fence;
        private int index;

        public IndexBasedImmutableSpliterator(ImmutableBooleanList list, int index, int fence) {
            this.list = list;
            this.start = index;
            this.fence = fence;
            this.index = index;
        }

        public IndexBasedImmutableSpliterator(ImmutableBooleanList list) {
            this(list, 0, list.size());
        }

        @Override
        public long skip(long n) {
            int i = index;
            index = (int) Math.min(index + n, fence);
            return index - i;
        }

        @Override
        public BooleanSpliterator trySplit() {
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
            return BooleanSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }

        @Override
        public boolean tryAdvance(BooleanConsumer action) {
            if (index >= fence) {
                return false;
            }

            action.accept(list.getBoolean(index++));
            return true;
        }
    }

    protected static class SubList extends ImmutableBooleanList {
        protected final ImmutableBooleanList list;
        protected final int from;
        protected final int size;
        private final int hashCode;

        public SubList(ImmutableBooleanList list, int from, int to) {
            this.list = list;
            this.from = from;
            this.size = to - from;

            BooleanListIterator it = list.listIterator(from);
            int h = 0;

            while (to-- != from) {
                h = 31 * h + Boolean.hashCode(it.nextBoolean());
            }

            this.hashCode = h;
        }

        @Override
        public boolean getBoolean(int index) {
            return list.getBoolean(from + index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public BooleanListIterator listIterator(int index) {
            return list.listIterator(from + index);
        }

        @Override
        public ImmutableBooleanList subList(int from, int to) {
            Preconditions.checkPositionIndexes(from, to, size());

            if (from == 0 && to == size()) {
                return this;
            }

            return new SubList(list, this.from + from, this.from + to);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    protected static class RandomAccessSubList extends SubList implements RandomAccess {
        public RandomAccessSubList(ImmutableBooleanList list, int from, int to) {
            super(list, from, to);
        }

        @Override
        public BooleanSpliterator spliterator() {
            return new IndexBasedImmutableSpliterator(list, from, from + size);
        }
    }
}
