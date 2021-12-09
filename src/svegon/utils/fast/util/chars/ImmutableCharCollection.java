package svegon.utils.fast.util.chars;

import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import svegon.utils.collections.ArrayUtil;

import java.util.Collection;

@Immutable
public abstract class ImmutableCharCollection implements ImprovedCharCollection {
    private final int hashCode = initHashCode();

    ImmutableCharCollection() {

    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public ImmutableCharCollection clone() throws CloneNotSupportedException {
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

        CharIterator it = iterator();
        int length = size();

        for (int i = 0; i < length; i++) {
            a[i] = (T) (Object) it.nextChar();
        }

        return a;
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        return parallelCharStream().allMatch(c::contains);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends Character> c) {
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
    public final boolean add(char key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final char key) {
        return parallelCharStream().anyMatch(bl -> bl == key);
    }

    @Override
    public final boolean rem(char key) {
        if (contains(key)) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final char[] toCharArray() {
        return toArray(CharArrays.EMPTY_ARRAY);
    }

    @Override
    public char[] toArray(char[] a) {
        if (a.length < size()) {
            a = new char[size()];
        }

        CharIterator it = iterator();

        for (int i = 0; i < size(); i++) {
            a[i] = it.nextChar();
        }

        return a;
    }

    @Override
    public final boolean addAll(CharCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(CharCollection c) {
        return parallelCharStream().allMatch(c::contains);
    }

    @Override
    public final boolean removeAll(CharCollection c) {
        if (!c.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        return false;
    }

    @Override
    public final boolean retainAll(CharCollection c) {
        if (c.isEmpty()) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

    protected abstract int initHashCode();
}
