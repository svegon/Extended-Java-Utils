package svegon.utils.math.really_big_math;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import svegon.utils.interfaces.IdentityComparable;

@Immutable
public abstract class Infinity extends InfiniNumber implements IdentityComparable {
    private final long asLong;
    private final double asDouble;
    private final int hashCode;
    private final InfiniFloat bitRepresentation;

    Infinity(double asDouble) {
        this.asLong = (long) asDouble;
        this.asDouble = asDouble;
        this.hashCode = Double.hashCode(asDouble);
        this.bitRepresentation = InfiniFloat.valueOf(Double.doubleToLongBits(asDouble));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public Infinity clone() throws CloneNotSupportedException {
        return this;
    }

    @Override
    public @NotNull InfiniNumber add(@NotNull Number other) {
        return other == ComplexMathUtil.NaN || other == neg() ? ComplexMathUtil.NaN : this;
    }

    @Override
    public @NotNull InfiniNumber substract(@NotNull Number other) {
        return other == this || other == ComplexMathUtil.NaN ? ComplexMathUtil.NaN : this;
    }

    @Override
    public @NotNull InfiniNumber mul(@NotNull Number other) {
        if (other == ComplexMathUtil.NaN || other.equals(0)) {
            return ComplexMathUtil.NaN;
        }

        if (ComplexMathUtil.compare(other, 0) < 0) {
            return neg();
        }

        return this;
    }

    @Override
    public @NotNull InfiniNumber div(@NotNull Number divider) {
        if (divider instanceof Infinity || divider == ComplexMathUtil.NaN) {
            return ComplexMathUtil.NaN;
        }

        if (ComplexMathUtil.compare(divider, 0) < 0) {
            return neg();
        }

        return this;
    }

    @Override
    public @NotNull InfiniNumber floorDiv(@NotNull Number other) {
        return div(other);
    }

    @Override
    public @NotNull InfiniNumber pow(@NotNull Number exp) {
        if (exp == ComplexMathUtil.NaN || exp == ComplexMathUtil.POSITIVE_INFINITY) {
            return (InfiniNumber) exp;
        }

        if (exp.equals(0)) {
            return InfiniFloat.valueOf(1);
        }

        return ComplexMathUtil.compare(exp, 0) < 0 ? InfiniFloat.ZERO : this;
    }

    @Override
    public @NotNull InfiniNumber log() {
        return ComplexMathUtil.NaN;
    }

    @Override
    public @NotNull InfiniNumber log(@NotNull Number base) {
        return ComplexMathUtil.NaN;
    }

    @Override
    public @NotNull InfiniNumber mod(@NotNull Number other) {
        return other == this ? InfiniFloat.valueOf(0) : this;
    }

    @Override
    public @NotNull ObjectObjectImmutablePair<@NotNull InfiniNumber, @NotNull InfiniNumber>
    divMod(@NotNull Number other) {
        return new ObjectObjectImmutablePair<>(floorDiv(other), mod(other));
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber lShift(long by) {
        return bitRepresentation.lShift(by);
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber rShift(long by) {
        return bitRepresentation.rShift(by);
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber and(@NotNull Number other) {
        return bitRepresentation.and(other);
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber or(@NotNull Number other) {
        return bitRepresentation.or(other);
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber xor(@NotNull Number other) {
        return bitRepresentation.xor(other);
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber invert() {
        return bitRepresentation.invert();
    }

    @Override
    public @NotNull InfiniNumber abs() {
        return ComplexMathUtil.POSITIVE_INFINITY;
    }

    @Override
    public @NotNull InfiniNumber floor() {
        return this;
    }

    @Override
    public @NotNull InfiniNumber ceil() {
        return this;
    }

    @Override
    public @NotNull InfiniNumber round(long decimalLength) {
        return this;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public long longValue() {
        return asLong;
    }

    @Override
    public double doubleValue() {
        return asDouble;
    }
}
