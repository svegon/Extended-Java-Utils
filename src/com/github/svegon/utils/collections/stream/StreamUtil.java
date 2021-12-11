package com.github.svegon.utils.collections.stream;

import com.github.svegon.utils.fast.util.booleans.BooleanPipeline;
import com.github.svegon.utils.fast.util.floats.FloatPipeline;
import com.github.svegon.utils.fast.util.shorts.ShortPipeline;
import com.github.svegon.utils.fuck_modifiers.*;
import com.github.svegon.utils.interfaces.function.Object2ByteFunction;
import com.github.svegon.utils.interfaces.function.Object2CharFunction;
import com.github.svegon.utils.interfaces.function.Object2FloatFunction;
import com.github.svegon.utils.interfaces.function.Object2ShortFunction;
import com.github.svegon.utils.fast.util.bytes.BytePipeline;
import com.github.svegon.utils.fast.util.chars.CharPipeline;
import svegon.utils.fuck_modifiers.*;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class StreamUtil {
    private StreamUtil() {
        throw new AssertionError();
    }

    /**
     * Creates a new sequential or parallel {@code BooleanStream} from a
     * {@code Spliterator.BooleanSpliterator}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #booleanStream(Supplier, int, boolean)} should
     * be used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator A {@code BooleanSpliterator} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code BooleanStream}
     */
    public static BooleanStream booleanStream(BooleanSpliterator spliterator, boolean parallel) {
        return new BooleanPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code BooleanStream} from a
     * {@code Supplier} of {@code BooleanSpliterator}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #booleanStream(BooleanSpliterator, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier A {@code Supplier} of a {@code Spliterator.OfBoolean}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code BooleanSpliterator}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code BooleanStream}
     * @see #booleanStream(BooleanSpliterator, boolean)
     */
    public static BooleanStream booleanStream(Supplier<? extends BooleanSpliterator> supplier,
                                        int characteristics, boolean parallel) {
        return new BooleanPipeline.Head<>(supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code ByteStream} from a
     * {@code Spliterator.ByteSpliterator}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #byteStream(Supplier, int, boolean)} should
     * be used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator A {@code ByteSpliterator} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code ByteStream}
     */
    public static ByteStream byteStream(ByteSpliterator spliterator, boolean parallel) {
        return new BytePipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code ByteStream} from a
     * {@code Supplier} of {@code ByteSpliterator}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #byteStream(ByteSpliterator, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier A {@code Supplier} of a {@code Spliterator.OfByte}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code ByteSpliterator}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code ByteStream}
     * @see #byteStream(ByteSpliterator, boolean)
     */
    public static ByteStream byteStream(Supplier<? extends ByteSpliterator> supplier,
                                        int characteristics, boolean parallel) {
        return new BytePipeline.Head<>(supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code ShortStream} from a
     * {@code Spliterator.ShortSpliterator}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #shortStream(Supplier, int, boolean)} should
     * be used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator A {@code ShortSpliterator} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code ShortStream}
     */
    public static ShortStream shortStream(ShortSpliterator spliterator, boolean parallel) {
        return new ShortPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code ShortStream} from a
     * {@code Supplier} of {@code ShortSpliterator}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #shortStream(ShortSpliterator, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier A {@code Supplier} of a {@code Spliterator.OfShort}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code ShortSpliterator}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code ShortStream}
     * @see #shortStream(ShortSpliterator, boolean)
     */
    public static ShortStream shortStream(Supplier<? extends ShortSpliterator> supplier,
                                          int characteristics, boolean parallel) {
        return new ShortPipeline.Head<>(supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code CharStream} from a
     * {@code Spliterator.CharSpliterator}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #charStream(Supplier, int, boolean)} should
     * be used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator A {@code CharSpliterator} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code CharStream}
     */
    public static CharStream charStream(CharSpliterator spliterator, boolean parallel) {
        return new CharPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code CharStream} from a
     * {@code Supplier} of {@code CharSpliterator}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #charStream(CharSpliterator, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier A {@code Supplier} of a {@code Spliterator.OfChar}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code CharSpliterator}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code CharStream}
     * @see #charStream(CharSpliterator, boolean)
     */
    public static CharStream charStream(Supplier<? extends CharSpliterator> supplier,
                                          int characteristics, boolean parallel) {
        return new CharPipeline.Head<>(supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code FloatStream} from a
     * {@code Spliterator.FloatSpliterator}.
     *
     * <p>The spliterator is only traversed, split, or queried for estimated size
     * after the terminal operation of the stream pipeline commences.
     *
     * <p>It is strongly recommended the spliterator report a characteristic of
     * {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <a href="../Spliterator.html#binding">late-binding</a>.  Otherwise,
     * {@link #floatStream(Supplier, int, boolean)} should
     * be used to reduce the scope of potential interference with the source.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param spliterator A {@code FloatSpliterator} describing the stream elements
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code FloatStream}
     */
    public static FloatStream floatStream(FloatSpliterator spliterator, boolean parallel) {
        return new FloatPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    /**
     * Creates a new sequential or parallel {@code FloatStream} from a
     * {@code Supplier} of {@code FloatSpliterator}.
     *
     * <p>The {@link Supplier#get()} method will be invoked on the supplier no
     * more than once, and only after the terminal operation of the stream pipeline
     * commences.
     *
     * <p>For spliterators that report a characteristic of {@code IMMUTABLE}
     * or {@code CONCURRENT}, or that are
     * <a href="../Spliterator.html#binding">late-binding</a>, it is likely
     * more efficient to use {@link #floatStream(FloatSpliterator, boolean)}
     * instead.
     * <p>The use of a {@code Supplier} in this form provides a level of
     * indirection that reduces the scope of potential interference with the
     * source.  Since the supplier is only invoked after the terminal operation
     * commences, any modifications to the source up to the start of the
     * terminal operation are reflected in the stream result.  See
     * <a href="package-summary.html#NonInterference">Non-Interference</a> for
     * more details.
     *
     * @param supplier A {@code Supplier} of a {@code Spliterator.OfFloat}
     * @param characteristics Spliterator characteristics of the supplied
     *        {@code FloatSpliterator}.  The characteristics must be equal to
     *        {@code supplier.get().characteristics()}, otherwise undefined
     *        behavior may occur when terminal operation commences.
     * @param parallel if {@code true} then the returned stream is a parallel
     *        stream; if {@code false} the returned stream is a sequential
     *        stream.
     * @return a new sequential or parallel {@code FloatStream}
     * @see #floatStream(FloatSpliterator, boolean)
     */
    public static FloatStream floatStream(Supplier<? extends FloatSpliterator> supplier,
                                          int characteristics, boolean parallel) {
        return new FloatPipeline.Head<>(supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static <E> BooleanStream mapToBoolean(final Stream<E> stream, final Predicate<? super E> mapper) {
        Objects.requireNonNull(mapper);
        return new BooleanPipeline.StatelessOp<E>(stream instanceof AbstractPipeline pipeline ? pipeline
                : new ReferencePipeline.Head<>(stream.spliterator(),
                StreamOpFlag.fromCharacteristics(stream.spliterator()), stream.isParallel()), StreamShape.REFERENCE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<E> opWrapSink(int flags, Sink<Boolean> sink) {
                return new Sink.ChainedReference<>(sink) {
                    @Override
                    public void accept(E u) {
                        downstream.accept(mapper.test(u));
                    }
                };
            }
        };
    }

    public static <E> ByteStream mapToByte(final Stream<E> stream, final Object2ByteFunction<? super E> mapper) {
        Objects.requireNonNull(mapper);
        return new BytePipeline.StatelessOp<E>(stream instanceof AbstractPipeline pipeline ? pipeline
                : new ReferencePipeline.Head<>(stream.spliterator(),
                StreamOpFlag.fromCharacteristics(stream.spliterator()), stream.isParallel()), StreamShape.REFERENCE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<E> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedReference<>(sink) {
                    @Override
                    public void accept(E u) {
                        downstream.accept(mapper.applyToByte(u));
                    }
                };
            }
        };
    }

    public static <E> ShortStream mapToShort(final Stream<E> stream, final Object2ShortFunction<? super E> mapper) {
        Objects.requireNonNull(mapper);
        return new ShortPipeline.StatelessOp<E>(stream instanceof AbstractPipeline pipeline ? pipeline
                : new ReferencePipeline.Head<>(stream.spliterator(),
                StreamOpFlag.fromCharacteristics(stream.spliterator()), stream.isParallel()), StreamShape.REFERENCE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<E> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedReference<>(sink) {
                    @Override
                    public void accept(E u) {
                        downstream.accept(mapper.applyToShort(u));
                    }
                };
            }
        };
    }

    public static <E> CharStream mapToChar(final Stream<E> stream, final Object2CharFunction<? super E> mapper) {
        Objects.requireNonNull(mapper);
        return new CharPipeline.StatelessOp<E>(stream instanceof AbstractPipeline pipeline ? pipeline
                : new ReferencePipeline.Head<>(stream.spliterator(),
                StreamOpFlag.fromCharacteristics(stream.spliterator()), stream.isParallel()), StreamShape.REFERENCE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<E> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedReference<>(sink) {
                    @Override
                    public void accept(E u) {
                        downstream.accept(mapper.applyToChar(u));
                    }
                };
            }
        };
    }

    public static <E> FloatStream mapToFloat(final Stream<E> stream, final Object2FloatFunction<? super E> mapper) {
        Objects.requireNonNull(mapper);
        return new FloatPipeline.StatelessOp<E>(stream instanceof AbstractPipeline pipeline ? pipeline
                : new ReferencePipeline.Head<>(stream.spliterator(),
                StreamOpFlag.fromCharacteristics(stream.spliterator()), stream.isParallel()), StreamShape.REFERENCE,
                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<E> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedReference<>(sink) {
                    @Override
                    public void accept(E u) {
                        downstream.accept(mapper.applyToFloat(u));
                    }
                };
            }
        };
    }

    public static <E> boolean isEmpty(@NotNull Stream<E> stream) {
        return stream.noneMatch((e) -> true);
    }
}
