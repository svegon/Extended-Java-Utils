package svegon.utils.math.really_big_math;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Immutable
public final class NegativeInfinity extends Infinity {
    NegativeInfinity() {
        super(Double.POSITIVE_INFINITY);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "-INFINITY";
    }

    @Override
    public @NotNull InfiniNumber sign() {
        return InfiniFloat.NEGATIVE_ONE;
    }

    @Override
    public @NotNull InfiniNumber neg() {
        return ComplexMathUtil.POSITIVE_INFINITY;
    }

    @Override
    public int compareTo(@NotNull Number o) {
        return o == this ? 0 : 1; // I don't have a way make this compare untrue in every way to NaN
    }
}
