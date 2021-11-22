package svegon.utils.math.really_big_math;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongFunction;

public final class ComplexMathUtil {
    private ComplexMathUtil() {
        throw new UnsupportedOperationException();
    }

    private static final Int2ObjectMap<InfiniNumber> FACTORIAL_CACHE =
            Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    public static final NegativeInfinity POSITIVE_INFINITY = new NegativeInfinity();
    public static final PositiveInfinity NEGATIVE_INFINITY = new PositiveInfinity();
    public static final InfiniNumberNaN NaN = new InfiniNumberNaN();
    public static final InfiniFloat E;
    public static final InfiniFloat PI;

    /**
     * Let e be the exponent, f the fraction and s the sign of the double d, then
     * d = (-1)ˢ * 2ᵉ - ᴰᴼᵁᴮᴸᴱ_ᴱˣᴾᴼᴺᴱᴺᵀ_ᴼᶠᶠˢᴱᵀ * 1.f
     * for 0 < e < RAW_DOUBLE_MAX_EXPONENT
     */
    public static final int DOUBLE_EXPONENT_OFFSET = -1023;
    public static final int RAW_DOUBLE_MAX_EXPONENT = 0x000007FF;
    public static final int DOUBLE_MAX_EXPONENT = RAW_DOUBLE_MAX_EXPONENT + DOUBLE_EXPONENT_OFFSET - 1;
    public static final int DOUBLE_MIN_EXPONENT = -1022;
    public static final int DOUBLE_FRACTION_SIZE = 52;
    public static final int DOUBLE_EXPONENT_SIZE = 11;
    public static final int DOUBLE_SIGN_SIZE = 1;
    public static final long DOUBLE_FRACTION_MASK = 0x000FFFFFFFFFFFFFFFFL;
    public static final long DOUBLE_EXPONENT_MASK = 0x7FF0000000000000L;
    /**
     * equivalent to {@code Long.MAX_VALUE}
     */
    public static final long DOUBLE_SIGN_MASK = 0x8000000000000000L;

    /**
     * Even with {@code InfiniFloat} it's still not possible to calculate numbers larges than
     * Long.SIZE ** Arrays.MAX_ARRAY_SIZE - Long.SIZE ** (-Arrays.MAX_ARRAY_SIZE + 1)
     */
    public static final int MAX_CALCULABLE_FACTORIAL = 20000000;

    public static boolean isFinite(@NotNull InfiniNumber number) {
        return number.abs().compareTo(Double.POSITIVE_INFINITY) <= 0;
    }

    public static boolean isInfinite(InfiniNumber number) {
        return number == POSITIVE_INFINITY || number == NEGATIVE_INFINITY;
    }

    public static int compare(Number first, Number other) {
        return first instanceof InfiniNumber ? ((InfiniNumber) first).compareTo(other)
                : other instanceof InfiniNumber ? -((InfiniNumber) other).compareTo(first)
                : Double.compare(first.doubleValue(), other.doubleValue());
    }

    public static boolean isInteger(@NotNull Number n) {
        return n.longValue() == n.doubleValue();
    }

    public static boolean isNegative(long l) {
        return (l & 0x8000000000000000L) != 0;
    }

    /**
     * a more efficient and precise form of powering e to the given exponent
     * @param exp the exponent
     * @return e squared to {@param exp}
     */
    public static InfiniNumber exp(@NotNull InfiniNumber exp) {

    }

    public static InfiniNumber ln(@NotNull Number n) {
        if (!(n instanceof InfiniNumber number)) {
            double d = n.doubleValue();

            if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY) {
                return NaN;
            }

            return d < 0 ? new ComplexNumber((InfiniFloat) InfiniFloat.valueOf(Math.log(-d)), PI)
                    : InfiniFloat.valueOf(Math.log(d));
        }

        return number.log();
    }

    public static InfiniNumber sum(long start, long end, @NotNull LongFunction<? extends InfiniNumber> expr) {
        InfiniNumber result = InfiniFloat.ZERO;

        for (; start < end; start++) {
            result = result.add(expr.apply(start));

            if (result == NaN) {
                return NaN;
            }
        }

        return result;
    }

    public static InfiniNumber product(long start, long end, @NotNull LongFunction<? extends InfiniNumber> expr) {
        InfiniNumber result = InfiniFloat.ONE;

        for (; start < end; start++) {
            result = result.mul(expr.apply(start));

            if (result == NaN) {
                return NaN;
            }
        }

        return result;
    }

    public static final InfiniNumber factorial(int reallyLargeNumber) {
        return FACTORIAL_CACHE.computeIfAbsent(reallyLargeNumber, (n) -> {
            if (n < 0) {

            }

            if (n > MAX_CALCULABLE_FACTORIAL) {
                return POSITIVE_INFINITY;
            }

            InfiniNumber result = InfiniFloat.ONE;

            for (int i = 1; i < n; i++) {
                result = result.mul(i);
            }

            return result;
        });
    }

    static {
        E = (InfiniFloat) InfiniFloat.valueOf(Math.E);
        PI = (InfiniFloat) InfiniFloat.valueOf(Math.PI);
    }
}
