package com.github.svegon.utils.fast.util.booleans.immutable;

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import com.github.svegon.utils.collections.ArrayUtil;
import com.github.svegon.utils.fast.util.booleans.ImprovedBooleanCollection;

import java.util.Collection;

@Immutable
public abstract class ImmutableBooleanCollection implements ImprovedBooleanCollection {
    ImmutableBooleanCollection() {

    }

    @Override
    public abstract int hashCode();

    @Override
    public ImmutableBooleanCollection clone() throws CloneNotSupportedException {
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

        BooleanIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextBoolean();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return parallelBooleanStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Boolean> c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
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
    public final boolean add(boolean key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final boolean key) {
        return parallelBooleanStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(boolean key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean[] toBooleanArray() {
        return toArray(BooleanArrays.EMPTY_ARRAY);
    }

    @Override
    public boolean[] toArray(boolean[] a) {
        if (a.length < size()) {
            a = new boolean[size()];
        }

        BooleanIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextBoolean();
        }

        return a;
    }

    @Override
    public final boolean addAll(BooleanCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean containsAll(BooleanCollection c) {
        return parallelBooleanStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(BooleanCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(BooleanCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }
}
