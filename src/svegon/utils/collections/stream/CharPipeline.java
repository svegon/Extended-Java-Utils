package svegon.utils.collections.stream;

import svegon.utils.PredicateUtil;
import svegon.utils.fuck_modifiers.*;
import svegon.utils.interfaces.function.ObjectCharConsumer;
import svegon.utils.optional.OptionalChar;
import it.unimi.dsi.fastutil.chars.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Abstract base class for an intermediate pipeline stage or pipeline source
 * stage implementing whose elements are of type {@code int}.
 *
 * @param <E_IN> type of elements in the upstream source
 * @since 1.0.0
 */
public abstract class CharPipeline<E_IN> extends AbstractPipeline<E_IN, Character, CharStream> implements CharStream {
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        forEach(builder::append);

        return builder.toString();
    }

    /**
     * Constructor for the head of a stream pipeline.
     *
     * @param source {@code Supplier<Spliterator>} describing the stream source
     * @param sourceFlags The source flags for the stream source, described in
     *        {@link StreamOpFlag}
     * @param parallel {@code true} if the pipeline is parallel
     */
    public CharPipeline(Supplier<? extends Spliterator<Character>> source, int sourceFlags, boolean parallel) {
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
    public CharPipeline(Spliterator<Character> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * Constructor for appending an intermediate operation onto an existing
     * pipeline.
     *
     * @param upstream the upstream element source
     * @param opFlags the operation flags for the new operation
     */
    public CharPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * Adapt a {@code Sink<Integer> to an {@code IntConsumer}, ideally simply
     * by casting.
     */
    private static CharConsumer adapt(Sink<Character> sink) {
        if (sink instanceof CharConsumer) {
            return (CharConsumer) sink;
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
    private static CharSpliterator adapt(Spliterator<Character> s) {
        return CharSpliterators.asCharSpliterator(s);
    }


    // Shape-specific methods

    @Override
    public final StreamShape getOutputShape() {
        return StreamShape.CHAR_VALUE;
    }

    @Override
    public final <P_IN> Node<Character> evaluateToNode(PipelineHelper<Character> helper, Spliterator<P_IN> spliterator,
                                                       boolean flattenTree, IntFunction<Character[]> generator) {
        return Nodes.collectChar(helper, spliterator, flattenTree);
    }

    @Override
    public final <P_IN> Spliterator<Character> wrap(PipelineHelper<Character> ph, Supplier<Spliterator<P_IN>> supplier,
                                                    boolean isParallel) {
        return new StreamSpliterators.CharWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final CharSpliterator lazySpliterator(Supplier<? extends Spliterator<Character>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfChar((Supplier<CharSpliterator>) supplier);
    }

    @Override
    public final boolean forEachWithCancel(Spliterator<Character> spliterator, Sink<Character> sink) {
        CharSpliterator spl = adapt(spliterator);
        CharConsumer adaptedSink = adapt(sink);
        boolean cancelled;

        while (!(cancelled = sink.cancellationRequested()) && spl.tryAdvance(adaptedSink));

        return cancelled;
    }

    @Override
    public final Node.Builder<Character> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Character[]> generator) {
        return Nodes.charBuilder(exactSizeIfKnown);
    }

    private <U> Stream<U> mapToObj(Char2ObjectFunction<? extends U> mapper, int opFlags) {
        return new ReferencePipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE, opFlags) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.get(t));
                    }
                };
            }
        };
    }

    // CharStream

    @Override
    public final @NotNull CharIterator iterator() {
        return CharSpliterators.asIterator(spliterator());
    }

    @Override
    public final @NotNull CharSpliterator spliterator() {
        return adapt(super.spliterator());
    }

    @Override
    public final Stream<Character> boxed() {
        return mapToObj(Character::valueOf, 0);
    }

    @Override
    public final CharStream map(CharUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.CHAR_VALUE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(Char2ObjectFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapToObj(mapper, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    public final BooleanStream mapToBoolean(final CharPredicate mapper) {
        Objects.requireNonNull(mapper);

        return new BooleanPipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.test(t));
                    }
                };
            }
        };
    }

    @Override
    public final ByteStream mapToByte(Char2ByteFunction mapper) {
        Objects.requireNonNull(mapper);

        return new BytePipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final ShortStream mapToShort(Char2ShortFunction mapper) {
        Objects.requireNonNull(mapper);

        return new ShortPipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(Char2IntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(Char2LongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final FloatStream mapToFloat(final Char2FloatFunction mapper) {
        Objects.requireNonNull(mapper);
        return new FloatPipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(final Char2DoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<>(this, StreamShape.CHAR_VALUE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final CharStream flatMap(final Char2ObjectFunction<? extends CharStream> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<>(this, StreamShape.CHAR_VALUE, StreamOpFlag.NOT_SORTED
                | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<>(sink) {
                    // true if cancellationRequested() has been called
                    boolean cancellationRequestedCalled;

                    // cache the consumer to avoid creation on every accepted element
                    CharConsumer downstreamAsChar = downstream::accept;

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(char t) {
                        try (CharStream result = mapper.get(t)) {
                            if (result != null) {
                                if (!cancellationRequestedCalled) {
                                    result.sequential().forEach(downstreamAsChar);
                                } else {
                                    var s = result.sequential().spliterator();
                                    while (!downstream.cancellationRequested() && s.tryAdvance(downstreamAsChar));
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
    public CharStream unordered() {
        if (!isOrdered()) {
            return this;
        }

        return new StatelessOp<>(this, StreamShape.CHAR_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return sink;
            }
        };
    }

    @Override
    public final CharStream filter(final CharPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<>(this, StreamShape.CHAR_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(char t) {
                        if (predicate.test(t)) {
                            downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final CharStream peek(CharConsumer action) {
        Objects.requireNonNull(action);

        return new StatelessOp<>(this, StreamShape.CHAR_VALUE, 0) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<>(sink) {
                    @Override
                    public void accept(char t) {
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

    // Stateful intermediate ops from IntStream

    @Override
    public final CharStream limit(long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }

        return SliceOps.makeChar(this, 0, maxSize);
    }

    @Override
    public final CharStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        }

        if (n == 0) {
            return this;
        }

        return SliceOps.makeChar(this, n, -1);
    }

    @Override
    public final CharStream takeWhile(CharPredicate predicate) {
        return WhileOps.makeTakeWhileChar(this, predicate);
    }

    @Override
    public final CharStream dropWhile(CharPredicate predicate) {
        return WhileOps.makeDropWhileChar(this, predicate);
    }

    @Override
    public final CharStream sorted() {
        return SortedOps.makeChar(this);
    }

    @Override
    public final CharStream distinct() {
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires an int-specific map/set implementation.
        return filter(PredicateUtil.charUniquenessPredicate());
    }

    // Terminal ops from IntStream

    @Override
    public void forEach(CharConsumer action) {
        evaluate(ForEachOps.makeChar(action, false));
    }

    @Override
    public void forEachOrdered(CharConsumer action) {
        evaluate(ForEachOps.makeChar(action, true));
    }

    @Override
    public final long sum() {
        final AtomicLong atomic = new AtomicLong();

        forEach(atomic::getAndAdd);

        return atomic.get();
    }

    @Override
    public final OptionalChar min() {
        return reduce((c, d) -> (char) Math.min(c, d));
    }

    @Override
    public final OptionalChar max() {
        return reduce((c, d) -> (char) Math.max(c, d));
    }

    @Override
    public final long count() {
        return evaluate(ReduceOps.makeCharCounting());
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
    public final CharSummaryStatistics summaryStatistics() {
        return collect(CharSummaryStatistics::new, CharSummaryStatistics::accept, CharSummaryStatistics::combine);
    }

    @Override
    public final char reduce(char identity, CharBinaryOperator op) {
        return evaluate(ReduceOps.makeChar(identity, op));
    }

    @Override
    public final OptionalChar reduce(CharBinaryOperator op) {
        return evaluate(ReduceOps.makeChar(op));
    }

    @Override
    public final <R> R collect(final Supplier<R> supplier, final ObjectCharConsumer<R> accumulator,
                               final BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);

        return evaluate(ReduceOps.makeChar(supplier, accumulator, (left, right) -> {
            combiner.accept(left, right);
            return left;
        }));
    }

    @Override
    public final boolean anyMatch(CharPredicate predicate) {
        return evaluate(MatchOps.makeChar(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(CharPredicate predicate) {
        return evaluate(MatchOps.makeChar(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(CharPredicate predicate) {
        return evaluate(MatchOps.makeChar(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalChar findFirst() {
        return evaluate(FindOps.makeChar(true));
    }

    @Override
    public final OptionalChar findAny() {
        return evaluate(FindOps.makeChar(false));
    }

    @Override
    public final char[] toArray() {
        return Nodes.flattenChar((Node.OfChar) evaluateToArrayNode(Character[]::new)).asPrimitiveArray();
    }

    //

    /**
     * Source stage of an IntStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 1.8
     */
    public static class Head<E_IN> extends CharPipeline<E_IN> {
        /**
         * Constructor for the source stage of an IntStream.
         *
         * @param source {@code Supplier<Spliterator>} describing the stream
         *               source
         * @param sourceFlags the source flags for the stream source, described
         *                    in {@link StreamOpFlag}
         * @param parallel {@code true} if the pipeline is parallel
         */
        public Head(Supplier<? extends Spliterator<Character>> source, int sourceFlags, boolean parallel) {
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
        public Head(Spliterator<Character> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final Sink<E_IN> opWrapSink(int flags, Sink<Character> sink) {
            throw new UnsupportedOperationException();
        }

        // Optimized sequential terminal operations for the head of the pipeline

        @Override
        public void forEach(CharConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        @Override
        public void forEachOrdered(CharConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    /**
     * Base class for a stateless intermediate stage of an CharStream
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 16.0.1
     */
    public abstract static class StatelessOp<E_IN> extends CharPipeline<E_IN> {
        /**
         * Construct a new CharStream by appending a stateless intermediate
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
     * Base class for a stateful intermediate stage of an CharStream.
     *
     * @param <E_IN> type of elements in the upstream source
     * @since 16.0.1
     */
    public abstract static class StatefulOp<E_IN> extends CharPipeline<E_IN> {
        /**
         * Construct a new CharStream by appending a stateful intermediate
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
        public abstract <P_IN> Node<Character> opEvaluateParallel(PipelineHelper<Character> helper,
                                                                  Spliterator<P_IN> spliterator,
                                                                  IntFunction<Character[]> generator);
    }
}
