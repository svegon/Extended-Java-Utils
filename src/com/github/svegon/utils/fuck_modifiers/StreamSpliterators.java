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

import com.github.svegon.utils.interfaces.function.CharSupplier;
import com.github.svegon.utils.interfaces.function.FloatSupplier;
import com.github.svegon.utils.interfaces.function.ShortSupplier;
import com.github.svegon.utils.interfaces.function.ByteSupplier;
import it.unimi.dsi.fastutil.booleans.BooleanComparator;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteComparator;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.chars.CharComparator;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.floats.FloatComparator;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortComparator;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Spliterator implementations for wrapping and delegating spliterators, used
 * in the implementation of the {@link Stream#spliterator()} method.
 *
 * @since 1.8
 */
public class StreamSpliterators {

    /**
     * Abstract wrapping spliterator that binds to the spliterator of a
     * pipeline helper on first operation.
     *
     * <p>This spliterator is not late-binding and will bind to the source
     * spliterator when first operated on.
     *
     * <p>A wrapping spliterator produced from a sequential stream
     * cannot be split if there are stateful operations present.
     */
    private abstract static class AbstractWrappingSpliterator<P_IN, P_OUT, T_BUFFER extends AbstractSpinedBuffer>
            implements Spliterator<P_OUT> {

        // @@@ Detect if stateful operations are present or not
        //     If not then can split otherwise cannot

        /**
         * True if this spliterator supports splitting
         */
        public final boolean isParallel;

        public final PipelineHelper<P_OUT> ph;

        /**
         * Supplier for the source spliterator.  Client provides either a
         * spliterator or a supplier.
         */
        private Supplier<Spliterator<P_IN>> spliteratorSupplier;

        /**
         * Source spliterator.  Either provided from client or obtained from
         * supplier.
         */
        public Spliterator<P_IN> spliterator;

        /**
         * Sink chain for the downstream stages of the pipeline, ultimately
         * leading to the buffer. Used during partial traversal.
         */
        public Sink<P_IN> bufferSink;

        /**
         * A function that advances one element of the spliterator, pushing
         * it to bufferSink.  Returns whether any elements were processed.
         * Used during partial traversal.
         */
        public BooleanSupplier pusher;

        /** Next element to consume from the buffer, used during partial traversal */
        public long nextToConsume;

        /** Buffer into which elements are pushed.  Used during partial traversal. */
        T_BUFFER buffer;

        /**
         * True if full traversal has occurred (with possible cancellation).
         * If doing a partial traversal, there may be still elements in buffer.
         */
        public boolean finished;

        /**
         * Construct an AbstractWrappingSpliterator from a
         * {@code Supplier<Spliterator>}.
         */
        public AbstractWrappingSpliterator(PipelineHelper<P_OUT> ph,
                                    Supplier<Spliterator<P_IN>> spliteratorSupplier,
                                    boolean parallel) {
            this.ph = ph;
            this.spliteratorSupplier = spliteratorSupplier;
            this.spliterator = null;
            this.isParallel = parallel;
        }

        /**
         * Construct an AbstractWrappingSpliterator from a
         * {@code Spliterator}.
         */
        public AbstractWrappingSpliterator(PipelineHelper<P_OUT> ph,
                                    Spliterator<P_IN> spliterator,
                                    boolean parallel) {
            this.ph = ph;
            this.spliteratorSupplier = null;
            this.spliterator = spliterator;
            this.isParallel = parallel;
        }

        /**
         * Called before advancing to set up spliterator, if needed.
         */
        public final void init() {
            if (spliterator == null) {
                spliterator = spliteratorSupplier.get();
                spliteratorSupplier = null;
            }
        }

        /**
         * Get an element from the source, pushing it into the sink chain,
         * setting up the buffer if needed
         * @return whether there are elements to consume from the buffer
         */
        public final boolean doAdvance() {
            if (buffer == null) {
                if (finished)
                    return false;

                init();
                initPartialTraversalState();
                nextToConsume = 0;
                bufferSink.begin(spliterator.getExactSizeIfKnown());
                return fillBuffer();
            }
            else {
                ++nextToConsume;
                boolean hasNext = nextToConsume < buffer.count();
                if (!hasNext) {
                    nextToConsume = 0;
                    buffer.clear();
                    hasNext = fillBuffer();
                }
                return hasNext;
            }
        }

        /**
         * Invokes the shape-specific constructor with the provided arguments
         * and returns the result.
         */
        public abstract AbstractWrappingSpliterator<P_IN, P_OUT, ?> wrap(Spliterator<P_IN> s);

        /**
         * Initializes buffer, sink chain, and pusher for a shape-specific
         * implementation.
         */
        public abstract void initPartialTraversalState();

        @Override
        public Spliterator<P_OUT> trySplit() {
            if (isParallel && buffer == null && !finished) {
                init();

                Spliterator<P_IN> split = spliterator.trySplit();
                return (split == null) ? null : wrap(split);
            }
            else
                return null;
        }

        /**
         * If the buffer is empty, push elements into the sink chain until
         * the source is empty or cancellation is requested.
         * @return whether there are elements to consume from the buffer
         */
        private boolean fillBuffer() {
            while (buffer.count() == 0) {
                if (bufferSink.cancellationRequested() || !pusher.getAsBoolean()) {
                    if (finished)
                        return false;
                    else {
                        bufferSink.end(); // might trigger more elements
                        finished = true;
                    }
                }
            }
            return true;
        }

        @Override
        public final long estimateSize() {
            init();
            // Use the estimate of the wrapped spliterator
            // Note this may not be accurate if there are filter/flatMap
            // operations filtering or adding elements to the stream
            return spliterator.estimateSize();
        }

        @Override
        public final long getExactSizeIfKnown() {
            init();
            return StreamOpFlag.SIZED.isKnown(ph.getStreamAndOpFlags())
                   ? spliterator.getExactSizeIfKnown()
                   : -1;
        }

        @Override
        public final int characteristics() {
            init();

            // Get the characteristics from the pipeline
            int c = StreamOpFlag.toCharacteristics(StreamOpFlag.toStreamFlags(ph.getStreamAndOpFlags()));

            // Mask off the size and uniform characteristics and replace with
            // those of the spliterator
            // Note that a non-uniform spliterator can change from something
            // with an exact size to an estimate for a sub-split, for example
            // with HashSet where the size is known at the top level spliterator
            // but for sub-splits only an estimate is known
            if ((c & Spliterator.SIZED) != 0) {
                c &= ~(Spliterator.SIZED | Spliterator.SUBSIZED);
                c |= (spliterator.characteristics() & (Spliterator.SIZED | Spliterator.SUBSIZED));
            }

            return c;
        }

        @Override
        public Comparator<? super P_OUT> getComparator() {
            if (!hasCharacteristics(SORTED))
                throw new IllegalStateException();
            return null;
        }

        @Override
        public final String toString() {
            return String.format("%s[%s]", getClass().getName(), spliterator);
        }
    }

    static final class WrappingSpliterator<P_IN, P_OUT>
            extends AbstractWrappingSpliterator<P_IN, P_OUT, SpinedBuffer<P_OUT>> {

        WrappingSpliterator(PipelineHelper<P_OUT> ph,
                            Supplier<Spliterator<P_IN>> supplier,
                            boolean parallel) {
            super(ph, supplier, parallel);
        }

        WrappingSpliterator(PipelineHelper<P_OUT> ph,
                            Spliterator<P_IN> spliterator,
                            boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public WrappingSpliterator<P_IN, P_OUT> wrap(Spliterator<P_IN> s) {
            return new WrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer<P_OUT> b = new SpinedBuffer<>();
            buffer = b;
            bufferSink = ph.wrapSink(b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public boolean tryAdvance(Consumer<? super P_OUT> consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(Consumer<? super P_OUT> consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink<P_OUT>) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    public static final class BooleanWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Boolean,
            SpinedBuffer.OfBoolean> implements BooleanSpliterator {

        public BooleanWrappingSpliterator(PipelineHelper<Boolean> ph, Supplier<Spliterator<P_IN>> supplier,
                                        boolean parallel) {
            super(ph, supplier, parallel);
        }

        public BooleanWrappingSpliterator(PipelineHelper<Boolean> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Boolean, ?> wrap(Spliterator<P_IN> s) {
            return new BooleanWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfBoolean b = new SpinedBuffer.OfBoolean();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfBoolean) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public BooleanSpliterator trySplit() {
            return (BooleanSpliterator) super.trySplit();
        }

        @Override
        public boolean tryAdvance(BooleanConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();

            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }

            return hasNext;
        }

        @Override
        public void forEachRemaining(BooleanConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfBoolean) consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer));
            }
        }

        @Override
        public BooleanComparator getComparator() {
            Comparator<? super Boolean> c = super.getComparator();
            return c == null || c instanceof BooleanComparator ? (BooleanComparator) c : c::compare;
        }
    }

    public static final class ByteWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Byte,
            SpinedBuffer.OfByte> implements ByteSpliterator {

        public ByteWrappingSpliterator(PipelineHelper<Byte> ph, Supplier<Spliterator<P_IN>> supplier,
                                        boolean parallel) {
            super(ph, supplier, parallel);
        }

        public ByteWrappingSpliterator(PipelineHelper<Byte> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Byte, ?> wrap(Spliterator<P_IN> s) {
            return new ByteWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfByte b = new SpinedBuffer.OfByte();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfByte) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public ByteSpliterator trySplit() {
            return (ByteSpliterator) super.trySplit();
        }

        @Override
        public boolean tryAdvance(ByteConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();

            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }

            return hasNext;
        }

        @Override
        public void forEachRemaining(ByteConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfByte) consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer));
            }
        }

        @Override
        public ByteComparator getComparator() {
            Comparator<? super Byte> c = super.getComparator();
            return c == null || c instanceof ByteComparator ? (ByteComparator) c : c::compare;
        }
    }

    public static final class ShortWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Short,
            SpinedBuffer.OfShort> implements ShortSpliterator {

        public ShortWrappingSpliterator(PipelineHelper<Short> ph, Supplier<Spliterator<P_IN>> supplier,
                                        boolean parallel) {
            super(ph, supplier, parallel);
        }

        public ShortWrappingSpliterator(PipelineHelper<Short> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Short, ?> wrap(Spliterator<P_IN> s) {
            return new ShortWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfShort b = new SpinedBuffer.OfShort();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfShort) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public ShortSpliterator trySplit() {
            return (ShortSpliterator) super.trySplit();
        }

        @Override
        public boolean tryAdvance(ShortConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();

            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }

            return hasNext;
        }

        @Override
        public void forEachRemaining(ShortConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfShort) consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer));
            }
        }

        @Override
        public ShortComparator getComparator() {
            Comparator<? super Short> c = super.getComparator();
            return c == null || c instanceof ShortComparator ? (ShortComparator) c : c::compare;
        }
    }

    public static final class IntWrappingSpliterator<P_IN>
            extends AbstractWrappingSpliterator<P_IN, Integer, SpinedBuffer.OfInt>
            implements Spliterator.OfInt {

        public IntWrappingSpliterator(PipelineHelper<Integer> ph,
                               Supplier<Spliterator<P_IN>> supplier,
                               boolean parallel) {
            super(ph, supplier, parallel);
        }

        public IntWrappingSpliterator(PipelineHelper<Integer> ph,
                               Spliterator<P_IN> spliterator,
                               boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Integer, ?> wrap(Spliterator<P_IN> s) {
            return new IntWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfInt b = new SpinedBuffer.OfInt();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfInt) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public OfInt trySplit() {
            return (OfInt) super.trySplit();
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(IntConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfInt) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    public static final class LongWrappingSpliterator<P_IN>
            extends AbstractWrappingSpliterator<P_IN, Long, SpinedBuffer.OfLong>
            implements Spliterator.OfLong {

        public LongWrappingSpliterator(PipelineHelper<Long> ph,
                                Supplier<Spliterator<P_IN>> supplier,
                                boolean parallel) {
            super(ph, supplier, parallel);
        }

        public LongWrappingSpliterator(PipelineHelper<Long> ph,
                                Spliterator<P_IN> spliterator,
                                boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Long, ?> wrap(Spliterator<P_IN> s) {
            return new LongWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfLong b = new SpinedBuffer.OfLong();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfLong) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public OfLong trySplit() {
            return (OfLong) super.trySplit();
        }

        @Override
        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(LongConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfLong) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    public static final class CharWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Character,
            SpinedBuffer.OfChar> implements CharSpliterator {

        public CharWrappingSpliterator(PipelineHelper<Character> ph, Supplier<Spliterator<P_IN>> supplier,
                                        boolean parallel) {
            super(ph, supplier, parallel);
        }

        public CharWrappingSpliterator(PipelineHelper<Character> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Character, ?> wrap(Spliterator<P_IN> s) {
            return new CharWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfChar b = new SpinedBuffer.OfChar();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfChar) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public CharSpliterator trySplit() {
            return (CharSpliterator) super.trySplit();
        }

        @Override
        public boolean tryAdvance(CharConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();

            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }

            return hasNext;
        }

        @Override
        public void forEachRemaining(CharConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfChar) consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer));
            }
        }

        @Override
        public CharComparator getComparator() {
            Comparator<? super Character> c = super.getComparator();
            return c == null || c instanceof CharComparator ? (CharComparator) c : c::compare;
        }
    }

    public static final class FloatWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Float,
            SpinedBuffer.OfFloat> implements FloatSpliterator {

        public FloatWrappingSpliterator(PipelineHelper<Float> ph, Supplier<Spliterator<P_IN>> supplier,
                                        boolean parallel) {
            super(ph, supplier, parallel);
        }

        public FloatWrappingSpliterator(PipelineHelper<Float> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Float, ?> wrap(Spliterator<P_IN> s) {
            return new FloatWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfFloat b = new SpinedBuffer.OfFloat();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfFloat) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public FloatSpliterator trySplit() {
            return (FloatSpliterator) super.trySplit();
        }

        @Override
        public boolean tryAdvance(FloatConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();

            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }

            return hasNext;
        }

        @Override
        public void forEachRemaining(FloatConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfFloat) consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer));
            }
        }

        @Override
        public FloatComparator getComparator() {
            Comparator<? super Float> c = super.getComparator();
            return c== null || c instanceof FloatComparator ? (FloatComparator) c : c::compare;
        }
    }

    public static final class DoubleWrappingSpliterator<P_IN>
            extends AbstractWrappingSpliterator<P_IN, Double, SpinedBuffer.OfFloat>
            implements Spliterator.OfDouble {

        public DoubleWrappingSpliterator(PipelineHelper<Double> ph,
                                         Supplier<Spliterator<P_IN>> supplier,
                                         boolean parallel) {
            super(ph, supplier, parallel);
        }

        public DoubleWrappingSpliterator(PipelineHelper<Double> ph,
                                         Spliterator<P_IN> spliterator,
                                         boolean parallel) {
            super(ph, spliterator, parallel);
        }

        @Override
        public AbstractWrappingSpliterator<P_IN, Double, ?> wrap(Spliterator<P_IN> s) {
            return new DoubleWrappingSpliterator<>(ph, s, isParallel);
        }

        @Override
        public void initPartialTraversalState() {
            SpinedBuffer.OfFloat b = new SpinedBuffer.OfFloat();
            buffer = b;
            bufferSink = ph.wrapSink((Sink.OfDouble) b::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public OfDouble trySplit() {
            return (OfDouble) super.trySplit();
        }

        @Override
        public boolean tryAdvance(DoubleConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext)
                consumer.accept(buffer.get(nextToConsume));
            return hasNext;
        }

        @Override
        public void forEachRemaining(DoubleConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();

                ph.wrapAndCopyInto((Sink.OfDouble) consumer::accept, spliterator);
                finished = true;
            }
            else {
                do { } while (tryAdvance(consumer));
            }
        }
    }

    /**
     * Spliterator implementation that delegates to an underlying spliterator,
     * acquiring the spliterator from a {@code Supplier<Spliterator>} on the
     * first call to any spliterator method.
     * @param <T>
     */
    public static class DelegatingSpliterator<T, T_SPLITR extends Spliterator<T>>
            implements Spliterator<T> {
        private final Supplier<? extends T_SPLITR> supplier;

        private T_SPLITR s;

        public DelegatingSpliterator(Supplier<? extends T_SPLITR> supplier) {
            this.supplier = supplier;
        }

        public T_SPLITR get() {
            if (s == null) {
                s = supplier.get();
            }
            return s;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T_SPLITR trySplit() {
            return (T_SPLITR) get().trySplit();
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            return get().tryAdvance(consumer);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            get().forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            return get().estimateSize();
        }

        @Override
        public int characteristics() {
            return get().characteristics();
        }

        @Override
        public Comparator<? super T> getComparator() {
            return get().getComparator();
        }

        @Override
        public long getExactSizeIfKnown() {
            return get().getExactSizeIfKnown();
        }

        @Override
        public String toString() {
            return getClass().getName() + "[" + get() + "]";
        }

        public static class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
                extends DelegatingSpliterator<T, T_SPLITR> implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            public OfPrimitive(Supplier<? extends T_SPLITR> supplier) {
                super(supplier);
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                return get().tryAdvance(consumer);
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                get().forEachRemaining(consumer);
            }
        }

        public static final class OfBoolean extends OfPrimitive<Boolean, BooleanConsumer, BooleanSpliterator>
                implements BooleanSpliterator {
            public OfBoolean(Supplier<BooleanSpliterator> supplier) {
                super(supplier);
            }

            @Override
            public BooleanComparator getComparator() {
                Comparator<? super Boolean> c = super.getComparator();
                return c == null || c instanceof BooleanComparator ? (BooleanComparator) c : c::compare;
            }
        }

        public static final class OfByte extends OfPrimitive<Byte, ByteConsumer, ByteSpliterator>
                implements ByteSpliterator {
            public OfByte(Supplier<ByteSpliterator> supplier) {
                super(supplier);
            }

            @Override
            public ByteComparator getComparator() {
                Comparator<? super Byte> c = super.getComparator();
                return c == null || c instanceof ByteComparator ? (ByteComparator) c : c::compare;
            }
        }

        public static final class OfShort extends OfPrimitive<Short, ShortConsumer, ShortSpliterator>
                implements ShortSpliterator {
            public OfShort(Supplier<ShortSpliterator> supplier) {
                super(supplier);
            }

            @Override
            public ShortComparator getComparator() {
                Comparator<? super Short> c = super.getComparator();
                return c == null || c instanceof ShortComparator ? (ShortComparator) c : c::compare;
            }
        }

        public static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
                implements Spliterator.OfInt {

            public OfInt(Supplier<Spliterator.OfInt> supplier) {
                super(supplier);
            }
        }

        public static final class OfLong extends OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
                implements Spliterator.OfLong {

            public OfLong(Supplier<Spliterator.OfLong> supplier) {
                super(supplier);
            }
        }

        public static final class OfChar extends OfPrimitive<Character, CharConsumer, CharSpliterator>
                implements CharSpliterator {
            public OfChar(Supplier<CharSpliterator> supplier) {
                super(supplier);
            }

            @Override
            public CharComparator getComparator() {
                Comparator<? super Character> c = super.getComparator();
                return c == null || c instanceof CharComparator ? (CharComparator) c : c::compare;
            }
        }

        public static final class OfFloat extends OfPrimitive<Float, FloatConsumer, FloatSpliterator>
                implements FloatSpliterator {
            public OfFloat(Supplier<FloatSpliterator> supplier) {
                super(supplier);
            }

            @Override
            public FloatComparator getComparator() {
                Comparator<? super Float> c = super.getComparator();
                return c == null || c instanceof FloatComparator ? (FloatComparator) c : c::compare;
            }
        }

        public static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
                implements Spliterator.OfDouble {

            public OfDouble(Supplier<Spliterator.OfDouble> supplier) {
                super(supplier);
            }
        }
    }

    /**
     * A slice Spliterator from a source Spliterator that reports
     * {@code SUBSIZED}.
     *
     */
    public abstract static class SliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        // The start index of the slice
        public final long sliceOrigin;
        // One past the last index of the slice
        public final long sliceFence;

        // The spliterator to slice
        T_SPLITR s;
        // current (absolute) index, modified on advance/split
        public long index;
        // one past last (absolute) index or sliceFence, which ever is smaller
        public long fence;

        public SliceSpliterator(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence) {
            assert s.hasCharacteristics(Spliterator.SUBSIZED);
            this.s = s;
            this.sliceOrigin = sliceOrigin;
            this.sliceFence = sliceFence;
            this.index = origin;
            this.fence = fence;
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence);

        public T_SPLITR trySplit() {
            if (sliceOrigin >= fence)
                return null;

            if (index >= fence)
                return null;

            // Keep splitting until the left and right splits intersect with the slice
            // thereby ensuring the size estimate decreases.
            // This also avoids creating empty spliterators which can result in
            // existing and additionally created F/J tasks that perform
            // redundant work on no elements.
            while (true) {
                @SuppressWarnings("unchecked")
                T_SPLITR leftSplit = (T_SPLITR) s.trySplit();
                if (leftSplit == null)
                    return null;

                long leftSplitFenceUnbounded = index + leftSplit.estimateSize();
                long leftSplitFence = Math.min(leftSplitFenceUnbounded, sliceFence);
                if (sliceOrigin >= leftSplitFence) {
                    // The left split does not intersect with, and is to the left of, the slice
                    // The right split does intersect
                    // Discard the left split and split further with the right split
                    index = leftSplitFence;
                }
                else if (leftSplitFence >= sliceFence) {
                    // The right split does not intersect with, and is to the right of, the slice
                    // The left split does intersect
                    // Discard the right split and split further with the left split
                    s = leftSplit;
                    fence = leftSplitFence;
                }
                else if (index >= sliceOrigin && leftSplitFenceUnbounded <= sliceFence) {
                    // The left split is contained within the slice, return the underlying left split
                    // Right split is contained within or intersects with the slice
                    index = leftSplitFence;
                    return leftSplit;
                } else {
                    // The left split intersects with the slice
                    // Right split is contained within or intersects with the slice
                    return makeSpliterator(leftSplit, sliceOrigin, sliceFence, index, index = leftSplitFence);
                }
            }
        }

        public long estimateSize() {
            return (sliceOrigin < fence)
                   ? fence - Math.max(sliceOrigin, index) : 0;
        }

        public int characteristics() {
            return s.characteristics();
        }

        public static final class OfRef<T>
                extends SliceSpliterator<T, Spliterator<T>>
                implements Spliterator<T> {

            public OfRef(Spliterator<T> s, long sliceOrigin, long sliceFence) {
                this(s, sliceOrigin, sliceFence, 0, Math.min(s.estimateSize(), sliceFence));
            }

            private OfRef(Spliterator<T> s,
                          long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator<T> makeSpliterator(Spliterator<T> s,
                                                     long sliceOrigin, long sliceFence,
                                                     long origin, long fence) {
                return new OfRef<>(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return false;

                while (sliceOrigin > index) {
                    s.tryAdvance(e -> {});
                    index++;
                }

                if (index >= fence)
                    return false;

                index++;
                return s.tryAdvance(action);
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return;

                if (index >= fence)
                    return;

                if (index >= sliceOrigin && (index + s.estimateSize()) <= sliceFence) {
                    // The spliterator is contained within the slice
                    s.forEachRemaining(action);
                    index = fence;
                } else {
                    // The spliterator intersects with the slice
                    while (sliceOrigin > index) {
                        s.tryAdvance(e -> {});
                        index++;
                    }
                    // Traverse elements up to the fence
                    for (;index < fence; index++) {
                        s.tryAdvance(action);
                    }
                }
            }
        }

        public abstract static class OfPrimitive<T,
                T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                T_CONS>
                extends SliceSpliterator<T, T_SPLITR>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {

            public OfPrimitive(T_SPLITR s, long sliceOrigin, long sliceFence) {
                this(s, sliceOrigin, sliceFence, 0, Math.min(s.estimateSize(), sliceFence));
            }

            private OfPrimitive(T_SPLITR s,
                                long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return false;

                while (sliceOrigin > index) {
                    s.tryAdvance(emptyConsumer());
                    index++;
                }

                if (index >= fence)
                    return false;

                index++;
                return s.tryAdvance(action);
            }

            @Override
            public void forEachRemaining(T_CONS action) {
                Objects.requireNonNull(action);

                if (sliceOrigin >= fence)
                    return;

                if (index >= fence)
                    return;

                if (index >= sliceOrigin && (index + s.estimateSize()) <= sliceFence) {
                    // The spliterator is contained within the slice
                    s.forEachRemaining(action);
                    index = fence;
                } else {
                    // The spliterator intersects with the slice
                    while (sliceOrigin > index) {
                        s.tryAdvance(emptyConsumer());
                        index++;
                    }
                    // Traverse elements up to the fence
                    for (;index < fence; index++) {
                        s.tryAdvance(action);
                    }
                }
            }

            protected abstract T_CONS emptyConsumer();
        }

        public static final class OfBoolean extends OfPrimitive<Boolean, BooleanSpliterator, BooleanConsumer>
                implements BooleanSpliterator {
            public OfBoolean(BooleanSpliterator s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfBoolean(BooleanSpliterator s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected BooleanSpliterator makeSpliterator(BooleanSpliterator s, long sliceOrigin, long sliceFence,
                                                       long origin, long fence) {
                return new OfBoolean(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected BooleanConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfByte extends OfPrimitive<Byte, ByteSpliterator, ByteConsumer>
                implements ByteSpliterator {
            public OfByte(ByteSpliterator s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfByte(ByteSpliterator s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected ByteSpliterator makeSpliterator(ByteSpliterator s, long sliceOrigin, long sliceFence,
                                                       long origin, long fence) {
                return new OfByte(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected ByteConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfShort extends OfPrimitive<Short, ShortSpliterator, ShortConsumer>
                implements ShortSpliterator {
            public OfShort(ShortSpliterator s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfShort(ShortSpliterator s,
                           long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected ShortSpliterator makeSpliterator(ShortSpliterator s, long sliceOrigin, long sliceFence,
                                                       long origin, long fence) {
                return new OfShort(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected ShortConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfInt extends OfPrimitive<Integer, Spliterator.OfInt, IntConsumer>
                implements Spliterator.OfInt {
            public OfInt(Spliterator.OfInt s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfInt(Spliterator.OfInt s,
                  long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator.OfInt makeSpliterator(Spliterator.OfInt s,
                                                        long sliceOrigin, long sliceFence,
                                                        long origin, long fence) {
                return new SliceSpliterator.OfInt(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected IntConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfLong extends OfPrimitive<Long, Spliterator.OfLong, LongConsumer>
                implements Spliterator.OfLong {
            public OfLong(Spliterator.OfLong s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfLong(Spliterator.OfLong s,
                   long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator.OfLong makeSpliterator(Spliterator.OfLong s,
                                                         long sliceOrigin, long sliceFence,
                                                         long origin, long fence) {
                return new SliceSpliterator.OfLong(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected LongConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfChar extends OfPrimitive<Character, CharSpliterator, CharConsumer>
                implements CharSpliterator {
            public OfChar(CharSpliterator s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfChar(CharSpliterator s,
                           long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected CharSpliterator makeSpliterator(CharSpliterator s, long sliceOrigin, long sliceFence,
                                                       long origin, long fence) {
                return new OfChar(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected CharConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfFloat extends OfPrimitive<Float, FloatSpliterator, FloatConsumer>
                implements FloatSpliterator {
            public OfFloat(FloatSpliterator s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfFloat(FloatSpliterator s,
                           long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected FloatSpliterator makeSpliterator(FloatSpliterator s, long sliceOrigin, long sliceFence,
                                                       long origin, long fence) {
                return new OfFloat(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected FloatConsumer emptyConsumer() {
                return e -> {};
            }
        }

        public static final class OfDouble extends OfPrimitive<Double, Spliterator.OfDouble, DoubleConsumer>
                implements Spliterator.OfDouble {
            public OfDouble(Spliterator.OfDouble s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            public OfDouble(Spliterator.OfDouble s,
                            long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected Spliterator.OfDouble makeSpliterator(Spliterator.OfDouble s,
                                                           long sliceOrigin, long sliceFence,
                                                           long origin, long fence) {
                return new SliceSpliterator.OfDouble(s, sliceOrigin, sliceFence, origin, fence);
            }

            @Override
            protected DoubleConsumer emptyConsumer() {
                return e -> {};
            }
        }
    }

    /**
     * A slice Spliterator that does not preserve order, if any, of a source
     * Spliterator.
     *
     * Note: The source spliterator may report {@code ORDERED} since that
     * spliterator be the result of a previous pipeline stage that was
     * collected to a {@code Node}. It is the order of the pipeline stage
     * that governs whether this slice spliterator is to be used or not.
     */
    public abstract static class UnorderedSliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        public static final int CHUNK_SIZE = 1 << 7;

        // The spliterator to slice
        protected final T_SPLITR s;
        protected final boolean unlimited;
        protected final int chunkSize;
        private final long skipThreshold;
        private final AtomicLong permits;

        public UnorderedSliceSpliterator(T_SPLITR s, long skip, long limit) {
            this.s = s;
            this.unlimited = limit < 0;
            this.skipThreshold = limit >= 0 ? limit : 0;
            this.chunkSize = limit >= 0 ? (int)Math.min(CHUNK_SIZE,
                                                        ((skip + limit) / AbstractTask.getLeafTarget()) + 1) : CHUNK_SIZE;
            this.permits = new AtomicLong(limit >= 0 ? skip + limit : skip);
        }

        public UnorderedSliceSpliterator(T_SPLITR s,
                                  UnorderedSliceSpliterator<T, T_SPLITR> parent) {
            this.s = s;
            this.unlimited = parent.unlimited;
            this.permits = parent.permits;
            this.skipThreshold = parent.skipThreshold;
            this.chunkSize = parent.chunkSize;
        }

        /**
         * Acquire permission to skip or process elements.  The caller must
         * first acquire the elements, then consult this method for guidance
         * as to what to do with the data.
         *
         * <p>We use an {@code AtomicLong} to atomically maintain a counter,
         * which is initialized as skip+limit if we are limiting, or skip only
         * if we are not limiting.  The user should consult the method
         * {@code checkPermits()} before acquiring data elements.
         *
         * @param numElements the number of elements the caller has in hand
         * @return the number of elements that should be processed; any
         * remaining elements should be discarded.
         */
        protected final long acquirePermits(long numElements) {
            long remainingPermits;
            long grabbing;
            // permits never increase, and don't decrease below zero
            assert numElements > 0;
            do {
                remainingPermits = permits.get();
                if (remainingPermits == 0)
                    return unlimited ? numElements : 0;
                grabbing = Math.min(remainingPermits, numElements);
            } while (grabbing > 0 &&
                     !permits.compareAndSet(remainingPermits, remainingPermits - grabbing));

            if (unlimited)
                return Math.max(numElements - grabbing, 0);
            else if (remainingPermits > skipThreshold)
                return Math.max(grabbing - (remainingPermits - skipThreshold), 0);
            else
                return grabbing;
        }

        public enum PermitStatus { NO_MORE, MAYBE_MORE, UNLIMITED }

        /** Call to check if permits might be available before acquiring data */
        protected final PermitStatus permitStatus() {
            if (permits.get() > 0)
                return PermitStatus.MAYBE_MORE;
            else
                return unlimited ?  PermitStatus.UNLIMITED : PermitStatus.NO_MORE;
        }

        public final T_SPLITR trySplit() {
            // Stop splitting when there are no more limit permits
            if (permits.get() == 0)
                return null;
            @SuppressWarnings("unchecked")
            T_SPLITR split = (T_SPLITR) s.trySplit();
            return split == null ? null : makeSpliterator(split);
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR s);

        public final long estimateSize() {
            return s.estimateSize();
        }

        public final int characteristics() {
            return s.characteristics() &
                   ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED);
        }

        public static final class OfRef<T> extends UnorderedSliceSpliterator<T, Spliterator<T>>
                implements Spliterator<T>, Consumer<T> {
            public T tmpSlot;

            public OfRef(Spliterator<T> s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfRef(Spliterator<T> s, OfRef<T> parent) {
                super(s, parent);
            }

            @Override
            public final void accept(T t) {
                tmpSlot = t;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                while (permitStatus() != PermitStatus.NO_MORE) {
                    if (!s.tryAdvance(this))
                        return false;
                    else if (acquirePermits(1) == 1) {
                        action.accept(tmpSlot);
                        tmpSlot = null;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                ArrayBuffer.OfRef<T> sb = null;
                PermitStatus permitStatus;
                while ((permitStatus = permitStatus()) != PermitStatus.NO_MORE) {
                    if (permitStatus == PermitStatus.MAYBE_MORE) {
                        // Optimistically traverse elements up to a threshold of chunkSize
                        if (sb == null)
                            sb = new ArrayBuffer.OfRef<>(chunkSize);
                        else
                            sb.reset();
                        long permitsRequested = 0;
                        do { } while (s.tryAdvance(sb) && ++permitsRequested < chunkSize);
                        if (permitsRequested == 0)
                            return;
                        sb.forEach(action, acquirePermits(permitsRequested));
                    }
                    else {
                        // Must be UNLIMITED; let 'er rip
                        s.forEachRemaining(action);
                        return;
                    }
                }
            }

            @Override
            protected Spliterator<T> makeSpliterator(Spliterator<T> s) {
                return new OfRef<>(s, this);
            }
        }

        /**
         * Concrete sub-types must also be an instance of type {@code T_CONS}.
         *
         * @param <T_BUFF> the type of the spined buffer. Must also be a type of
         *        {@code T_CONS}.
         */
        public abstract static class OfPrimitive<
                T,
                T_CONS,
                T_BUFF extends ArrayBuffer.OfPrimitive<T_CONS>,
                T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
                extends UnorderedSliceSpliterator<T, T_SPLITR>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            public OfPrimitive(T_SPLITR s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfPrimitive(T_SPLITR s, UnorderedSliceSpliterator.OfPrimitive<T, T_CONS, T_BUFF, T_SPLITR> parent) {
                super(s, parent);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                Objects.requireNonNull(action);
                @SuppressWarnings("unchecked")
                T_CONS consumer = (T_CONS) this;

                while (permitStatus() != PermitStatus.NO_MORE) {
                    if (!s.tryAdvance(consumer))
                        return false;
                    else if (acquirePermits(1) == 1) {
                        acceptConsumed(action);
                        return true;
                    }
                }
                return false;
            }

            protected abstract void acceptConsumed(T_CONS action);

            @Override
            public void forEachRemaining(T_CONS action) {
                Objects.requireNonNull(action);

                T_BUFF sb = null;
                PermitStatus permitStatus;
                while ((permitStatus = permitStatus()) != PermitStatus.NO_MORE) {
                    if (permitStatus == PermitStatus.MAYBE_MORE) {
                        // Optimistically traverse elements up to a threshold of chunkSize
                        if (sb == null)
                            sb = bufferCreate(chunkSize);
                        else
                            sb.reset();
                        @SuppressWarnings("unchecked")
                        T_CONS sbc = (T_CONS) sb;
                        long permitsRequested = 0;
                        do { } while (s.tryAdvance(sbc) && ++permitsRequested < chunkSize);
                        if (permitsRequested == 0)
                            return;
                        sb.forEach(action, acquirePermits(permitsRequested));
                    }
                    else {
                        // Must be UNLIMITED; let 'er rip
                        s.forEachRemaining(action);
                        return;
                    }
                }
            }

            protected abstract T_BUFF bufferCreate(int initialCapacity);
        }

        public static final class OfBoolean extends OfPrimitive<Boolean, BooleanConsumer, ArrayBuffer.OfBoolean,
                BooleanSpliterator> implements BooleanSpliterator, BooleanConsumer {
            public boolean tmpValue;

            public OfBoolean(BooleanSpliterator s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfBoolean(BooleanSpliterator s, OfBoolean parent) {
                super(s, parent);
            }

            @Override
            public void accept(boolean value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(BooleanConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfBoolean bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfBoolean(initialCapacity);
            }

            @Override
            protected BooleanSpliterator makeSpliterator(BooleanSpliterator s) {
                return new OfBoolean(s, this);
            }
        }

        public static final class OfByte extends OfPrimitive<Byte, ByteConsumer, ArrayBuffer.OfByte,
                ByteSpliterator> implements ByteSpliterator, ByteConsumer {
            public byte tmpValue;

            public OfByte(ByteSpliterator s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfByte(ByteSpliterator s, OfByte parent) {
                super(s, parent);
            }

            @Override
            public void accept(byte value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(ByteConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfByte bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfByte(initialCapacity);
            }

            @Override
            protected ByteSpliterator makeSpliterator(ByteSpliterator s) {
                return new OfByte(s, this);
            }
        }

        public static final class OfShort extends OfPrimitive<Short, ShortConsumer, ArrayBuffer.OfShort,
                ShortSpliterator> implements ShortSpliterator, ShortConsumer {
            public short tmpValue;

            public OfShort(ShortSpliterator s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfShort(ShortSpliterator s, OfShort parent) {
                super(s, parent);
            }

            @Override
            public void accept(short value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(ShortConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfShort bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfShort(initialCapacity);
            }

            @Override
            protected ShortSpliterator makeSpliterator(ShortSpliterator s) {
                return new OfShort(s, this);
            }
        }

        public static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, ArrayBuffer.OfInt, Spliterator.OfInt>
                implements Spliterator.OfInt, IntConsumer {

            int tmpValue;

            public OfInt(Spliterator.OfInt s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfInt(Spliterator.OfInt s, UnorderedSliceSpliterator.OfInt parent) {
                super(s, parent);
            }

            @Override
            public void accept(int value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(IntConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfInt bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfInt(initialCapacity);
            }

            @Override
            protected Spliterator.OfInt makeSpliterator(Spliterator.OfInt s) {
                return new UnorderedSliceSpliterator.OfInt(s, this);
            }
        }

        public static final class OfLong
                extends OfPrimitive<Long, LongConsumer, ArrayBuffer.OfLong, Spliterator.OfLong>
                implements Spliterator.OfLong, LongConsumer {

            long tmpValue;

            public OfLong(Spliterator.OfLong s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfLong(Spliterator.OfLong s, UnorderedSliceSpliterator.OfLong parent) {
                super(s, parent);
            }

            @Override
            public void accept(long value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(LongConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfLong bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfLong(initialCapacity);
            }

            @Override
            protected Spliterator.OfLong makeSpliterator(Spliterator.OfLong s) {
                return new UnorderedSliceSpliterator.OfLong(s, this);
            }
        }

        public static final class OfChar extends OfPrimitive<Character, CharConsumer, ArrayBuffer.OfChar,
                CharSpliterator> implements CharSpliterator, CharConsumer {
            public char tmpValue;

            public OfChar(CharSpliterator s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfChar(CharSpliterator s, OfChar parent) {
                super(s, parent);
            }

            @Override
            public void accept(char value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(CharConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfChar bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfChar(initialCapacity);
            }

            @Override
            protected CharSpliterator makeSpliterator(CharSpliterator s) {
                return new OfChar(s, this);
            }
        }

        public static final class OfFloat extends OfPrimitive<Float, FloatConsumer, ArrayBuffer.OfFloat,
                FloatSpliterator> implements FloatSpliterator, FloatConsumer {

            public float tmpValue;

            public OfFloat(FloatSpliterator s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfFloat(FloatSpliterator s, OfFloat parent) {
                super(s, parent);
            }

            @Override
            public void accept(float value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(FloatConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfFloat bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfFloat(initialCapacity);
            }

            @Override
            protected FloatSpliterator makeSpliterator(FloatSpliterator s) {
                return new OfFloat(s, this);
            }
        }

        public static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, ArrayBuffer.OfDouble, Spliterator.OfDouble>
                implements Spliterator.OfDouble, DoubleConsumer {

            public double tmpValue;

            public OfDouble(Spliterator.OfDouble s, long skip, long limit) {
                super(s, skip, limit);
            }

            public OfDouble(Spliterator.OfDouble s, UnorderedSliceSpliterator.OfDouble parent) {
                super(s, parent);
            }

            @Override
            public void accept(double value) {
                tmpValue = value;
            }

            @Override
            protected void acceptConsumed(DoubleConsumer action) {
                action.accept(tmpValue);
            }

            @Override
            protected ArrayBuffer.OfDouble bufferCreate(int initialCapacity) {
                return new ArrayBuffer.OfDouble(initialCapacity);
            }

            @Override
            protected Spliterator.OfDouble makeSpliterator(Spliterator.OfDouble s) {
                return new UnorderedSliceSpliterator.OfDouble(s, this);
            }
        }
    }

    /**
     * A wrapping spliterator that only reports distinct elements of the
     * underlying spliterator. Does not preserve size and encounter order.
     */
    static final class DistinctSpliterator<T> implements Spliterator<T>, Consumer<T> {

        // The value to represent null in the ConcurrentHashMap
        private static final Object NULL_VALUE = new Object();

        // The underlying spliterator
        private final Spliterator<T> s;

        // ConcurrentHashMap holding distinct elements as keys
        private final ConcurrentHashMap<T, Boolean> seen;

        // Temporary element, only used with tryAdvance
        private T tmpSlot;

        DistinctSpliterator(Spliterator<T> s) {
            this(s, new ConcurrentHashMap<>());
        }

        private DistinctSpliterator(Spliterator<T> s, ConcurrentHashMap<T, Boolean> seen) {
            this.s = s;
            this.seen = seen;
        }

        @Override
        public void accept(T t) {
            this.tmpSlot = t;
        }

        @SuppressWarnings("unchecked")
        private T mapNull(T t) {
            return t != null ? t : (T) NULL_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (s.tryAdvance(this)) {
                if (seen.putIfAbsent(mapNull(tmpSlot), Boolean.TRUE) == null) {
                    action.accept(tmpSlot);
                    tmpSlot = null;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            s.forEachRemaining(t -> {
                if (seen.putIfAbsent(mapNull(t), Boolean.TRUE) == null) {
                    action.accept(t);
                }
            });
        }

        @Override
        public Spliterator<T> trySplit() {
            Spliterator<T> split = s.trySplit();
            return (split != null) ? new DistinctSpliterator<>(split, seen) : null;
        }

        @Override
        public long estimateSize() {
            return s.estimateSize();
        }

        @Override
        public int characteristics() {
            return (s.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED |
                                            Spliterator.SORTED | Spliterator.ORDERED))
                   | Spliterator.DISTINCT;
        }

        @Override
        public Comparator<? super T> getComparator() {
            return s.getComparator();
        }
    }

    /**
     * A Spliterator that infinitely supplies elements in no particular order.
     *
     * <p>Splitting divides the estimated size in two and stops when the
     * estimate size is 0.
     *
     * <p>The {@code forEachRemaining} method if invoked will never terminate.
     * The {@code tryAdvance} method always returns true.
     *
     */
    public abstract static class InfiniteSupplyingSpliterator<T> implements Spliterator<T> {
        long estimate;

        protected InfiniteSupplyingSpliterator(long estimate) {
            this.estimate = estimate;
        }

        @Override
        public long estimateSize() {
            return estimate;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE;
        }

        static final class OfRef<T> extends InfiniteSupplyingSpliterator<T> {
            final Supplier<? extends T> s;

            OfRef(long size, Supplier<? extends T> s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);

                action.accept(s.get());
                return true;
            }

            @Override
            public Spliterator<T> trySplit() {
                if (estimate == 0)
                    return null;
                return new OfRef<>(estimate >>>= 1, s);
            }
        }

        public static final class OfBoolean extends InfiniteSupplyingSpliterator<Boolean>
                implements BooleanSpliterator {
            public final BooleanSupplier s;

            public OfBoolean(long size, BooleanSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(BooleanConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsBoolean());
                return true;
            }

            @Override
            public BooleanSpliterator trySplit() {
                if (estimate == 0)
                    return null;
                return new OfBoolean(estimate = estimate >>> 1, s);
            }
        }

        public static final class OfByte extends InfiniteSupplyingSpliterator<Byte> implements ByteSpliterator {
            public final ByteSupplier s;

            public OfByte(long size, ByteSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(ByteConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsByte());
                return true;
            }

            @Override
            public ByteSpliterator trySplit() {
                if (estimate == 0)
                    return null;
                return new OfByte(estimate = estimate >>> 1, s);
            }
        }

        public static final class OfShort extends InfiniteSupplyingSpliterator<Short> implements ShortSpliterator {
            public final ShortSupplier s;

            public OfShort(long size, ShortSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(ShortConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsShort());
                return true;
            }

            @Override
            public ShortSpliterator trySplit() {
                if (estimate == 0)
                    return null;
                return new OfShort(estimate = estimate >>> 1, s);
            }
        }

        static final class OfInt extends InfiniteSupplyingSpliterator<Integer>
                implements Spliterator.OfInt {
            final IntSupplier s;

            OfInt(long size, IntSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(IntConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsInt());
                return true;
            }

            @Override
            public Spliterator.OfInt trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfInt(estimate = estimate >>> 1, s);
            }
        }

        static final class OfLong extends InfiniteSupplyingSpliterator<Long>
                implements Spliterator.OfLong {
            final LongSupplier s;

            OfLong(long size, LongSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(LongConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsLong());
                return true;
            }

            @Override
            public Spliterator.OfLong trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfLong(estimate = estimate >>> 1, s);
            }
        }

        public static final class OfChar extends InfiniteSupplyingSpliterator<Character> implements CharSpliterator {
            public final CharSupplier s;

            public OfChar(long size, CharSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(CharConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsChar());
                return true;
            }

            @Override
            public CharSpliterator trySplit() {
                if (estimate == 0)
                    return null;
                return new OfChar(estimate = estimate >>> 1, s);
            }
        }

        public static final class OfFloat extends InfiniteSupplyingSpliterator<Float>
                implements FloatSpliterator {
            public final FloatSupplier s;

            public OfFloat(long size, FloatSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(FloatConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsFloat());
                return true;
            }

            @Override
            public FloatSpliterator trySplit() {
                if (estimate == 0)
                    return null;
                return new OfFloat(estimate = estimate >>> 1, s);
            }
        }

        public static final class OfDouble extends InfiniteSupplyingSpliterator<Double>
                implements Spliterator.OfDouble {
            public final DoubleSupplier s;

            public OfDouble(long size, DoubleSupplier s) {
                super(size);
                this.s = s;
            }

            @Override
            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);

                action.accept(s.getAsDouble());
                return true;
            }

            @Override
            public Spliterator.OfDouble trySplit() {
                if (estimate == 0)
                    return null;
                return new InfiniteSupplyingSpliterator.OfDouble(estimate = estimate >>> 1, s);
            }
        }
    }

    // @@@ Consolidate with Node.Builder
    public abstract static class ArrayBuffer {
        public int index;

        public void reset() {
            index = 0;
        }

        public static final class OfRef<T> extends ArrayBuffer implements Consumer<T> {
            public final Object[] array;

            public OfRef(int size) {
                this.array = new Object[size];
            }

            @Override
            public void accept(T t) {
                array[index++] = t;
            }

            public void forEach(Consumer<? super T> action, long fence) {
                for (int i = 0; i < fence; i++) {
                    @SuppressWarnings("unchecked")
                    T t = (T) array[i];
                    action.accept(t);
                }
            }
        }

        public abstract static class OfPrimitive<T_CONS> extends ArrayBuffer {
            public int index;

            @Override
            public void reset() {
                index = 0;
            }

            public abstract void forEach(T_CONS action, long fence);
        }

        public static final class OfBoolean extends OfPrimitive<BooleanConsumer> implements BooleanConsumer {
            public final boolean[] array;

            public OfBoolean(int size) {
                this.array = new boolean[size];
            }

            @Override
            public void accept(boolean t) {
                array[index++] = t;
            }

            @Override
            public void forEach(BooleanConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfByte extends OfPrimitive<ByteConsumer> implements ByteConsumer {
            public final byte[] array;

            public OfByte(int size) {
                this.array = new byte[size];
            }

            @Override
            public void accept(byte t) {
                array[index++] = t;
            }

            @Override
            public void forEach(ByteConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfShort extends OfPrimitive<ShortConsumer> implements ShortConsumer {
            public final short[] array;

            public OfShort(int size) {
                this.array = new short[size];
            }

            @Override
            public void accept(short t) {
                array[index++] = t;
            }

            @Override
            public void forEach(ShortConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfInt extends OfPrimitive<IntConsumer>
                implements IntConsumer {
            public final int[] array;

            public OfInt(int size) {
                this.array = new int[size];
            }

            @Override
            public void accept(int t) {
                array[index++] = t;
            }

            @Override
            public void forEach(IntConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfLong extends OfPrimitive<LongConsumer>
                implements LongConsumer {
            public final long[] array;

            public OfLong(int size) {
                this.array = new long[size];
            }

            @Override
            public void accept(long t) {
                array[index++] = t;
            }

            @Override
            public void forEach(LongConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfChar extends OfPrimitive<CharConsumer> implements CharConsumer {
            public final char[] array;

            public OfChar(int size) {
                this.array = new char[size];
            }

            @Override
            public void accept(char t) {
                array[index++] = t;
            }

            @Override
            public void forEach(CharConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfFloat extends OfPrimitive<FloatConsumer> implements FloatConsumer {
            public final float[] array;

            public OfFloat(int size) {
                this.array = new float[size];
            }

            @Override
            public void accept(float t) {
                array[index++] = t;
            }

            @Override
            public void forEach(FloatConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }

        public static final class OfDouble extends OfPrimitive<DoubleConsumer>
                implements DoubleConsumer {
            public final double[] array;

            public OfDouble(int size) {
                this.array = new double[size];
            }

            @Override
            public void accept(double t) {
                array[index++] = t;
            }

            @Override
            public void forEach(DoubleConsumer action, long fence) {
                for (int i = 0; i < fence; i++) {
                    action.accept(array[i]);
                }
            }
        }
    }
}

