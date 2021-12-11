package com.github.svegon.utils.fast.util.doubles;

import com.github.svegon.utils.collections.ArrayUtil;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Immutable
public abstract class ImmutableDoubleCollection implements DoubleCollection {
    private final int hashCode = initHashCode();

    ImmutableDoubleCollection() {
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public ImmutableDoubleCollection clone() throws CloneNotSupportedException {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public final Object @NotNull [] toArray() {
        return toArray(ObjectArrays.EMPTY_ARRAY);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
        if (a.length < size()) {
            a = ArrayUtil.newArray(a, size());
        }

        DoubleIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextDouble();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return doubleParallelStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Double> c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean removeAll(@NotNull Collection<?> c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(@NotNull Collection<?> c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final void clear() {
        if (!isEmpty()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean add(double key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final double key) {
        return doubleParallelStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(double key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final double[] toDoubleArray() {
        return toArray(DoubleArrays.EMPTY_ARRAY);
    }

    @Override
    public double[] toArray(double[] a) {
        if (a.length < size()) {
            a = new double[size()];
        }

        DoubleIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextDouble();
        }

        return a;
    }

    @Override
    public final boolean addAll(DoubleCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(DoubleCollection c) {
        return doubleParallelStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(DoubleCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(DoubleCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    protected abstract int initHashCode();
}
