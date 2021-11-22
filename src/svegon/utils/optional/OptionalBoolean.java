package svegon.utils.optional;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.booleans.*;
import svegon.utils.collections.stream.BooleanStream;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public enum OptionalBoolean {
    DEFAULT {
        @Override
        public boolean get() {
            throw new NoSuchElementException("No element present.");
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void ifPresent(BooleanConsumer action) {

        }

        @Override
        public void ifPresentOrElse(BooleanConsumer action, Runnable emptyAction) {
            emptyAction.run();
        }

        @Override
        public OptionalBoolean filter(BooleanPredicate predicate) {
            Preconditions.checkNotNull(predicate);
            return empty();
        }

        @Override
        public OptionalBoolean map(BooleanUnaryOperator mapper) {
            Preconditions.checkNotNull(mapper);
            return empty();
        }

        @Override
        public <T> Optional<T> mapToObject(Boolean2ObjectFunction<? extends T> mapper) {
            Preconditions.checkNotNull(mapper);
            return Optional.empty();
        }

        @Override
        public OptionalByte mapToByte(Boolean2ByteFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalByte.empty();
        }

        @Override
        public OptionalShort mapToShort(Boolean2ShortFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalShort.empty();
        }

        @Override
        public OptionalInt mapToInt(Boolean2IntFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalInt.empty();
        }

        @Override
        public OptionalLong mapToLong(Boolean2LongFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalLong.empty();
        }

        @Override
        public OptionalChar mapToChar(Boolean2CharFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalChar.empty();
        }

        @Override
        public OptionalFloat mapToFloat(Boolean2FloatFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalFloat.empty();
        }

        @Override
        public OptionalDouble mapToDouble(Boolean2DoubleFunction mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalDouble.empty();
        }

        @Override
        public OptionalBoolean flatMap(Boolean2ObjectFunction<? extends OptionalBoolean> mapper) {
            Preconditions.checkNotNull(mapper);
            return empty();
        }

        @Override
        public <T> Optional<T> flatMapToObject(Boolean2ObjectFunction<? extends Optional<? extends T>> mapper) {
            Preconditions.checkNotNull(mapper);
            return Optional.empty();
        }

        @Override
        public OptionalByte flatMapToByte(Boolean2ObjectFunction<? extends OptionalByte> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalByte.empty();
        }

        @Override
        public OptionalShort flatMapToShort(Boolean2ObjectFunction<? extends OptionalShort> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalShort.empty();
        }

        @Override
        public OptionalInt flatMapToInt(Boolean2ObjectFunction<? extends OptionalInt> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalInt.empty();
        }

        @Override
        public OptionalLong flatMapToLong(Boolean2ObjectFunction<? extends OptionalLong> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalLong.empty();
        }

        @Override
        public OptionalChar flatMapToChar(Boolean2ObjectFunction<? extends OptionalChar> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalChar.empty();
        }

        @Override
        public OptionalFloat flatMapToFloat(Boolean2ObjectFunction<? extends OptionalFloat> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalFloat.empty();
        }

        @Override
        public OptionalDouble flatMapToDouble(Boolean2ObjectFunction<? extends OptionalDouble> mapper) {
            Preconditions.checkNotNull(mapper);
            return OptionalDouble.empty();
        }

        @Override
        public OptionalBoolean or(Supplier<? extends OptionalBoolean> supplier) {
            return Preconditions.checkNotNull(supplier.get());
        }

        @Override
        public BooleanStream stream() {
            return BooleanStream.empty();
        }

        @Override
        public boolean orElse(boolean other) {
            return other;
        }

        @Override
        public boolean orElseGet(BooleanSupplier supplier) {
            return supplier.getAsBoolean();
        }

        @Override
        public boolean orElseThrow() {
            throw new NoSuchElementException("No value present");
        }

        @Override
        public<X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public String toString() {
            return "OptionalBoolean.empty";
        }
    },
    FALSE {
        @Override
        public boolean get() {
            return false;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public void ifPresent(BooleanConsumer action) {
            action.accept(false);
        }

        @Override
        public void ifPresentOrElse(BooleanConsumer action, Runnable emptyAction) {
            action.accept(false);
        }

        @Override
        public OptionalBoolean filter(BooleanPredicate predicate) {
            return predicate.test(false) ? this : empty();
        }

        @Override
        public OptionalBoolean map(BooleanUnaryOperator mapper) {
            return of(mapper.apply(false));
        }

        @Override
        public <T> Optional<T> mapToObject(Boolean2ObjectFunction<? extends T> mapper) {
            return Optional.ofNullable(mapper.apply(false));
        }

        @Override
        public OptionalByte mapToByte(Boolean2ByteFunction mapper) {
            return OptionalByte.of(mapper.apply(false));
        }

        @Override
        public OptionalShort mapToShort(Boolean2ShortFunction mapper) {
            return OptionalShort.of(mapper.apply(false));
        }

        @Override
        public OptionalInt mapToInt(Boolean2IntFunction mapper) {
            return OptionalInt.of(mapper.apply(false));
        }

        @Override
        public OptionalLong mapToLong(Boolean2LongFunction mapper) {
            return OptionalLong.of(mapper.apply(false));
        }

        @Override
        public OptionalChar mapToChar(Boolean2CharFunction mapper) {
            return OptionalChar.of(mapper.apply(false));
        }

        @Override
        public OptionalFloat mapToFloat(Boolean2FloatFunction mapper) {
            return OptionalFloat.of(mapper.apply(false));
        }

        @Override
        public OptionalDouble mapToDouble(Boolean2DoubleFunction mapper) {
            return OptionalDouble.of(mapper.apply(false));
        }

        @Override
        public OptionalBoolean flatMap(Boolean2ObjectFunction<? extends OptionalBoolean> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> flatMapToObject(Boolean2ObjectFunction<? extends Optional<? extends T>> mapper) {
            return (Optional<T>) Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalByte flatMapToByte(Boolean2ObjectFunction<? extends OptionalByte> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalShort flatMapToShort(Boolean2ObjectFunction<? extends OptionalShort> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalInt flatMapToInt(Boolean2ObjectFunction<? extends OptionalInt> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalLong flatMapToLong(Boolean2ObjectFunction<? extends OptionalLong> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalChar flatMapToChar(Boolean2ObjectFunction<? extends OptionalChar> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalFloat flatMapToFloat(Boolean2ObjectFunction<? extends OptionalFloat> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalDouble flatMapToDouble(Boolean2ObjectFunction<? extends OptionalDouble> mapper) {
            return Preconditions.checkNotNull(mapper.apply(false));
        }

        @Override
        public OptionalBoolean or(Supplier<? extends OptionalBoolean> supplier) {
            return this;
        }

        @Override
        public BooleanStream stream() {
            return BooleanStream.of(false);
        }

        @Override
        public boolean orElse(boolean other) {
            return false;
        }

        @Override
        public boolean orElseGet(BooleanSupplier supplier) {
            return false;
        }

        @Override
        public boolean orElseThrow() {
            return false;
        }

        @Override
        public<X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return false;
        }

        @Override
        public String toString() {
            return "OptionalBoolean[false]";
        }
    },
    TRUE {
        @Override
        public boolean get() {
            return true;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public void ifPresent(BooleanConsumer action) {
            action.accept(true);
        }

        @Override
        public void ifPresentOrElse(BooleanConsumer action, Runnable emptyAction) {
            action.accept(true);
        }

        @Override
        public OptionalBoolean filter(BooleanPredicate predicate) {
            return predicate.test(true) ? this : empty();
        }

        @Override
        public OptionalBoolean map(BooleanUnaryOperator mapper) {
            return of(mapper.apply(true));
        }

        @Override
        public <T> Optional<T> mapToObject(Boolean2ObjectFunction<? extends T> mapper) {
            return Optional.ofNullable(mapper.get(true));
        }

        @Override
        public OptionalByte mapToByte(Boolean2ByteFunction mapper) {
            return OptionalByte.of(mapper.get(true));
        }

        @Override
        public OptionalShort mapToShort(Boolean2ShortFunction mapper) {
            return OptionalShort.of(mapper.get(true));
        }

        @Override
        public OptionalInt mapToInt(Boolean2IntFunction mapper) {
            return OptionalInt.of(mapper.get(true));
        }

        @Override
        public OptionalLong mapToLong(Boolean2LongFunction mapper) {
            return OptionalLong.of(mapper.get(true));
        }

        @Override
        public OptionalChar mapToChar(Boolean2CharFunction mapper) {
            return OptionalChar.of(mapper.get(true));
        }

        @Override
        public OptionalFloat mapToFloat(Boolean2FloatFunction mapper) {
            return OptionalFloat.of(mapper.get(true));
        }

        @Override
        public OptionalDouble mapToDouble(Boolean2DoubleFunction mapper) {
            return OptionalDouble.of(mapper.get(true));
        }

        @Override
        public OptionalBoolean flatMap(Boolean2ObjectFunction<? extends OptionalBoolean> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> flatMapToObject(Boolean2ObjectFunction<? extends Optional<? extends T>> mapper) {
            return (Optional<T>) Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalByte flatMapToByte(Boolean2ObjectFunction<? extends OptionalByte> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalShort flatMapToShort(Boolean2ObjectFunction<? extends OptionalShort> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalInt flatMapToInt(Boolean2ObjectFunction<? extends OptionalInt> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalLong flatMapToLong(Boolean2ObjectFunction<? extends OptionalLong> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalChar flatMapToChar(Boolean2ObjectFunction<? extends OptionalChar> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalFloat flatMapToFloat(Boolean2ObjectFunction<? extends OptionalFloat> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalDouble flatMapToDouble(Boolean2ObjectFunction<? extends OptionalDouble> mapper) {
            return Preconditions.checkNotNull(mapper.get(true));
        }

        @Override
        public OptionalBoolean or(Supplier<? extends OptionalBoolean> supplier) {
            return this;
        }

        @Override
        public BooleanStream stream() {
            return BooleanStream.of(true);
        }

        @Override
        public boolean orElse(boolean other) {
            return true;
        }

        @Override
        public boolean orElseGet(BooleanSupplier supplier) {
            return true;
        }

        @Override
        public boolean orElseThrow() {
            return true;
        }

        @Override
        public<X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return true;
        }

        @Override
        public String toString() {
            return "OptionalBoolean[true]";
        }
    };

    /**
     * Returns an empty {@code OptionalBoolean} instance.  No value is present for
     * this {@code OptionalBoolean}.
     *
     * @apiNote
     * Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} against instances returned by
     * {@code OptionalLong.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @return an empty {@code OptionalLong}.
     */
    public static OptionalBoolean empty() {
        return DEFAULT;
    }

    /**
     * Returns an {@code OptionalBoolean} describing the given value.
     *
     * @param value the value to describe
     * @return an {@code OptionalBoolean} with the value present
     */
    public static OptionalBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Returns an {@code OptionalBoolean} describing the given value.
     *
     * @param value the value to describe
     * @return an {@code OptionalBoolean} with the value present
     */
    public static OptionalBoolean of(Boolean value) {
        return value == null ? empty() : of(value.booleanValue());
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
    public abstract boolean get();

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public abstract boolean isPresent();

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
    public abstract void ifPresent(BooleanConsumer action);

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
    public abstract void ifPresentOrElse(BooleanConsumer action, Runnable emptyAction);

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code OptionalBoolean} describing the value, otherwise returns an
     * empty {@code OptionalBoolean}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code OptionalBoolean} describing the value of this
     *         {@code OptionalBoolean}, if a value is present and the value matches the
     *         given predicate, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public abstract OptionalBoolean filter(BooleanPredicate predicate);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@link #of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalBoolean}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalBoolean} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalBoolean map(BooleanUnaryOperator mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code #Optional.ofNullable}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code Optional<T>}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code Optional<T>} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract <T> Optional<T> mapToObject(Boolean2ObjectFunction<? extends T> mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalByte.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalByte}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalByte} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalByte mapToByte(Boolean2ByteFunction mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalShort.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalShort}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalShort} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalShort mapToShort(Boolean2ShortFunction mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalInt.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalInt}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalInt} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalInt mapToInt(Boolean2IntFunction mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalLong.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalLong}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalLong} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalLong mapToLong(Boolean2LongFunction mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalChar.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalChar}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalChar} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalChar mapToChar(Boolean2CharFunction mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalFloat.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalFloat}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalFloat} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalFloat mapToFloat(Boolean2FloatFunction mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing (as if by
     * {@code OptionalDouble.of}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code OptionalDouble}.
     *
     * @apiNote
     * This method supports post-processing on {@code OptionalBoolean} values, without
     * the need to explicitly check for a return status.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@code OptionalDouble} describing the result of applying a mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public abstract OptionalDouble mapToDouble(Boolean2DoubleFunction mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalBoolean}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalBoolean}.
     *
     * <p>This method is similar to {@link #map(BooleanUnaryOperator)}, but the mapping
     * function is one whose result is already an {@code OptionalBoolean}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code OptionalBoolean}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalBoolean}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalBoolean}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalBoolean flatMap(Boolean2ObjectFunction<? extends OptionalBoolean> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code Optional<T>}-bearing mapping function to the value, otherwise returns
     * an empty {@code Optional<T>}.
     *
     * <p>This method is similar to {@link #mapToObject(Boolean2ObjectFunction)}, but the mapping
     * function is one whose result is already an {@code Optional<T>}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code Optional<T>}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code Optional<T>}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code Optional<T>}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract <T> Optional<T> flatMapToObject(Boolean2ObjectFunction<? extends Optional<? extends T>> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalByte}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalByte}.
     *
     * <p>This method is similar to {@link #mapToByte(Boolean2ByteFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalByte}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalByte}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalByte}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalByte flatMapToByte(Boolean2ObjectFunction<? extends OptionalByte> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalShort}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalShort}.
     *
     * <p>This method is similar to {@link #mapToShort(Boolean2ShortFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalShort}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalShort}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalShort}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalShort flatMapToShort(Boolean2ObjectFunction<? extends OptionalShort> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalInt}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalInt}.
     *
     * <p>This method is similar to {@link #mapToInt(Boolean2IntFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalInt}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalInt}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalInt}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalInt flatMapToInt(Boolean2ObjectFunction<? extends OptionalInt> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalLong}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalLong}.
     *
     * <p>This method is similar to {@link #mapToLong(Boolean2LongFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalLong}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalLong}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalLong}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalLong flatMapToLong(Boolean2ObjectFunction<? extends OptionalLong> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalChar}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalChar}.
     *
     * <p>This method is similar to {@link #mapToChar(Boolean2CharFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalChar}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalChar}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalChar}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalChar flatMapToChar(Boolean2ObjectFunction<? extends OptionalChar> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalFloat}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalFloat}.
     *
     * <p>This method is similar to {@link #mapToFloat(Boolean2FloatFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalFloat}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalFloat}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalFloat}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalFloat flatMapToFloat(Boolean2ObjectFunction<? extends OptionalFloat> mapper);

    /**
     * If a value is present, returns the result of applying the given
     * {@code OptionalDouble}-bearing mapping function to the value, otherwise returns
     * an empty {@code OptionalDouble}.
     *
     * <p>This method is similar to {@link #mapToDouble(Boolean2DoubleFunction)}, but the mapping
     * function is one whose result is already an {@code OptionalDouble}
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code OptionalDouble}-bearing mapping
     *         function to the value of this {@code OptionalBoolean}, if a value is
     *         present, otherwise an empty {@code OptionalDouble}
     * @throws NullPointerException if the mapping function is {@code null} or
     *         returns a {@code null} result
     */
    public abstract OptionalDouble flatMapToDouble(Boolean2ObjectFunction<? extends OptionalDouble> mapper);

    /**
     * If a value is present, returns an {@code OptionalBoolean} describing the value,
     * otherwise returns an {@code OptionalBoolean} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code OptionalBoolean}
     *        to be returned
     * @return returns an {@code OptionalBoolean} describing the value of this
     *         {@code OptionalBoolean}, if a value is present, otherwise an
     *         {@code OptionalBoolean} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null} or
     *         produces a {@code null} result
     * @since 9
     */
    public abstract OptionalBoolean or(Supplier<? extends OptionalBoolean> supplier);

    /**
     * If a value is present, returns a sequential {@link BooleanStream} containing
     * only that value, otherwise returns an empty {@code BooleanStream}.
     *
     * @apiNote
     * This method can be used to transform a {@code Stream} of optional
     * elements to a {@code BooleanStream} of present value elements:
     * <pre>{@code
     *     Stream<OptionalBoolean> os = ..
     *     BooleanStream s = StreamUtil.flatMapToBoolean(os, OptionalBoolean::stream)
     * }</pre>
     *
     * @return the optional value as a {@code Stream}
     * @since 1.0.0
     */
    public abstract BooleanStream stream();

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present
     * @return the value, if present, otherwise {@code other}
     */
    public abstract boolean orElse(boolean other);

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
    public abstract boolean orElseGet(BooleanSupplier supplier);

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the value described by this {@code OptionalBoolean}
     * @throws NoSuchElementException if no value is present
     * @since 16.0.1
     */
    public abstract boolean orElseThrow();

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
    public abstract <X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * Returns a non-empty string representation of this {@code OptionalBoolean}
     * suitable for debugging.  The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @implSpec
     * If a value is present the result must include its string representation
     * in the result.  Empty and present {@code OptionalBoolean}s must be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public abstract String toString();
}
