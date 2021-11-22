/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
package svegon.utils.fuck_modifiers;

import it.unimi.dsi.fastutil.booleans.BooleanBinaryOperator;
import it.unimi.dsi.fastutil.bytes.ByteBinaryOperator;
import it.unimi.dsi.fastutil.chars.CharBinaryOperator;
import it.unimi.dsi.fastutil.floats.FloatBinaryOperator;
import it.unimi.dsi.fastutil.shorts.ShortBinaryOperator;
import svegon.utils.interfaces.function.*;
import svegon.utils.optional.*;

import java.util.*;
import java.util.concurrent.CountedCompleter;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Factory for creating instances of {@code TerminalOp} that implement
 * reductions.
 *
 * @since 1.8
 */
public final class ReduceOps {

    private ReduceOps() { }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * reference values.
     *
     * @param <T> the type of the input elements
     * @param <U> the type of the result
     * @param seed the identity element for the reduction
     * @param reducer the accumulating function that incorporates an additional
     *        input element into the result
     * @param combiner the combining function that combines two intermediate
     *        results
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <T, U> TerminalOp<T, U>
    makeRef(U seed, BiFunction<U, ? super T, U> reducer, BinaryOperator<U> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<U> implements AccumulatingSink<T, U, ReducingSink> {
            @Override
            public void begin(long size) {
                state = seed;
            }

            @Override
            public void accept(T t) {
                state = reducer.apply(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<T, U, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * reference values producing an optional reference result.
     *
     * @param <T> The type of the input elements, and the type of the result
     * @param operator The reducing function
     * @return A {@code TerminalOp} implementing the reduction
     */
    public static <T> TerminalOp<T, Optional<T>>
    makeRef(BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<T, Optional<T>, ReducingSink> {
            private boolean empty;
            private T state;

            public void begin(long size) {
                empty = true;
                state = null;
            }

            @Override
            public void accept(T t) {
                if (empty) {
                    empty = false;
                    state = t;
                } else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public Optional<T> get() {
                return empty ? Optional.empty() : Optional.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<T, Optional<T>, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * reference values.
     *
     * @param <T> the type of the input elements
     * @param <I> the type of the intermediate reduction result
     * @param collector a {@code Collector} defining the reduction
     * @return a {@code ReduceOp} implementing the reduction
     */
    public static <T, I> TerminalOp<T, I> makeRef(Collector<? super T, I, ?> collector) {
        Supplier<I> supplier = Objects.requireNonNull(collector).supplier();
        BiConsumer<I, ? super T> accumulator = collector.accumulator();
        BinaryOperator<I> combiner = collector.combiner();
        class ReducingSink extends Box<I>
                implements AccumulatingSink<T, I, ReducingSink> {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(T t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<T, I, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }

            @Override
            public int getOpFlags() {
                return collector.characteristics().contains(Collector.Characteristics.UNORDERED)
                       ? StreamOpFlag.NOT_ORDERED
                       : 0;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * reference values.
     *
     * @param <T> the type of the input elements
     * @param <R> the type of the result
     * @param seedFactory a factory to produce a new base accumulator
     * @param accumulator a function to incorporate an element into an
     *        accumulator
     * @param reducer a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <T, R> TerminalOp<T, R>
    makeRef(Supplier<R> seedFactory,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R,R> reducer) {
        Objects.requireNonNull(seedFactory);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(reducer);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<T, R, ReducingSink> {
            @Override
            public void begin(long size) {
                state = seedFactory.get();
            }

            @Override
            public void accept(T t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                reducer.accept(state, other.state);
            }
        }
        return new ReduceOp<T, R, ReducingSink>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @param <T> the type of the input elements
     * @return a {@code TerminalOp} implementing the counting
     */
    public static <T> TerminalOp<T, Long>
    makeRefCounting() {
        return new ReduceOp<T, Long, CountingSink<T>>(StreamShape.REFERENCE) {
            @Override
            public CountingSink<T> makeSink() { return new CountingSink.OfRef<>(); }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<T> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<T> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code Boolean} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Boolean, Boolean> makeBoolean(Boolean identity, BooleanBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Boolean, Boolean, ReducingSink>, Sink.OfBoolean {
            private boolean state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(boolean t) {
                state = operator.apply(state, t);
            }

            @Override
            public Boolean get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Boolean, Boolean, ReducingSink>(StreamShape.BOOLEAN_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Boolean, OptionalBoolean> makeBoolean(BooleanBinaryOperator operator) {
        Objects.requireNonNull(operator);

        class ReducingSink implements AccumulatingSink<Boolean, OptionalBoolean, ReducingSink>, Sink.OfBoolean {
            private boolean empty;
            private boolean state;

            public void begin(long size) {
                empty = true;
                state = false;
            }

            @Override
            public void accept(boolean t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public OptionalBoolean get() {
                return empty ? OptionalBoolean.empty() : OptionalBoolean.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }
        }
        return new ReduceOp<Boolean, OptionalBoolean, ReducingSink>(StreamShape.BOOLEAN_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Boolean, R> makeBoolean(Supplier<R> supplier, ObjectBooleanConsumer<R> accumulator,
                                                     BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);

        class ReducingSink extends Box<R> implements AccumulatingSink<Boolean, R, ReducingSink>, Sink.OfBoolean {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(boolean t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }

        return new ReduceOp<Boolean, R, ReducingSink>(StreamShape.BOOLEAN_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Boolean, Long> makeBooleanCounting() {
        return new ReduceOp<Boolean, Long, CountingSink<Boolean>>(StreamShape.BOOLEAN_VALUE) {
            @Override
            public CountingSink<Boolean> makeSink() {
                return new CountingSink.OfBoolean();
            }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Boolean> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Boolean> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code Byte} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Byte, Byte> makeByte(byte identity, ByteBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Byte, Byte, ReducingSink>, Sink.OfByte {
            private byte state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(byte t) {
                state = operator.apply(state, t);
            }

            @Override
            public Byte get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Byte, Byte, ReducingSink>(StreamShape.BYTE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Byte, OptionalByte> makeByte(ByteBinaryOperator operator) {
        Objects.requireNonNull(operator);

        class ReducingSink implements AccumulatingSink<Byte, OptionalByte, ReducingSink>, Sink.OfByte {
            private boolean empty;
            private byte state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(byte t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public OptionalByte get() {
                return empty ? OptionalByte.empty() : OptionalByte.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }
        }
        return new ReduceOp<Byte, OptionalByte, ReducingSink>(StreamShape.BYTE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Byte, R> makeByte(Supplier<R> supplier, ObjectByteConsumer<R> accumulator,
                                                         BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);

        class ReducingSink extends Box<R> implements AccumulatingSink<Byte, R, ReducingSink>, Sink.OfByte {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(byte t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }

        return new ReduceOp<Byte, R, ReducingSink>(StreamShape.BYTE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Byte, Long> makeByteCounting() {
        return new ReduceOp<Byte, Long, CountingSink<Byte>>(StreamShape.BYTE_VALUE) {
            @Override
            public CountingSink<Byte> makeSink() {
                return new CountingSink.OfByte();
            }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Byte> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Byte> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code Short} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Short, Short> makeShort(short identity, ShortBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Short, Short, ReducingSink>, Sink.OfShort {
            private short state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(short t) {
                state = operator.apply(state, t);
            }

            @Override
            public Short get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Short, Short, ReducingSink>(StreamShape.SHORT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Short, OptionalShort> makeShort(final ShortBinaryOperator operator) {
        Objects.requireNonNull(operator);

        class ReducingSink implements AccumulatingSink<Short, OptionalShort, ReducingSink>, Sink.OfShort {
            private boolean empty;
            private short state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(short t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public OptionalShort get() {
                return empty ? OptionalShort.empty() : OptionalShort.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }
        }
        return new ReduceOp<Short, OptionalShort, ReducingSink>(StreamShape.SHORT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Short, R> makeShort(Supplier<R> supplier, ObjectShortConsumer<R> accumulator,
                                                   BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);

        class ReducingSink extends Box<R> implements AccumulatingSink<Short, R, ReducingSink>, Sink.OfShort {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(short t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }

        return new ReduceOp<Short, R, ReducingSink>(StreamShape.SHORT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Short, Long> makeShortCounting() {
        return new ReduceOp<Short, Long, CountingSink<Short>>(StreamShape.SHORT_VALUE) {
            @Override
            public CountingSink<Short> makeSink() {
                return new CountingSink.OfShort();
            }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Short> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Short> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code int} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Integer, Integer>
    makeInt(int identity, IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Integer, Integer, ReducingSink>, Sink.OfInt {
            private int state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(int t) {
                state = operator.applyAsInt(state, t);
            }

            @Override
            public Integer get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Integer, Integer, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code int} values, producing an optional integer result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Integer, OptionalInt>
    makeInt(IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Integer, OptionalInt, ReducingSink>, Sink.OfInt {
            private boolean empty;
            private int state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(int t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsInt(state, t);
                }
            }

            @Override
            public OptionalInt get() {
                return empty ? OptionalInt.empty() : OptionalInt.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Integer, OptionalInt, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code int} values.
     *
     * @param <R> The type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return A {@code ReduceOp} implementing the reduction
     */
    public static <R> TerminalOp<Integer, R>
    makeInt(Supplier<R> supplier,
            ObjIntConsumer<R> accumulator,
            BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Integer, R, ReducingSink>, Sink.OfInt {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(int t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Integer, R, ReducingSink>(StreamShape.INT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Integer, Long>
    makeIntCounting() {
        return new ReduceOp<Integer, Long, CountingSink<Integer>>(StreamShape.INT_VALUE) {
            @Override
            public CountingSink<Integer> makeSink() { return new CountingSink.OfInt(); }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Integer> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Integer> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code long} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Long, Long>
    makeLong(long identity, LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Long, Long, ReducingSink>, Sink.OfLong {
            private long state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(long t) {
                state = operator.applyAsLong(state, t);
            }

            @Override
            public Long get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Long, Long, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code long} values, producing an optional long result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Long, OptionalLong>
    makeLong(LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Long, OptionalLong, ReducingSink>, Sink.OfLong {
            private boolean empty;
            private long state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(long t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsLong(state, t);
                }
            }

            @Override
            public OptionalLong get() {
                return empty ? OptionalLong.empty() : OptionalLong.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Long, OptionalLong, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code long} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Long, R>
    makeLong(Supplier<R> supplier,
             ObjLongConsumer<R> accumulator,
             BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Long, R, ReducingSink>, Sink.OfLong {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(long t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Long, R, ReducingSink>(StreamShape.LONG_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Long, Long>
    makeLongCounting() {
        return new ReduceOp<Long, Long, CountingSink<Long>>(StreamShape.LONG_VALUE) {
            @Override
            public CountingSink<Long> makeSink() { return new CountingSink.OfLong(); }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Long> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Long> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code Char} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Character, Character> makeChar(char identity, CharBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Character, Character, ReducingSink>, Sink.OfChar {
            private char state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(char t) {
                state = operator.apply(state, t);
            }

            @Override
            public Character get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Character, Character, ReducingSink>(StreamShape.CHAR_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Character, OptionalChar> makeChar(CharBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Character, OptionalChar, ReducingSink>, Sink.OfChar {
            private boolean empty;
            private char state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(char t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public OptionalChar get() {
                return empty ? OptionalChar.empty() : OptionalChar.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Character, OptionalChar, ReducingSink>(StreamShape.CHAR_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Character, R> makeChar(Supplier<R> supplier, ObjectCharConsumer<R> accumulator,
                                                     BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);

        class ReducingSink extends Box<R> implements AccumulatingSink<Character, R, ReducingSink>, Sink.OfChar {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(char t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }

        return new ReduceOp<Character, R, ReducingSink>(StreamShape.CHAR_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Character, Long> makeCharCounting() {
        return new ReduceOp<Character, Long, CountingSink<Character>>(StreamShape.CHAR_VALUE) {
            @Override
            public CountingSink<Character> makeSink() {
                return new CountingSink.OfChar();
            }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Character> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Character> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
                    return spliterator.getExactSizeIfKnown();
                }

                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code float} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Float, Float> makeFloat(float identity, FloatBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Float, Float, ReducingSink>, Sink.OfFloat {
            private float state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(float t) {
                state = operator.apply(state, t);
            }

            @Override
            public Float get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Float, Float, ReducingSink>(StreamShape.FLOAT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Float, OptionalFloat> makeFloat(FloatBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink implements AccumulatingSink<Float, OptionalFloat, ReducingSink>, Sink.OfFloat {
            private boolean empty;
            private float state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(float t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.apply(state, t);
                }
            }

            @Override
            public OptionalFloat get() {
                return empty ? OptionalFloat.empty() : OptionalFloat.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Float, OptionalFloat, ReducingSink>(StreamShape.FLOAT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Float, R> makeFloat(Supplier<R> supplier, ObjectFloatConsumer<R> accumulator,
                                                     BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);

        class ReducingSink extends Box<R> implements AccumulatingSink<Float, R, ReducingSink>, Sink.OfFloat {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(float t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }

        return new ReduceOp<Float, R, ReducingSink>(StreamShape.FLOAT_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Float, Long> makeFloatCounting() {
        return new ReduceOp<Float, Long, CountingSink<Float>>(StreamShape.FLOAT_VALUE) {
            @Override
            public CountingSink<Float> makeSink() { return new CountingSink.OfFloat(); }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Float> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Float> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values.
     *
     * @param identity the identity for the combining function
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Double, Double>
    makeDouble(double identity, DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Double, Double, ReducingSink>, Sink.OfDouble {
            private double state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(double t) {
                state = operator.applyAsDouble(state, t);
            }

            @Override
            public Double get() {
                return state;
            }

            @Override
            public void combine(ReducingSink other) {
                accept(other.state);
            }
        }
        return new ReduceOp<Double, Double, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a functional reduce on
     * {@code double} values, producing an optional double result.
     *
     * @param operator the combining function
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static TerminalOp<Double, OptionalDouble>
    makeDouble(DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        class ReducingSink
                implements AccumulatingSink<Double, OptionalDouble, ReducingSink>, Sink.OfDouble {
            private boolean empty;
            private double state;

            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(double t) {
                if (empty) {
                    empty = false;
                    state = t;
                }
                else {
                    state = operator.applyAsDouble(state, t);
                }
            }

            @Override
            public OptionalDouble get() {
                return empty ? OptionalDouble.empty() : OptionalDouble.of(state);
            }

            @Override
            public void combine(ReducingSink other) {
                if (!other.empty)
                    accept(other.state);
            }
        }
        return new ReduceOp<Double, OptionalDouble, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that implements a mutable reduce on
     * {@code double} values.
     *
     * @param <R> the type of the result
     * @param supplier a factory to produce a new accumulator of the result type
     * @param accumulator a function to incorporate an int into an
     *        accumulator
     * @param combiner a function to combine an accumulator into another
     * @return a {@code TerminalOp} implementing the reduction
     */
    public static <R> TerminalOp<Double, R>
    makeDouble(Supplier<R> supplier,
               ObjDoubleConsumer<R> accumulator,
               BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        class ReducingSink extends Box<R>
                implements AccumulatingSink<Double, R, ReducingSink>, Sink.OfDouble {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(double t) {
                accumulator.accept(state, t);
            }

            @Override
            public void combine(ReducingSink other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Double, R, ReducingSink>(StreamShape.DOUBLE_VALUE) {
            @Override
            public ReducingSink makeSink() {
                return new ReducingSink();
            }
        };
    }

    /**
     * Constructs a {@code TerminalOp} that counts the number of stream
     * elements.  If the size of the pipeline is known then count is the size
     * and there is no need to evaluate the pipeline.  If the size of the
     * pipeline is non known then count is produced, via reduction, using a
     * {@link CountingSink}.
     *
     * @return a {@code TerminalOp} implementing the counting
     */
    public static TerminalOp<Double, Long> makeDoubleCounting() {
        return new ReduceOp<Double, Long, CountingSink<Double>>(StreamShape.DOUBLE_VALUE) {
            @Override
            public CountingSink<Double> makeSink() { return new CountingSink.OfDouble(); }

            @Override
            public <P_IN> Long evaluateSequential(PipelineHelper<Double> helper,
                                                  Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateSequential(helper, spliterator);
            }

            @Override
            public <P_IN> Long evaluateParallel(PipelineHelper<Double> helper,
                                                Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags()))
                    return spliterator.getExactSizeIfKnown();
                return super.evaluateParallel(helper, spliterator);
            }

            @Override
            public int getOpFlags() {
                return StreamOpFlag.NOT_ORDERED;
            }
        };
    }

    /**
     * A sink that counts elements
     */
    public abstract static class CountingSink<T>
            extends Box<Long>
            implements AccumulatingSink<T, Long, CountingSink<T>> {
        public long count;

        @Override
        public void begin(long size) {
            count = 0L;
        }

        @Override
        public Long get() {
            return count;
        }

        @Override
        public void combine(CountingSink<T> other) {
            count += other.count;
        }

        public static final class OfRef<T> extends CountingSink<T> {
            @Override
            public void accept(T t) {
                count++;
            }
        }

        public static final class OfBoolean extends CountingSink<Boolean> implements Sink.OfBoolean {
            @Override
            public void accept(boolean t) {
                count++;
            }
        }

        public static final class OfByte extends CountingSink<Byte> implements Sink.OfByte {
            @Override
            public void accept(byte t) {
                count++;
            }
        }

        public static final class OfShort extends CountingSink<Short> implements Sink.OfShort {
            @Override
            public void accept(short t) {
                count++;
            }
        }

        public static final class OfInt extends CountingSink<Integer> implements Sink.OfInt {
            @Override
            public void accept(int t) {
                count++;
            }
        }

        public static final class OfLong extends CountingSink<Long> implements Sink.OfLong {
            @Override
            public void accept(long t) {
                count++;
            }
        }

        public static final class OfChar extends CountingSink<Character> implements Sink.OfChar {
            @Override
            public void accept(char t) {
                count++;
            }
        }

        public static final class OfFloat extends CountingSink<Float> implements Sink.OfFloat {
            @Override
            public void accept(float t) {
                count++;
            }
        }

        public static final class OfDouble extends CountingSink<Double> implements Sink.OfDouble {
            @Override
            public void accept(double t) {
                count++;
            }
        }
    }

    /**
     * A type of {@code TerminalSink} that implements an associative reducing
     * operation on elements of type {@code T} and producing a result of type
     * {@code R}.
     *
     * @param <T> the type of input element to the combining operation
     * @param <R> the result type
     * @param <K> the type of the {@code AccumulatingSink}.
     */
    private interface AccumulatingSink<T, R, K extends AccumulatingSink<T, R, K>> extends TerminalSink<T, R> {
        void combine(K other);
    }

    /**
     * State box for a single state element, used as a base class for
     * {@code AccumulatingSink} instances
     *
     * @param <U> The type of the state element
     */
    private abstract static class Box<U> {
        U state;

        Box() {} // Avoid creation of special accessor

        public U get() {
            return state;
        }
    }

    /**
     * A {@code TerminalOp} that evaluates a stream pipeline and sends the
     * output into an {@code AccumulatingSink}, which performs a reduce
     * operation. The {@code AccumulatingSink} must represent an associative
     * reducing operation.
     *
     * @param <T> the output type of the stream pipeline
     * @param <R> the result type of the reducing operation
     * @param <S> the type of the {@code AccumulatingSink}
     */
    private abstract static class ReduceOp<T, R, S extends AccumulatingSink<T, R, S>>
            implements TerminalOp<T, R> {
        private final StreamShape inputShape;

        /**
         * Create a {@code ReduceOp} of the specified stream shape which uses
         * the specified {@code Supplier} to create accumulating sinks.
         *
         * @param shape The shape of the stream pipeline
         */
        ReduceOp(StreamShape shape) {
            inputShape = shape;
        }

        public abstract S makeSink();

        @Override
        public StreamShape inputShape() {
            return inputShape;
        }

        @Override
        public <P_IN> R evaluateSequential(PipelineHelper<T> helper,
                                           Spliterator<P_IN> spliterator) {
            return helper.wrapAndCopyInto(makeSink(), spliterator).get();
        }

        @Override
        public <P_IN> R evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<P_IN> spliterator) {
            return new ReduceTask<>(this, helper, spliterator).invoke().get();
        }
    }

    /**
     * A {@code ForkJoinTask} for performing a parallel reduce operation.
     */
    @SuppressWarnings("serial")
    private static final class ReduceTask<P_IN, P_OUT, R,
                                          S extends AccumulatingSink<P_OUT, R, S>>
            extends AbstractTask<P_IN, P_OUT, S, ReduceTask<P_IN, P_OUT, R, S>> {
        private final ReduceOp<P_OUT, R, S> op;

        ReduceTask(ReduceOp<P_OUT, R, S> op,
                   PipelineHelper<P_OUT> helper,
                   Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        ReduceTask(ReduceTask<P_IN, P_OUT, R, S> parent,
                   Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected ReduceTask<P_IN, P_OUT, R, S> makeChild(Spliterator<P_IN> spliterator) {
            return new ReduceTask<>(this, spliterator);
        }

        @Override
        protected S doLeaf() {
            return helper.wrapAndCopyInto(op.makeSink(), spliterator);
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                S leftResult = leftChild.getLocalResult();
                leftResult.combine(rightChild.getLocalResult());
                setLocalResult(leftResult);
            }
            // GC spliterator, left and right child
            super.onCompletion(caller);
        }
    }
}
