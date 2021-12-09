package svegon.utils.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMultiset<E> extends AbstractCollection<E> implements Multiset<E> {
    private final Set<Entry<E>> entrySet = initEntrySet();
    private final Set<E> elementSet = initElementSet();

    @Override
    public int count(@Nullable Object o) {
        int c = 0;
        Iterator<E> it = iterator();

        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    c++;
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    c++;
        }

        return c;
    }

    @Override
    public int add(@Nullable E e, int i) {
        Preconditions.checkArgument(i >= 0);
        int c = count(e);

        for (int j = 0; j < i; j++) {
            add(e);
        }

        return c;
    }

    @Override
    public int remove(@Nullable Object o, int i) {
        Preconditions.checkArgument(i >= 0);
        int c = count(o);

        for (int j = 0; j < i; j++) {
            remove(o);
        }

        return c;
    }

    @Override
    public int setCount(E e, int i) {
        Preconditions.checkArgument(i >= 0);
        int c = count(e) - i;

        if (c < 0) {
            return remove(e, -c);
        } else {
            return add(e, c);
        }
    }

    @Override
    public boolean setCount(E e, int i, int i1) {
        Preconditions.checkArgument(i >= 0 && i1 >= 0);

        if (i == i1 || count(e) != i) {
            return false;
        }

        setCount(e, i1);
        return true;
    }

    @Override
    public Set<Entry<E>> entrySet() {
        return entrySet;
    }

    @Override
    public Set<E> elementSet() {
        return elementSet;
    }

    protected abstract Set<Entry<E>> initEntrySet();

    protected Set<E> initElementSet() {
        return new AbstractSet<>() {
            private final Set<Entry<E>> es = entrySet();

            @Override
            public Iterator<E> iterator() {
                return new Iterator<>() {
                    private final Iterator<Entry<E>> it = es.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public E next() {
                        return it.next().getElement();
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public boolean add(E e) {
                return AbstractMultiset.this.add(e);
            }

            @Override
            public boolean remove(Object o) {
                try {
                    return setCount((E) o, 0) != 0;
                } catch (ClassCastException ignore) {
                    return false;
                }
            }

            @Override
            public boolean addAll(@NotNull Collection<? extends E> c) {
                return AbstractMultiset.this.addAll(c);
            }

            @Override
            public boolean retainAll(@NotNull Collection<?> c) {
                return AbstractMultiset.this.retainAll(c);
            }

            @Override
            public boolean removeAll(@NotNull Collection<?> c) {
                return AbstractMultiset.this.removeAll(c);
            }

            @Override
            public void clear() {
                AbstractMultiset.this.clear();
            }

            @Override
            public int size() {
                return es.size();
            }

            @Override
            public boolean isEmpty() {
                return AbstractMultiset.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return AbstractMultiset.this.contains(o);
            }
        };
    }
}
