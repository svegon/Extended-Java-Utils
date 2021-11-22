package svegon.utils.iteration;

import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;

public final class IterationUtil {
    private IterationUtil() {
        throw new AssertionError();
    }

    public static <E> Iterable<E> suppressComodCheck(final @NotNull List<E> of) {
        return () -> simpleListIterator(of);
    }

    public static <E> ListIterator<E> simpleListIterator(@NotNull List<E> of) {
        return simpleListIterator(of, 0);
    }

    public static <E> ListIterator<E> simpleListIterator(final @NotNull List<E> of, final int startingIndex) {
        return new ListIterator<>() {
            private int index = startingIndex;

            @Override
            public boolean hasNext() {
                return index < of.size();
            }

            @Override
            public E next() {
                try {
                    return of.get(index++);
                } catch (IndexOutOfBoundsException e) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public boolean hasPrevious() {
                return index > 0;
            }

            @Override
            public E previous() {
                try {
                    index--;
                    return of.get(index);
                } catch (IndexOutOfBoundsException e) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public int nextIndex() {
                return index;
            }

            @Override
            public int previousIndex() {
                return index - 1;
            }

            @Override
            public void remove() {
                index--;
                of.remove(index);
            }

            @Override
            public void set(E e) {
                of.set(previousIndex(), e);
            }

            @Override
            public void add(E e) {
                of.add(nextIndex(), e);
            }
        };
    }
}
