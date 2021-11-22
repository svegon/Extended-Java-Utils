package svegon.utils.math.really_big_math;

import org.jetbrains.annotations.Contract;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public final class InfiniNumberNaN extends Infinity {
    InfiniNumberNaN() {
        super(Double.NaN);
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return Double.NaN;
    }

    @Override
    public boolean equals(Object o) {
        return false; // to keep the attribute of NaN that Double.NaN != Double.NaN
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "NaN";
    }

    @Override
    public @NotNull InfiniNumber add(@NotNull Number other) {
        return this;
    }

    @Override
    public @NotNull InfiniNumber substract(@NotNull Number other) {
        return this;
    }

    @Override
    public @NotNull InfiniNumber mul(@NotNull Number other) {
        return this;
    }

    @Override
    public @NotNull InfiniNumber div(@NotNull Number divider) {
        return this;
    }

    @Override
    public @NotNull InfiniNumber floorDiv(@NotNull Number other) {
        return this;
    }

    @Override
    public @NotNull InfiniNumber pow(@NotNull Number exp) {
        return ComplexMathUtil.compare(exp, InfiniFloat.ZERO) == 0 ? InfiniFloat.ONE : this;
    }

    @Override
    public @NotNull InfiniNumber mod(@NotNull Number other) {
        return other == this ? InfiniFloat.ZERO : this;
    }

    @Contract("_ -> new")
    @Override
    public @NotNull Pair<InfiniNumber, InfiniNumber> divMod(@NotNull Number other) {
        return new ObjectObjectImmutablePair<>(this, mod(other));
    }

    @Deprecated
    @Override
    public @NotNull InfiniNumber sign() {
        return this;
    }

    @Override
    public @NotNull InfiniNumber abs() {
        return this;
    }

    @Override
    public @NotNull InfiniNumber neg() {
        return this;
    }

    @Override
    public @NotNull InfiniNumber round(long decimalLength) {
        return InfiniFloat.valueOf(0);
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public int compareTo(@NotNull Number o) {
        return 1;
    }
}
