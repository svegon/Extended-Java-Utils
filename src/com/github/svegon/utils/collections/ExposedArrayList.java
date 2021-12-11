package com.github.svegon.utils.collections;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * a version of {@code java.util.Vector} which passes a reference of its array instead of copying where possible
 * @param <E>
 */
public class ExposedArrayList<E> extends AbstractList<E> implements RandomAccess, Cloneable {
    private Object[] a;

    public ExposedArrayList(Object[] array) {
        setArray(array);
    }

    public ExposedArrayList() {
        a = ObjectArrays.EMPTY_ARRAY;
    }

    public ExposedArrayList(Collection<? extends E> c) {
        this(c.toArray());
    }

    @Override
    public synchronized boolean add(E e) {
        a = ArrayUtil.merge(a, e);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) a[index];
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public synchronized boolean contains(Object o) {
        return Arrays.asList(a).contains(o);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return removeIf(c::contains);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean removeIf(final Predicate<? super E> filter) {
        return !Arrays.equals(a, a = ArrayUtil.filter(a, (o) -> filter.test((E) o)));
    }

    @Override
    public boolean retainAll(final @NotNull Collection<?> c) {
        return removeIf((e) -> !c.contains(e));
    }

    @Override
    public synchronized E set(int index, E element) {
        E e = get(index);
        a[index] = element;
        return e;
    }

    @Override
    public synchronized void add(int index, E element) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException(index);
        }

        Object[] data = a;
        data = Arrays.copyOf(data, data.length + 1);
        System.arraycopy(data, index, data, index + 1, data.length - index - 1);
        data[index] = element;
        a = data;
    }

    @Override
    public E remove(int index) {
        E e = get(index);
        removeRange(index, index + 1);
        return e;
    }

    @Override
    public synchronized boolean remove(Object o) {
        Object[] data = a;
        int i = Arrays.asList(data).indexOf(o);

        if (i < 0) {
            return false;
        }

        a = ArrayUtil.pop(data, i);
        return true;
    }

    @Override
    public void clear() {
        a = ObjectArrays.EMPTY_ARRAY;
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }

        a = Arrays.copyOf(a, a.length + c.size());
        System.arraycopy(a, index, a, index + c.size(), a.length - index - c.size());

        for (E element : c) {
            a[index++] = element;
        }

        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof List<?> list)) {
            return false;
        }

        if (o instanceof ExposedArrayList<?> that) {
            return Arrays.equals(a, that.a);
        }

        if (size() != list.size()) {
            return false;
        }

        ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();

        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();

            if (!(Objects.equals(o1, o2)))
                return false;
        }

        return !(e1.hasNext() || e2.hasNext());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(a);
    }

    @Override
    protected synchronized void removeRange(int fromIndex, int toIndex) {
        Object[] data = a;
        System.arraycopy(data, toIndex, data, fromIndex, toIndex - fromIndex);
        a = Arrays.copyOf(data, data.length + fromIndex - toIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExposedArrayList<E> clone() throws CloneNotSupportedException {
        ExposedArrayList<E> clone = (ExposedArrayList<E>) super.clone();
        clone.a = a.clone();

        return clone;
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return a;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        Object[] data = this.a;

        if (a.length < data.length) {
            if (a.getClass() == Object[].class) {
                return (T[]) toArray();
            }

            return (T[]) Arrays.copyOf(data, data.length, a.getClass());
        }

        System.arraycopy(data, 0, a, 0, data.length);
        return a;
    }

    public final Object[] getArray() {
        return a;
    }

    public final void setArray(Object[] array) {
        a = Preconditions.checkNotNull(array);
    }
}
