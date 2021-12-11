package com.github.svegon.utils.optional;

import com.github.svegon.utils.interfaces.function.FloatSupplier;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.floats.*;
import com.github.svegon.utils.collections.stream.FloatStream;

import java.util.*;
import java.util.function.Supplier;

public class OptionalFloat {
    private static final OptionalFloat EMPTY = new OptionalFloat();

    private OptionalFloat() {

    }

    /**
     * Returns an empty {@code OptionalFloat} instance.  No value is present for
     * this {@code OptionalFloat}.
     *
     * @apiNote
     * Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} against instances returned by
     * {@code OptionalFloat.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @return an empty {@code OptionalFloat}.
     */
    public static OptionalFloat empty() {
        return EMPTY;
    }

    /**
     * Returns an {@code OptionalFloat} describing the given value.
     *
     * @param value the value to describe
     * @return an {@code OptionalFloat} with the value present
     */
    public static OptionalFloat of(float value) {
        return new PresentOptionalFloat(value);
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the value described by this {@code OptionalFloat}
     * @throws NoSuchElementException if no value is present
     */
    public float get() {
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
    public void ifPresent(FloatConsumer action) {
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
    public void ifPresentOrElse(FloatConsumer action, Runnable emptyAction) {
        emptyAction.run();
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code OptionalFloat} describing the value, otherwise returns an
     * empty {@code OptionalFloat}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code OptionalFloat} describing the value of this
     *         {@code OptionalFloat}, if a value is present and the value matches the
     *         given predicate, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public OptionalFloat filter(FloatPredicate predicate) {
        Preconditions.checkNotNull(predicate);
        return this;
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalFloat}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalFloat} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalFloat map(FloatUnaryOperator mapper) {
        Preconditions.checkNotNull(mapper);
        return this;
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code #Optional.ofNullable}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code Optional<T>}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code Optional<T>} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public <T> Optional<T> mapToObject(Float2ObjectFunction<? extends T> mapper) {
        Preconditions.checkNotNull(mapper);
        return Optional.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalBoolean}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalBoolean} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalBoolean mapToBoolean(FloatPredicate mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalBoolean.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code OptionalByte.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalByte}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalByte} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalByte mapToByte(Float2ByteFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalByte.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code OptionalShort.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalShort}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalShort} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalShort mapToShort(Float2ShortFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalShort.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code OptionalInt.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalInt}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalInt} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalInt mapToInt(Float2IntFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalInt.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code OptionalLong.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalLong}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalLong} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalLong mapToLong(Float2LongFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalLong.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code OptionalChar.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalChar}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalChar} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalChar mapToChar(Float2CharFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalChar.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing (as if by
     * {@code OptionalDouble.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalDouble}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalFloat} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalDouble} describing the result of applying a mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalDouble mapToDouble(Float2DoubleFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalDouble.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalFloat}.
     *
     * <p>This method is similar to {@link #map(FloatUnaryOperator)}, but the mapping
     * function is one whose result is already an {@code OptionalFloat}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code OptionalFloat}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalFloat}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalFloat flatMap(Float2ObjectFunction<? extends OptionalFloat> mapper) {
        Preconditions.checkNotNull(mapper);
        return this;
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
     * an empty {@code Optional<T>}.
     *
     * <p>This method is similar to {@link #mapToObject(Float2ObjectFunction)}, but the mapping
     * function is one whose result is already an {@code Optional<T>}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code Optional<T>}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code Optional<T>}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public <T> Optional<T> flatMapToObject(Float2ObjectFunction<? extends Optional<? extends T>> mapper) {
        Preconditions.checkNotNull(mapper);
        return Optional.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalBoolean}.
     *
     * <p>This method is similar to {@link #mapToBoolean(FloatPredicate)}, but the mapping
     * function is one whose result is already an {@code OptionalBoolean}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalBoolean}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalBoolean flatMapToBoolean(Float2ObjectFunction<? extends OptionalBoolean> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalBoolean.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalByte}.
     *
     * <p>This method is similar to {@link #mapToByte(Float2ByteFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalByte}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalByte}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalByte flatMapToByte(Float2ObjectFunction<? extends OptionalByte> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalByte.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalShort}.
     *
     * <p>This method is similar to {@link #mapToShort(Float2ShortFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalShort}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalShort}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalShort flatMapToShort(Float2ObjectFunction<? extends OptionalShort> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalShort.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalInt}.
     *
     * <p>This method is similar to {@link #mapToInt(Float2IntFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalInt}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalInt}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalInt flatMapToInt(Float2ObjectFunction<? extends OptionalInt> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalInt.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalLong}.
     *
     * <p>This method is similar to {@link #mapToLong(Float2LongFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalLong}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalLong}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalLong flatMapToLong(Float2ObjectFunction<? extends OptionalLong> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalLong.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalChar}.
     *
     * <p>This method is similar to {@link #mapToChar(Float2CharFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalChar}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalChar}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalChar flatMapToChar(Float2ObjectFunction<? extends OptionalChar> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalChar.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalDouble}.
     *
     * <p>This method is similar to {@link #mapToDouble(Float2DoubleFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalDouble}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalDouble}-bearing mapping
     *         function to the value of this {@code OptionalFloat}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalDouble flatMapToDouble(Float2ObjectFunction<? extends OptionalDouble> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalDouble.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalFloat} describing the value,
     * otherwise returns an {@code OptionalFloat} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code OptionalFloat}
     *        to be returned
     * @return returns an {@code OptionalFloat} describing the value of this
     *         {@code OptionalFloat}, if a value is present, otherwise an
     *         {@code OptionalFloat} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null} or
     *         produces a {@code null} result
     * @since 9
     */
    public OptionalFloat or(Supplier<? extends OptionalFloat> supplier) {
        return Preconditions.checkNotNull(supplier.get());
    }

    /**
     * If a value is present, returns a sequential {@link FloatStream} containing
     * only that value, otherwise returns an empty {@code FloatStream}.
     *
     * @apiNote
     * This method can be used to transform a {@code Stream} of optional Floats
     * to an {@code FloatStream} of present Floats:
     * <pre>{@code
     *     Stream<OptionalFloat> os = ..
     *     FloatStream s = os.flatMapToFloat(OptionalFloat::stream)
     * }</pre>
     *
     * @return the optional value as an {@code FloatStream}
     * @since 9
     */
    public FloatStream stream() {
        return FloatStream.empty();
    }

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present
     * @return the value, if present, otherwise {@code other}
     */
    public float orElse(float other) {
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
    public float orElseGet(FloatSupplier supplier) {
        return supplier.getAsFloat();
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the value described by this {@code OptionalFloat}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public float orElseThrow() {
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
    public<X extends Throwable> float orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        throw exceptionSupplier.get();
    }

    /**
     * Indicates whether some other object is "equal to" this
     * {@code OptionalFloat}.  The other object is considered equal if:
     * <ul>
     * <li>it is also an {@code OptionalFloat} and;
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
     * Returns a non-empty string representation of this {@code OptionalFloat}
     * suitable for debugging.  The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @implSpec
     * If a value is present the result must include its string representation
     * in the result.  Empty and present {@code OptionalFloat}s must be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return "OptionalFloat.empty";
    }

    private static final class PresentOptionalFloat extends OptionalFloat {
        private final float value;

        private PresentOptionalFloat(float value) {
            this.value = value;
        }

        /**
         * If a value is present, returns the value, otherwise throws
         * {@code NoSuchElementException}.
         *
         * @apiNote
         * The preferred alternative to this method is {@link #orElseThrow()}.
         *
         * @return the value described by this {@code OptionalFloat}
         * @throws NoSuchElementException if no value is present
         */
        @Override
        public float get() {
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
        public void ifPresent(FloatConsumer action) {
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
        public void ifPresentOrElse(FloatConsumer action, Runnable emptyAction) {
            action.accept(value);
        }

        /**
         * If a value is present, and the value matches the given predicate,
         * returns an {@code OptionalFloat} describing the value, otherwise returns an
         * empty {@code OptionalFloat}.
         *
         * @param predicate the predicate to apply to a value, if present
         * @return an {@code OptionalFloat} describing the value of this
         *         {@code OptionalFloat}, if a value is present and the value matches the
         *         given predicate, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the predicate is {@code null}
         */
        @Override
        public OptionalFloat filter(FloatPredicate predicate) {
            return predicate.test(get()) ? this : OptionalFloat.empty();
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@link #of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalFloat}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalFloat} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalFloat map(FloatUnaryOperator mapper) {
            return of(mapper.apply(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code #Optional.ofNullable}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code Optional<T>}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code Optional<T>} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code Optional<T>}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public <T> Optional<T> mapToObject(Float2ObjectFunction<? extends T> mapper) {
            return Optional.ofNullable(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@link #of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalBoolean}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalBoolean} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalBoolean}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalBoolean mapToBoolean(FloatPredicate mapper) {
            return OptionalBoolean.of(mapper.test(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code OptionalByte.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalByte}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalByte} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalByte mapToByte(Float2ByteFunction mapper) {
            return OptionalByte.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code OptionalShort.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalShort}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalShort} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalShort mapToShort(Float2ShortFunction mapper) {
            return OptionalShort.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code OptionalInt.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalInt}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalInt} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalInt}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalInt mapToInt(Float2IntFunction mapper) {
            return OptionalInt.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code OptionalLong.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalLong}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalLong} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalLong}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalLong mapToLong(Float2LongFunction mapper) {
            return OptionalLong.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code OptionalChar.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalChar}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalChar} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalChar}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalChar mapToChar(Float2CharFunction mapper) {
            return OptionalChar.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing (as if by
         * {@code OptionalDouble.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalDouble}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalFloat} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalDouble} describing the result of applying a mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalDouble}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalDouble mapToDouble(Float2DoubleFunction mapper) {
            return OptionalDouble.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalFloat}.
         *
         * <p>This method is similar to {@link #map(FloatUnaryOperator)}, but the mapping
         * function is one whose result is already an {@code OptionalFloat}, and if
         * invoked, {@code flatMap} does not wrap it within an additional
         * {@code OptionalFloat}.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalFloat}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalFloat flatMap(Float2ObjectFunction<? extends OptionalFloat> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
         * an empty {@code Optional<T>}.
         *
         * <p>This method is similar to {@link #mapToObject(Float2ObjectFunction)}, but the mapping
         * function is one whose result is already an {@code Optional<T>}, and if
         * invoked, {@code flatMap} does not wrap it within an additional
         * {@code Optional<T>}.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code Optional<T>}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code Optional<T>}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> flatMapToObject(Float2ObjectFunction<? extends Optional<? extends T>> mapper) {
            return (Optional<T>) Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalBoolean}.
         *
         * <p>This method is similar to {@link #mapToBoolean(FloatPredicate)}, but the mapping
         * function is one whose result is already an {@code OptionalBoolean}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalBoolean}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalBoolean}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalBoolean flatMapToBoolean(Float2ObjectFunction<? extends OptionalBoolean> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalByte}.
         *
         * <p>This method is similar to {@link #mapToByte(Float2ByteFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalByte}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalByte}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalByte flatMapToByte(Float2ObjectFunction<? extends OptionalByte> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalShort}.
         *
         * <p>This method is similar to {@link #mapToShort(Float2ShortFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalShort}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalShort}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalShort flatMapToShort(Float2ObjectFunction<? extends OptionalShort> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalInt}.
         *
         * <p>This method is similar to {@link #mapToInt(Float2IntFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalInt}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalInt}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalInt}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalInt flatMapToInt(Float2ObjectFunction<? extends OptionalInt> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalLong}.
         *
         * <p>This method is similar to {@link #mapToLong(Float2LongFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalLong}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalLong}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalLong}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalLong flatMapToLong(Float2ObjectFunction<? extends OptionalLong> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalChar}.
         *
         * <p>This method is similar to {@link #mapToChar(Float2CharFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalChar}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalChar}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalChar}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalChar flatMapToChar(Float2ObjectFunction<? extends OptionalChar> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalDouble}.
         *
         * <p>This method is similar to {@link #mapToDouble(Float2DoubleFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalDouble}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalDouble}-bearing mapping
         *         function to the value of this {@code OptionalFloat}, if a value is
         *         present, otherwise an empty {@code OptionalDouble}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalDouble flatMapToDouble(Float2ObjectFunction<? extends OptionalDouble> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalFloat} describing the value,
         * otherwise returns an {@code OptionalFloat} produced by the supplying function.
         *
         * @param supplier the supplying function that produces an {@code OptionalFloat}
         *        to be returned
         * @return returns an {@code OptionalFloat} describing the value of this
         *         {@code OptionalFloat}, if a value is present, otherwise an
         *         {@code OptionalFloat} produced by the supplying function.
         * @throws NullPointerException if the supplying function is {@code null} or
         *         produces a {@code null} result
         * @since 1.0.0
         */
        @Override
        public OptionalFloat or(Supplier<? extends OptionalFloat> supplier) {
            Preconditions.checkNotNull(supplier);
            return this;
        }

        /**
         * If a value is present, returns a sequential {@link FloatStream} containing
         * only that value, otherwise returns an empty {@code FloatStream}.
         *
         * @apiNote
         * This method can be used to transform a {@code Stream} of optional Floats
         * to an {@code FloatStream} of present Floats:
         * <pre>{@code
         *     Stream<OptionalFloat> os = ..
         *     FloatStream s = os.flatMapToFloat(OptionalFloat::stream)
         * }</pre>
         *
         * @return the optional value as an {@code FloatStream}
         * @since 9
         */
        @Override
        public FloatStream stream() {
            return FloatStream.of(value);
        }

        /**
         * If a value is present, returns the value, otherwise returns
         * {@code other}.
         *
         * @param other the value to be returned, if no value is present
         * @return the value, if present, otherwise {@code other}
         */
        @Override
        public float orElse(float other) {
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
        public float orElseGet(FloatSupplier supplier) {
            return value;
        }

        /**
         * If a value is present, returns the value, otherwise throws
         * {@code NoSuchElementException}.
         *
         * @return the value described by this {@code OptionalFloat}
         * @throws NoSuchElementException if no value is present
         * @since 10
         */
        @Override
        public float orElseThrow() {
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
        public<X extends Throwable> float orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return value;
        }

        /**
         * Indicates whether some other object is "equal to" this
         * {@code OptionalFloat}.  The other object is considered equal if:
         * <ul>
         * <li>it is also an {@code OptionalFloat} and;
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

            if (!(obj instanceof PresentOptionalFloat optional)) {
                return false;
            }

            return Float.compare(value, optional.value) == 0;
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
            return Float.hashCode(value);
        }

        /**
         * Returns a non-empty string representation of this {@code OptionalFloat}
         * suitable for debugging.  The exact presentation format is unspecified and
         * may vary between implementations and versions.
         *
         * @implSpec
         * If a value is present the result must include its string representation
         * in the result.  Empty and present {@code OptionalFloat}s must be
         * unambiguously differentiable.
         *
         * @return the string representation of this instance
         */
        @Override
        public String toString() {
            return String.format("OptionalFloat[%s]", value);
        }
    }
}
