package svegon.utils.math.really_big_math;

import org.jetbrains.annotations.NotNull;
import svegon.utils.StringUtil;
import it.unimi.dsi.fastutil.Pair;
import net.jcip.annotations.Immutable;

/**
 * class for representing numbers with higher precision than primitives like long or double
 * all instances of its subclasses are assumed to be immutable by all classes in this module
 */
@Immutable
public abstract class InfiniNumber extends Number implements Comparable<Number> {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Number n) {
            return compareTo(n) == 0;
        }

        return false;
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

    public abstract @NotNull InfiniNumber floorDiv(@NotNull Number other);

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
        return log().div(ComplexMathUtil.ln(base));
    }

    /**
     * Equivalent to the mathematical operation modulo.
     *
     * if both this and {@param other} are finite then 0 <= this.mod({@param other}) < {@param other}
     *
     * @param other the modulo
     * @return The rest after substracting the higher integer multiplier of {@param other}
     */
    public abstract @NotNull InfiniNumber mod(@NotNull Number other);

    public abstract @NotNull Pair<@NotNull InfiniNumber, @NotNull InfiniNumber> divMod(@NotNull Number other);

    public abstract @NotNull InfiniNumber lShift(long by);

    public abstract @NotNull InfiniNumber rShift(long by);

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
