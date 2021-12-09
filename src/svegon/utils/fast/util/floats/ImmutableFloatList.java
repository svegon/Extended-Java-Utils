package svegon.utils.fast.util.floats;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.floats.*;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

@Immutable
public abstract class ImmutableFloatList extends ImmutableFloatCollection implements FloatList {
    ImmutableFloatList() {

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
    public final @NotNull FloatListIterator iterator() {
        return listIterator();
    }

    @Override
    public boolean contains(float key) {
        return indexOf(key) >= 0;
    }

    @Override
    public float[] toArray(float[] a) {
        if (a.length < size()) {
            a = new float[size()];
        }

        getElements(0, a, 0, size());
        return a;
    }

    @Override
    public FloatSpliterator spliterator() {
        if (this instanceof RandomAccess) {
            return new IndexBasedImmutableSpliterator(this);
        } else {
            return FloatSpliterators.asSpliterator(iterator(), sizeOf(this),
                    FloatSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE);
        }
    }

    @Override
    public final void addElements(int index, float[] a, int offset, int length) {
        if (length != 0) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends Float> c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final @NotNull FloatListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public FloatListIterator listIterator(int index) {
        return new RandomAccessImmutableListIterator(Preconditions.checkPositionIndex(index, size()));
    }

    @Override
    public ImmutableFloatList subList(int from, int to) {
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
    public void getElements(int from, float[] a, int offset, int length) {
        Preconditions.checkPositionIndexes(from, from + length, size());
        FloatArrays.ensureOffsetLength(a, offset, length);

        FloatListIterator it = listIterator(from);

        while (it.hasNext()) {
            a[it.nextIndex()] = it.nextFloat();
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
    public final void addElements(int index, float[] a) {
        Preconditions.checkPositionIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, float key) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, FloatCollection c) {
        Preconditions.checkPositionIndex(index, size());

        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final float set(int index, float k) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(float k) {
        FloatListIterator it = listIterator();

        while (it.hasNext()) {
            if (it.nextFloat() == k) {
                return it.previousIndex();
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(float k) {
        FloatListIterator it = listIterator(size() - 1);

        while (it.hasPrevious()) {
            if (it.previousFloat() == k) {
                return it.nextIndex();
            }
        }

        return -1;
    }

    @Override
    public final float removeFloat(int index) {
        Preconditions.checkElementIndex(index, size());
        throw new UnsupportedOperationException();
    }

    @Override
    public final void sort(FloatComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void unstableSort(FloatComparator comparator) {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int compareTo(@NotNull List<? extends Float> o) {
        int c = size() - o.size();

        if (c != 0) {
            return c;
        }

        FloatListIterator itr = listIterator();
        ListIterator<? extends Float> oItr = o.listIterator();

        while (itr.hasNext()) {
            if (!oItr.hasNext()) {
                return 1;
            }

            c = Float.compare(itr.nextFloat(), oItr.next());

            if (c != 0) {
                return c;
            }
        }

        return oItr.hasNext() ? -1 : 0;
    }

    @Override
    protected int initHashCode() {
        int h = 0;
        FloatIterator it = iterator();

        while (it.hasNext()) {
            h = 31 * h + Float.hashCode(it.nextFloat());
        }

        return h;
    }

    public static ImmutableFloatList of(float... values) {
        return values.length == 0 ? RegularImmutableFloatList.EMPTY : new RegularImmutableFloatList(values.clone());
    }

    public static ImmutableFloatList of(FloatIterator it) {
        return copyOf(new FloatArrayList(it));
    }

    public static ImmutableFloatList copyOf(FloatIterable iterable) {
        if (iterable.getClass() == FloatArrayList.class) {
            float[] array = ((FloatArrayList) iterable).toFloatArray();
            return array.length == 0 ? RegularImmutableFloatList.EMPTY : new RegularImmutableFloatList(array);
        }

        return iterable instanceof FloatCollection ? of(((FloatCollection) iterable).toFloatArray())
                : of(iterable.iterator());
    }

    protected class RandomAccessImmutableListIterator implements FloatListIterator {
        protected int index;

        public RandomAccessImmutableListIterator(int index) {
            this.index = index;
        }

        @Override
        public float nextFloat() {
            return ImmutableFloatList.this.getFloat(index++);
        }

        @Override
        public boolean hasNext() {
            return index < ImmutableFloatList.this.size();
        }

        @Override
        public float previousFloat() {
            return ImmutableFloatList.this.getFloat(--index);
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

    protected static class IndexBasedImmutableSpliterator implements FloatSpliterator {
        private final ImmutableFloatList list;
        private final int start;
        private final int fence;
        private int index;

        public IndexBasedImmutableSpliterator(ImmutableFloatList list, int index, int fence) {
            this.list = list;
            this.start = index;
            this.fence = fence;
            this.index = index;
        }

        public IndexBasedImmutableSpliterator(ImmutableFloatList list) {
            this(list, 0, list.size());
        }

        @Override
        public long skip(long n) {
            int i = index;
            index = (int) Math.min(index + n, fence);
            return index - i;
        }

        @Override
        public FloatSpliterator trySplit() {
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
            return FloatSpliterators.LIST_SPLITERATOR_CHARACTERISTICS | Spliterator.IMMUTABLE;
        }

        @Override
        public boolean tryAdvance(FloatConsumer action) {
            if (index >= fence) {
                return false;
            }

            action.accept(list.getFloat(index++));
            return true;
        }
    }

    protected static class SubList extends ImmutableFloatList {
        protected final ImmutableFloatList list;
        protected final int from;
        protected final int size;

        public SubList(ImmutableFloatList list, int from, int to) {
            this.list = list;
            this.from = from;
            this.size = to - from;
        }

        @Override
        public float getFloat(int index) {
            return list.getFloat(index);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public FloatListIterator listIterator(int index) {
            return list.listIterator(from + index);
        }

        @Override
        public ImmutableFloatList subList(int from, int to) {
            Preconditions.checkPositionIndexes(from, to, size());
            return new SubList(list, this.from + from, this.from + to);
        }
    }

    protected static class RandomAccessSubList extends SubList implements RandomAccess {
        public RandomAccessSubList(ImmutableFloatList list, int from, int to) {
            super(list, from, to);
        }

        @Override
        public FloatSpliterator spliterator() {
            return new IndexBasedImmutableSpliterator(list, from, from + size);
        }
    }
}
