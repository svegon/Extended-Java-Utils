package svegon.utils.collections.stream;

import svegon.utils.fuck_modifiers.AbstractPipeline;
import svegon.utils.fuck_modifiers.DoublePipeline;
import svegon.utils.fuck_modifiers.FindOps;
import svegon.utils.fuck_modifiers.ForEachOps;
import svegon.utils.fuck_modifiers.IntPipeline;
import svegon.utils.fuck_modifiers.LongPipeline;
import svegon.utils.fuck_modifiers.MatchOps;
import svegon.utils.fuck_modifiers.Node;
import svegon.utils.fuck_modifiers.Nodes;
import svegon.utils.fuck_modifiers.PipelineHelper;
import svegon.utils.fuck_modifiers.ReduceOps;
import svegon.utils.fuck_modifiers.ReferencePipeline;
import svegon.utils.fuck_modifiers.Sink;
import svegon.utils.fuck_modifiers.SliceOps;
import svegon.utils.fuck_modifiers.SortedOps;
import svegon.utils.fuck_modifiers.StreamShape;
import svegon.utils.fuck_modifiers.StreamSpliterators;
import svegon.utils.fuck_modifiers.WhileOps;
import svegon.utils.interfaces.function.ObjectFloatConsumer;
import svegon.utils.math.MathUtil;
import svegon.utils.optional.OptionalFloat;
import it.unimi.dsi.fastutil.floats.*;
import svegon.utils.fuck_modifiers.StreamOpFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.*;

/**
 * Abstract base class for an intermediate pipeline stage or pipeline source
 * stage implementing whose elements are of type {@code Float}.
 *
 * @param <E_IN> type of elements in the upstream source
 *
 * @since 1.0.0
 */
