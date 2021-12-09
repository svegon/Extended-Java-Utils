package svegon.utils.fast.util.booleans;

import svegon.utils.collections.collecting.BooleanCollector;
import svegon.utils.fast.util.bytes.BytePipeline;
import svegon.utils.fast.util.chars.CharPipeline;
import svegon.utils.fast.util.floats.FloatPipeline;
import svegon.utils.fast.util.shorts.ShortPipeline;
import svegon.utils.collections.stream.*;
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
import svegon.utils.fuck_modifiers.StreamOpFlag;
import svegon.utils.fuck_modifiers.StreamShape;
import svegon.utils.fuck_modifiers.StreamSpliterators;
import svegon.utils.fuck_modifiers.WhileOps;
import svegon.utils.interfaces.function.ObjectBooleanConsumer;
import svegon.utils.optional.OptionalBoolean;
import it.unimi.dsi.fastutil.booleans.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.*;

/**
 * Abstract base class for an intermediate pipeline stage or pipeline source
 * stage implementing whose elements are of type {@code int}.
 *
 * @param <E_IN> type of elements in the upstream source
 * @since 1.0.0
 */
public abstract class BooleanPipeline<E_IN> extends AbstractPipeline<E_IN, Boolean, BooleanStream> implements BooleanStream {

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Supplier<Spliterator>} describing the stream source
     * @param sourceFlags The source flags for the stream source, described in
     *        {@link StreamOpFlag}
     * @param parallel {@code true} if the pipeline is parallel
     */
    public BooleanPipeline(Supplier<? extends Spliterator<Boolean>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Spliterator} describing the stream source
     * @param sourceFlags The source flags for the stream source, described in
     *        {@link StreamOpFlag}
     * @param parallel {@code true} if the pipeline is parallel
     */
    public BooleanPipeline(Spliterator<Boolean> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for appending an intermediate operation onto an existing
     * pipeline.
     *
     * @param upstream the upstream element source
     * @param opFlags the operation flags for the new operation
     */
    public BooleanPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * Adapt a {@code Sink<Integer> to an {@code IntConsumer}, ideally simply
     * by casting.
     */
    private static BooleanConsumer adapt(Sink<Boolean> sink) {
        if (sink instanceof BooleanConsumer) {
            return (BooleanConsumer) sink;
        } else {
            return sink::accept;
        }
    }

    /**
     * Adapt a {@code Spliterator<Integer>} to a {@code Spliterator.OfInt}.
     *
     * @implNote
     * The implementation attempts to cast to a Spliterator.OfInt, and throws an
     * exception if this cast is not possible.
     */
    private static BooleanSpliterator adapt(Spliterator<Boolean> s) {
        return BooleanSpliterators.asBooleanSpliterator(s);
    }


    // Shape-specific methods

    @Override
    public final StreamShape getOutputShape() {
        return StreamShape.BOOLEAN_VALUE;
    }

    @Override
    public final <P_IN> Node<Boolean> evaluateToNode(PipelineHelper<Boolean> helper, Spliterator<P_IN> spliterator,
                                                     boolean flattenTree, IntFunction<Boolean[]> generator) {
        return Nodes.collectBoolean(helper, spliterator, flattenTree);
    }

    @Override
    public final <P_IN> Spliterator<Boolean> wrap(PipelineHelper<Boolean> ph, Supplier<Spliterator<P_IN>> supplier,
                                                    boolean isParallel) {
        return new StreamSpliterators.BooleanWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final BooleanSpliterator lazySpliterator(Supplier<? extends Spliterator<Boolean>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfBoolean((Supplier<BooleanSpliterator>) supplier);
    }

    @Override
    public final boolean forEachWithCancel(Spliterator<Boolean> spliterator, Sink<Boolean> sink) {
        BooleanSpliterator spl = adapt(spliterator);
        BooleanConsumer adaptedSink = adapt(sink);
        boolean cancelled;

        while (!(cancelled = sink.cancellationRequested()) && spl.tryAdvance(adaptedSink));

        return cancelled;
    }

    @Override
    public final Node.Builder<Boolean> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Boolean[]> generator) {
        return Nodes.booleanBuilder(exactSizeIfKnown);
    }

    private <U> Stream<U> mapToObj(Boolean2ObjectFunction<? extends U> mapper, int opFlags) {
        return new ReferencePipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE, opFlags) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.get(t));
                    }
                };
            }
        };
    }

    // BooleanStream

    @Override
    public final @NotNull BooleanIterator iterator() {
        return BooleanSpliterators.asIterator(spliterator());
    }

    @Override
    public final @NotNull BooleanSpliterator spliterator() {
        return adapt(super.spliterator());
    }

    @Override
    public final Stream<Boolean> boxed() {
        return mapToObj(Boolean::valueOf, 0);
    }

    @Override
    public final BooleanStream map(BooleanUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(Boolean2ObjectFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapToObj(mapper, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    @Override
    public final ByteStream mapToByte(Boolean2ByteFunction mapper) {
        Objects.requireNonNull(mapper);

        return new BytePipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final ShortStream mapToShort(final Boolean2ShortFunction mapper) {
        Objects.requireNonNull(mapper);

        return new ShortPipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(Boolean2IntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(Boolean2LongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final CharStream mapToChar(final Boolean2CharFunction mapper) {
        Objects.requireNonNull(mapper);

        return new CharPipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final FloatStream mapToFloat(final Boolean2FloatFunction mapper) {
        Objects.requireNonNull(mapper);
        return new FloatPipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(final Boolean2DoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<>(this, StreamShape.BOOLEAN_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final BooleanStream flatMap(final Boolean2ObjectFunction<? extends BooleanStream> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.BOOLEAN_VALUE, StreamOpFlag.NOT_SORTED
                | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    BooleanConsumer downstreamAsBoolean = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(boolean t) {
                        try (BooleanStream result = mapper.get(t)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsBoolean);
                                } else {
                                    var s = result.sequential().spliterator();
                                    while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsBoolean));
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        // If this method is called then an operation within the stream
                        // pipeline is boolean-circuiting (see AbstractPipeline.copyInto).
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
    public BooleanStream unordered() {
        if (!isOrdered()) {
            return this;
        }

        return new StatelessOp<>(this, StreamShape.BOOLEAN_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Boolean> sink) {
                return sink;
            }
        };
    }

    @Override
    public final BooleanStream filter(final BooleanPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<>(this, StreamShape.BOOLEAN_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(boolean t) {
                        if (predicate.test(t)) {
                            downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final BooleanStream peek(BooleanConsumer action) {
        Objects.requireNonNull(action);

        return new StatelessOp<>(this, StreamShape.BOOLEAN_VALUE, 0) {
            @Override
            public Sink<Boolean> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedBoolean<>(sink) {
                    @Override
                    public void accept(boolean t) {
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

    // Stateful intermediate ops from IntStream

    @Override
    public final BooleanStream limit(long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }

        return SliceOps.makeBoolean(this, 0, maxSize);
    }

    @Override
    public final BooleanStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        }

        if (n == 0) {
            return this;
        }

        return SliceOps.makeBoolean(this, n, -1);
    }

    @Override
    public final BooleanStream takeWhile(BooleanPredicate predicate) {
        return WhileOps.makeTakeWhileBoolean(this, predicate);
    }

    @Override
    public final BooleanStream dropWhile(BooleanPredicate predicate) {
        return WhileOps.makeDropWhileBoolean(this, predicate);
    }

    @Override
    public final BooleanStream sorted() {
        return SortedOps.makeBoolean(this);
    }

    @Override
    public final BooleanStream distinct() {
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires an int-specific map/set implementation.
        return StreamUtil.mapToBoolean(boxed().distinct(), i -> (boolean) i);
    }

    // Terminal ops from IntStream

    @Override
    public void forEach(BooleanConsumer action) {
        evaluate(ForEachOps.makeBoolean(action, false));
    }

    @Override
    public void forEachOrdered(BooleanConsumer action) {
        evaluate(ForEachOps.makeBoolean(action, true));
    }

    @Override
    public final long sum() {
        final AtomicLong atomic = new AtomicLong();

        forEach((bl) -> {
            if (bl) {
                atomic.getAndIncrement();
            }
        });

        return atomic.get();
    }

    @Override
    public final OptionalBoolean min() {
        return reduce((c, d) -> c && d);
    }

    @Override
    public final OptionalBoolean max() {
        return reduce((c, d) -> c || d);
    }

    @Override
    public final long count() {
        return evaluate(ReduceOps.makeBooleanCounting());
    }

    @Override
    public final OptionalDouble average() {
        double[] avg = collect(() -> new double[2], (ll, i) -> {
                                 ll[0]++;
            if (i) {
                ll[1]++;
            }
                             }, (ll, rr) -> {
                                 ll[0] += rr[0];
                                 ll[1] += rr[1];
                             });
        return avg[0] > 0 ? OptionalDouble.of(avg[1] / avg[0]) : OptionalDouble.empty();
    }

    @Override
    public final BooleanSummaryStatistics summaryStatistics() {
        return collect(BooleanSummaryStatistics::new, BooleanSummaryStatistics::accept, BooleanSummaryStatistics::combine);
    }

    @Override
    public final boolean reduce(boolean identity, BooleanBinaryOperator op) {
        return evaluate(ReduceOps.makeBoolean(identity, op));
    }

    @Override
    public final OptionalBoolean reduce(BooleanBinaryOperator op) {
        return evaluate(ReduceOps.makeBoolean(op));
    }

    @Override
    public final <R> R collect(final Supplier<R> supplier, final ObjectBooleanConsumer<R> accumulator,
                               final BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);

        return evaluate(ReduceOps.makeBoolean(supplier, accumulator, (left, right) -> {
            combiner.accept(left, right);
            return left;
        }));
    }

    @Override
    public <R, A> R collect(BooleanCollector<A, R> collector) {
        A container;

        if (isParallel()
                && (collector.characteristics().contains(Collector.Characteristics.CONCURRENT))
                && (!isOrdered() || collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
            container = collector.supplier().get();
            ObjectBooleanConsumer<A> accumulator = collector.accumulator();
            forEach(u -> accumulator.accept(container, u));
        } else {
            container = evaluate(ReduceOps.makeBoolean(collector));
        }

        return collector.finisher().apply(container);
    }

    @Override
    public final boolean anyMatch(BooleanPredicate predicate) {
        return evaluate(MatchOps.makeBoolean(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(BooleanPredicate predicate) {
        return evaluate(MatchOps.makeBoolean(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(BooleanPredicate predicate) {
        return evaluate(MatchOps.makeBoolean(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalBoolean findFirst() {
        return evaluate(FindOps.makeBoolean(true));
    }

    @Override
    public final OptionalBoolean findAny() {
        return evaluate(FindOps.makeBoolean(false));
    }

    @Override
    public final boolean[] toArray() {
        return Nodes.flattenBoolean((Node.OfBoolean) evaluateToArrayNode(Boolean[]::new)).asPrimitiveArray();
    }

    //

    /**
     * Source stage of an IntStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public static class Head<E_IN> extends BooleanPipeline<E_IN> {
        /**
         * Constructor for the source stage of an IntStream.
         *
         * @param source {@code Supplier<Spliterator>} describing the stream
         *               source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Supplier<? extends Spliterator<Boolean>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        /**
         * Constructor for the source stage of an IntStream.
         *
         * @param source {@code Spliterator} describing the stream source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Spliterator<Boolean> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final Sink<E_IN> opWrapSink(int flags, Sink<Boolean> sink) {
            throw new UnsupportedOperationException();
        }

        // Optimized sequential terminal operations for the head of the pipeline

        @Override
        public void forEach(BooleanConsumer action) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(BooleanConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    /**
     * Base class for a stateless intermediate stage of an IntStream
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public abstract static class StatelessOp<E_IN> extends BooleanPipeline<E_IN> {
        /**
         * Construct a new IntStream by appending a stateless intermediate
         * operation to an existing stream.
         * @param upstream The upstream pipeline stage
         * @param inputShape The stream shape for the upstream pipeline stage
         * @param opFlags Operation flags for the new stage
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
     * Base class for a stateful intermediate stage of an IntStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public abstract static class StatefulOp<E_IN> extends BooleanPipeline<E_IN> {
        /**
         * Construct a new IntStream by appending a stateful intermediate
         * operation to an existing stream.
         * @param upstream The upstream pipeline stage
         * @param inputShape The stream shape for the upstream pipeline stage
         * @param opFlags Operation flags for the new stage
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
        public abstract <P_IN> Node<Boolean> opEvaluateParallel(PipelineHelper<Boolean> helper,
                                                                  Spliterator<P_IN> spliterator,
                                                                  IntFunction<Boolean[]> generator);
    }
}
