package com.github.svegon.utils.collections.collecting;

import com.github.svegon.utils.fast.util.floats.FloatSummaryStatistics;
import com.github.svegon.utils.fast.util.shorts.ShortSummaryStatistics;
import com.github.svegon.utils.interfaces.function.Object2ByteFunction;
import com.github.svegon.utils.interfaces.function.Object2FloatFunction;
import com.github.svegon.utils.interfaces.function.Object2ShortFunction;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharLists;
import com.github.svegon.utils.fast.util.booleans.BooleanSummaryStatistics;
import com.github.svegon.utils.fast.util.bytes.ByteSummaryStatistics;
import com.github.svegon.utils.fast.util.chars.ImmutableCharList;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class CollectingUtil {
    private CollectingUtil() {
        throw new AssertionError();
    }

    public static final Set<Collector.Characteristics> CH_CONCURRENT_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
            Collector.Characteristics.UNORDERED,
            Collector.Characteristics.IDENTITY_FINISH));
    public static final Set<Collector.Characteristics> CH_CONCURRENT_NOID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
            Collector.Characteristics.UNORDERED));
    public static final Set<Collector.Characteristics> CH_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    public static final Set<Collector.Characteristics> CH_UNORDERED_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
            Collector.Characteristics.IDENTITY_FINISH));
    public static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();
    public static final Set<Collector.Characteristics> CH_UNORDERED_NOID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED));

    private static final BinaryOperator<Collection<Object>> COLLECTION_COMBINER =
            (c1, c2) -> {c1.addAll(c2); return c1;};
    private static final CharCollector<CharList, CharList> IMMUTABLE_CHAR_LIST_COLLECTOR
            = CharCollector.of(CharArrayList::new, CharList::add, collectionCombiner(), ImmutableCharList::copyOf);

    /**
     * Returns a {@code Collector} which applies an {@code short}-producing
     * mapping function to each input element, and returns summary statistics
     * for the resulting values.
     *
     * @param <T> the type of the input elements
     * @param mapper the mapping function to apply to each element
     * @return a {@code Collector} implementing the summary-statistics reduction
     */
    public static <T> Collector<T, ?, BooleanSummaryStatistics> summarizingBoolean(final Predicate<T> mapper) {
        return Collector.of(BooleanSummaryStatistics::new, (r, t) -> r.accept(mapper.test(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID.toArray(Collector.Characteristics[]::new));
    }

    /**
     * Returns a {@code Collector} which applies an {@code short}-producing
     * mapping function to each input element, and returns summary statistics
     * for the resulting values.
     *
     * @param <T> the type of the input elements
     * @param mapper the mapping function to apply to each element
     * @return a {@code Collector} implementing the summary-statistics reduction
     */
    public static <T> Collector<T, ?, ByteSummaryStatistics> summarizingByte(final Object2ByteFunction<T> mapper) {
        return Collector.of(ByteSummaryStatistics::new, (r, t) -> r.accept(mapper.applyToByte(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID.toArray(Collector.Characteristics[]::new));
    }

    /**
     * Returns a {@code Collector} which applies an {@code short}-producing
     * mapping function to each input element, and returns summary statistics
     * for the resulting values.
     *
     * @param <T> the type of the input elements
     * @param mapper the mapping function to apply to each element
     * @return a {@code Collector} implementing the summary-statistics reduction
     */
    public static <T> Collector<T, ?, ShortSummaryStatistics> summarizingShort(final Object2ShortFunction<T> mapper) {
        return Collector.of(ShortSummaryStatistics::new, (r, t) -> r.accept(mapper.applyToShort(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID.toArray(Collector.Characteristics[]::new));
    }

    /**
     * Returns a {@code Collector} which applies an {@code float}-producing
     * mapping function to each input element, and returns summary statistics
     * for the resulting values.
     *
     * @param <T> the type of the input elements
     * @param mapper the mapping function to apply to each element
     * @return a {@code Collector} implementing the summary-statistics reduction
     */
    public static <T> Collector<T, ?, FloatSummaryStatistics> summarizingFloat(final Object2FloatFunction<T> mapper) {
        return Collector.of(FloatSummaryStatistics::new, (r, t) -> r.accept(mapper.applyToFloat(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID.toArray(Collector.Characteristics[]::new));
    }

    @SuppressWarnings("unchecked")
    public static <E, C extends Collection<E>> BinaryOperator<C> collectionCombiner() {
        return (BinaryOperator<C>) COLLECTION_COMBINER;
    }

    public static <A extends CharList> CharCollector<A, CharList> toUnmodifiableCharList(Supplier<A> supplier) {
        return CharCollector.of(supplier, CharList::add, collectionCombiner(), CharLists::unmodifiable);
    }

    public static CharCollector<CharList, CharList> toImmutableCharList() {
        return IMMUTABLE_CHAR_LIST_COLLECTOR;
    }
}
