package svegon.utils.optional;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.bytes.*;
import svegon.utils.collections.stream.ByteStream;
import svegon.utils.interfaces.function.ByteSupplier;

import java.util.*;
import java.util.function.Supplier;

public class OptionalByte {
    private static final OptionalByte EMPTY = new OptionalByte();

    private OptionalByte() {

    }

    /**
     * Returns an empty {@code OptionalByte} instance.  No value is present for
     * this {@code OptionalByte}.
     *
     * @apiNote
     * Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} against instances returned by
     * {@code OptionalByte.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @return an empty {@code OptionalByte}.
     */
    public static OptionalByte empty() {
        return EMPTY;
    }

    /**
     * Returns an {@code OptionalByte} describing the given value.
     *
     * @param value the value to describe
     * @return an {@code OptionalByte} with the value present
     */
    public static OptionalByte of(byte value) {
        return new PresentOptionalByte(value);
    }

    /**
     * Returns an {@code OptionalByte} describing the given value.
     *
     * @param value the value to describe
     * @return an {@code OptionalByte} with the value present
     */
    public static OptionalByte of(Byte value) {
        return value == null ? empty() : new PresentOptionalByte(value);
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the value described by this {@code OptionalLong}
     * @throws NoSuchElementException if no value is present
     */
    public byte get() {
        throw new NoSuchElementException("No value present");
    }

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return false;
    }

