package com.github.svegon.utils.math.really_big_math;

import com.github.svegon.utils.reflect.CommonReflections;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.internal.LazilyParsedNumber;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import com.github.svegon.utils.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.*;
import java.util.function.IntFunction;
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
    public static final long DOUBLE_EXPONENT_MASK = (long) RAW_DOUBLE_MAX_EXPONENT << DOUBLE_FRACTION_SIZE;
    /**
     * equivalent to {@code Long.MAX_VALUE}
     */
    public static final long DOUBLE_SIGN_MASK = 0x8000000000000000L;

    /**
     * Even with {@code InfiniFloat} it's still not possible to calculate numbers larges than
     * Long.SIZE ** Arrays.MAX_ARRAY_SIZE - Long.SIZE ** (-Arrays.MAX_ARRAY_SIZE + 1)
     * api note: couldn't calc it actually, so just put something that wouldn't take too much memory
     */
    public static final int MAX_CALCULABLE_FACTORIAL = 9999;

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

    public static InfiniNumber cast(@NotNull Number n) {
        if (n instanceof InfiniNumber) {
            return (InfiniNumber) n;
        }

        if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long
                || n instanceof AtomicInteger || n instanceof AtomicLong || n instanceof UnsignedInteger
                || n instanceof LongAdder || n instanceof LongAccumulator) {
            return InfiniFloat.valueOf(n.longValue());
        }

        if (n instanceof Float || n instanceof Double || n instanceof AtomicDouble || n instanceof DoubleAdder
                || n instanceof DoubleAccumulator) {
            return InfiniFloat.valueOf(n.doubleValue());
        }

        if (n instanceof LazilyParsedNumber) {
            return StringUtil.parseInfiniNumber(n.toString());
        }

        if (n instanceof UnsignedLong) {
            n = ((UnsignedLong) n).bigIntegerValue();
        }

        if (n instanceof BigInteger) {
            final int[] mag;
            final long[] intBits;

            try {
                mag = (int[]) CommonReflections.BIG_INTEGER_MAG_FIELD.get(n);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }

            if ((mag.length & 1) == 0) {
                intBits = new long[mag.length >> 1];

                Arrays.setAll(intBits, (i) -> (long) mag[i << 1] << Integer.SIZE + mag[i << 1 + 1]);
            } else {
                intBits = new long[mag.length >> 1 + 1];
                intBits[0] = mag[0];
                int length = intBits.length;

                for (int i = 1; i != length; i++) {
                    int temp = i << 1;
                    intBits[i] = (long) mag[temp - 1] << Integer.SIZE + mag[temp];
                }
            }


            return intBits.length != 0 ? new InfiniFloat(((BigInteger) n).signum() < 0, intBits)
                    : ((BigInteger) n).signum() < 0 ? InfiniFloat.NEGATIVE_ZERO : InfiniFloat.ZERO;
        }

        if (n instanceof BigDecimal d) {
            return cast(d.unscaledValue()).mul(InfiniFloat.ZERO_POINT_ONE.pow(d.scale()));
        }

        return InfiniFloat.valueOf(n.doubleValue());
    }

    public static boolean isNegative(long l) {
        return (l & 0x8000000000000000L) != 0;
    }

    /**
     * a more efficient and precise form of powering e to the given exponent
     * @param exp the exponent
     * @return e squared to {@param exp}
     */
    public static InfiniNumber exp(final @NotNull InfiniNumber exp) {
        return sum(0, MAX_CALCULABLE_FACTORIAL, (int i) -> exp.pow(InfiniFloat.valueOf(i)).div(factorial(i)));
    }

    public static InfiniNumber sum(int start, int end, @NotNull IntFunction<? extends InfiniNumber> expr) {
        InfiniNumber result = InfiniFloat.ZERO;

        for (; start < end; start++) {
            result = result.add(expr.apply(start));

            if (result == NaN) {
                return NaN;
            }
        }

        return result;
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

    public static InfiniNumber product(int start, int end, @NotNull IntFunction<? extends InfiniNumber> expr) {
        InfiniNumber result = InfiniFloat.ONE;

        for (; start < end; start++) {
            result = result.mul(expr.apply(start));

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

    public static InfiniNumber factorial(int reallyLargeNumber) {
        return FACTORIAL_CACHE.computeIfAbsent(reallyLargeNumber, (n) -> {
            if (n < 0) {
                return NaN;
            }

            if (n > MAX_CALCULABLE_FACTORIAL) {
                throw new OutOfMemoryError("Please stop. This is too much even for scientific calculators.");
            }

            return factorial(n - 1).mul(InfiniFloat.valueOf(n));
        });
    }

    static {
        FACTORIAL_CACHE.put(0, InfiniFloat.ONE);
        FACTORIAL_CACHE.put(1, InfiniFloat.ONE);
    }
}
