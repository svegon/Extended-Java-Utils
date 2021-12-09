/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package svegon.utils.fast.util.shorts;

import svegon.utils.collections.collecting.ShortCollector;
import svegon.utils.fast.util.booleans.BooleanPipeline;
import svegon.utils.fast.util.bytes.BytePipeline;
import svegon.utils.fast.util.chars.CharPipeline;
import svegon.utils.fast.util.floats.FloatPipeline;
import svegon.utils.collections.stream.*;
import svegon.utils.fuck_modifiers.*;
import svegon.utils.interfaces.function.ObjectShortConsumer;
import svegon.utils.optional.OptionalShort;
import it.unimi.dsi.fastutil.shorts.*;
import org.jetbrains.annotations.NotNull;

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
public abstract class ShortPipeline<E_IN> extends AbstractPipeline<E_IN, Short, ShortStream> implements ShortStream {

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Supplier<Spliterator>} describing the stream source
     * @param sourceFlags The source flags for the stream source, described in
     *        {@link StreamOpFlag}
     * @param parallel {@code true} if the pipeline is parallel
     */
    public ShortPipeline(Supplier<? extends Spliterator<Short>> source, int sourceFlags, boolean parallel) {
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
    public ShortPipeline(Spliterator<Short> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for appending an intermediate operation onto an existing
     * pipeline.
     *
     * @param upstream the upstream element source
     * @param opFlags the operation flags for the new operation
     */
    public ShortPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * Adapt a {@code Sink<Integer> to an {@code IntConsumer}, ideally simply
     * by casting.
     */
    private static ShortConsumer adapt(Sink<Short> sink) {
        if (sink instanceof ShortConsumer) {
            return (ShortConsumer) sink;
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
    private static ShortSpliterator adapt(Spliterator<Short> s) {
        return ShortSpliterators.asShortSpliterator(s);
    }


    // Shape-specific methods

    @Override
    public final StreamShape getOutputShape() {
        return StreamShape.SHORT_VALUE;
    }

    @Override
    public final <P_IN> Node<Short> evaluateToNode(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator,
                                                       boolean flattenTree, IntFunction<Short[]> generator) {
        return Nodes.collectShort(helper, spliterator, flattenTree);
    }

    @Override
    public final <P_IN> Spliterator<Short> wrap(PipelineHelper<Short> ph, Supplier<Spliterator<P_IN>> supplier,
                                                    boolean isParallel) {
        return new StreamSpliterators.ShortWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ShortSpliterator lazySpliterator(Supplier<? extends Spliterator<Short>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfShort((Supplier<ShortSpliterator>) supplier);
    }

    @Override
    public final boolean forEachWithCancel(Spliterator<Short> spliterator, Sink<Short> sink) {
        ShortSpliterator spl = adapt(spliterator);
        ShortConsumer adaptedSink = adapt(sink);
        boolean cancelled;

        while (!(cancelled = sink.cancellationRequested()) && spl.tryAdvance(adaptedSink));

        return cancelled;
    }

    @Override
    public final Node.Builder<Short> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Short[]> generator) {
        return Nodes.shortBuilder(exactSizeIfKnown);
    }

    private <U> Stream<U> mapToObj(Short2ObjectFunction<? extends U> mapper, int opFlags) {
        return new ReferencePipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE, opFlags) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.get(t));
                    }
                };
            }
        };
    }

    // ShortStream

    @Override
    public final @NotNull ShortIterator iterator() {
        return ShortSpliterators.asIterator(spliterator());
    }

    @Override
    public final @NotNull ShortSpliterator spliterator() {
        return adapt(super.spliterator());
    }

    @Override
    public final Stream<Short> boxed() {
        return mapToObj(Short::valueOf, 0);
    }

    @Override
    public final ShortStream map(ShortUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.SHORT_VALUE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(Short2ObjectFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapToObj(mapper, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    public final BooleanStream mapToBoolean(final ShortPredicate mapper) {
        Objects.requireNonNull(mapper);

        return new BooleanPipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.test(t));
                    }
                };
            }
        };
    }

    @Override
    public final ByteStream mapToByte(final Short2ByteFunction mapper) {
        Objects.requireNonNull(mapper);
        return new BytePipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(Short2IntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(Short2LongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final CharStream mapToChar(final Short2CharFunction mapper) {
        Objects.requireNonNull(mapper);
        return new CharPipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final FloatStream mapToFloat(final Short2FloatFunction mapper) {
        Objects.requireNonNull(mapper);
        return new FloatPipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(final Short2DoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final ShortStream flatMap(final Short2ObjectFunction<? extends ShortStream> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, StreamOpFlag.NOT_SORTED
                | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    ShortConsumer downstreamAsShort = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(short t) {
                        try (ShortStream result = mapper.get(t)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsShort);
                                } else {
                                    var s = result.sequential().spliterator();
                                    while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsShort));
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
    public ShortStream unordered() {
        if (!isOrdered()) {
            return this;
        }

        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return sink;
            }
        };
    }

    @Override
    public final ShortStream filter(final ShortPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(short t) {
                        if (predicate.test(t)) {
                            downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final ShortStream peek(ShortConsumer action) {
        Objects.requireNonNull(action);

        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, 0) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
                    @Override
                    public void accept(short t) {
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

    // Stateful intermediate ops from IntStream

    @Override
    public final ShortStream limit(long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }

        return SliceOps.makeShort(this, 0, maxSize);
    }

    @Override
    public final ShortStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        }

        if (n == 0) {
            return this;
        }

        return SliceOps.makeShort(this, n, -1);
    }

    @Override
    public final ShortStream takeWhile(ShortPredicate predicate) {
        return WhileOps.makeTakeWhileShort(this, predicate);
    }

    @Override
    public final ShortStream dropWhile(ShortPredicate predicate) {
        return WhileOps.makeDropWhileShort(this, predicate);
    }

    @Override
    public final ShortStream sorted() {
        return SortedOps.makeShort(this);
    }

    @Override
    public final ShortStream distinct() {
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires an int-specific map/set implementation.
        return StreamUtil.mapToShort(boxed().distinct(), i -> (short) i);
    }

    // Terminal ops from IntStream

    @Override
    public void forEach(ShortConsumer action) {
        evaluate(ForEachOps.makeShort(action, false));
    }

    @Override
    public void forEachOrdered(ShortConsumer action) {
        evaluate(ForEachOps.makeShort(action, true));
    }

    @Override
    public final long sum() {
        final AtomicLong atomic = new AtomicLong();

        forEach(atomic::getAndAdd);

        return atomic.get();
    }

    @Override
    public final OptionalShort min() {
        return reduce((c, d) -> (short) Math.min(c, d));
    }

    @Override
    public final OptionalShort max() {
        return reduce((c, d) -> (short) Math.max(c, d));
    }

    @Override
    public final long count() {
        return evaluate(ReduceOps.makeShortCounting());
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
    public final ShortSummaryStatistics summaryStatistics() {
        return collect(ShortSummaryStatistics::new, ShortSummaryStatistics::accept, ShortSummaryStatistics::combine);
    }

    @Override
    public final short reduce(short identity, ShortBinaryOperator op) {
        return evaluate(ReduceOps.makeShort(identity, op));
    }

    @Override
    public final OptionalShort reduce(ShortBinaryOperator op) {
        return evaluate(ReduceOps.makeShort(op));
    }

    @Override
    public final <R> R collect(final Supplier<R> supplier, final ObjectShortConsumer<R> accumulator,
                               final BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);

        return evaluate(ReduceOps.makeShort(supplier, accumulator, (left, right) -> {
            combiner.accept(left, right);
            return left;
        }));
    }

    @Override
    public final <R, A> R collect(ShortCollector<A, R> collector) {
        A container;

        if (isParallel()
                && (collector.characteristics().contains(Collector.Characteristics.CONCURRENT))
                && (!isOrdered() || collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
            container = collector.supplier().get();
            ObjectShortConsumer<A> accumulator = collector.accumulator();
            forEach(u -> accumulator.accept(container, u));
        } else {
            container = evaluate(ReduceOps.makeShort(collector));
        }

        return collector.finisher().apply(container);
    }

    @Override
    public final boolean anyMatch(ShortPredicate predicate) {
        return evaluate(MatchOps.makeShort(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(ShortPredicate predicate) {
        return evaluate(MatchOps.makeShort(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(ShortPredicate predicate) {
        return evaluate(MatchOps.makeShort(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalShort findFirst() {
        return evaluate(FindOps.makeShort(true));
    }

    @Override
    public final OptionalShort findAny() {
        return evaluate(FindOps.makeShort(false));
    }

    @Override
    public final short[] toArray() {
        return Nodes.flattenShort((Node.OfShort) evaluateToArrayNode(Short[]::new)).asPrimitiveArray();
    }

    //

    /**
     * Source stage of an IntStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public static class Head<E_IN> extends ShortPipeline<E_IN> {
        /**
         * Constructor for the source stage of an IntStream.
         *
         * @param source {@code Supplier<Spliterator>} describing the stream
         *               source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Supplier<? extends Spliterator<Short>> source, int sourceFlags, boolean parallel) {
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
        public Head(Spliterator<Short> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final Sink<E_IN> opWrapSink(int flags, Sink<Short> sink) {
            throw new UnsupportedOperationException();
        }

        // Optimized sequential terminal operations for the head of the pipeline

        @Override
        public void forEach(ShortConsumer action) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(ShortConsumer action) {
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
    public abstract static class StatelessOp<E_IN> extends ShortPipeline<E_IN> {
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
    public abstract static class StatefulOp<E_IN> extends ShortPipeline<E_IN> {
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
        public abstract <P_IN> Node<Short> opEvaluateParallel(PipelineHelper<Short> helper,
                                                                  Spliterator<P_IN> spliterator,
                                                                  IntFunction<Short[]> generator);
    }
}
