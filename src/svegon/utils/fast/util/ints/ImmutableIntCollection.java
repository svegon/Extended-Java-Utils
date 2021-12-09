package svegon.utils.fast.util.ints;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import svegon.utils.collections.ArrayUtil;

import java.util.Collection;

@Immutable
public abstract class ImmutableIntCollection implements IntCollection {
    private final int hashCode = initHashCode();

    ImmutableIntCollection() {
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public ImmutableIntCollection clone() throws CloneNotSupportedException {
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

        IntIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextInt();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return intParallelStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Integer> c) {
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
    public final boolean add(int key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final int key) {
        return intParallelStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(int key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final int[] toIntArray() {
        return toArray(IntArrays.EMPTY_ARRAY);
    }

    @Override
    public int[] toArray(int[] a) {
        if (a.length < size()) {
            a = new int[size()];
        }

        IntIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextInt();
        }

        return a;
    }

    @Override
    public final boolean addAll(IntCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(IntCollection c) {
        return intParallelStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(IntCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(IntCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    protected abstract int initHashCode();
}
