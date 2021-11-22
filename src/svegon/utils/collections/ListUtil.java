package svegon.utils.collections;

import svegon.utils.interfaces.function.IntObjectBiFunction;
import svegon.utils.math.MathUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class ListUtil {
    private ListUtil() {
        throw new AssertionError();
    }

    public static <E> FixedCacheCombinedList<E> newFixedCacheCombinedList(ImmutableList<List<E>> cache) {
        return new FixedCacheCombinedList<>();
    }

    public static  <E> FixedCacheCombinedList<E> newFixedCacheCombinedList(List<? extends E>... collections) {
        return new FixedCacheCombinedList<>(collections);
    }

    public static  <E> FixedCacheCombinedList<E> newFixedCacheCombinedList(Iterable<? extends Collection<? extends E>>
                                                                                   collections) {
        return collections instanceof Collection
                ? new FixedCacheCombinedList<>((Collection<? extends Collection<? extends E>>) collections)
                : new FixedCacheCombinedList<>(Lists.newArrayList(collections));
    }

    public static  <E> FixedCacheCombinedList<E> newFixedCacheCombinedList(final Iterator<? extends Collection<? extends E>>
                                                                                   collections) {
        return new FixedCacheCombinedList<>(Lists.newArrayList(collections));
    }

    public static <E> ExposedArrayList<E> newExposedArrayList() {
        return new ExposedArrayList<>();
    }

    public static <E> ExposedArrayList<E> newExposedArrayList(@NotNull final Iterable<E> c) {
        return c instanceof Collection ? new ExposedArrayList<>((Collection<? extends E>) c)
                : newExposedArrayList(c.iterator());
    }


    public static <E> ExposedArrayList<E> newExposedArrayList(@NotNull final Iterator<E> iterator) {
        ExposedArrayList<E> a = new ExposedArrayList<>();

        iterator.forEachRemaining(a::add);

        return a;
    }

    public static <E> ExposedArrayList<E> newVisibleArrayListWithCapacity(final int capacity) {
        return capacity == 0 ? new ExposedArrayList<>() : new ExposedArrayList<>(new Object[capacity]);
    }

    @SafeVarargs
    public static <E, L extends List<E>> L merge(IntFunction<L> resultInstanceSupplier, List<E>... lists) {
        L list = resultInstanceSupplier.apply(Arrays.stream(lists).mapToInt(List::size).sum());

        for (List<E> merged : lists) {
            list.addAll(merged);
        }

        return list;
    }

    public static <E, L extends List<E>> L merge(final Supplier<L> resultInstanceSupplier, final List<E>... lists) {
        return merge((i) -> resultInstanceSupplier.get(), lists);
    }

    public static <E> ArrayList<E> merge(final List<E>... lists) {
        return merge(Lists::newArrayListWithCapacity, lists);
    }

    public static <E> void transformEach(List<E> list, IntObjectBiFunction<E, E> transformingOperator) {
        Objects.requireNonNull(transformingOperator);

        ListIterator<E> listIterator = list.listIterator();

        while (listIterator.hasNext()) {
            listIterator.set(transformingOperator.apply(listIterator.nextIndex(), listIterator.next()));
        }
    }

    public static <E> List<E> repeat(@Nullable E element, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("time is negative: " + times);
        }

        return new AbstractList<E>() {
            @Override
            public E get(int index) {
                if (index < 0 || index >= size()) {
                    throw new IndexOutOfBoundsException();
                }

                return element;
            }

            @Override
            public int size() {
                return times;
            }
        };
    }

    public static <E> List<E> iterate(IntFunction<E> elementAccessor, int size) {
        Objects.requireNonNull(elementAccessor);

        if (size <= 0) {
            throw new IllegalArgumentException();
        }

        return new IterationBasedList<>(new int[]{size}, (matrixIndices) -> elementAccessor.apply(matrixIndices[0]));
    }

    public static <E> List<E> iterate(IntInt2ObjectFunction<E> elementAccessor, int i, int j) {
        Objects.requireNonNull(elementAccessor);

        if (i <= 0 || j <= 0) {
            throw new IllegalArgumentException();
        }

        return new IterationBasedList<>(new int[]{i, j}, (matrixIndices) -> elementAccessor.apply(matrixIndices[0],
                matrixIndices[1]));
    }

    public static <E> List<E> iterate(IntIntInt2ObjectFunction<E> elementAccessor, int i, int j, int k) {
        Objects.requireNonNull(elementAccessor);

        if (i <= 0 || j <= 0 || k <= 0) {
            throw new IllegalArgumentException();
        }

        return new IterationBasedList<>(new int[]{i, j, k}, (matrixIndices) -> elementAccessor.apply(matrixIndices[0],
                matrixIndices[1], matrixIndices[2]));
    }

    public static <E> List<E> iterate(Function<int[], E> elementAccessor, int... dimensions) {
        dimensions = dimensions.clone();

        for (int i : dimensions) {
            if (i <= 0) {
                throw new IllegalArgumentException();
            }
        }

        return new IterationBasedList<>(dimensions, Objects.requireNonNull(elementAccessor));
    }

    public static <E> List<E> subList(List<E> list, int fromIndex, int toIndex) {
        return list instanceof RandomAccess ? new RandomAccessSubList<>(Objects.requireNonNull(list), fromIndex,
                toIndex) : new SubList<>(Objects.requireNonNull(list), fromIndex, toIndex);
    }

    public static <E> ListIterator<E> listIterator(List<E> list, int index) {
        return new ListItr<>(Objects.requireNonNull(list), index);
    }

    public static <E> ListIterator<E> listIterator(List<E> list) {
        return listIterator(list, 0);
    }

    private static class IterationBasedList<E> extends AbstractList<E> implements RandomAccess {
        private final int[] dimensions;
        private final Function<int[], E> elementAccessor;
        private final int size;

        private IterationBasedList(final int[] dimensions, @NotNull final Function<int[], E> elementAccessor) {
            this.dimensions = dimensions;
            this.elementAccessor = elementAccessor;
            this.size = (int) MathUtil.product(dimensions);
        }

        @Override
        public E get(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException(index);
            }

            return elementAccessor.apply(MathUtil.arrayIndexToMatrixIndices(index, dimensions));
        }

        @Override
        public int size() {
            return size;
        }

        /**
         * @return an optimized iterator for matrices
         */
        @Override
        public @NotNull Iterator<E> iterator() {
            return new Iterator<>() {
                private final int[] matrixIndices = new int[dimensions.length];

                @Override
                public boolean hasNext() {
                    return !Arrays.equals(matrixIndices, dimensions);
                }

                @Override
                public E next() {
                    E result = elementAccessor.apply(matrixIndices);
                    matrixIndices[matrixIndices.length - 1]++;

                    for (int i = matrixIndices.length - 1; matrixIndices[i] >= dimensions[i]; --i) {
                        matrixIndices[i] = 0;
                        matrixIndices[i - 1]++;
                    }

                    return result;
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof List<?> other))
                return false;

            if (other.size() != size()) {
                return false;
            }

            Iterator<E> e1 = iterator();
            Iterator<?> e2 = other.iterator();

            while (e1.hasNext() && e2.hasNext()) {
                if (!Objects.equals(e1.next(), e2.next()))
                    return false;
            }

            return !(e1.hasNext() || e2.hasNext());
        }
    }

    private static class SubList<E> extends AbstractList<E> {
        private final List<E> root;
        private final int offset;
        protected int size;

        /**
         * Constructs a sublist of an arbitrary AbstractList, which is
         * not a SubList itself.
         */
        public SubList(List<E> root, int fromIndex, int toIndex) {
            this.root = root;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
        }

        /**
         * Constructs a sublist of another SubList.
         */
        protected SubList(SubList<E> parent, int fromIndex, int toIndex) {
            this.root = parent.root;
            this.offset = parent.offset + fromIndex;
            this.size = toIndex - fromIndex;
        }

        public E set(int index, E element) {
            Objects.checkIndex(index, size);
            return root.set(offset + index, element);
        }

        public E get(int index) {
            Objects.checkIndex(index, size);
            return root.get(offset + index);
        }

        public int size() {
            return size;
        }

        public void add(int index, E element) {
            Objects.checkIndex(index, size);
            root.add(offset + index, element);
        }

        public E remove(int index) {
            Objects.checkIndex(index, size);
            return root.remove(offset + index);
        }

        protected void removeRange(int fromIndex, int toIndex) {
            int index = offset + fromIndex;

            for (int i = fromIndex; i < toIndex; ++i) {
                root.remove(index);
            }
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            Objects.checkIndex(index, size);
            int cSize = c.size();

            if (cSize==0)
                return false;

            root.addAll(offset + index, c);
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(int index) {
            Objects.checkIndex(index, size);

            return new ListIterator<E>() {
                private final ListIterator<E> i = root.listIterator(offset + index);

                public boolean hasNext() {
                    return nextIndex() < size;
                }

                public E next() {
                    if (hasNext()) {
                        return i.next();
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public boolean hasPrevious() {
                    return previousIndex() >= 0;
                }

                public E previous() {
                    if (hasPrevious()) {
                        return i.previous();
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public int nextIndex() {
                    return i.nextIndex() - offset;
                }

                public int previousIndex() {
                    return i.previousIndex() - offset;
                }

                public void remove() {
                    i.remove();
                }

                public void set(E e) {
                    i.set(e);
                }

                public void add(E e) {
                    i.add(e);
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new SubList<>(this, fromIndex, toIndex);
        }
    }

    private static class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {

        /**
         * Constructs a sublist of an arbitrary AbstractList, which is
         * not a RandomAccessSubList itself.
         */
        RandomAccessSubList(List<E> root,
                            int fromIndex, int toIndex) {
            super(root, fromIndex, toIndex);
        }

        /**
         * Constructs a sublist of another RandomAccessSubList.
         */
        RandomAccessSubList(RandomAccessSubList<E> parent, int fromIndex, int toIndex) {
            super(parent, fromIndex, toIndex);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            Objects.checkFromToIndex(fromIndex, toIndex, size);
            return new RandomAccessSubList<>(this, fromIndex, toIndex);
        }
    }

    private static class ListItr<E> implements ListIterator<E> {
        final List<E> backReference;

        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor;

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1;

        ListItr(List<E> backReference, int index) {
            this.backReference = backReference;
            this.cursor = index;
        }

        public boolean hasNext() {
            return cursor != backReference.size();
        }

        public E next() {
            try {
                int i = cursor;
                E next = backReference.get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException(e);
            }
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();

            try {
                backReference.remove(lastRet);

                if (lastRet < cursor)
                    cursor--;

                lastRet = -1;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public E previous() {
            try {
                int i = cursor - 1;
                E previous = backReference.get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException(e);
            }
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();

            try {
                backReference.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            try {
                int i = cursor;
                backReference.add(i, e);
                lastRet = -1;
                cursor = i + 1;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public interface IntInt2ObjectFunction<R> {
        R apply(int i, int j);
    }

    public interface IntIntInt2ObjectFunction<R> {
        R apply(int i, int j, int k);
    }
}
