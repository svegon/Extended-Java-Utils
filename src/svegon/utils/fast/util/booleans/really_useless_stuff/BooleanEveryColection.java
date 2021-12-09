package svegon.utils.fast.util.booleans.really_useless_stuff;

import it.unimi.dsi.fastutil.booleans.*;
import svegon.utils.fast.util.booleans.BooleanDeque;
import svegon.utils.fast.util.booleans.BooleanSortedSet;
import svegon.utils.fast.util.booleans.ImprovedBooleanCollection;

import java.util.Spliterator;

import static it.unimi.dsi.fastutil.Size64.sizeOf;

public interface BooleanEveryColection extends ImprovedBooleanCollection, BooleanList, BooleanSortedSet, BooleanDeque {
    @Override
    default boolean add(Boolean k) {
        return BooleanList.super.add(k);
    }

    @Override
    default BooleanSpliterator spliterator() {
        return BooleanSpliterators.asSpliterator(iterator(), sizeOf(this),
                BooleanSpliterators.LIST_SPLITERATOR_CHARACTERISTICS
                        | BooleanSpliterators.SORTED_SET_SPLITERATOR_CHARACTERISTICS
                        | Spliterator.IMMUTABLE | Spliterator.CONCURRENT);
    }

    @Override
    default boolean remove(Object key) {
        return BooleanList.super.remove(key);
    }

    @Override
    default boolean contains(Object key) {
        return BooleanList.super.contains(key);
    }
}
