/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.github.svegon.utils.fuck_modifiers;

import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import it.unimi.dsi.fastutil.bytes.BytePredicate;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import it.unimi.dsi.fastutil.floats.FloatPredicate;
import it.unimi.dsi.fastutil.shorts.ShortPredicate;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.*;

/**
 * Factory for instances of a short-circuiting {@code TerminalOp} that implement
 * quantified predicate matching on the elements of a stream. Supported variants
 * include match-all, match-any, and match-none.
 *
 * @since 1.8
 */
public final class MatchOps {

    private MatchOps() { }

    /**
     * Enum describing quantified match options -- all match, any match, none
     * match.
     */
    public enum MatchKind {
        /** Do all elements match the predicate? */
        ANY(true, true),

        /** Do any elements match the predicate? */
        ALL(false, false),

        /** Do no elements match the predicate? */
        NONE(true, false);

        private final boolean stopOnPredicateMatches;
        private final boolean shortCircuitResult;

        MatchKind(boolean stopOnPredicateMatches,
                  boolean shortCircuitResult) {
            this.stopOnPredicateMatches = stopOnPredicateMatches;
            this.shortCircuitResult = shortCircuitResult;
        }
    }

    /**
     * Constructs a quantified predicate matcher for a Stream.
     *
     * @param <T> the type of stream elements
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static <T> TerminalOp<T, Boolean> makeRef(Predicate<? super T> predicate,
                                                                      MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<T> {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(T t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.REFERENCE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for an {@code BooleanStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Boolean, Boolean> makeBoolean(final BooleanPredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Boolean> implements Sink.OfBoolean {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(boolean t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.BYTE_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for an {@code ByteStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Byte, Boolean> makeByte(final BytePredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Byte> implements Sink.OfByte {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(byte t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.BYTE_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for an {@code IntStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Short, Boolean> makeShort(final ShortPredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Short> implements Sink.OfShort {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(short t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.SHORT_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for an {@code IntStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Integer, Boolean> makeInt(IntPredicate predicate,
                                                       MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Integer> implements Sink.OfInt {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(int t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.INT_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for a {@code LongStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Long, Boolean> makeLong(LongPredicate predicate,
                                                                      MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Long> implements Sink.OfLong {

            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(long t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.LONG_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for a {@code FloatStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Character, Boolean> makeChar(final CharPredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Character> implements Sink.OfChar {

            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(char t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.CHAR_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for a {@code FloatStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Float, Boolean> makeFloat(final FloatPredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Float> implements Sink.OfFloat {

            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(float t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.FLOAT_VALUE, matchKind, MatchSink::new);
    }

    /**
     * Constructs a quantified predicate matcher for a {@code DoubleStream}.
     *
     * @param predicate the {@code Predicate} to apply to stream elements
     * @param matchKind the kind of quantified match (all, any, none)
     * @return a {@code TerminalOp} implementing the desired quantified match
     *         criteria
     */
    public static TerminalOp<Double, Boolean> makeDouble(DoublePredicate predicate,
                                                         MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Double> implements Sink.OfDouble {

            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(double t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.DOUBLE_VALUE, matchKind, MatchSink::new);
    }

    /**
     * A short-circuiting {@code TerminalOp} that evaluates a predicate on the
     * elements of a stream and determines whether all, any or none of those
     * elements match the predicate.
     *
     * @param <T> the output type of the stream pipeline
     */
    private static final class MatchOp<T> implements TerminalOp<T, Boolean> {
        private final StreamShape inputShape;
        final MatchKind matchKind;
        final Supplier<BooleanTerminalSink<T>> sinkSupplier;

        /**
         * Constructs a {@code MatchOp}.
         *
         * @param shape the output shape of the stream pipeline
         * @param matchKind the kind of quantified match (all, any, none)
         * @param sinkSupplier {@code Supplier} for a {@code Sink} of the
         *        appropriate shape which implements the matching operation
         */
        MatchOp(StreamShape shape,
                MatchKind matchKind,
                Supplier<BooleanTerminalSink<T>> sinkSupplier) {
            this.inputShape = shape;
            this.matchKind = matchKind;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | StreamOpFlag.NOT_ORDERED;
        }

        @Override
        public StreamShape inputShape() {
            return inputShape;
        }

        @Override
        public <S> Boolean evaluateSequential(PipelineHelper<T> helper,
                                              Spliterator<S> spliterator) {
            return helper.wrapAndCopyInto(sinkSupplier.get(), spliterator).getAndClearState();
        }

        @Override
        public <S> Boolean evaluateParallel(PipelineHelper<T> helper,
                                            Spliterator<S> spliterator) {
            // Approach for parallel implementation:
            // - Decompose as per usual
            // - run match on leaf chunks, call result "b"
            // - if b == matchKind.shortCircuitOn, complete early and return b
            // - else if we complete normally, return !shortCircuitOn

            return new MatchTask<>(this, helper, spliterator).invoke();
        }
    }

    /**
     * Boolean specific terminal sink to avoid the boxing costs when returning
     * results.  Subclasses implement the shape-specific functionality.
     *
     * @param <T> The output type of the stream pipeline
     */
    private abstract static class BooleanTerminalSink<T> implements Sink<T> {
        boolean stop;
        boolean value;

        BooleanTerminalSink(MatchKind matchKind) {
            value = !matchKind.shortCircuitResult;
        }

        public boolean getAndClearState() {
            return value;
        }

        @Override
        public boolean cancellationRequested() {
            return stop;
        }
    }

    /**
     * ForkJoinTask implementation to implement a parallel short-circuiting
     * quantified match
     *
     * @param <P_IN> the type of source elements for the pipeline
     * @param <P_OUT> the type of output elements for the pipeline
     */
    @SuppressWarnings("serial")
    private static final class MatchTask<P_IN, P_OUT>
            extends AbstractShortCircuitTask<P_IN, P_OUT, Boolean, MatchTask<P_IN, P_OUT>> {
        private final MatchOp<P_OUT> op;

        /**
         * Constructor for root node
         */
        MatchTask(MatchOp<P_OUT> op, PipelineHelper<P_OUT> helper,
                  Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        /**
         * Constructor for non-root node
         */
        MatchTask(MatchTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected MatchTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new MatchTask<>(this, spliterator);
        }

        @Override
        protected Boolean doLeaf() {
            boolean b = helper.wrapAndCopyInto(op.sinkSupplier.get(), spliterator).getAndClearState();
            if (b == op.matchKind.shortCircuitResult)
                shortCircuit(b);
            return null;
        }

        @Override
        protected Boolean getEmptyResult() {
            return !op.matchKind.shortCircuitResult;
        }
    }
}

