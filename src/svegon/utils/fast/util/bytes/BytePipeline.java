package svegon.utils.fast.util.bytes;

import it.unimi.dsi.fastutil.bytes.*;
import org.jetbrains.annotations.NotNull;
import svegon.utils.collections.collecting.ByteCollector;
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
import svegon.utils.interfaces.function.ObjectByteConsumer;
import svegon.utils.optional.OptionalByte;
import svegon.utils.fast.util.booleans.BooleanPipeline;
import svegon.utils.fast.util.chars.CharPipeline;
import svegon.utils.fast.util.floats.FloatPipeline;
import svegon.utils.fast.util.shorts.ShortPipeline;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.*;

/**
 * Abstract base class for an intermediate pipeline stage or pipeline source
 * stage implementing whose elements are of type {@code int}.
 *
 * @param <E_IN> type of elements in the upstream source
 * @since 1.0.0
 */
public abstract class BytePipeline<E_IN> extends AbstractPipeline<E_IN, Byte, ByteStream> implements ByteStream {

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Supplier<Spliterator>} describing the stream source
     * @param sourceFlags The source flags for the stream source, described in
     *        {@link StreamOpFlag}
     * @param parallel {@code true} if the pipeline is parallel
     */
    public BytePipeline(Supplier<? extends Spliterator<Byte>> source, int sourceFlags, boolean parallel) {
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
    public BytePipeline(Spliterator<Byte> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for appending an intermediate operation onto an existing
     * pipeline.
     *
     * @param upstream the upstream element source
     * @param opFlags the operation flags for the new operation
     */
    public BytePipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * Adapt a {@code Sink<Integer> to an {@code IntConsumer}, ideally simply
     * by casting.
     */
    private static ByteConsumer adapt(Sink<Byte> sink) {
        if (sink instanceof ByteConsumer) {
            return (ByteConsumer) sink;
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
    private static ByteSpliterator adapt(Spliterator<Byte> s) {
        return ByteSpliterators.asByteSpliterator(s);
    }


    // Shape-specific methods

    @Override
    public final StreamShape getOutputShape() {
        return StreamShape.BYTE_VALUE;
    }

    @Override
    public final <P_IN> Node<Byte> evaluateToNode(PipelineHelper<Byte> helper, Spliterator<P_IN> spliterator,
                                                       boolean flattenTree, IntFunction<Byte[]> generator) {
        return Nodes.collectByte(helper, spliterator, flattenTree);
    }

    @Override
    public final <P_IN> Spliterator<Byte> wrap(PipelineHelper<Byte> ph, Supplier<Spliterator<P_IN>> supplier,
                                                    boolean isParallel) {
        return new StreamSpliterators.ByteWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ByteSpliterator lazySpliterator(Supplier<? extends Spliterator<Byte>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfByte((Supplier<ByteSpliterator>) supplier);
    }

    @Override
    public final boolean forEachWithCancel(Spliterator<Byte> spliterator, Sink<Byte> sink) {
        ByteSpliterator spl = adapt(spliterator);
        ByteConsumer adaptedSink = adapt(sink);
        boolean cancelled;

        while (!(cancelled = sink.cancellationRequested()) && spl.tryAdvance(adaptedSink));

        return cancelled;
    }

    @Override
    public final Node.Builder<Byte> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Byte[]> generator) {
        return Nodes.byteBuilder(exactSizeIfKnown);
    }

    private <U> Stream<U> mapToObj(Byte2ObjectFunction<? extends U> mapper, int opFlags) {
        return new ReferencePipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE, opFlags) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.get(t));
                    }
                };
            }
        };
    }

    // ByteStream

    @Override
    public final @NotNull ByteIterator iterator() {
        return ByteSpliterators.asIterator(spliterator());
    }

    @Override
    public final @NotNull ByteSpliterator spliterator() {
        return adapt(super.spliterator());
    }

    @Override
    public final Stream<Byte> boxed() {
        return mapToObj(Byte::valueOf, 0);
    }

    @Override
    public final ByteStream map(ByteUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.BYTE_VALUE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(Byte2ObjectFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapToObj(mapper, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    public final BooleanStream mapToBoolean(final BytePredicate mapper) {
        Objects.requireNonNull(mapper);

        return new BooleanPipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.test(t));
                    }
                };
            }
        };
    }

    @Override
    public final ShortStream mapToShort(final Byte2ShortFunction mapper) {
        Objects.requireNonNull(mapper);
        return new ShortPipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(Byte2IntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(Byte2LongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final CharStream mapToChar(final Byte2CharFunction mapper) {
        Objects.requireNonNull(mapper);
        return new CharPipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final FloatStream mapToFloat(final Byte2FloatFunction mapper) {
        Objects.requireNonNull(mapper);
        return new FloatPipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(final Byte2DoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<>(this, StreamShape.BYTE_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final ByteStream flatMap(final Byte2ObjectFunction<? extends ByteStream> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.BYTE_VALUE, StreamOpFlag.NOT_SORTED
                | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    ByteConsumer downstreamAsByte = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(byte t) {
                        try (ByteStream result = mapper.get(t)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsByte);
                                } else {
                                    var s = result.sequential().spliterator();
                                    while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsByte));
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        // If this method is called then an operation within the stream
                        // pipeline is bytes-circuiting (see AbstractPipeline.copyInto).
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
    public ByteStream unordered() {
        if (!isOrdered()) {
            return this;
        }

        return new StatelessOp<>(this, StreamShape.BYTE_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return sink;
            }
        };
    }

    @Override
    public final ByteStream filter(final BytePredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<>(this, StreamShape.BYTE_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(byte t) {
                        if (predicate.test(t)) {
                            downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final ByteStream peek(ByteConsumer action) {
        Objects.requireNonNull(action);

        return new StatelessOp<>(this, StreamShape.BYTE_VALUE, 0) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<>(sink) {
                    @Override
                    public void accept(byte t) {
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

    // Stateful intermediate ops from IntStream

    @Override
    public final ByteStream limit(long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }

        return SliceOps.makeByte(this, 0, maxSize);
    }

    @Override
    public final ByteStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        }

        if (n == 0) {
            return this;
        }

        return SliceOps.makeByte(this, n, -1);
    }

    @Override
    public final ByteStream takeWhile(BytePredicate predicate) {
        return WhileOps.makeTakeWhileByte(this, predicate);
    }

    @Override
    public final ByteStream dropWhile(BytePredicate predicate) {
        return WhileOps.makeDropWhileByte(this, predicate);
    }

    @Override
    public final ByteStream sorted() {
        return SortedOps.makeByte(this);
    }

    @Override
    public final ByteStream distinct() {
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires an int-specific map/set implementation.
        return StreamUtil.mapToByte(boxed().distinct(), i -> (byte) i);
    }

    // Terminal ops from IntStream

    @Override
    public void forEach(ByteConsumer action) {
        evaluate(ForEachOps.makeByte(action, false));
    }

    @Override
    public void forEachOrdered(ByteConsumer action) {
        evaluate(ForEachOps.makeByte(action, true));
    }

    @Override
    public final long sum() {
        final AtomicLong atomic = new AtomicLong();

        forEach(atomic::getAndAdd);

        return atomic.get();
    }

    @Override
    public final OptionalByte min() {
        return reduce((c, d) -> (byte) Math.min(c, d));
    }

    @Override
    public final OptionalByte max() {
        return reduce((c, d) -> (byte) Math.max(c, d));
    }

    @Override
    public final long count() {
        return evaluate(ReduceOps.makeByteCounting());
    }

    @Override
    public final OptionalDouble average() {
        double[] avg = collect(() -> new double[2], (ll, i) -> {
                                 ll[0]++;
                                 ll[1] += i;
                             }, (ll, rr) -> {
                                 ll[0] += rr[0];
                                 ll[1] += rr[1];
                             });
        return avg[0] > 0 ? OptionalDouble.of(avg[1] / avg[0]) : OptionalDouble.empty();
    }

    @Override
    public final ByteSummaryStatistics summaryStatistics() {
        return collect(ByteSummaryStatistics::new, ByteSummaryStatistics::accept, ByteSummaryStatistics::combine);
    }

    @Override
    public final byte reduce(byte identity, ByteBinaryOperator op) {
        return evaluate(ReduceOps.makeByte(identity, op));
    }

    @Override
    public final OptionalByte reduce(ByteBinaryOperator op) {
        return evaluate(ReduceOps.makeByte(op));
    }

    @Override
    public final <R> R collect(final Supplier<R> supplier, final ObjectByteConsumer<R> accumulator,
                               final BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);

        return evaluate(ReduceOps.makeByte(supplier, accumulator, (left, right) -> {
            combiner.accept(left, right);
            return left;
        }));
    }

    @Override
    public final <R, A> R collect(ByteCollector<A, R> collector) {
        A container;

        if (isParallel()
                && (collector.characteristics().contains(Collector.Characteristics.CONCURRENT))
                && (!isOrdered() || collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
            container = collector.supplier().get();
            ObjectByteConsumer<A> accumulator = collector.accumulator();
            forEach(u -> accumulator.accept(container, u));
        } else {
            container = evaluate(ReduceOps.makeByte(collector));
        }

        return collector.finisher().apply(container);
    }

    @Override
    public final boolean anyMatch(BytePredicate predicate) {
        return evaluate(MatchOps.makeByte(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(BytePredicate predicate) {
        return evaluate(MatchOps.makeByte(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(BytePredicate predicate) {
        return evaluate(MatchOps.makeByte(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalByte findFirst() {
        return evaluate(FindOps.makeByte(true));
    }

    @Override
    public final OptionalByte findAny() {
        return evaluate(FindOps.makeByte(false));
    }

    @Override
    public final byte[] toArray() {
        return Nodes.flattenByte((Node.OfByte) evaluateToArrayNode(Byte[]::new)).asPrimitiveArray();
    }

    //

    /**
     * Source stage of an IntStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public static class Head<E_IN> extends BytePipeline<E_IN> {
        /**
         * Constructor for the source stage of an IntStream.
         *
         * @param source {@code Supplier<Spliterator>} describing the stream
         *               source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Supplier<? extends Spliterator<Byte>> source, int sourceFlags, boolean parallel) {
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
        public Head(Spliterator<Byte> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final Sink<E_IN> opWrapSink(int flags, Sink<Byte> sink) {
            throw new UnsupportedOperationException();
        }

        // Optimized sequential terminal operations for the head of the pipeline

        @Override
        public void forEach(ByteConsumer action) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(ByteConsumer action) {
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
    public abstract static class StatelessOp<E_IN> extends BytePipeline<E_IN> {
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
    public abstract static class StatefulOp<E_IN> extends BytePipeline<E_IN> {
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
        public abstract <P_IN> Node<Byte> opEvaluateParallel(PipelineHelper<Byte> helper,
                                                                  Spliterator<P_IN> spliterator,
                                                                  IntFunction<Byte[]> generator);
    }
}
