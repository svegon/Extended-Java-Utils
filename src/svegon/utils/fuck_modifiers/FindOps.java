/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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

import org.jetbrains.annotations.Nullable;
import svegon.utils.optional.*;

import java.util.*;
import java.util.concurrent.CountedCompleter;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Factory for instances of a short-circuiting {@code TerminalOp} that searches
 * for an element in a stream pipeline, and terminates when it finds one.
 * Supported variants include find-first (find the first element in the
 * encounter order) and find-any (find any element, may not be the first in
 * encounter order.)
 *
 * @since 1.8
 */
public final class FindOps {

    private FindOps() { }

    /**
     * Constructs a {@code TerminalOp} for streams of objects.
     *
     * @param <T> the type of elements of the stream
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    @SuppressWarnings("unchecked")
    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean mustFindFirst) {
        return (TerminalOp<T, Optional<T>>) (mustFindFirst ? FindSink.OfRef.OP_FIND_FIRST : FindSink.OfRef.OP_FIND_ANY);
    }

    /**
     * Constructs a {@code TerminalOp} for streams of ints.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Boolean, OptionalBoolean> makeBoolean(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfBoolean.OP_FIND_FIRST : FindSink.OfBoolean.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of ints.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Byte, OptionalByte> makeByte(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfByte.OP_FIND_FIRST : FindSink.OfByte.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of ints.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Short, OptionalShort> makeShort(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfShort.OP_FIND_FIRST : FindSink.OfShort.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of ints.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Integer, OptionalInt> makeInt(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfInt.OP_FIND_FIRST : FindSink.OfInt.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of longs.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Long, OptionalLong> makeLong(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfLong.OP_FIND_FIRST : FindSink.OfLong.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of ints.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Character, OptionalChar> makeChar(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfChar.OP_FIND_FIRST : FindSink.OfChar.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code FindOp} for streams of doubles.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Float, OptionalFloat> makeFloat(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfFloat.OP_FIND_FIRST : FindSink.OfFloat.OP_FIND_ANY;
    }

    /**
     * Constructs a {@code FindOp} for streams of doubles.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfDouble.OP_FIND_FIRST : FindSink.OfDouble.OP_FIND_ANY;
    }

    /**
     * A short-circuiting {@code TerminalOp} that searches for an element in a
     * stream pipeline, and terminates when it finds one.  Implements both
     * find-first (find the first element in the encounter order) and find-any
     * (find any element, may not be the first in encounter order.)
     *
     * @param <T> the output type of the stream pipeline
     * @param <O> the result type of the find operation, typically an optional
     *        type
     */
    private static final class FindOp<T, O> implements TerminalOp<T, O> {
        private final StreamShape shape;
        final int opFlags;
        final O emptyValue;
        final Predicate<O> presentPredicate;
        public final Supplier<TerminalSink<T, O>> sinkSupplier;

        /**
         * Constructs a {@code FindOp}.
         *
         * @param mustFindFirst if true, must find the first element in
         *        encounter order, otherwise can find any element
         * @param shape stream shape of elements to search
         * @param emptyValue result value corresponding to "found nothing"
         * @param presentPredicate {@code Predicate} on result value
         *        corresponding to "found something"
         * @param sinkSupplier supplier for a {@code TerminalSink} implementing
         *        the matching functionality
         */
        FindOp(boolean mustFindFirst,
                       StreamShape shape,
                       O emptyValue,
                       Predicate<O> presentPredicate,
                       Supplier<TerminalSink<T, O>> sinkSupplier) {
            this.opFlags = StreamOpFlag.IS_SHORT_CIRCUIT | (mustFindFirst ? 0 : StreamOpFlag.NOT_ORDERED);
            this.shape = shape;
            this.emptyValue = emptyValue;
            this.presentPredicate = presentPredicate;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return opFlags;
        }

        @Override
        public StreamShape inputShape() {
            return shape;
        }

        @Override
        public <S> O evaluateSequential(PipelineHelper<T> helper,
                                        Spliterator<S> spliterator) {
            O result = helper.wrapAndCopyInto(sinkSupplier.get(), spliterator).get();
            return result != null ? result : emptyValue;
        }

