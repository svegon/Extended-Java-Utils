package svegon.utils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface CombinedCollection<E> extends Collection<E> {
    @NotNull Collection<Collection<E>> getCache();
}