public abstract class FloatPipeline<E_IN> extends AbstractPipeline<E_IN, Float, FloatStream> implements FloatStream {

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Supplier<Spliterator>} describing the stream source
     * @param sourceFlags the source flags for the stream source, described in
     * {@link StreamOpFlag}
     */
    public FloatPipeline(Supplier<? extends Spliterator<Float>> source,
                  int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Spliterator} describing the stream source
     * @param sourceFlags the source flags for the stream source, described in
     * {@link StreamOpFlag}
     */
    public FloatPipeline(Spliterator<Float> source,
                  int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for appending an intermediate operation onto an existing
     * pipeline.
     *
     * @param upstream the upstream element source.
     * @param opFlags the operation flags
     */
    public FloatPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * Adapt a {@code Sink<Float> to a {@code FloatConsumer}, ideally simply
     * by casting.
     */
    private static FloatConsumer adapt(Sink<Float> sink) {
        if (sink instanceof  FloatConsumer) {
            return ( FloatConsumer) sink;
        } else {
            return sink::accept;
        }
    }

    /**
     * Adapt a {@code Spliterator<Float>} to a {@code Spliterator.OfFloat}.
     *
     * @implNote
     * The implementation attempts to cast to a Spliterator.OfFloat, and throws
     * an exception if this cast is not possible.
     */
    private static FloatSpliterator adapt(Spliterator<Float> s) {
        if (s instanceof FloatSpliterator) {
            return (FloatSpliterator) s;
        } else {
            throw new UnsupportedOperationException("FloatStream.adapt(Spliterator<Float> s)");
        }
    }


    // Shape-specific methods

    @Override
    public final StreamShape getOutputShape() {
        return StreamShape.FLOAT_VALUE;
    }

    @Override
    public final <P_IN> Node<Float> evaluateToNode(PipelineHelper<Float> helper,
                                                   Spliterator<P_IN> spliterator,
                                                   boolean flattenTree,
                                                   IntFunction<Float[]> generator) {
        return Nodes.collectFloat(helper, spliterator, flattenTree);
    }

    @Override
    public final <P_IN> Spliterator<Float> wrap(PipelineHelper<Float> ph, Supplier<Spliterator<P_IN>> supplier,
                                                boolean isParallel) {
        return new StreamSpliterators.FloatWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final FloatSpliterator lazySpliterator(Supplier<? extends Spliterator<Float>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfFloat((Supplier<FloatSpliterator>) supplier);
    }

    @Override
    public final boolean forEachWithCancel(Spliterator<Float> spliterator, Sink<Float> sink) {
        FloatSpliterator spl = adapt(spliterator);
        FloatConsumer adaptedSink = adapt(sink);
        boolean cancelled;
        while (!(cancelled = sink.cancellationRequested()) && spl.tryAdvance(adaptedSink));
        return cancelled;
    }

    @Override
    public final Node.Builder<Float> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Float[]> generator) {
        return Nodes.floatBuilder(exactSizeIfKnown);
    }

    private <U> Stream<U> mapToObj(Float2ObjectFunction<? extends U> mapper, int opFlags) {
        return new ReferencePipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE, opFlags) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedFloat<U>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    // FloatStream

    @Override
    public final @NotNull FloatIterator iterator() {
        return FloatSpliterators.asIterator(spliterator());
    }

    @Override
    public final @NotNull FloatSpliterator spliterator() {
        return adapt(super.spliterator());
    }

    // Stateless intermediate ops from FloatStream

    @Override
    public final Stream<Float> boxed() {
        return mapToObj(Float::valueOf, 0);
    }

    @Override
    public final FloatStream map(FloatUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                                       StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(Float2ObjectFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapToObj(mapper, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    @Override
    public final BooleanStream mapToBoolean(FloatPredicate mapper) {
        Objects.requireNonNull(mapper);
        return new BooleanPipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.test(t));
                    }
                };
            }
        };
    }

    @Override
    public final ByteStream mapToByte(Float2ByteFunction mapper) {
        Objects.requireNonNull(mapper);
        return new BytePipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final ShortStream mapToShort(Float2ShortFunction mapper) {
        Objects.requireNonNull(mapper);
        return new ShortPipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(Float2IntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<Float>(this, StreamShape.FLOAT_VALUE,
                                                   StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(Float2LongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final CharStream mapToChar(Float2CharFunction mapper) {
        Objects.requireNonNull(mapper);
        return new CharPipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(Float2DoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final FloatStream flatMap(Float2ObjectFunction<? extends FloatStream> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.FLOAT_VALUE, StreamOpFlag.NOT_SORTED
                | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    FloatConsumer downstreamAsFloat = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(float t) {
                        try (FloatStream result = mapper.apply(t)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsFloat);
                                } else {
                                    var s = result.sequential().spliterator();
                                    while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsFloat)) ;
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        // If this method is called then an operation within the stream
                        // pipeline is short-circuiting (see AbstractPipeline.copyInto).
                        // Note that we cannot differentiate between an upstream or
                        // downstream operation
                        cancellationRequestedCalled = true;
                        return downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    @Override
    public FloatStream unordered() {
        if (!isOrdered())
            return this;
        return new StatelessOp<>(this, StreamShape.FLOAT_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return sink;
            }
        };
    }

    @Override
    public final FloatStream filter(FloatPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                                       StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(float t) {
                        if (predicate.test(t))
                            downstream.accept(t);
                    }
                };
            }
        };
    }

    @Override
    public final FloatStream peek(FloatConsumer action) {
        Objects.requireNonNull(action);
        return new StatelessOp<>(this, StreamShape.FLOAT_VALUE,
                                       0) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<>(sink) {
                    @Override
                    public void accept(float t) {
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

    // Stateful intermediate ops from FloatStream

    @Override
    public final FloatStream limit(long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }

        return SliceOps.makeFloat(this, 0, maxSize);
    }

    @Override
    public final FloatStream skip(long n) {
        if (n < 0)
            throw new IllegalArgumentException(Long.toString(n));
        if (n == 0)
            return this;
        else {
            long limit = -1;
            return SliceOps.makeFloat(this, n, limit);
        }
    }

    @Override
    public final FloatStream takeWhile(FloatPredicate predicate) {
        return WhileOps.makeTakeWhileFloat(this, predicate);
    }

    @Override
    public final FloatStream dropWhile(FloatPredicate predicate) {
        return WhileOps.makeDropWhileFloat(this, predicate);
    }

    @Override
    public final FloatStream sorted() {
        return SortedOps.makeFloat(this);
    }

    @Override
    public final FloatStream distinct() {
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires a Float-specific map/set implementation.
        return StreamUtil.mapToFloat(boxed().distinct(), i -> (float) i);
    }

    // Terminal ops from FloatStream

    @Override
    public void forEach(FloatConsumer consumer) {
        evaluate(ForEachOps.makeFloat(consumer, false));
    }

    @Override
    public void forEachOrdered(FloatConsumer consumer) {
        evaluate(ForEachOps.makeFloat(consumer, true));
    }

    @Override
    public final double sum() {
        /*
         * In the arrays allocated for the collect operation, index 0
         * holds the high-order bits of the running sum, index 1 holds
         * the low-order bits of the sum computed via compensated
         * summation, and index 2 holds the simple sum used to compute
         * the proper result if the stream contains infinite values of
         * the same sign.
         */
        double[] summation = collect(() -> new double[3],
                               (ll, d) -> {
                                   MathUtil.sumWithCompensation(ll, d);
                                   ll[2] += d;
                               },
                               (ll, rr) -> {
                                   MathUtil.sumWithCompensation(ll, rr[0]);
                                   MathUtil.sumWithCompensation(ll, rr[1]);
                                   ll[2] += rr[2];
                               });

        return MathUtil.computeFinalSum(summation);
    }

    @Override
    public final OptionalFloat min() {
        return reduce(Math::min);
    }

    @Override
    public final OptionalFloat max() {
        return reduce(Math::max);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The {@code Float} format can represent all
     * consecutive integers in the range -2<sup>53</sup> to
     * 2<sup>53</sup>. If the pipeline has more than 2<sup>53</sup>
     * values, the divisor in the average computation will saturate at
     * 2<sup>53</sup>, leading to additional numerical errors.
     */
    @Override
    public final OptionalDouble average() {
        /*
         * In the arrays allocated for the collect operation, index 0
         * holds the high-order bits of the running sum, index 1 holds
         * the low-order bits of the sum computed via compensated
         * summation, index 2 holds the number of values seen, index 3
         * holds the simple sum.
         */
        double[] avg = collect(() -> new double[4],
                               (ll, d) -> {
                                   ll[2]++;
                                   MathUtil.sumWithCompensation(ll, d);
                                   ll[3] += d;
                               },
                               (ll, rr) -> {
                                   MathUtil.sumWithCompensation(ll, rr[0]);
                                   MathUtil.sumWithCompensation(ll, rr[1]);
                                   ll[2] += rr[2];
                                   ll[3] += rr[3];
                               });
        return avg[2] > 0 ? OptionalDouble.of(MathUtil.computeFinalSum(avg) / avg[2]) : OptionalDouble.empty();
    }

    @Override
    public final long count() {
        return evaluate(ReduceOps.makeFloatCounting());
    }

    @Override
    public final FloatSummaryStatistics summaryStatistics() {
        return collect(FloatSummaryStatistics::new, FloatSummaryStatistics::accept,
                       FloatSummaryStatistics::combine);
    }

    @Override
    public final float reduce(float identity, FloatBinaryOperator op) {
        return evaluate(ReduceOps.makeFloat(identity, op));
    }

    @Override
    public final OptionalFloat reduce(FloatBinaryOperator op) {
        return evaluate(ReduceOps.makeFloat(op));
    }

    @Override
    public final <R> R collect(Supplier<R> supplier, ObjectFloatConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);
        BinaryOperator<R> operator = (left, right) -> {
            combiner.accept(left, right);
            return left;
        };
        return evaluate(ReduceOps.makeFloat(supplier, accumulator, operator));
    }

    @Override
    public final boolean anyMatch(FloatPredicate predicate) {
        return evaluate(MatchOps.makeFloat(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(FloatPredicate predicate) {
        return evaluate(MatchOps.makeFloat(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(FloatPredicate predicate) {
        return evaluate(MatchOps.makeFloat(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalFloat findFirst() {
        return evaluate(FindOps.makeFloat(true));
    }

    @Override
    public final OptionalFloat findAny() {
        return evaluate(FindOps.makeFloat(false));
    }

    @Override
    public final float[] toArray() {
        return Nodes.flattenFloat((Node.OfFloat) evaluateToArrayNode(Float[]::new)).asPrimitiveArray();
    }

    //

    /**
     * Source stage of a FloatStream
     *
     * @param <E_IN> type of elements in the upstream source
     */
    public static class Head<E_IN> extends FloatPipeline<E_IN> {
        /**
         * Constructor for the source stage of a FloatStream.
         *
         * @param source {@code Supplier<Spliterator>} describing the stream
         *               source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Supplier<? extends Spliterator<Float>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        /**
         * Constructor for the source stage of a FloatStream.
         *
         * @param source {@code Spliterator} describing the stream source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Spliterator<Float> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final Sink<E_IN> opWrapSink(int flags, Sink<Float> sink) {
            throw new UnsupportedOperationException();
        }

        // Optimized sequential terminal operations for the head of the pipeline

        @Override
        public void forEach(FloatConsumer consumer) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }
            else {
                super.forEach(consumer);
            }
        }

        @Override
        public void forEachOrdered(FloatConsumer consumer) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            } else {
                super.forEachOrdered(consumer);
            }
        }

    }

    /**
     * Base class for a stateless intermediate stage of a FloatStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public static abstract class StatelessOp<E_IN> extends FloatPipeline<E_IN> {
        /**
         * Construct a new FloatStream by appending a stateless intermediate
         * operation to an existing stream.
         *
         * @param upstream the upstream pipeline stage
         * @param inputShape the stream shape for the upstream pipeline stage
         * @param opFlags operation flags for the new stage
         */
        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        public final boolean opIsStateful() {
            return false;
        }
    }

    /**
     * Base class for a stateful intermediate stage of a FloatStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public abstract static class StatefulOp<E_IN> extends FloatPipeline<E_IN> {
        /**
         * Construct a new FloatStream by appending a stateful intermediate
         * operation to an existing stream.
         *
         * @param upstream the upstream pipeline stage
         * @param inputShape the stream shape for the upstream pipeline stage
         * @param opFlags operation flags for the new stage
         */
        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        public final boolean opIsStateful() {
            return true;
        }

        @Override
        public abstract <P_IN> Node<Float> opEvaluateParallel(PipelineHelper<Float> helper,
                                                              Spliterator<P_IN> spliterator,
                                                              IntFunction<Float[]> generator);
    }
}
