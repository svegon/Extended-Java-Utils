package com.github.svegon.utils.fast.util.bytes.immutable;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import com.github.svegon.utils.collections.ArrayUtil;
import com.github.svegon.utils.fast.util.bytes.ImprovedByteCollection;

import java.util.Collection;

@Immutable
public abstract class ImmutableByteCollection implements ImprovedByteCollection {
    ImmutableByteCollection() {

    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public final ImmutableByteCollection clone() throws CloneNotSupportedException {
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

        ByteIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextByte();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return parallelByteStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Byte> c) {
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
    public final boolean add(byte key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final byte key) {
        return parallelByteStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(byte key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final byte[] toByteArray() {
        return toArray(ByteArrays.EMPTY_ARRAY);
    }

    @Override
    public byte[] toArray(byte[] a) {
        if (a.length < size()) {
            a = new byte[size()];
        }

        ByteIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextByte();
        }

        return a;
    }

    @Override
    public final boolean addAll(ByteCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(ByteCollection c) {
        return parallelByteStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(ByteCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(ByteCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }
}
