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
package com.github.svegon.utils.fuck_modifiers;

import com.github.svegon.utils.collections.stream.*;
import com.github.svegon.utils.fast.util.booleans.BooleanPipeline;
import com.github.svegon.utils.fast.util.bytes.BytePipeline;
import com.github.svegon.utils.fast.util.chars.CharPipeline;
import com.github.svegon.utils.fast.util.floats.FloatPipeline;
import com.github.svegon.utils.fast.util.shorts.ShortPipeline;
import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import svegon.utils.collections.stream.*;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;


/**
 * Factory methods for transforming streams into sorted streams.
 *
 * @since 1.8
 */
public final class SortedOps {

    private SortedOps() { }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param <T> the type of both input and output elements
     * @param upstream a reference stream with element type T
     */
    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new OfRef<>(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param <T> the type of both input and output elements
     * @param upstream a reference stream with element type T
     * @param comparator the comparator to order elements by
     */
    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream,
                                 Comparator<? super T> comparator) {
        return new OfRef<>(upstream, comparator);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param upstream a reference stream with element type float
     */
    public static BooleanStream makeBoolean(AbstractPipeline<?, Boolean, ?> upstream) {
        return new OfBoolean(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param upstream a reference stream with element type float
     */
    public static ByteStream makeByte(AbstractPipeline<?, Byte, ?> upstream) {
        return new OfByte(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param upstream a reference stream with element type float
     */
    public static ShortStream makeShort(AbstractPipeline<?, Short, ?> upstream) {
        return new OfShort(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param <T> the type of both input and output elements
     * @param upstream a reference stream with element type T
     */
    public static <T> IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream) {
        return new OfInt(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param <T> the type of both input and output elements
     * @param upstream a reference stream with element type T
     */
    public static <T> LongStream makeLong(AbstractPipeline<?, Long, ?> upstream) {
        return new OfLong(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param upstream a reference stream with element type float
     */
    public static CharStream makeChar(AbstractPipeline<?, Character, ?> upstream) {
        return new OfChar(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param upstream a reference stream with element type float
     */
    public static FloatStream makeFloat(AbstractPipeline<?, Float, ?> upstream) {
        return new OfFloat(upstream);
    }

    /**
     * Appends a "sorted" operation to the provided stream.
     *
     * @param <T> the type of both input and output elements
     * @param upstream a reference stream with element type T
     */
    public static <T> DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream) {
        return new OfDouble(upstream);
    }

    /**
     * Specialized subtype for sorting reference streams
     */
    private static final class OfRef<T> extends ReferencePipeline.StatefulOp<T, T> {
        /**
         * Comparator used for sorting
         */
        private final boolean isNaturalSort;
        private final Comparator<? super T> comparator;

        /**
         * Sort using natural order of {@literal <T>} which must be
         * {@code Comparable}.
         */
        OfRef(AbstractPipeline<?, T, ?> upstream) {
            super(upstream, StreamShape.REFERENCE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
            this.isNaturalSort = true;
            // Will throw CCE when we try to sort if T is not Comparable
            @SuppressWarnings("unchecked")
            Comparator<? super T> comp = (Comparator<? super T>) Comparator.naturalOrder();
            this.comparator = comp;
        }

        /**
         * Sort using the provided comparator.
         *
         * @param comparator The comparator to be used to evaluate ordering.
         */
        OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
            super(upstream, StreamShape.REFERENCE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SORTED);
            this.isNaturalSort = false;
            this.comparator = Objects.requireNonNull(comparator);
        }

        @Override
        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);

            // If the input is already naturally sorted and this operation
            // also naturally sorted then this is a no-op
            if (StreamOpFlag.SORTED.isKnown(flags) && isNaturalSort)
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedRefSortingSink<>(sink, comparator);
            else
                return new RefSortingSink<>(sink, comparator);
        }

        @Override
        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper,
                                                                  Spliterator<P_IN> spliterator,
                                                                  IntFunction<T[]> generator) {
            // If the input is already naturally sorted and this operation
            // naturally sorts then collect the output
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && isNaturalSort) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                // @@@ Weak two-pass parallel implementation; parallel collect, parallel sort
                T[] flattenedData = helper.evaluate(spliterator, true, generator).asArray(generator);
                Arrays.parallelSort(flattenedData, comparator);
                return Nodes.node(flattenedData);
            }
        }
    }

    /**
     * Specialized subtype for sorting boolean streams.
     */
    private static final class OfBoolean extends BooleanPipeline.StatefulOp<Boolean> {
        public OfBoolean(AbstractPipeline<?, Boolean, ?> upstream) {
            super(upstream, StreamShape.BOOLEAN_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Boolean> opWrapSink(int flags, Sink<Boolean> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedBooleanSortingSink(sink);
            } else {
                return new BooleanSortingSink(sink);
            }
        }

        @Override
        public <P_IN> Node<Boolean> opEvaluateParallel(PipelineHelper<Boolean> helper, Spliterator<P_IN> spliterator,
                                                       IntFunction<Boolean[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            } else {
                Node.OfBoolean n = (Node.OfBoolean) helper.evaluate(spliterator, true, generator);

                boolean[] content = n.asPrimitiveArray();
                BooleanArrays.parallelQuickSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting float streams.
     */
    private static final class OfByte extends BytePipeline.StatefulOp<Byte> {
        public OfByte(AbstractPipeline<?, Byte, ?> upstream) {
            super(upstream, StreamShape.BYTE_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedByteSortingSink(sink);
            } else {
                return new ByteSortingSink(sink);
            }
        }

        @Override
        public <P_IN> Node<Byte> opEvaluateParallel(PipelineHelper<Byte> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     IntFunction<Byte[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfByte n = (Node.OfByte) helper.evaluate(spliterator, true, generator);

                byte[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting float streams.
     */
    private static final class OfShort extends ShortPipeline.StatefulOp<Short> {
        public OfShort(AbstractPipeline<?, Short, ?> upstream) {
            super(upstream, StreamShape.SHORT_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedShortSortingSink(sink);
            } else {
                return new ShortSortingSink(sink);
            }
        }

        @Override
        public <P_IN> Node<Short> opEvaluateParallel(PipelineHelper<Short> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     IntFunction<Short[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfShort n = (Node.OfShort) helper.evaluate(spliterator, true, generator);

                short[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting int streams.
     */
    private static final class OfInt extends IntPipeline.StatefulOp<Integer> {
        OfInt(AbstractPipeline<?, Integer, ?> upstream) {
            super(upstream, StreamShape.INT_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedIntSortingSink(sink);
            else
                return new IntSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper,
                                                                        Spliterator<P_IN> spliterator,
                                                                        IntFunction<Integer[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfInt n = (Node.OfInt) helper.evaluate(spliterator, true, generator);

                int[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting long streams.
     */
    private static final class OfLong extends LongPipeline.StatefulOp<Long> {
        OfLong(AbstractPipeline<?, Long, ?> upstream) {
            super(upstream, StreamShape.LONG_VALUE,
                  StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedLongSortingSink(sink);
            else
                return new LongSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper,
                                                                     Spliterator<P_IN> spliterator,
                                                                     IntFunction<Long[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfLong n = (Node.OfLong) helper.evaluate(spliterator, true, generator);

                long[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting float streams.
     */
    private static final class OfChar extends CharPipeline.StatefulOp<Character> {
        public OfChar(AbstractPipeline<?, Character, ?> upstream) {
            super(upstream, StreamShape.CHAR_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedCharSortingSink(sink);
            } else {
                return new CharSortingSink(sink);
            }
        }

        @Override
        public <P_IN> Node<Character> opEvaluateParallel(PipelineHelper<Character> helper,
                                                         Spliterator<P_IN> spliterator,
                                                         IntFunction<Character[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            } else {
                Node.OfChar n = (Node.OfChar) helper.evaluate(spliterator, true, generator);

                char[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting float streams.
     */
    private static final class OfFloat extends FloatPipeline.StatefulOp<Float> {
        public OfFloat(AbstractPipeline<?, Float, ?> upstream) {
            super(upstream, StreamShape.FLOAT_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedFloatSortingSink(sink);
            } else {
                return new FloatSortingSink(sink);
            }
        }

        @Override
        public <P_IN> Node<Float> opEvaluateParallel(PipelineHelper<Float> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     IntFunction<Float[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfFloat n = (Node.OfFloat) helper.evaluate(spliterator, true, generator);

                float[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Specialized subtype for sorting double streams.
     */
    private static final class OfDouble extends DoublePipeline.StatefulOp<Double> {
        OfDouble(AbstractPipeline<?, Double, ?> upstream) {
            super(upstream, StreamShape.DOUBLE_VALUE,
                    StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags))
                return sink;
            else if (StreamOpFlag.SIZED.isKnown(flags))
                return new SizedDoubleSortingSink(sink);
            else
                return new DoubleSortingSink(sink);
        }

        @Override
        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                                      Spliterator<P_IN> spliterator,
                                                      IntFunction<Double[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            else {
                Node.OfDouble n = (Node.OfDouble) helper.evaluate(spliterator, true, generator);

                double[] content = n.asPrimitiveArray();
                Arrays.parallelSort(content);

                return Nodes.node(content);
            }
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on reference streams.
     *
     * <p>
     * Note: documentation below applies to reference and all fast sinks.
     * <p>
     * Sorting sinks first accept all elements, buffering then into an array
     * or a re-sizable data structure, if the size of the pipeline is known or
     * unknown respectively.  At the end of the sink protocol those elements are
     * sorted and then pushed downstream.
     * This class records if {@link #cancellationRequested} is called.  If so it
     * can be inferred that the source pushing source elements into the pipeline
     * knows that the pipeline is short-circuiting.  In such cases sub-classes
     * pushing elements downstream will preserve the short-circuiting protocol
     * by calling {@code downstream.cancellationRequested()} and checking the
     * result is {@code false} before an element is pushed.
     * <p>
     * Note that the above behaviour is an optimization for sorting with
     * sequential streams.  It is not an error that more elements, than strictly
     * required to produce a result, may flow through the pipeline.  This can
     * occur, in general (not restricted to just sorting), for short-circuiting
     * parallel pipelines.
     */
    private abstract static class AbstractRefSortingSink<T> extends Sink.ChainedReference<T, T> {
        protected final Comparator<? super T> comparator;
        // @@@ could be a lazy final value, if/when support is added
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
            super(downstream);
            this.comparator = comparator;
        }

        /**
         * Records is cancellation is requested so short-circuiting behaviour
         * can be preserved when the sorted elements are pushed downstream.
         *
         * @return false, as this sink never short-circuits.
         */
        @Override
        public final boolean cancellationRequested() {
            // If this method is called then an operation within the stream
            // pipeline is short-circuiting (see AbstractPipeline.copyInto).
            // Note that we cannot differentiate between an upstream or
            // downstream operation
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED reference streams.
     */
    private static final class SizedRefSortingSink<T> extends AbstractRefSortingSink<T> {
        private T[] array;
        private int offset;

        SizedRefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = (T[]) new Object[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset, comparator);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(T t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on reference streams.
     */
    private static final class RefSortingSink<T> extends AbstractRefSortingSink<T> {
        private ArrayList<T> list;

        RefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            list = (size >= 0) ? new ArrayList<>((int) size) : new ArrayList<>();
        }

        @Override
        public void end() {
            list.sort(comparator);
            downstream.begin(list.size());
            if (!cancellationRequestedCalled) {
                list.forEach(downstream::accept);
            }
            else {
                for (T t : list) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(t);
                }
            }
            downstream.end();
            list = null;
        }

        @Override
        public void accept(T t) {
            list.add(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on boolean streams.
     */
    private abstract static class AbstractBooleanSortingSink extends Sink.ChainedBoolean<Boolean> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        public AbstractBooleanSortingSink(Sink<? super Boolean> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED boolean streams.
     */
    private static final class SizedBooleanSortingSink extends AbstractBooleanSortingSink {
        private boolean[] array;
        private int offset;

        public SizedBooleanSortingSink(Sink<? super Boolean> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            array = new boolean[(int) size];
        }

        @Override
        public void end() {
            BooleanArrays.quickSort(array, 0, offset);
            downstream.begin(offset);

            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++) {
                    downstream.accept(array[i]);
                }
            } else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++) {
                    downstream.accept(array[i]);
                }
            }

            downstream.end();
            array = null;
        }

        @Override
        public void accept(boolean t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on double streams.
     */
    private static final class BooleanSortingSink extends AbstractBooleanSortingSink {
        private SpinedBuffer.OfBoolean b;

        private BooleanSortingSink(Sink<? super Boolean> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            b = (size > 0) ? new SpinedBuffer.OfBoolean((int) size) : new SpinedBuffer.OfBoolean();
        }

        @Override
        public void end() {
            boolean[] booleans = b.asPrimitiveArray();
            BooleanArrays.quickSort(booleans);
            downstream.begin(booleans.length);

            if (!cancellationRequestedCalled) {
                for (boolean aBoolean : booleans)
                    downstream.accept(aBoolean);
            }
            else {
                for (boolean aBoolean : booleans) {
                    if (downstream.cancellationRequested()) {
                        break;
                    }

                    downstream.accept(aBoolean);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(boolean t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on bytes streams.
     */
    private abstract static class AbstractByteSortingSink extends Sink.ChainedByte<Byte> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        public AbstractByteSortingSink(Sink<? super Byte> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED double streams.
     */
    private static final class SizedByteSortingSink extends AbstractByteSortingSink {
        private byte[] array;
        private int offset;

        public SizedByteSortingSink(Sink<? super Byte> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            array = new byte[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);

            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++) {
                    downstream.accept(array[i]);
                }
            } else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++) {
                    downstream.accept(array[i]);
                }
            }

            downstream.end();
            array = null;
        }

        @Override
        public void accept(byte t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on double streams.
     */
    private static final class ByteSortingSink extends AbstractByteSortingSink {
        private SpinedBuffer.OfByte b;

        ByteSortingSink(Sink<? super Byte> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            b = (size > 0) ? new SpinedBuffer.OfByte((int) size) : new SpinedBuffer.OfByte();
        }

        @Override
        public void end() {
            byte[] bytes = b.asPrimitiveArray();
            Arrays.sort(bytes);
            downstream.begin(bytes.length);

            if (!cancellationRequestedCalled) {
                for (float aByte : bytes) {
                    downstream.accept(aByte);
                }
            } else {
                for (float aDouble : bytes) {
                    if (downstream.cancellationRequested()) {
                        break;
                    }

                    downstream.accept(aDouble);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(byte t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on short streams.
     */
    private abstract static class AbstractShortSortingSink extends Sink.ChainedShort<Short> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        public AbstractShortSortingSink(Sink<? super Short> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED short streams.
     */
    private static final class SizedShortSortingSink extends AbstractShortSortingSink {
        private short[] array;
        private int offset;

        public SizedShortSortingSink(Sink<? super Short> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            array = new short[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);

            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++) {
                    downstream.accept(array[i]);
                }
            } else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++) {
                    downstream.accept(array[i]);
                }
            }

            downstream.end();
            array = null;
        }

        @Override
        public void accept(short t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on double streams.
     */
    private static final class ShortSortingSink extends AbstractShortSortingSink {
        private SpinedBuffer.OfShort b;

        ShortSortingSink(Sink<? super Short> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            b = (size > 0) ? new SpinedBuffer.OfShort((int) size) : new SpinedBuffer.OfShort();
        }

        @Override
        public void end() {
            short[] shorts = b.asPrimitiveArray();
            Arrays.sort(shorts);
            downstream.begin(shorts.length);

            if (!cancellationRequestedCalled) {
                for (float aDouble : shorts) {
                    downstream.accept(aDouble);
                }
            } else {
                for (float aDouble : shorts) {
                    if (downstream.cancellationRequested()) {
                        break;
                    }

                    downstream.accept(aDouble);
                }
            }

            downstream.end();
        }

        @Override
        public void accept(short t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on int streams.
     */
    private abstract static class AbstractIntSortingSink extends Sink.ChainedInt<Integer> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED int streams.
     */
    private static final class SizedIntSortingSink extends AbstractIntSortingSink {
        private int[] array;
        private int offset;

        SizedIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new int[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(int t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on int streams.
     */
    private static final class IntSortingSink extends AbstractIntSortingSink {
        private SpinedBuffer.OfInt b;

        IntSortingSink(Sink<? super Integer> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfInt((int) size) : new SpinedBuffer.OfInt();
        }

        @Override
        public void end() {
            int[] ints = b.asPrimitiveArray();
            Arrays.sort(ints);
            downstream.begin(ints.length);
            if (!cancellationRequestedCalled) {
                for (int anInt : ints)
                    downstream.accept(anInt);
            }
            else {
                for (int anInt : ints) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(anInt);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(int t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on long streams.
     */
    private abstract static class AbstractLongSortingSink extends Sink.ChainedLong<Long> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED long streams.
     */
    private static final class SizedLongSortingSink extends AbstractLongSortingSink {
        private long[] array;
        private int offset;

        SizedLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new long[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(long t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on long streams.
     */
    private static final class LongSortingSink extends AbstractLongSortingSink {
        private SpinedBuffer.OfLong b;

        LongSortingSink(Sink<? super Long> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfLong((int) size) : new SpinedBuffer.OfLong();
        }

        @Override
        public void end() {
            long[] longs = b.asPrimitiveArray();
            Arrays.sort(longs);
            downstream.begin(longs.length);
            if (!cancellationRequestedCalled) {
                for (long aLong : longs)
                    downstream.accept(aLong);
            }
            else {
                for (long aLong : longs) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aLong);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(long t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on long streams.
     */
    private abstract static class AbstractCharSortingSink extends Sink.ChainedChar<Character> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        public AbstractCharSortingSink(Sink<? super Character> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED double streams.
     */
    private static final class SizedCharSortingSink extends AbstractCharSortingSink {
        private char[] array;
        private int offset;

        public SizedCharSortingSink(Sink<? super Character> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            array = new char[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);

            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++) {
                    downstream.accept(array[i]);
                }
            } else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++) {
                    downstream.accept(array[i]);
                }
            }

            downstream.end();
            array = null;
        }

        @Override
        public void accept(char t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on double streams.
     */
    private static final class CharSortingSink extends AbstractCharSortingSink {
        private SpinedBuffer.OfChar b;

        public CharSortingSink(Sink<? super Character> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            b = (size > 0) ? new SpinedBuffer.OfChar((int) size) : new SpinedBuffer.OfChar();
        }

        @Override
        public void end() {
            char[] chars = b.asPrimitiveArray();
            Arrays.sort(chars);
            downstream.begin(chars.length);

            if (!cancellationRequestedCalled) {
                for (char aChar : chars)
                    downstream.accept(aChar);
            } else {
                for (char aChar : chars) {
                    if (downstream.cancellationRequested()) {
                        break;
                    }

                    downstream.accept(aChar);
                }
            }

            downstream.end();
        }

        @Override
        public void accept(char t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on long streams.
     */
    private abstract static class AbstractFloatSortingSink extends Sink.ChainedFloat<Float> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        public AbstractFloatSortingSink(Sink<? super Float> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED double streams.
     */
    private static final class SizedFloatSortingSink extends AbstractFloatSortingSink {
        private float[] array;
        private int offset;

        public SizedFloatSortingSink(Sink<? super Float> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new float[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(float t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on double streams.
     */
    private static final class FloatSortingSink extends AbstractFloatSortingSink {
        private SpinedBuffer.OfFloat b;

        FloatSortingSink(Sink<? super Float> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfFloat((int) size) : new SpinedBuffer.OfFloat();
        }

        @Override
        public void end() {
            float[] floats = b.asPrimitiveArray();
            Arrays.sort(floats);
            downstream.begin(floats.length);
            if (!cancellationRequestedCalled) {
                for (float aDouble : floats)
                    downstream.accept(aDouble);
            }
            else {
                for (float aDouble : floats) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aDouble);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(float t) {
            b.accept(t);
        }
    }

    /**
     * Abstract {@link Sink} for implementing sort on long streams.
     */
    private abstract static class AbstractDoubleSortingSink extends Sink.ChainedDouble<Double> {
        // true if cancellationRequested() has been called
        protected boolean cancellationRequestedCalled;

        AbstractDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            cancellationRequestedCalled = true;
            return false;
        }
    }

    /**
     * {@link Sink} for implementing sort on SIZED double streams.
     */
    private static final class SizedDoubleSortingSink extends AbstractDoubleSortingSink {
        private double[] array;
        private int offset;

        public SizedDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            array = new double[(int) size];
        }

        @Override
        public void end() {
            Arrays.sort(array, 0, offset);
            downstream.begin(offset);
            if (!cancellationRequestedCalled) {
                for (int i = 0; i < offset; i++)
                    downstream.accept(array[i]);
            }
            else {
                for (int i = 0; i < offset && !downstream.cancellationRequested(); i++)
                    downstream.accept(array[i]);
            }
            downstream.end();
            array = null;
        }

        @Override
        public void accept(double t) {
            array[offset++] = t;
        }
    }

    /**
     * {@link Sink} for implementing sort on double streams.
     */
    private static final class DoubleSortingSink extends AbstractDoubleSortingSink {
        private SpinedBuffer.OfDouble b;

        DoubleSortingSink(Sink<? super Double> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            b = (size > 0) ? new SpinedBuffer.OfDouble((int) size) : new SpinedBuffer.OfDouble();
        }

        @Override
        public void end() {
            double[] doubles = b.asPrimitiveArray();
            Arrays.sort(doubles);
            downstream.begin(doubles.length);
            if (!cancellationRequestedCalled) {
                for (double aDouble : doubles)
                    downstream.accept(aDouble);
            }
            else {
                for (double aDouble : doubles) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(aDouble);
                }
            }
            downstream.end();
        }

        @Override
        public void accept(double t) {
            b.accept(t);
        }
    }
}
