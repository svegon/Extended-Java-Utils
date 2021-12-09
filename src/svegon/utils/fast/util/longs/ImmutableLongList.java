package svegon.utils.fast.util.longs;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;


import java.util.*;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

@Immutable
public abstract class ImmutableLongList extends ImmutableLongCollection implements LongList {
    ImmutableLongList() {

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
    public final @NotNull LongListIterator iterator() {
        return listIterator();
    }

    @Override
    public boolean contains(long key) {
        return indexOf(key) >= 0;
    }

    @Override
    public long[] toArray(long[] a) {
        if (a.length < size()) {
            a = new long[size()];
        }

        getElements(0, a, 0, size());
        return a;
    }

    @Override
    public LongSpliterator spliterator() {
        if (this instanceof RandomAccess) {
            return new IndexBasedImmutableSpliterator(this);
        } else {
            return LongSpliterators.asSpliterator(iterator(), sizeOf(this),
                    LongSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public final void addElements(int index, long[] a, int offset, int length) {
        if (length != 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends Long> c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final @NotNull LongListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public LongListIterator listIterator(int index) {
        return new RandomAccessImmutableListIterator(Preconditions.checkPositionIndex(index, size()));
    }

    @Override
    public ImmutableLongList subList(int from, int to) {
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
    public void getElements(int from, long[] a, int offset, int length) {
        Preconditions.checkPositionIndexes(from, from + length, size());
        LongArrays.ensureOffsetLength(a, offset, length);

        LongListIterator it = listIterator(from);

        while (it.hasNext()) {
            a[it.nextIndex()] = it.nextLong();
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
    public final void addElements(int index, long[] a) {
        Preconditions.checkPositionIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, long key) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, LongCollection c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final long set(int index, long k) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(long k) {
        LongListIterator it = listIterator();

        while (it.hasNext()) {
            if (it.nextLong() == k) {
                return it.previousIndex();
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(long k) {
        LongListIterator it = listIterator(size() - 1);

        while (it.hasPrevious()) {
            if (it.previousLong() == k) {
                return it.nextIndex();
            }
        }

        return -1;
    }

    @Override
    public final long removeLong(int index) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void sort(LongComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void unstableSort(LongComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int compareTo(@NotNull List<? extends Long> o) {
        int c = size() - o.size();

        if (c != 0) {
            return c;
        }

        LongListIterator itr = listIterator();
        ListIterator<? extends Long> oItr = o.listIterator();

        while (itr.hasNext()) {
            if (!oItr.hasNext()) {
                return 1;
            }

            c = Long.compare(itr.nextLong(), oItr.next());

            if (c != 0) {
                return c;
            }
        }

        return oItr.hasNext() ? -1 : 0;
    }

    public static ImmutableLongList of(long... values) {
        return values.length == 0 ? RegularImmutableLongList.EMPTY : new RegularImmutableLongList(values.clone());
    }

    public static ImmutableLongList copyOf(LongIterator it) {
        return copyOf(new LongArrayList(it));
    }

    public static ImmutableLongList copyOf(LongIterable iterable) {
        if (iterable.getClass() == LongArrayList.class) {
            long[] array = ((LongArrayList) iterable).toLongArray();
            return array.length == 0 ? RegularImmutableLongList.EMPTY : new RegularImmutableLongList(array);
        }

        return iterable instanceof LongCollection ? of(((LongCollection) iterable).toLongArray())
                : copyOf(iterable.iterator());
    }

    protected class RandomAccessImmutableListIterator implements LongListIterator {
        protected int index;

        public RandomAccessImmutableListIterator(int index) {
            this.index = index;
        }

        @Override
        public long nextLong() {
            return ImmutableLongList.this.getLong(index++);
        }

        @Override
        public boolean hasNext() {
            return index < ImmutableLongList.this.size();
        }

        @Override
        public long previousLong() {
            return ImmutableLongList.this.getLong(--index);
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

    protected static class IndexBasedImmutableSpliterator implements LongSpliterator {
        private final ImmutableLongList list;
        private final int start;
        private final int fence;
        private int index;

        public IndexBasedImmutableSpliterator(ImmutableLongList list, int index, int fence) {
            this.list = list;
            this.start = index;
            this.fence = fence;
            this.index = index;
        }

        public IndexBasedImmutableSpliterator(ImmutableLongList list) {
            this(list, 0, list.size());
        }

        @Override
        public long skip(long n) {
            int i = index;
            index = (int) Math.min(index + n, fence);
            return index - i;
        }

        @Override
        public LongSpliterator trySplit() {
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
            return LongSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }

        @Override
        public boolean tryAdvance(java.util.function.LongConsumer action) {
            if (index >= fence) {
                return false;
            }

            action.accept(list.getLong(index++));
            return true;
        }
    }

    protected static class SubList extends ImmutableLongList {
        private final int hashCode;
        protected final ImmutableLongList list;
        protected final int from;
        protected final int size;

        public SubList(ImmutableLongList list, int from, int to) {
            this.list = list;
            this.from = from;
            this.size = to - from;

            int h = 0;
            LongIterator it = list.listIterator(from);

            while (to-- != from) {
                h = 31 * h + Long.hashCode(it.nextLong());
            }

            hashCode = h;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public long getLong(int index) {
            return list.getLong(index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public LongListIterator listIterator(int index) {
            return list.listIterator(from + index);
        }

        @Override
        public ImmutableLongList subList(int from, int to) {
            Preconditions.checkPositionIndexes(from, to, size());
            return new ImmutableLongList.SubList(list, this.from + from, this.from + to);
        }
    }

    protected static class RandomAccessSubList extends SubList implements RandomAccess {
        public RandomAccessSubList(ImmutableLongList list, int from, int to) {
            super(list, from, to);
        }

        @Override
        public LongSpliterator spliterator() {
            return new ImmutableLongList.IndexBasedImmutableSpliterator(list, from, from + size);
        }
    }
}
