package svegon.utils.optional;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.shorts.*;
import svegon.utils.collections.stream.ShortStream;
import svegon.utils.interfaces.function.ShortSupplier;

import java.util.*;
import java.util.function.Supplier;

public class OptionalShort {
    private static final OptionalShort EMPTY = new OptionalShort();

    private OptionalShort() {

    }

    /**
     * Returns an empty {@code OptionalShort} instance.  No value is present for
     * this {@code OptionalShort}.
     *
     * @apiNote
     * Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} against instances returned by
     * {@code OptionalShort.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @return an empty {@code OptionalShort}.
     */
    public static OptionalShort empty() {
        return EMPTY;
    }

    /**
     * Returns an {@code OptionalShort} describing the given value.
     *
     * @param value the value to describe
     * @return an {@code OptionalShort} with the value present
     */
    public static OptionalShort of(short value) {
        return new PresentOptionalShort(value);
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the value described by this {@code OptionalShort}
     * @throws NoSuchElementException if no value is present
     */
    public short get() {
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
    public void ifPresent(ShortConsumer action) {
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
    public void ifPresentOrElse(ShortConsumer action, Runnable emptyAction) {
        emptyAction.run();
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code OptionalShort} describing the value, otherwise returns an
     * empty {@code OptionalShort}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code OptionalShort} describing the value of this
     *         {@code OptionalShort}, if a value is present and the value matches the
     *         given predicate, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public OptionalShort filter(ShortPredicate predicate) {
        Preconditions.checkNotNull(predicate);
        return this;
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalShort}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalShort} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalShort map(ShortUnaryOperator mapper) {
        Preconditions.checkNotNull(mapper);
        return this;
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code #Optional.ofNullable}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code Optional<T>}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code Optional<T>} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public <T> Optional<T> mapToObject(Short2ObjectFunction<? extends T> mapper) {
        Preconditions.checkNotNull(mapper);
        return Optional.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalBoolean}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalBoolean} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalBoolean mapToBoolean(ShortPredicate mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalBoolean.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code OptionalByte.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalByte}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalByte} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalByte mapToByte(Short2ByteFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalByte.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code OptionalInt.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalInt}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalInt} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalInt mapToInt(Short2IntFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalInt.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code OptionalLong.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalLong}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalLong} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalLong mapToLong(Short2LongFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalLong.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code OptionalChar.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalChar}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalChar} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalChar mapToChar(Short2CharFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalChar.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code OptionalFloat.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalFloat}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalFloat} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalFloat mapToFloat(Short2FloatFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalFloat.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing (as if by
     * {@code OptionalDouble.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalDouble}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalShort} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalDouble} describing the result of applying a mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public OptionalDouble mapToDouble(Short2DoubleFunction mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalDouble.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalShort}.
     *
     * <p>This method is similar to {@link #map(ShortUnaryOperator)}, but the mapping
     * function is one whose result is already an {@code OptionalShort}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code OptionalShort}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalShort}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalShort flatMap(Short2ObjectFunction<? extends OptionalShort> mapper) {
        Preconditions.checkNotNull(mapper);
        return this;
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
     * an empty {@code Optional<T>}.
     *
     * <p>This method is similar to {@link #mapToObject(Short2ObjectFunction)}, but the mapping
     * function is one whose result is already an {@code Optional<T>}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code Optional<T>}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code Optional<T>}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public <T> Optional<T> flatMapToObject(Short2ObjectFunction<? extends Optional<? extends T>> mapper) {
        Preconditions.checkNotNull(mapper);
        return Optional.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalBoolean}.
     *
     * <p>This method is similar to {@link #mapToBoolean(ShortPredicate)}, but the mapping
     * function is one whose result is already an {@code OptionalBoolean}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalBoolean}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalBoolean flatMapToBoolean(Short2ObjectFunction<? extends OptionalBoolean> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalBoolean.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalByte}.
     *
     * <p>This method is similar to {@link #mapToByte(Short2ByteFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalByte}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalByte}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalByte flatMapToByte(Short2ObjectFunction<? extends OptionalByte> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalByte.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalInt}.
     *
     * <p>This method is similar to {@link #mapToInt(Short2IntFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalInt}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalInt}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalInt flatMapToInt(Short2ObjectFunction<? extends OptionalInt> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalInt.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalLong}.
     *
     * <p>This method is similar to {@link #mapToLong(Short2LongFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalLong}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalLong}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalLong flatMapToLong(Short2ObjectFunction<? extends OptionalLong> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalLong.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalChar}.
     *
     * <p>This method is similar to {@link #mapToChar(Short2CharFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalChar}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalChar}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalChar flatMapToChar(Short2ObjectFunction<? extends OptionalChar> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalChar.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalFloat}.
     *
     * <p>This method is similar to {@link #mapToFloat(Short2FloatFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalFloat}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalFloat}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalFloat flatMapToFloat(Short2ObjectFunction<? extends OptionalFloat> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalFloat.empty();
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalDouble}.
     *
     * <p>This method is similar to {@link #mapToDouble(Short2DoubleFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalDouble}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalDouble}-bearing mapping
     *         function to the value of this {@code OptionalShort}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public OptionalDouble flatMapToDouble(Short2ObjectFunction<? extends OptionalDouble> mapper) {
        Preconditions.checkNotNull(mapper);
        return OptionalDouble.empty();
    }

    /**
     * If a value is present, returns an {@code OptionalShort} describing the value,
     * otherwise returns an {@code OptionalShort} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code OptionalShort}
     *        to be returned
     * @return returns an {@code OptionalShort} describing the value of this
     *         {@code OptionalShort}, if a value is present, otherwise an
     *         {@code OptionalShort} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null} or
     *         produces a {@code null} result
     * @since 9
     */
    public OptionalShort or(Supplier<? extends OptionalShort> supplier) {
        return Preconditions.checkNotNull(supplier.get());
    }

    /**
     * If a value is present, returns a sequential {@link ShortStream} containing
     * only that value, otherwise returns an empty {@code ShortStream}.
     *
     * @apiNote
     * This method can be used to transform a {@code Stream} of optional Shorts
     * to an {@code ShortStream} of present Shorts:
     * <pre>{@code
     *     Stream<OptionalShort> os = ..
     *     ShortStream s = os.flatMapToShort(OptionalShort::stream)
     * }</pre>
     *
     * @return the optional value as an {@code ShortStream}
     * @since 9
     */
    public ShortStream stream() {
        return ShortStream.empty();
    }

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present
     * @return the value, if present, otherwise {@code other}
     */
    public short orElse(short other) {
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
    public short orElseGet(ShortSupplier supplier) {
        return supplier.getAsShort();
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the value described by this {@code OptionalShort}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public short orElseThrow() {
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
    public<X extends Throwable> short orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        throw exceptionSupplier.get();
    }

    /**
     * Indicates whether some other object is "equal to" this
     * {@code OptionalShort}.  The other object is considered equal if:
     * <ul>
     * <li>it is also an {@code OptionalShort} and;
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
     * Returns a non-empty string representation of this {@code OptionalShort}
     * suitable for debugging.  The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @implSpec
     * If a value is present the result must include its string representation
     * in the result.  Empty and present {@code OptionalShort}s must be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return "OptionalShort.empty";
    }

    private static final class PresentOptionalShort extends OptionalShort {
        private final short value;

        private PresentOptionalShort(short value) {
            this.value = value;
        }

        /**
         * If a value is present, returns the value, otherwise throws
         * {@code NoSuchElementException}.
         *
         * @apiNote
         * The preferred alternative to this method is {@link #orElseThrow()}.
         *
         * @return the value described by this {@code OptionalShort}
         * @throws NoSuchElementException if no value is present
         */
        @Override
        public short get() {
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
        public void ifPresent(ShortConsumer action) {
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
        public void ifPresentOrElse(ShortConsumer action, Runnable emptyAction) {
            action.accept(value);
        }

        /**
         * If a value is present, and the value matches the given predicate,
         * returns an {@code OptionalShort} describing the value, otherwise returns an
         * empty {@code OptionalShort}.
         *
         * @param predicate the predicate to apply to a value, if present
         * @return an {@code OptionalShort} describing the value of this
         *         {@code OptionalShort}, if a value is present and the value matches the
         *         given predicate, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the predicate is {@code null}
         */
        @Override
        public OptionalShort filter(ShortPredicate predicate) {
            return predicate.test(get()) ? this : OptionalShort.empty();
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@link #of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalShort}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalShort} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalShort map(ShortUnaryOperator mapper) {
            return of(mapper.apply(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code #Optional.ofNullable}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code Optional<T>}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code Optional<T>} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code Optional<T>}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public <T> Optional<T> mapToObject(Short2ObjectFunction<? extends T> mapper) {
            return Optional.ofNullable(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@link #of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalBoolean}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalBoolean} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalBoolean}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalBoolean mapToBoolean(ShortPredicate mapper) {
            return OptionalBoolean.of(mapper.test(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code OptionalByte.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalByte}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalByte} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalByte mapToByte(Short2ByteFunction mapper) {
            return OptionalByte.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code OptionalInt.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalInt}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalInt} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalInt}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalInt mapToInt(Short2IntFunction mapper) {
            return OptionalInt.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code OptionalLong.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalLong}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalLong} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalLong}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalLong mapToLong(Short2LongFunction mapper) {
            return OptionalLong.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code OptionalChar.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalChar}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalChar} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalChar}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalChar mapToChar(Short2CharFunction mapper) {
            return OptionalChar.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code OptionalFloat.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalFloat}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalFloat} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalFloat mapToFloat(Short2FloatFunction mapper) {
            return OptionalFloat.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing (as if by
         * {@code OptionalDouble.of}) the result of applying the given mapping function to
         * the value, otherwise returns an empty {@code OptionalDouble}.
         *
         * @apiNote
         * This method supports post-processing on {@code OptionalShort} values, without
         * the need to explicitly check for a return status.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return an {@code OptionalDouble} describing the result of applying a mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalDouble}
         * @throws NullPointerException if the mapping function is {@code null}
         */
        @Override
        public OptionalDouble mapToDouble(Short2DoubleFunction mapper) {
            return OptionalDouble.of(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalShort}.
         *
         * <p>This method is similar to {@link #map(ShortUnaryOperator)}, but the mapping
         * function is one whose result is already an {@code OptionalShort}, and if
         * invoked, {@code flatMap} does not wrap it within an additional
         * {@code OptionalShort}.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalShort}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalShort}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalShort flatMap(Short2ObjectFunction<? extends OptionalShort> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
         * an empty {@code Optional<T>}.
         *
         * <p>This method is similar to {@link #mapToObject(Short2ObjectFunction)}, but the mapping
         * function is one whose result is already an {@code Optional<T>}, and if
         * invoked, {@code flatMap} does not wrap it within an additional
         * {@code Optional<T>}.
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code Optional<T>}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code Optional<T>}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> flatMapToObject(Short2ObjectFunction<? extends Optional<? extends T>> mapper) {
            return (Optional<T>) Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalBoolean}.
         *
         * <p>This method is similar to {@link #mapToBoolean(ShortPredicate)}, but the mapping
         * function is one whose result is already an {@code OptionalBoolean}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalBoolean}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalBoolean}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalBoolean flatMapToBoolean(Short2ObjectFunction<? extends OptionalBoolean> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalByte}.
         *
         * <p>This method is similar to {@link #mapToByte(Short2ByteFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalByte}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalByte}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalByte}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalByte flatMapToByte(Short2ObjectFunction<? extends OptionalByte> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalInt}.
         *
         * <p>This method is similar to {@link #mapToInt(Short2IntFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalInt}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalInt}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalInt}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalInt flatMapToInt(Short2ObjectFunction<? extends OptionalInt> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalLong}.
         *
         * <p>This method is similar to {@link #mapToLong(Short2LongFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalLong}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalLong}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalLong}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalLong flatMapToLong(Short2ObjectFunction<? extends OptionalLong> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalChar}.
         *
         * <p>This method is similar to {@link #mapToChar(Short2CharFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalChar}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalChar}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalChar}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalChar flatMapToChar(Short2ObjectFunction<? extends OptionalChar> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalFloat}.
         *
         * <p>This method is similar to {@link #mapToFloat(Short2FloatFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalFloat}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalFloat}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalFloat}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalFloat flatMapToFloat(Short2ObjectFunction<? extends OptionalFloat> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns the result of applying the given
         * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
         * an empty {@code OptionalDouble}.
         *
         * <p>This method is similar to {@link #mapToDouble(Short2DoubleFunction)}, but the mapping
         * function is one whose result is already an {@code OptionalDouble}
         *
         * @param mapper the mapping function to apply to a value, if present
         * @return the result of applying an {@code OptionalDouble}-bearing mapping
         *         function to the value of this {@code OptionalShort}, if a value is
         *         present, otherwise an empty {@code OptionalDouble}
         * @throws NullPointerException if the mapping function is {@code null} or
         *         returns a {@code null} result
         */
        @Override
        public OptionalDouble flatMapToDouble(Short2ObjectFunction<? extends OptionalDouble> mapper) {
            return Preconditions.checkNotNull(mapper.get(get()));
        }

        /**
         * If a value is present, returns an {@code OptionalShort} describing the value,
         * otherwise returns an {@code OptionalShort} produced by the supplying function.
         *
         * @param supplier the supplying function that produces an {@code OptionalShort}
         *        to be returned
         * @return returns an {@code OptionalShort} describing the value of this
         *         {@code OptionalShort}, if a value is present, otherwise an
         *         {@code OptionalShort} produced by the supplying function.
         * @throws NullPointerException if the supplying function is {@code null} or
         *         produces a {@code null} result
         * @since 1.0.0
         */
        @Override
        public OptionalShort or(Supplier<? extends OptionalShort> supplier) {
            Preconditions.checkNotNull(supplier);
            return this;
        }

        /**
         * If a value is present, returns a sequential {@link ShortStream} containing
         * only that value, otherwise returns an empty {@code ShortStream}.
         *
         * @apiNote
         * This method can be used to transform a {@code Stream} of optional Shorts
         * to an {@code ShortStream} of present Shorts:
         * <pre>{@code
         *     Stream<OptionalShort> os = ..
         *     ShortStream s = os.flatMapToShort(OptionalShort::stream)
         * }</pre>
         *
         * @return the optional value as an {@code ShortStream}
         * @since 9
         */
        @Override
        public ShortStream stream() {
            return ShortStream.of(value);
        }

        /**
         * If a value is present, returns the value, otherwise returns
         * {@code other}.
         *
         * @param other the value to be returned, if no value is present
         * @return the value, if present, otherwise {@code other}
         */
        @Override
        public short orElse(short other) {
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
        public short orElseGet(ShortSupplier supplier) {
            return value;
        }

        /**
         * If a value is present, returns the value, otherwise throws
         * {@code NoSuchElementException}.
         *
         * @return the value described by this {@code OptionalShort}
         * @throws NoSuchElementException if no value is present
         * @since 10
         */
        @Override
        public short orElseThrow() {
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
        public<X extends Throwable> short orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return value;
        }

        /**
         * Indicates whether some other object is "equal to" this
         * {@code OptionalShort}.  The other object is considered equal if:
         * <ul>
         * <li>it is also an {@code OptionalShort} and;
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

            if (!(obj instanceof PresentOptionalShort optional)) {
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
            return Short.hashCode(value);
        }

        /**
         * Returns a non-empty string representation of this {@code OptionalShort}
         * suitable for debugging.  The exact presentation format is unspecified and
         * may vary between implementations and versions.
         *
         * @implSpec
         * If a value is present the result must include its string representation
         * in the result.  Empty and present {@code OptionalShort}s must be
         * unambiguously differentiable.
         *
         * @return the string representation of this instance
         */
        @Override
        public String toString() {
            return String.format("OptionalShort[%s]", value);
        }
    }
}
