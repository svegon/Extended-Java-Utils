package com.github.svegon.utils.fast.util.shorts;

import com.github.svegon.utils.collections.ArrayUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Immutable
public abstract class ImmutableShortCollection implements ImprovedShortCollection {
    private final int hashCode = initHashCode();

    ImmutableShortCollection() {

    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public ImmutableShortCollection clone() throws CloneNotSupportedException {
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

        ShortIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextShort();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return parallelShortStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Short> c) {
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
    public final boolean add(short key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final short key) {
        return parallelShortStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(short key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final short[] toShortArray() {
        return toArray(ShortArrays.EMPTY_ARRAY);
    }

    @Override
    public short[] toArray(short[] a) {
        if (a.length < size()) {
            a = new short[size()];
        }

        ShortIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextShort();
        }

        return a;
    }

    @Override
    public final boolean addAll(ShortCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(ShortCollection c) {
        return parallelShortStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(ShortCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(ShortCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    protected abstract int initHashCode();
}
