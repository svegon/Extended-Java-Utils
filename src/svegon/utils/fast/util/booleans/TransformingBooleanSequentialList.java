package svegon.utils.fast.util.booleans;

import it.unimi.dsi.fastutil.booleans.AbstractBooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class TransformingBooleanSequentialList<E, L extends List<E>> extends AbstractBooleanList {
    protected final L list;

    protected TransformingBooleanSequentialList(L list) {
        this.list = list;
    }

    @Override
    public final void clear() {
        list.clear();
    }

    @Override
    public final boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public final boolean getBoolean(int index) {
        return listIterator(index).nextBoolean();
    }

    @Override
    public final @NotNull BooleanListIterator iterator() {
        return listIterator();
    }

    @Override
    public final int size() {
        return list.size();
    }

    @Override
    public abstract BooleanListIterator listIterator(int index);

    @Override
    public abstract boolean removeIf(final BooleanPredicate filter);

    @Override
    public abstract boolean removeBoolean(int index);
}
