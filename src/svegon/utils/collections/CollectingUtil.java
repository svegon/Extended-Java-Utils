package svegon.utils.collections;

import svegon.utils.collections.stream.BooleanSummaryStatistics;
import svegon.utils.collections.stream.ByteSummaryStatistics;
import svegon.utils.collections.stream.FloatSummaryStatistics;
import svegon.utils.collections.stream.ShortSummaryStatistics;
import svegon.utils.interfaces.function.Object2ByteFunction;
import svegon.utils.interfaces.function.Object2FloatFunction;
import svegon.utils.interfaces.function.Object2ShortFunction;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
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
}
