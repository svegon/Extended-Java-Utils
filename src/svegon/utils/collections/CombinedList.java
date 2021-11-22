package svegon.utils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface CombinedList<E> extends List<E> {
    @NotNull Collection<? extends List<E>> getCache();
}
