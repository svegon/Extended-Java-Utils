package com.github.svegon.utils.fast.util.longs;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import com.github.svegon.utils.collections.ArrayUtil;

import java.util.Collection;

@Immutable
public abstract class ImmutableLongCollection implements LongCollection {
    ImmutableLongCollection() {
    }

    @Override
    public abstract int hashCode();

    @Override
    public ImmutableLongCollection clone() throws CloneNotSupportedException {
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

        LongIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextLong();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return longParallelStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Long> c) {
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
    public final boolean add(long key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final long key) {
        return longParallelStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(long key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final long[] toLongArray() {
        return toArray(LongArrays.EMPTY_ARRAY);
    }

    @Override
    public long[] toArray(long[] a) {
        if (a.length < size()) {
            a = new long[size()];
        }

        LongIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextLong();
        }

        return a;
    }

    @Override
    public final boolean addAll(LongCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(LongCollection c) {
        return longParallelStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(LongCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(LongCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }
}
