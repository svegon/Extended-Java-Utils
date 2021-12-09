package svegon.utils.fast.util.bytes.immutable;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.bytes.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

@Immutable
public abstract class ImmutableByteList extends ImmutableByteCollection implements ByteList {
    ImmutableByteList() {

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
    public final @NotNull ByteListIterator iterator() {
        return listIterator();
    }

    @Override
    public boolean contains(byte key) {
        return indexOf(key) >= 0;
    }

    @Override
    public byte[] toArray(byte[] a) {
        if (a.length < size()) {
            a = new byte[size()];
        }

        getElements(0, a, 0, size());
        return a;
    }

    @Override
    public ByteSpliterator spliterator() {
        if (this instanceof RandomAccess) {
            return new IndexBasedImmutableSpliterator(this);
        } else {
            return ByteSpliterators.asSpliterator(iterator(), sizeOf(this),
                    ByteSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public final void addElements(int index, byte[] a, int offset, int length) {
        if (length != 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends Byte> c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final @NotNull ByteListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public ByteListIterator listIterator(int index) {
        return new RandomAccessImmutableListIterator(Preconditions.checkPositionIndex(index, size()));
    }

    @Override
    public ImmutableByteList subList(int from, int to) {
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
    public void getElements(int from, byte[] a, int offset, int length) {
        Preconditions.checkPositionIndexes(from, from + length, size());
        ByteArrays.ensureOffsetLength(a, offset, length);

        ByteListIterator it = listIterator(from);

        while (it.hasNext()) {
            a[it.nextIndex()] = it.nextByte();
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
    public final void addElements(int index, byte[] a) {
        Preconditions.checkPositionIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, byte key) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, ByteCollection c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final byte set(int index, byte k) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(byte k) {
        ByteListIterator it = listIterator();

        while (it.hasNext()) {
            if (it.nextByte() == k) {
                return it.previousIndex();
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(byte k) {
        ByteListIterator it = listIterator(size() - 1);

        while (it.hasPrevious()) {
            if (it.previousByte() == k) {
                return it.nextIndex();
            }
        }

        return -1;
    }

    @Override
    public final byte removeByte(int index) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void sort(ByteComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void unstableSort(ByteComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int compareTo(@NotNull List<? extends Byte> o) {
        int c = size() - o.size();

        if (c != 0) {
            return c;
        }

        ByteListIterator itr = listIterator();
        ListIterator<? extends Byte> oItr = o.listIterator();

        while (itr.hasNext()) {
            if (!oItr.hasNext()) {
                return 1;
            }

            c = Byte.compare(itr.nextByte(), oItr.next());

            if (c != 0) {
                return c;
            }
        }

        return oItr.hasNext() ? -1 : 0;
    }

    public static ImmutableByteList of(byte... values) {
        return values.length == 0 ? RegularImmutableByteList.EMPTY : new RegularImmutableByteList(values.clone());
    }

    public static ImmutableByteList of(ByteIterator it) {
        return copyOf(new ByteArrayList(it));
    }

    public static ImmutableByteList copyOf(ByteIterable iterable) {
        if (iterable.getClass() == ByteArrayList.class) {
            byte[] array = ((ByteArrayList) iterable).toByteArray();
            return array.length == 0 ? RegularImmutableByteList.EMPTY : new RegularImmutableByteList(array);
        }

        return iterable instanceof ByteCollection ? of(((ByteCollection) iterable).toByteArray())
                : of(iterable.iterator());
    }

    protected class RandomAccessImmutableListIterator implements ByteListIterator {
        protected int index;

        public RandomAccessImmutableListIterator(int index) {
            this.index = index;
        }

        @Override
        public byte nextByte() {
            return ImmutableByteList.this.getByte(index++);
        }

        @Override
        public boolean hasNext() {
            return index < ImmutableByteList.this.size();
        }

        @Override
        public byte previousByte() {
            return ImmutableByteList.this.getByte(--index);
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

    protected static class IndexBasedImmutableSpliterator implements ByteSpliterator {
        private final ImmutableByteList list;
        private final int start;
        private final int fence;
        private int index;

        public IndexBasedImmutableSpliterator(ImmutableByteList list, int index, int fence) {
            this.list = list;
            this.start = index;
            this.fence = fence;
            this.index = index;
        }

        public IndexBasedImmutableSpliterator(ImmutableByteList list) {
            this(list, 0, list.size());
        }

        @Override
        public long skip(long n) {
            int i = index;
            index = (int) Math.min(index + n, fence);
            return index - i;
        }

        @Override
        public ByteSpliterator trySplit() {
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
            return ByteSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }

        @Override
        public boolean tryAdvance(ByteConsumer action) {
            if (index >= fence) {
                return false;
            }

            action.accept(list.getByte(index++));
            return true;
        }
    }

    protected static class SubList extends ImmutableByteList {
        private final int hashCode;
        protected final ImmutableByteList list;
        protected final int from;
        protected final int size;

        public SubList(ImmutableByteList list, int from, int to) {
            this.list = list;
            this.from = from;
            this.size = to - from;

            ByteListIterator it = list.listIterator(from);
            int h = 0;

            while (to-- != from) {
                h = 31 * h + Byte.hashCode(it.nextByte());
            }

            hashCode = h;
        }

        @Override
        public final int hashCode() {
            return hashCode;
        }

        @Override
        public byte getByte(int index) {
            return list.getByte(index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public ByteListIterator listIterator(int index) {
            return list.listIterator(from + index);
        }

        @Override
        public ImmutableByteList subList(int from, int to) {
            Preconditions.checkPositionIndexes(from, to, size());
            return new ImmutableByteList.SubList(list, this.from + from, this.from + to);
        }
    }

    protected static class RandomAccessSubList extends SubList implements RandomAccess {
        public RandomAccessSubList(ImmutableByteList list, int from, int to) {
            super(list, from, to);
        }

        @Override
        public ByteSpliterator spliterator() {
            return new ImmutableByteList.IndexBasedImmutableSpliterator(list, from, from + size);
        }
    }
}
