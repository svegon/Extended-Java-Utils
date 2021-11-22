package svegon.utils.collections;

import com.google.common.collect.ObjectArrays;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Immutable
public abstract class ImmutableEnumSet<E extends Enum<E>> extends AbstractSet<E> {
    protected final Class<E> enumClass;

    ImmutableEnumSet(@NotNull final Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> Set<E> of(final @NotNull Collection<E> elements) {
        if (elements.isEmpty()) {
            return Collections.emptySet();
        }

        Class<E> enumClass;

        try {
            enumClass = (Class<E>) elements.iterator().next().getClass();
        } catch (NoSuchElementException e) {
            return Collections.emptySet();
        }

        boolean[] flags = new boolean[enumClass.getEnumConstants().length];

        for (E element : elements) {
            flags[element.ordinal()] = true;
        }

        return new RegularImmutableEnumSet<>(enumClass, flags);
    }

    public static <E extends Enum<E>> Set<E> of(E @NotNull ... elements) {
        return of(Arrays.asList(elements));
    }

    private static class RegularImmutableEnumSet<E extends Enum<E>> extends ImmutableEnumSet<E> {
        private final boolean[] flags;
        private final Collection<E> iterable;

        RegularImmutableEnumSet(final @NotNull Class<E> enumClass, final boolean @NotNull [] flags) {
            super(enumClass);

            this.flags = flags;
            this.iterable = Arrays.asList(Arrays.stream(enumClass.getEnumConstants()).filter((e) -> flags[e.ordinal()])
                    .toArray((i) -> ObjectArrays.newArray(enumClass, i)));
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            return iterable.iterator();
        }

        @Override
        public int size() {
            return iterable.size();
        }

        @Override
        public boolean contains(Object o) {
            return enumClass.isInstance(o) && flags[((E) o).ordinal()];
        }
    }
}
