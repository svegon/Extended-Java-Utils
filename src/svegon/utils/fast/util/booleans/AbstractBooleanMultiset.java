package svegon.utils.fast.util.booleans;

import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.booleans.*;
import org.jetbrains.annotations.NotNull;
import svegon.utils.collections.AbstractMultiset;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractBooleanMultiset extends AbstractMultiset<Boolean> implements BooleanMultiset {
    @SuppressWarnings("unchecked")
    private final Set<BooleanMultiset.Entry> entrySet = (Set<BooleanMultiset.Entry>) (Object) entrySet();

    @Override
    public boolean add(boolean key) {
        return add(key, 1) > 0;
    }

    @Override
    public boolean contains(final boolean key) {
        return parallelBooleanStream().anyMatch((bl) -> bl == key);
    }

    @Override
    public boolean rem(boolean key) {
        return remove(key, 1) > 0;
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
    public int add(boolean aBoolean, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(boolean bl, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull BooleanCollection c) {
        BooleanIterator it = c.iterator();
        boolean modified = false;

        while (it.hasNext()) {
            modified |= add(it.nextBoolean());
        }

        return modified;
    }

    @Override
    public boolean containsAll(final @NotNull BooleanCollection c) {
        return parallelBooleanStream().allMatch(c::contains);
    }

    @Override
    public int setCount(boolean aBoolean, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<BooleanMultiset.Entry> booleanEntrySet() {
        return entrySet;
    }

    @Override
    public BooleanSet elementSet() {
        return (BooleanSet) super.elementSet();
    }

    protected abstract Set<BooleanMultiset.Entry> initBooleanEntrySet();

    @Override
    @SuppressWarnings("unchecked")
    protected final Set<Multiset.Entry<Boolean>> initEntrySet() {
        return (Set<Multiset.Entry<Boolean>>) (Object) initBooleanEntrySet();
    }

    @Override
    protected BooleanSet initElementSet() {
        return new AbstractBooleanSet() {
            private final Set<BooleanMultiset.Entry> es = booleanEntrySet();

            @Override
            public BooleanIterator iterator() {
                return new BooleanIterator() {
                    private final Iterator<BooleanMultiset.Entry> it = es.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public boolean nextBoolean() {
                        return it.next().getBoolean();
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public boolean add(boolean bl) {
                return AbstractBooleanMultiset.this.add(bl);
            }

            @Override
            public boolean rem(boolean o) {
                try {
                    return setCount(o, 0) != 0;
                } catch (ClassCastException ignore) {
                    return false;
                }
            }

            @Override
            public boolean addAll(@NotNull Collection<? extends Boolean> c) {
                return AbstractBooleanMultiset.this.addAll(c);
            }

            @Override
            public boolean retainAll(@NotNull BooleanCollection c) {
                return AbstractBooleanMultiset.this.retainAll(c);
            }

            @Override
            public boolean removeAll(@NotNull BooleanCollection c) {
                return AbstractBooleanMultiset.this.removeAll(c);
            }

            @Override
            public void clear() {
                AbstractBooleanMultiset.this.clear();
            }

            @Override
            public int size() {
                return es.size();
            }

            @Override
            public boolean isEmpty() {
                return AbstractBooleanMultiset.this.isEmpty();
            }

            @Override
            public boolean contains(boolean o) {
                return AbstractBooleanMultiset.this.contains(o);
            }
        };
    }
}
