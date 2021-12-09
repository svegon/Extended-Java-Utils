package svegon.utils.fast.util.shorts;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.shorts.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

@Immutable
public abstract class ImmutableShortList extends ImmutableShortCollection implements ShortList {
    ImmutableShortList() {

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
    public final @NotNull ShortListIterator iterator() {
        return listIterator();
    }

    @Override
    public boolean contains(short key) {
        return indexOf(key) >= 0;
    }

    @Override
    public short[] toArray(short[] a) {
        if (a.length < size()) {
            a = new short[size()];
        }

        getElements(0, a, 0, size());
        return a;
    }

    @Override
    public ShortSpliterator spliterator() {
        if (this instanceof RandomAccess) {
            return new IndexBasedImmutableSpliterator(this);
        } else {
            return ShortSpliterators.asSpliterator(iterator(), sizeOf(this),
                    ShortSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public final void addElements(int index, short[] a, int offset, int length) {
        if (length != 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends Short> c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final @NotNull ShortListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public ShortListIterator listIterator(int index) {
        return new RandomAccessImmutableListIterator(Preconditions.checkPositionIndex(index, size()));
    }

    @Override
    public ImmutableShortList subList(int from, int to) {
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
    public void getElements(int from, short[] a, int offset, int length) {
        Preconditions.checkPositionIndexes(from, from + length, size());
        ShortArrays.ensureOffsetLength(a, offset, length);

        ShortListIterator it = listIterator(from);

        while (it.hasNext()) {
            a[it.nextIndex()] = it.nextShort();
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
    public final void addElements(int index, short[] a) {
        Preconditions.checkPositionIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, short key) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, ShortCollection c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final short set(int index, short k) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(short k) {
        ShortListIterator it = listIterator();

        while (it.hasNext()) {
            if (it.nextShort() == k) {
                return it.previousIndex();
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(short k) {
        ShortListIterator it = listIterator(size() - 1);

        while (it.hasPrevious()) {
            if (it.previousShort() == k) {
                return it.nextIndex();
            }
        }

        return -1;
    }

    @Override
    public final short removeShort(int index) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void sort(ShortComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void unstableSort(ShortComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int compareTo(@NotNull List<? extends Short> o) {
        int c = size() - o.size();

        if (c != 0) {
            return c;
        }

        ShortListIterator itr = listIterator();
        ListIterator<? extends Short> oItr = o.listIterator();

        while (itr.hasNext()) {
            if (!oItr.hasNext()) {
                return 1;
            }

            c = Short.compare(itr.nextShort(), oItr.next());

            if (c != 0) {
                return c;
            }
        }

        return oItr.hasNext() ? -1 : 0;
    }

    @Override
    protected int initHashCode() {
        int h = 0;
        ShortIterator it = iterator();

        while (it.hasNext()) {
            h = 31 * h + Short.hashCode(it.nextShort());
        }

        return h;
    }

    public static ImmutableShortList of(short... values) {
        return values.length == 0 ? RegularImmutableShortList.EMPTY : new RegularImmutableShortList(values.clone());
    }

    public static ImmutableShortList of(ShortIterator it) {
        return copyOf(new ShortArrayList(it));
    }

    public static ImmutableShortList copyOf(ShortIterable iterable) {
        if (iterable.getClass() == ShortArrayList.class) {
            short[] array = ((ShortArrayList) iterable).toShortArray();
            return array.length == 0 ? RegularImmutableShortList.EMPTY : new RegularImmutableShortList(array);
        }

        return iterable instanceof ShortCollection ? of(((ShortCollection) iterable).toShortArray())
                : of(iterable.iterator());
    }

    protected class RandomAccessImmutableListIterator implements ShortListIterator {
        protected int index;

        public RandomAccessImmutableListIterator(int index) {
            this.index = index;
        }

        @Override
        public short nextShort() {
            return ImmutableShortList.this.getShort(index++);
        }

        @Override
        public boolean hasNext() {
            return index < ImmutableShortList.this.size();
        }

        @Override
        public short previousShort() {
            return ImmutableShortList.this.getShort(--index);
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

    protected static class IndexBasedImmutableSpliterator implements ShortSpliterator {
        private final ImmutableShortList list;
        private final int start;
        private final int fence;
        private int index;

        public IndexBasedImmutableSpliterator(ImmutableShortList list, int index, int fence) {
            this.list = list;
            this.start = index;
            this.fence = fence;
            this.index = index;
        }

        public IndexBasedImmutableSpliterator(ImmutableShortList list) {
            this(list, 0, list.size());
        }

        @Override
        public long skip(long n) {
            int i = index;
            index = (int) Math.min(index + n, fence);
            return index - i;
        }

        @Override
        public ShortSpliterator trySplit() {
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
            return ShortSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }

        @Override
        public boolean tryAdvance(ShortConsumer action) {
            if (index >= fence) {
                return false;
            }

            action.accept(list.getShort(index++));
            return true;
        }
    }

    protected static class SubList extends ImmutableShortList {
        protected final ImmutableShortList list;
        protected final int from;
        protected final int size;

        public SubList(ImmutableShortList list, int from, int to) {
            this.list = list;
            this.from = from;
            this.size = to - from;
        }

        @Override
        public short getShort(int index) {
            return list.getShort(index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public ShortListIterator listIterator(int index) {
            return list.listIterator(from + index);
        }

        @Override
        public ImmutableShortList subList(int from, int to) {
            Preconditions.checkPositionIndexes(from, to, size());
            return new SubList(list, this.from + from, this.from + to);
        }
    }

    protected static class RandomAccessSubList extends SubList implements RandomAccess {
        public RandomAccessSubList(ImmutableShortList list, int from, int to) {
            super(list, from, to);
        }

        @Override
        public ShortSpliterator spliterator() {
            return new IndexBasedImmutableSpliterator(list, from, from + size);
        }
    }
}