        @Override
        public <P_IN> O evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<P_IN> spliterator) {
            // This takes into account the upstream ops flags and the terminal
            // op flags and therefore takes into account findFirst or findAny
            boolean mustFindFirst = StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags());
            return new FindTask<>(this, mustFindFirst, helper, spliterator).invoke();
        }
    }

    /**
     * Implementation of @{code TerminalSink} that implements the find
     * functionality, requesting cancellation when something has been found
     *
     * @param <T> The type of input element
     * @param <O> The result type, typically an optional type
     */
    private abstract static class FindSink<T, O> implements TerminalSink<T, O> {
        boolean hasValue;
        T value;

        FindSink() {} // Avoid creation of special accessor

        @Override
        public void accept(T value) {
            if (!hasValue) {
                hasValue = true;
                this.value = value;
            }
        }

        @Override
        public boolean cancellationRequested() {
            return hasValue;
        }

        /** Specialization of {@code FindSink} for reference streams */
        public static final class OfRef<T> extends FindSink<T, Optional<T>> {
            @Override
            public Optional<T> get() {
                return hasValue ? Optional.of(value) : null;
            }

            public static final TerminalOp<?, ?> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.REFERENCE, Optional.empty(),
                    Optional::isPresent, OfRef::new);

            public static final TerminalOp<?, ?> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.REFERENCE, Optional.empty(),
                    Optional::isPresent, OfRef::new);
        }

        /** Specialization of {@code FindSink} for int streams */
        public static final class OfBoolean extends FindSink<Boolean, OptionalBoolean> implements Sink.OfBoolean {
            @Override
            public void accept(boolean value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Boolean) value);
            }

            @Override
            public @Nullable OptionalBoolean get() {
                return hasValue ? OptionalBoolean.of(value) : null;
            }

            public static final TerminalOp<Boolean, OptionalBoolean> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.BYTE_VALUE, OptionalBoolean.empty(), OptionalBoolean::isPresent,
                    FindOps.FindSink.OfBoolean::new);
            public static final TerminalOp<Boolean, OptionalBoolean> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.BYTE_VALUE, OptionalBoolean.empty(), OptionalBoolean::isPresent,
                    FindOps.FindSink.OfBoolean::new);
        }

        /** Specialization of {@code FindSink} for int streams */
        public static final class OfByte extends FindSink<Byte, OptionalByte> implements Sink.OfByte {
            @Override
            public void accept(byte value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Byte) value);
            }

            @Override
            public OptionalByte get() {
                return hasValue ? OptionalByte.of(value) : null;
            }

            public static final TerminalOp<Byte, OptionalByte> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.BYTE_VALUE, OptionalByte.empty(), OptionalByte::isPresent,
                    FindOps.FindSink.OfByte::new);
            public static final TerminalOp<Byte, OptionalByte> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.BYTE_VALUE, OptionalByte.empty(), OptionalByte::isPresent,
                    FindOps.FindSink.OfByte::new);
        }

        /** Specialization of {@code FindSink} for int streams */
        public static final class OfShort extends FindSink<Short, OptionalShort> implements Sink.OfShort {
            @Override
            public void accept(short value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Short) value);
            }

            @Override
            public OptionalShort get() {
                return hasValue ? OptionalShort.of(value) : null;
            }

            public static final TerminalOp<Short, OptionalShort> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.SHORT_VALUE, OptionalShort.empty(), OptionalShort::isPresent,
                    FindOps.FindSink.OfShort::new);
            public static final TerminalOp<Short, OptionalShort> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.SHORT_VALUE, OptionalShort.empty(), OptionalShort::isPresent,
                    FindOps.FindSink.OfShort::new);
        }

        /** Specialization of {@code FindSink} for int streams */
        public static final class OfInt extends FindSink<Integer, OptionalInt>
                implements Sink.OfInt {
            @Override
            public void accept(int value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Integer) value);
            }

            @Override
            public OptionalInt get() {
                return hasValue ? OptionalInt.of(value) : null;
            }

            public static final TerminalOp<Integer, OptionalInt> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.INT_VALUE, OptionalInt.empty(),
                    OptionalInt::isPresent, FindOps.FindSink.OfInt::new);
            public static final TerminalOp<Integer, OptionalInt> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.INT_VALUE, OptionalInt.empty(),
                    OptionalInt::isPresent, FindOps.FindSink.OfInt::new);
        }

        /** Specialization of {@code FindSink} for long streams */
        public static final class OfLong extends FindSink<Long, OptionalLong>
                implements Sink.OfLong {
            @Override
            public void accept(long value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Long) value);
            }

            @Override
            public OptionalLong get() {
                return hasValue ? OptionalLong.of(value) : null;
            }

            public static final TerminalOp<Long, OptionalLong> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.LONG_VALUE, OptionalLong.empty(),
                    OptionalLong::isPresent, FindOps.FindSink.OfLong::new);
            public static final TerminalOp<Long, OptionalLong> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.LONG_VALUE, OptionalLong.empty(),
                    OptionalLong::isPresent, FindOps.FindSink.OfLong::new);
        }

        /** Specialization of {@code FindSink} for int streams */
        public static final class OfChar extends FindSink<Character, OptionalChar> implements Sink.OfChar {
            @Override
            public void accept(char value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Character) value);
            }

            @Override
            public OptionalChar get() {
                return hasValue ? OptionalChar.of(value) : null;
            }

            public static final TerminalOp<Character, OptionalChar> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.CHAR_VALUE, OptionalChar.empty(), OptionalChar::isPresent,
                    FindOps.FindSink.OfChar::new);
            public static final TerminalOp<Character, OptionalChar> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.CHAR_VALUE, OptionalChar.empty(), OptionalChar::isPresent,
                    FindOps.FindSink.OfChar::new);
        }

        /** Specialization of {@code FindSink} for double streams */
        public static final class OfFloat extends FindSink<Float, OptionalFloat> implements Sink.OfFloat {
            @Override
            public void accept(float value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Float) value);
            }

            @Override
            public OptionalFloat get() {
                return hasValue ? OptionalFloat.of(value) : null;
            }

            public static final TerminalOp<Float, OptionalFloat> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.FLOAT_VALUE, OptionalFloat.empty(), OptionalFloat::isPresent,
                    FindOps.FindSink.OfFloat::new);
            public static final TerminalOp<Float, OptionalFloat> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.FLOAT_VALUE, OptionalFloat.empty(), OptionalFloat::isPresent,
                    FindOps.FindSink.OfFloat::new);
        }

        /** Specialization of {@code FindSink} for double streams */
        public static final class OfDouble extends FindSink<Double, OptionalDouble>
                implements Sink.OfDouble {
            @Override
            public void accept(double value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Double) value);
            }

            @Override
            public OptionalDouble get() {
                return hasValue ? OptionalDouble.of(value) : null;
            }

            public static final TerminalOp<Double, OptionalDouble> OP_FIND_FIRST = new FindOp<>(true,
                    StreamShape.DOUBLE_VALUE, OptionalDouble.empty(),
                    OptionalDouble::isPresent, FindOps.FindSink.OfDouble::new);
            public static final TerminalOp<Double, OptionalDouble> OP_FIND_ANY = new FindOp<>(false,
                    StreamShape.DOUBLE_VALUE, OptionalDouble.empty(),
                    OptionalDouble::isPresent, FindOps.FindSink.OfDouble::new);
        }
    }

    /**
     * {@code ForkJoinTask} implementing parallel short-circuiting search
     * @param <P_IN> Input element type to the stream pipeline
     * @param <P_OUT> Output element type from the stream pipeline
     * @param <O> Result type from the find operation
     */
    @SuppressWarnings("serial")
    private static final class FindTask<P_IN, P_OUT, O>
            extends AbstractShortCircuitTask<P_IN, P_OUT, O, FindTask<P_IN, P_OUT, O>> {
        private final FindOp<P_OUT, O> op;
        private final boolean mustFindFirst;

        FindTask(FindOp<P_OUT, O> op,
                 boolean mustFindFirst,
                 PipelineHelper<P_OUT> helper,
                 Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.mustFindFirst = mustFindFirst;
            this.op = op;
        }

        FindTask(FindTask<P_IN, P_OUT, O> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.mustFindFirst = parent.mustFindFirst;
            this.op = parent.op;
        }

        @Override
        protected FindTask<P_IN, P_OUT, O> makeChild(Spliterator<P_IN> spliterator) {
            return new FindTask<>(this, spliterator);
        }

        @Override
        protected O getEmptyResult() {
            return op.emptyValue;
        }

        private void foundResult(O answer) {
            if (isLeftmostNode())
                shortCircuit(answer);
            else
                cancelLaterNodes();
        }

        @Override
        protected O doLeaf() {
            O result = helper.wrapAndCopyInto(op.sinkSupplier.get(), spliterator).get();
            if (!mustFindFirst) {
                if (result != null)
                    shortCircuit(result);
                return null;
            }
            else {
                if (result != null) {
                    foundResult(result);
                    return result;
                }
                else
                    return null;
            }
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (mustFindFirst) {
                    for (FindTask<P_IN, P_OUT, O> child = leftChild, p = null; child != p;
                         p = child, child = rightChild) {
                    O result = child.getLocalResult();
                    if (result != null && op.presentPredicate.test(result)) {
                        setLocalResult(result);
                        foundResult(result);
                        break;
                    }
                }
            }
            super.onCompletion(caller);
        }
    }
}