    /**
     * If a value is not present, returns {@code true}, otherwise
     * {@code false}.
     *
     * @return  {@code true} if a value is not present, otherwise {@code false}
     * @since   11
     */
    public boolean isEmpty() {
        return !isPresent();
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     * @throws NullPointerException if value is present and the given action is
     *         {@code null}
     */
    public void ifPresent(ByteConsumer action) {
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is
     *        present
     * @throws NullPointerException if a value is present and the given action
     *         is {@code null}, or no value is present and the given empty-based
     *         action is {@code null}.
     * @since 9
     */
    public void ifPresentOrElse(ByteConsumer action, Runnable emptyAction) {
        emptyAction.run();
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code OptionalByte} describing the value, otherwise returns an
     * empty {@code OptionalByte}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code OptionalByte} describing the value of this
     *         {@code OptionalByte}, if a value is present and the value matches the
     *         given predicate, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public OptionalByte filter(BytePredicate predicate) {
        Preconditions.checkNotNull(predicate);
        return this;
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalByte}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalByte} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalByte map(ByteUnaryOperator mapper) {
        Preconditions.checkNotNull(mapper);
        return this;
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code #Optional.ofNullable}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code Optional<T>}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code Optional<T>} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public <T> Optional<T> mapToObject(Byte2ObjectFunction<? extends T> mapper) {
        Preconditions.checkNotNull(mapper);
        return Optional.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalBoolean}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalBoolean} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalBoolean mapToBoolean(BytePredicate mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalBoolean.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code OptionalShort.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalShort}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalShort} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalShort mapToShort(Byte2ShortFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalShort.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code OptionalInt.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalInt}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalInt} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalInt mapToInt(Byte2IntFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalInt.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code OptionalLong.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalLong}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalLong} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalLong mapToLong(Byte2LongFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalLong.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code OptionalChar.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalChar}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalChar} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalChar mapToChar(Byte2CharFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalChar.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code OptionalFloat.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalFloat}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalFloat} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalFloat mapToFloat(Byte2FloatFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalFloat.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing (as if by
     * {@code OptionalDouble.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalDouble}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalByte} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalDouble} describing the result of applying a mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalDouble mapToDouble(Byte2DoubleFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalDouble.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalByte}.
     *
     * <p>This method is similar to {@link #map(ByteUnaryOperator)}, but the mapping
     * function is one whose result is already an {@code OptionalByte}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code OptionalByte}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalByte}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalByte flatMap(Byte2ObjectFunction<? extends OptionalByte> mapper) {
        Preconditions.checkNotNull(mapper);
        return this;
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
     * an empty {@code Optional<T>}.
     *
     * <p>This method is similar to {@link #mapToObject(Byte2ObjectFunction)}, but the mapping
     * function is one whose result is already an {@code Optional<T>}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code Optional<T>}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code Optional<T>}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public <T> Optional<T> flatMapToObject(Byte2ObjectFunction<? extends Optional<? extends T>> mapper) {
        Preconditions.checkNotNull(mapper);
        return Optional.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalBoolean}.
     *
     * <p>This method is similar to {@link #mapToBoolean(BytePredicate)}, but the mapping
     * function is one whose result is already an {@code OptionalBoolean}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalBoolean}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalBoolean flatMapToBoolean(Byte2ObjectFunction<? extends OptionalBoolean> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalBoolean.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalShort}.
     *
     * <p>This method is similar to {@link #mapToShort(Byte2ShortFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalShort}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalShort}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalShort flatMapToShort(Byte2ObjectFunction<? extends OptionalShort> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalShort.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalInt}.
     *
     * <p>This method is similar to {@link #mapToInt(Byte2IntFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalInt}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalInt}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalInt flatMapToInt(Byte2ObjectFunction<? extends OptionalInt> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalInt.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalLong}.
     *
     * <p>This method is similar to {@link #mapToLong(Byte2LongFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalLong}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalLong}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalLong flatMapToLong(Byte2ObjectFunction<? extends OptionalLong> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalLong.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalChar}.
     *
     * <p>This method is similar to {@link #mapToChar(Byte2CharFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalChar}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalChar}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalChar flatMapToChar(Byte2ObjectFunction<? extends OptionalChar> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalChar.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalFloat}.
     *
     * <p>This method is similar to {@link #mapToFloat(Byte2FloatFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalFloat}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalFloat}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalFloat flatMapToFloat(Byte2ObjectFunction<? extends OptionalFloat> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalFloat.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalDouble}.
     *
     * <p>This method is similar to {@link #mapToDouble(Byte2DoubleFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalDouble}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalDouble}-bearing mapping
     *         function to the value of this {@code OptionalByte}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalDouble flatMapToDouble(Byte2ObjectFunction<? extends OptionalDouble> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalDouble.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalByte} describing the value,
     * otherwise returns an {@code OptionalByte} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code OptionalByte}
     *        to be returned
     * @return returns an {@code OptionalByte} describing the value of this
     *         {@code OptionalByte}, if a value is present, otherwise an
     *         {@code OptionalByte} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null} or
     *         produces a {@code null} result
     * @since 9
     */
    public OptionalByte or(Supplier<? extends OptionalByte> supplier) {
        return Preconditions.checkNotNull(supplier.get());
    }

    /**
     * If a value is present, returns a sequential {@link ByteStream} containing
     * only that value, otherwise returns an empty {@code ByteStream}.
     *
     * @apiNote
     * This method can be used to transform a {@code Stream} of optional longs
     * to an {@code ByteStream} of present longs:
     * <pre>{@code
     *     Stream<OptionalLong> os = ..
     *     ByteStream s = os.flatMapToLong(OptionalByte::stream)
     * }</pre>
     *
     * @return the optional value as an {@code ByteStream}
     * @since 1.0.0
     */
    public ByteStream stream() {
        return ByteStream.empty();
    }

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present
     * @return the value, if present, otherwise {@code other}
     */
    public byte orElse(byte other) {
        return other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     *         supplying function
     * @throws NullPointerException if no value is present and the supplying
     *         function is {@code null}
     */
    public byte orElseGet(ByteSupplier supplier) {
        return supplier.getAsByte();
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the value described by this {@code OptionalLong}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public byte orElseThrow() {
        throw new NoSuchElementException("No value present");
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception
     * produced by the exception supplying function.
     *
     * @apiNote
     * A method reference to the exception constructor with an empty argument
     * list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an
     *        exception to be thrown
     * @return the value, if present
     * @throws X if no value is present
     * @throws NullPointerException if no value is present and the exception
     *         supplying function is {@code null}
     */
    public<X extends Throwable> byte orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        throw exceptionSupplier.get();
    }

    /**
     * Indicates whether some other object is "equal to" this
     * {@code OptionalLong}.  The other object is considered equal if:
     * <ul>
     * <li>it is also an {@code OptionalLong} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code ==}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is "equal to" this object
     *         otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    /**
     * Returns the hash code of the value, if present, otherwise {@code 0}
     * (zero) if no value is present.
     *
     * @return hash code value of the present value or {@code 0} if no value is
     *         present
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Returns a non-empty string representation of this {@code OptionalLong}
     * suitable for debugging.  The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @implSpec
     * If a value is present the result must include its string representation
     * in the result.  Empty and present {@code OptionalLong}s must be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return "OptionalByte.empty";
    }

    private static final class PresentOptionalByte extends OptionalByte {
        private final byte value;

        private PresentOptionalByte(byte value) {
            this.value = value;
        }

        /**
         * If a value is present, returns the value, otherwise throws
         * {@code NoSuchElementException}.
         *
         * @apiNote
         * The preferred alternative to this method is {@link #orElseThrow()}.
         *
         * @return the value described by this {@code OptionalLong}
         * @throws NoSuchElementException if no value is present
         */
        @Override
        public byte get() {
            return value;
        }

        /**
         * If a value is present, returns {@code true}, otherwise {@code false}.
         *
         * @return {@code true} if a value is present, otherwise {@code false}
         */
        @Override
        public boolean isPresent() {
            return true;
        }

        /**
         * If a value is present, performs the given action with the value,
         * otherwise does nothing.
         *
         * @param action the action to be performed, if a value is present
         * @throws NullPointerException if value is present and the given action is
         *         {@code null}
         */
        @Override
        public void ifPresent(ByteConsumer action) {
            action.accept(value);
        }

        /**
         * If a value is present, performs the given action with the value,
         * otherwise performs the given empty-based action.
         *
         * @param action the action to be performed, if a value is present
         * @param emptyAction the empty-based action to be performed, if no value is
         *        present
         * @throws NullPointerException if a value is present and the given action
         *         is {@code null}, or no value is present and the given empty-based
         *         action is {@code null}.
         * @since 9
         */
        @Override
        public void ifPresentOrElse(ByteConsumer action, Runnable emptyAction) {
            action.accept(value);
        }

        /**
         * If a value is present, and the value matches the given predicate,
         * returns an {@code OptionalByte} describing the value, otherwise returns an
         * empty {@code OptionalByte}.
         *
         * @param predicate the predicate to apply to a value, if present
         * @return an {@code OptionalByte} describing the value of this
         *         {@code OptionalByte}, if a value is present and the value matches the
         *         given predicate, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the predicate is {@code null}
         */
        @Override
        public OptionalByte filter(BytePredicate predicate) {
            return predicate.test(get()) ? this : OptionalByte.empty();
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@link #of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalByte}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalByte} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalByte map(ByteUnaryOperator mapper) {
            return of(mapper.apply(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code #Optional.ofNullable}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code Optional<T>}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code Optional<T>} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code Optional<T>}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public <T> Optional<T> mapToObject(Byte2ObjectFunction<? extends T> mapper) {
            return Optional.ofNullable(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@link #of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalBoolean}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalBoolean} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalBoolean}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalBoolean mapToBoolean(BytePredicate mapper) {
            return OptionalBoolean.of(mapper.test(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code OptionalShort.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalShort}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalShort} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalShort mapToShort(Byte2ShortFunction mapper) {
            return OptionalShort.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code OptionalInt.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalInt}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalInt} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalInt}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalInt mapToInt(Byte2IntFunction mapper) {
            return OptionalInt.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code OptionalLong.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalLong}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalLong} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalLong}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalLong mapToLong(Byte2LongFunction mapper) {
            return OptionalLong.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code OptionalChar.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalChar}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalChar} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalChar}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalChar mapToChar(Byte2CharFunction mapper) {
            return OptionalChar.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code OptionalFloat.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalFloat}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalFloat} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalFloat mapToFloat(Byte2FloatFunction mapper) {
            return OptionalFloat.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing (as if by
         * {@code OptionalDouble.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalDouble}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalByte} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalDouble} describing the result of applying a mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalDouble}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalDouble mapToDouble(Byte2DoubleFunction mapper) {
            return OptionalDouble.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalByte}.
         *
         * <p>This method is similar to {@link #map(ByteUnaryOperator)}, but the mapping
         * function is one whose result is already an {@code OptionalByte}, and if
         * invoked, {@code flatMap} does not wrap it within an additional
         * {@code OptionalByte}.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalByte}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalByte flatMap(Byte2ObjectFunction<? extends OptionalByte> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
         * an empty {@code Optional<T>}.
         *
         * <p>This method is similar to {@link #mapToObject(Byte2ObjectFunction)}, but the mapping
         * function is one whose result is already an {@code Optional<T>}, and if
         * invoked, {@code flatMap} does not wrap it within an additional
         * {@code Optional<T>}.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code Optional<T>}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code Optional<T>}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> flatMapToObject(Byte2ObjectFunction<? extends Optional<? extends T>> mapper) {
            return (Optional<T>) Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalBoolean}.
         *
         * <p>This method is similar to {@link #mapToBoolean(BytePredicate)}, but the mapping
         * function is one whose result is already an {@code OptionalBoolean}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalBoolean}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalBoolean}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalBoolean flatMapToBoolean(Byte2ObjectFunction<? extends OptionalBoolean> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalShort}.
         *
         * <p>This method is similar to {@link #mapToShort(Byte2ShortFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalShort}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalShort}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalShort flatMapToShort(Byte2ObjectFunction<? extends OptionalShort> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalInt}.
         *
         * <p>This method is similar to {@link #mapToInt(Byte2IntFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalInt}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalInt}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalInt}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalInt flatMapToInt(Byte2ObjectFunction<? extends OptionalInt> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalLong}.
         *
         * <p>This method is similar to {@link #mapToLong(Byte2LongFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalLong}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalLong}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalLong}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalLong flatMapToLong(Byte2ObjectFunction<? extends OptionalLong> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalChar}.
         *
         * <p>This method is similar to {@link #mapToChar(Byte2CharFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalChar}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalChar}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalChar}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalChar flatMapToChar(Byte2ObjectFunction<? extends OptionalChar> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalFloat}.
         *
         * <p>This method is similar to {@link #mapToFloat(Byte2FloatFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalFloat}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalFloat}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalFloat flatMapToFloat(Byte2ObjectFunction<? extends OptionalFloat> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalDouble}.
         *
         * <p>This method is similar to {@link #mapToDouble(Byte2DoubleFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalDouble}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalDouble}-bearing mapping
         *         function to the value of this {@code OptionalByte}, if a value is
         *         present, otherwise an empty {@code OptionalDouble}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalDouble flatMapToDouble(Byte2ObjectFunction<? extends OptionalDouble> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalByte} describing the value,
         * otherwise returns an {@code OptionalByte} produced by the supplying function.
         *
         * @param supplier the supplying function that produces an {@code OptionalByte}
         *        to be returned
         * @return returns an {@code OptionalByte} describing the value of this
         *         {@code OptionalByte}, if a value is present, otherwise an
         *         {@code OptionalByte} produced by the supplying function.
         * @throws NullPointerException if the supplying function is {@code null} or
         *         produces a {@code null} result
         * @since 1.0.0
         */
        @Override
        public OptionalByte or(Supplier<? extends OptionalByte> supplier) {
            Preconditions.checkNotNull(supplier);
            return this;
        }

        /**
         * If a value is present, returns a sequential {@link ByteStream} containing
         * only that value, otherwise returns an empty {@code ByteStream}.
         *
         * @apiNote
         * This method can be used to transform a {@code Stream} of optional bytes
         * to an {@code ByteStream} of present longs:
         * <pre>{@code
         *     Stream<OptionalByte> os = ..
         *     ByteStream s = os.flatMapToLong(OptionalByte::stream)
         * }</pre>
         *
         * @return the optional value as an {@code ByteStream}
         * @since 16.0.1
         */
        @Override
        public ByteStream stream() {
            return ByteStream.of(value);
        }

        /**
         * If a value is present, returns the value, otherwise returns
         * {@code other}.
         *
         * @param other the value to be returned, if no value is present
         * @return the value, if present, otherwise {@code other}
         */
        @Override
        public byte orElse(byte other) {
            return value;
        }

        /**
         * If a value is present, returns the value, otherwise returns the result
         * produced by the supplying function.
         *
         * @param supplier the supplying function that produces a value to be returned
         * @return the value, if present, otherwise the result produced by the
         *         supplying function
         * @throws NullPointerException if no value is present and the supplying
         *         function is {@code null}
         */
        @Override
        public byte orElseGet(ByteSupplier supplier) {
            return value;
        }

        /**
         * If a value is present, returns the value, otherwise throws
         * {@code NoSuchElementException}.
         *
         * @return the value described by this {@code OptionalLong}
         * @throws NoSuchElementException if no value is present
         * @since 10
         */
        @Override
        public byte orElseThrow() {
            return value;
        }

        /**
         * If a value is present, returns the value, otherwise throws an exception
         * produced by the exception supplying function.
         *
         * @apiNote
         * A method reference to the exception constructor with an empty argument
         * list can be used as the supplier. For example,
         * {@code IllegalStateException::new}
         *
         * @param <X> Type of the exception to be thrown
         * @param exceptionSupplier the supplying function that produces an
         *        exception to be thrown
         * @return the value, if present
         * @throws X if no value is present
         * @throws NullPointerException if no value is present and the exception
         *         supplying function is {@code null}
         */
        @Override
        public<X extends Throwable> byte orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return value;
        }

        /**
         * Indicates whether some other object is "equal to" this
         * {@code OptionalLong}.  The other object is considered equal if:
         * <ul>
         * <li>it is also an {@code OptionalLong} and;
         * <li>both instances have no value present or;
         * <li>the present values are "equal to" each other via {@code ==}.
         * </ul>
         *
         * @param obj an object to be tested for equality
         * @return {@code true} if the other object is "equal to" this object
         *         otherwise {@code false}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof PresentOptionalByte optional)) {
                return false;
            }

            return value == optional.value;
        }

        /**
         * Returns the hash code of the value, if present, otherwise {@code 0}
         * (zero) if no value is present.
         *
         * @return hash code value of the present value or {@code 0} if no value is
         *         present
         */
        @Override
        public int hashCode() {
            return Byte.hashCode(value);
        }

        /**
         * Returns a non-empty string representation of this {@code OptionalLong}
         * suitable for debugging.  The exact presentation format is unspecified and
         * may vary between implementations and versions.
         *
         * @implSpec
         * If a value is present the result must include its string representation
         * in the result.  Empty and present {@code OptionalLong}s must be
         * unambiguously differentiable.
         *
         * @return the string representation of this instance
         */
        @Override
        public String toString() {
            return String.format("OptionalByte[%s]", value);
        }
    }
}
