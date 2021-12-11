package com.github.svegon.utils.fast.util.floats;

import com.github.svegon.utils.collections.ArrayUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Immutable
public abstract class ImmutableFloatCollection implements ImprovedFloatCollection {
    private final int hashCode = initHashCode();

    ImmutableFloatCollection() {

    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public ImmutableFloatCollection clone() throws CloneNotSupportedException {
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

        FloatIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextFloat();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return parallelFloatStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Float> c) {
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
    public final boolean add(float key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final float key) {
        return parallelFloatStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(float key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final float[] toFloatArray() {
        return toArray(FloatArrays.EMPTY_ARRAY);
    }

    @Override
    public float[] toArray(float[] a) {
        if (a.length < size()) {
            a = new float[size()];
        }

        FloatIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextFloat();
        }

        return a;
    }

    @Override
    public final boolean addAll(FloatCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(FloatCollection c) {
        return parallelFloatStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(FloatCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(FloatCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    protected abstract int initHashCode();
}
