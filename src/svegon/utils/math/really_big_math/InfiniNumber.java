package svegon.utils.math.really_big_math;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import svegon.utils.StringUtil;

/**
 * class for representing numbers with higher precision than primitives like long or double
 * all instances of its subclasses are assumed to be immutable by all classes in this module
 */
@Immutable
public abstract class InfiniNumber extends Number implements Comparable<Number> {
    InfiniNumber() {

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Number && compareTo((Number) obj) == 0;
    }

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return StringUtil.toString(this);
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }

    public abstract @NotNull InfiniNumber add(@NotNull Number other);

    public @NotNull InfiniNumber substract(@NotNull Number other) {
        return add(other instanceof InfiniNumber ? ((InfiniNumber) other).neg() : -other.doubleValue());
    }

    public final @NotNull InfiniNumber multiply(@NotNull Number other) {
        return mul(other);
    }

    public abstract @NotNull InfiniNumber mul(@NotNull Number other);

    public final @NotNull InfiniNumber divide(@NotNull Number divider) {
        return div(divider);
    }

    public abstract @NotNull InfiniNumber div(@NotNull Number divider);

    public @NotNull InfiniNumber floorDiv(@NotNull Number other) {
        return divMod(other).first();
    }

    public final @NotNull InfiniNumber power(@NotNull Number exponent) {
        return pow(exponent);
    }

    public abstract @NotNull InfiniNumber pow(@NotNull Number exp);

    /**
     * Returns this number squared to {@param exp} modulo {@param mod}.
     *
     * Equal to calling {@code mod} after calling {@code pow} but may be significantly faster with big integers
     *
     * @param exp the exponent to square the number before modulo
     * @param mod applied modulo
     * @return this number squared to {@param exp} modulo {@param mod}
     */
    public @NotNull InfiniNumber pow(@NotNull Number exp, @NotNull Number mod) {
        return pow(exp).mod(mod);
    }

    public abstract @NotNull InfiniNumber log();

    public @NotNull InfiniNumber log(@NotNull Number base) {
        return log().div(ComplexMathUtil.cast(base).log());
    }

    /**
     * Equivalent to the mathematical operation modulo.
     *
     * if both this and {@param other} are finite then 0 <= this.mod({@param other}) < {@param other}
     *
     * @param other the modulo
     * @return The rest after substracting the higher integer multiplier of {@param other}
     */
    public @NotNull InfiniNumber mod(@NotNull Number other) {
        return divMod(other).right();
    }

    /**
     * Returns an object containing both the floor division
     * and modulo of this number with the {@param other}.
     *
     * This may be significantly faster than calling each
     * respective function seperately since the modulo
     * is often calculated along with the floor division.
     *
     * @param other the divider
     * @return an {@code ObjectObjectImmutablePair} with its first
     * element being the floor division of this and {@param other}
     * and the right elemtn being the modulo of this and
     * {@param other}.
     */
    public abstract @NotNull ObjectObjectImmutablePair<@NotNull InfiniNumber, @NotNull InfiniNumber>
    divMod(@NotNull Number other);

    /**
     * Shifts this number by the given number of bits to the left.
     *
     * <p>
     * Special cases:
     * <ul><li>If this is infinite (an instance of {@code Infinity},
     * the result is the {@code InfiniFloat} value of the long bits
     * of the double value of this number shifted to the left
     * by the given amount.</ul>
     *
     * @param by the number of bits to shift to the left
     * @return a number representing this shifted by the given amount
     * of bits to the left
     */
    public abstract @NotNull InfiniNumber lShift(long by);

    /**
     * Shifts this number by the given number of bits to the right.
     *
     * <p>
     * Special cases:
     * <ul><li>If this is infinite (an instance of {@code Infinity},
     * the result is the {@code InfiniFloat} value of the long bits
     * of the double value of this number shifted to the right
     * by the given amount.</ul>
     *
     * @param by the number of bits to shift to the right
     * @return a number representing this shifted by the given amount
     * of bits to the right
     */
    public @NotNull InfiniNumber rShift(long by) {
        return lShift(-by);
    }

    /**
     * @param other
     * @return
     */
    public abstract @NotNull InfiniNumber and(@NotNull Number other);

    /**
     * @param other
     * @return
     */
    public abstract @NotNull InfiniNumber or(@NotNull Number other);

    /**
     * @param other
     * @return
     */
    public abstract @NotNull InfiniNumber xor(@NotNull Number other);

    public abstract @NotNull InfiniNumber invert();

    public final @NotNull InfiniNumber signum() {
        return sign();
    }

    public abstract @NotNull InfiniNumber sign();

    public abstract @NotNull InfiniNumber abs();

    /**
     * @return a number with equal InfiniNumber.abs() but opposite signum
     */
    public abstract @NotNull InfiniNumber neg();

    public abstract @NotNull InfiniNumber floor();

    public abstract @NotNull InfiniNumber ceil();

    public @NotNull InfiniNumber round() {
        return round(0);
    }

    public abstract @NotNull InfiniNumber round(long decimalLength);

    public abstract long size();

    public long bytes() {
        return (long) Math.ceil((double) size() / Byte.SIZE);
    }
}
