package svegon.utils.math.really_big_math;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import svegon.utils.collections.ArrayUtil;

import java.util.Arrays;
import java.util.ConcurrentModificationException;

@Immutable
public final class InfiniFloat extends InfiniNumber {
    private static final Long2ObjectMap<InfiniFloat> LONG_VALUE_CACHE =
            Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private static final Double2ObjectMap<InfiniNumber> DOUBLE_VALUE_CACHE =
            Double2ObjectMaps.synchronize(new Double2ObjectOpenHashMap<>());
    private static final int MAX_LONG_ARRAY_SIZE_POSSIBLY_CONTAINING_A_DOUBLE_REPRESENTABLE_NUMBER =
            (int) Math.ceil(((double) -ComplexMathUtil.DOUBLE_MIN_EXPONENT) / Long.SIZE);
    private static final int DOUBLE_NON_FRACTION_SIZE = Double.SIZE - ComplexMathUtil.DOUBLE_FRACTION_SIZE;
    private static final int POSSIBLE_LEADING_ZEROES = DOUBLE_NON_FRACTION_SIZE - 1;

    public static final InfiniFloat NEGATIVE_ONE = valueOf(-1);
    public static final InfiniFloat ZERO = valueOf(0);
    public static final InfiniFloat ONE = valueOf(1);
    public static final InfiniFloat TWO = valueOf(2);
    public static final InfiniFloat TEN = valueOf(10);

    private static final long[] ZERO_ARRAY = ZERO.intBits;

    public static final InfiniFloat NEGATIVE_ZERO = new InfiniFloat(true, ZERO_ARRAY);

    private final boolean sign;
    private final long[] intBits;
    private final long[] floatBits;
    private Integer hashCode;

    InfiniFloat(boolean sign, long[] intBits, long[] floatBits) {
        this.sign = sign;
        this.intBits = intBits;
        this.floatBits = floatBits;
    }

    private InfiniFloat(boolean sign, long[] intBits) {
        this(sign, intBits, LongArrays.EMPTY_ARRAY);
    }

    @Override
    public int compareTo(@NotNull Number o) {
        if (this == o) {
            return 0;
        }

        if (o == ComplexMathUtil.NaN) {
            return isNegative() ? 1 : -1;
        }

        if (o == ComplexMathUtil.NEGATIVE_INFINITY) {
            return 1;
        }

        if (o == ComplexMathUtil.POSITIVE_INFINITY) {
            return -1;
        }

        if (o instanceof InfiniFloat other) {
            if (intBits.length != other.intBits.length) {
                return intBits.length < other.intBits.length == isNegative() ? -1 : 1;
            }

            int c = Long.compare(intBits[0], other.intBits[0]);

            if (c != 0) {
                return c;
            }

            int length = intBits.length;

            for (int i = 1; i < length; i++) {
                c = Long.compareUnsigned(intBits[i], other.intBits[i]);

                if (c != 0) {
                    return c;
                }
            }

            length = Math.min(floatBits.length, other.floatBits.length);

            for (int i = 0; i < length; i++) {
                c = Long.compareUnsigned(floatBits[i], other.floatBits[i]);

                if (c != 0) {
                    return c;
                }
            }

            if (floatBits.length == other.floatBits.length) {
                return 0;
            }

            return floatBits.length < other.floatBits.length == isNegative() ? -1 : 1;
        }

        return Double.compare(doubleValue(), o.doubleValue());
    }

    @Override
    public long longValue() {
        return intBits[intBits.length - 1];
    }

    /**
     * Returns the closest possible double representation of this InfiniFloat
     * Due to difference in double and InfiniFloat arithmetics this method
     * is quiete ineffective and should be avoided where possible.
     * @return double with the same value at 52-bit precision
     */
    @Override
    public double doubleValue() {
        int intBitsLength = intBits.length;
        int floatBitsLength = floatBits.length;
        long firstBits = intBits[0];

        long longBits;

        if (firstBits != 0) {
            int leadingZeroes = Long.numberOfLeadingZeros(firstBits);
            long exponent = (long) intBitsLength * Long.SIZE - leadingZeroes;

            if (exponent > ComplexMathUtil.DOUBLE_MAX_EXPONENT) {
                return isNegative() ? Double.MIN_VALUE : Double.MAX_VALUE;
            }

            longBits = (firstBits << (leadingZeroes - POSSIBLE_LEADING_ZEROES))
                    & ComplexMathUtil.DOUBLE_FRACTION_MASK;

            if (leadingZeroes > POSSIBLE_LEADING_ZEROES) {
                if (intBitsLength > 1) {
                    longBits |= intBits[1] >> POSSIBLE_LEADING_ZEROES + Long.SIZE - leadingZeroes;
                } else if (!isInteger()) {
                    longBits |= floatBits[0] >> POSSIBLE_LEADING_ZEROES + Long.SIZE - leadingZeroes;
                }
            }

            longBits |= (exponent - ComplexMathUtil.DOUBLE_EXPONENT_OFFSET)
                    << ComplexMathUtil.DOUBLE_FRACTION_SIZE;
        } else {
            int index = 0;

            while (floatBits[index++] == 0);

            long firstFloatBits = floatBits[index];
            int leadingZeroes = Long.numberOfLeadingZeros(firstFloatBits);
            long exponent = -((long) index * Long.SIZE + leadingZeroes);

            if (exponent < ComplexMathUtil.DOUBLE_MIN_EXPONENT) {
                if (exponent < ComplexMathUtil.DOUBLE_MIN_EXPONENT - ComplexMathUtil.DOUBLE_FRACTION_SIZE) {
                    return 0;
                }

                leadingZeroes += ComplexMathUtil.DOUBLE_EXPONENT_OFFSET - exponent;
                exponent = ComplexMathUtil.DOUBLE_EXPONENT_OFFSET;
            }

            longBits = (firstFloatBits << (leadingZeroes - POSSIBLE_LEADING_ZEROES))
                    & ComplexMathUtil.DOUBLE_FRACTION_MASK;

            if (leadingZeroes > POSSIBLE_LEADING_ZEROES && floatBitsLength > ++index) {
                longBits |= floatBits[index] >>> POSSIBLE_LEADING_ZEROES + Long.SIZE - leadingZeroes;
            }

            longBits |= (exponent - ComplexMathUtil.DOUBLE_EXPONENT_OFFSET)
                    << ComplexMathUtil.DOUBLE_FRACTION_SIZE;
        }

        if (isNegative()) {
            longBits |= ComplexMathUtil.DOUBLE_SIGN_MASK;
        }

        return Double.longBitsToDouble(longBits);
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = 31 * Arrays.hashCode(intBits) + Arrays.hashCode(floatBits);
        }

        return hashCode;
    }

    @Override
    public @NotNull InfiniNumber add(@NotNull Number other) {
        if (other instanceof Infinity) {
            return (InfiniNumber) other;
        }

        if (other instanceof InfiniFloat infiniFloat) {
            if (infiniFloat.isNegative()) {
                if (isNegative()) {
                    return neg().add(infiniFloat.neg()).neg();
                }

                return infiniFloat.add(this);
            }

            long[] shorterIntBits = intBits;
            long[] longerIntBits = infiniFloat.intBits;
            long[] shorterFloatBits = floatBits;
            long[] longerFloatBits = infiniFloat.floatBits;

            if (longerIntBits.length < shorterIntBits.length) {
                shorterIntBits = longerIntBits;
                longerIntBits = floatBits;
            }

            if (longerFloatBits.length < shorterFloatBits.length) {
                shorterFloatBits = longerFloatBits;
                longerFloatBits = floatBits;
            }

            boolean resultSign = isNegative();
            long[] resultIntBits;
            long[] resultFloatBits;

            long temp;
            boolean bit64 = false;

            int longerBitsIndex = longerFloatBits.length;
            int shorterBitsIndex = shorterFloatBits.length;

            if (shorterBitsIndex != 0) {
                resultFloatBits = new long[longerBitsIndex];

                System.arraycopy(longerFloatBits, shorterBitsIndex, resultFloatBits, shorterBitsIndex,
                        longerBitsIndex - shorterBitsIndex);

                while (shorterBitsIndex-- > 0) {
                    long first = longerFloatBits[shorterBitsIndex];
                    long second = shorterFloatBits[shorterBitsIndex];

                    if (bit64) {
                        resultFloatBits[shorterBitsIndex] = temp = first + second + 1;
                    } else {
                        resultFloatBits[shorterBitsIndex] = temp = first + second;
                    }

                    bit64 = ComplexMathUtil.isNegative(first) && ComplexMathUtil.isNegative(second)
                            && !ComplexMathUtil.isNegative(temp);
                }
            } else {
                resultFloatBits = longerFloatBits;
            }

            longerBitsIndex = longerIntBits.length;
            shorterBitsIndex = shorterIntBits.length;

            if (shorterBitsIndex != 1 || shorterIntBits[0] != 0) {
                resultIntBits = new long[longerBitsIndex];

                while (longerBitsIndex-- > 0 && shorterBitsIndex-- > 0) {
                    long first = longerIntBits[longerBitsIndex];
                    long second = shorterIntBits[shorterBitsIndex];

                    if (bit64) {
                        resultIntBits[longerBitsIndex] = temp = first + second + 1;
                    } else {
                        resultIntBits[longerBitsIndex] = temp = first + second;
                    }

                    bit64 = ComplexMathUtil.isNegative(first) && ComplexMathUtil.isNegative(second)
                            && !ComplexMathUtil.isNegative(temp);
                }

                if (longerBitsIndex < 0) {
                    if (bit64) {
                        if (resultSign) {
                            resultSign = false;
                        } else {
                            if (resultIntBits.length + 1 > it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                                throw new OutOfMemoryError("array size out of range");
                            }

                            resultIntBits = ArrayUtil.merge(new long[]{1}, resultIntBits);
                        }
                    }
                } else {
                    if (bit64) {
                        resultIntBits[longerBitsIndex] = longerIntBits[longerBitsIndex] + 1;
                        longerBitsIndex--;
                    }

                    System.arraycopy(longerIntBits, 0, resultIntBits, 0, longerBitsIndex);
                }
            } else {
                resultIntBits = longerIntBits;
            }

            return new InfiniFloat(resultSign, resultIntBits, resultFloatBits);
        }

        long longValue = other.longValue();
        double doubleValue = other.doubleValue();

        if (longValue == doubleValue) {
            return add(valueOf(longValue));
        } else {
            return add(valueOf(doubleValue));
        }
    }

    @Override
    public @NotNull InfiniNumber mul(@NotNull Number other) {
        if (!(other instanceof InfiniNumber infiniNumber)) {
            return mul(valueOf(other.doubleValue()));
        }

        if (!(other instanceof InfiniFloat infiniFloat)) {
            return infiniNumber.mul(this);
        }

        int arrayLength = infiniFloat.intBits.length;

        if (arrayLength > intBits.length) {
            return infiniFloat.mul(this);
        }

        InfiniNumber result = ZERO;

        for (int i = 0; i < arrayLength; i++) {
            if (infiniFloat.intBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                int offset = (arrayLength - i) * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfTrailingZeros(setBits)) != 64) {
                    result = result.add(lShift(offset + setBit));
                    setBits &= ~(1L << setBit);
                }
            }
        }

        arrayLength = infiniFloat.floatBits.length;

        for (int i = 0; i < arrayLength; i++) {
            if (infiniFloat.floatBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                int offset = i * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    result = result.add(rShift(offset + setBit));
                    setBits &= ~(0x8000000000000000L >> setBit);
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull InfiniNumber div(@NotNull Number divider) {
        if (!(divider instanceof InfiniNumber infiniNumber)) {
            return floorDiv(valueOf(divider.doubleValue()));
        }

        if (infiniNumber instanceof Infinity) {
            if (infiniNumber == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return ZERO;
        }

        if (!(infiniNumber instanceof InfiniFloat infiniFloat)) {
            return ComplexNumber.ONE.div(infiniNumber.div(this));
        }

        int arrayLength = infiniFloat.intBits.length;
        InfiniNumber result = ZERO;
        InfiniFloat rest = this;

        for (int i = 0; i < arrayLength && !rest.isZero(); i++) {
            if (infiniFloat.intBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                int offset = (arrayLength - i) * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    InfiniFloat resultMember = rShift(offset + setBit);
                    result = result.add(resultMember);
                    rest = (InfiniFloat) rest.substract(resultMember.mul(infiniFloat));
                    setBits &= ~(1L << setBit);
                }
            }
        }

        arrayLength = infiniFloat.floatBits.length;

        for (int i = 0; i < arrayLength && !rest.isZero(); i++) {
            if (infiniFloat.floatBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                int offset = i * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    InfiniFloat resultMember = lShift(offset + setBit);
                    result = result.add(resultMember);
                    rest = (InfiniFloat) rest.substract(resultMember.mul(infiniFloat));
                    setBits &= ~(0x8000000000000000L >> setBit);
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull InfiniNumber floorDiv(@NotNull Number other) {
        if (!(other instanceof InfiniNumber infiniNumber)) {
            return floorDiv(valueOf(other.doubleValue()));
        }

        if (other instanceof Infinity) {
            if (other == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return ZERO;
        }

        if (!(infiniNumber instanceof InfiniFloat infiniFloat)) {
            return div(infiniNumber).floor();
        }

        int otherFloatBitsLength = infiniFloat.floatBits.length;

        if (otherFloatBitsLength > 0) {
            long shift = (long) otherFloatBitsLength * Long.SIZE
                    - Long.numberOfTrailingZeros(infiniFloat.floatBits[otherFloatBitsLength - 1]);
            return lShift(shift).floorDiv(infiniFloat.lShift(shift));
        }

        int arrayLength = infiniFloat.intBits.length;
        InfiniNumber result = ZERO;
        InfiniNumber rest = this;

        for (int i = 0; i < arrayLength; i++) {
            if (infiniFloat.intBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                int offset = (arrayLength - i) * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    InfiniFloat resultMember = rShift(offset + setBit);
                    InfiniFloat flooredMember = resultMember.floor();

                    if (flooredMember.isZero()) {
                        return result;
                    }

                    result = result.add(flooredMember);
                    rest = rest.substract(resultMember.mul(infiniFloat));
                    setBits &= ~(1L << setBit);
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull InfiniNumber pow(@NotNull Number exp) {
        if (exp instanceof Infinity) {
            if (exp == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            int cmp = abs().compareTo(ONE);

            if (cmp == 0) {
                return ComplexMathUtil.NaN;
            }

            return (cmp < 0) != (exp == ComplexMathUtil.NEGATIVE_INFINITY)
                    ? ZERO : ComplexMathUtil.POSITIVE_INFINITY;
        }

        if (!(exp instanceof InfiniNumber)) {
            return pow(valueOf(exp.doubleValue()));
        }

        if (exp instanceof InfiniFloat other) {
            if (other.isZero()) {
                return ONE;
            }

            if (other.compareTo(ONE) == 0) {
                return this;
            }
        }

        return ComplexMathUtil.exp(log().mul(exp));
    }

    @Override
    public @NotNull InfiniNumber pow(@NotNull Number exp, @NotNull Number mod) {
        if (exp instanceof Infinity) {
            if (exp == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return pow(exp).mod(mod);
        }

        if (mod instanceof Infinity) {
            if (mod == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return ZERO;
        }

        if (exp instanceof ComplexNumber) {
            return new ComplexNumber(this).pow(exp);
        }

        if (!(exp instanceof InfiniFloat other)) {
            return pow(valueOf(exp.doubleValue()), mod);
        }

        if (other.isInteger()) {
            if (other.isZero()) {
                return ONE.mod(mod);
            }

            if (other.compareTo(ONE) == 0)) {
                return mod(mod);
            }

            if (ONE.compareTo(mod) == 0) {
                InfiniFloat result = new InfiniFloat(isNegative(), ZERO_ARRAY, floatBits);

                return result.isNegative() ? ONE.add(result) : result;
            }

            if (mod instanceof InfiniFloat infiniFloat) {
                InfiniNumber var = infiniFloat.substract(ONE);

                if (var.mul(var).compareTo(this) > 0) {
                    return pow(exp).mod(mod); // using right-to-left method would incorrectly result in 0
                }
            }

            InfiniNumber result = ONE;
            InfiniNumber base = mod(mod);

            while (other.compareTo(ZERO) > 0) {
                if ((other.intBits[other.intBits.length - 1] & 1) == 1) {
                    result = result.mul(base).mod(mod);
                }

                other = other.rShift(1).floor();
                base = base.mul(base).mod(mod);
            }

            return result;
        }

        return pow(exp).mod(mod);
    }

    @Override
    public @NotNull InfiniNumber log() {
        if (isNegative()) {
            return new ComplexNumber((InfiniFloat) neg().log(), ComplexMathUtil.PI);
        }

        InfiniFloat base = (InfiniFloat) substract(ONE);

        return ComplexMathUtil.sum(1, ComplexMathUtil.MAX_CALCULABLE_FACTORIAL, (i) -> {
            return ComplexMathUtil.factorial(i);
        });
    }

    @Override
    public @NotNull InfiniNumber mod(@NotNull Number other) {
        if (other instanceof Infinity) {
            if (other == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return ZERO;
        }

        if (!(other instanceof InfiniFloat infiniFloat)) {
            return mod(valueOf(other.doubleValue()));
        }
    }

    @Override
    public @NotNull Pair<@NotNull InfiniNumber, @NotNull InfiniNumber> divMod(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniFloat lShift(long by) {
        return null;
    }

    @Override
    public @NotNull InfiniFloat rShift(long by) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber and(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber or(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber xor(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniFloat invert() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber sign() {
        if (isNegative()) {
            return -1;
        }

        return isZero() ? 0 : 1;
    }

    @Override
    public @NotNull InfiniFloat abs() {
        return isNegative() ? new InfiniFloat(false, intBits, floatBits) : this;
    }

    @Override
    public @NotNull InfiniFloat neg() {
        return new InfiniFloat(!isNegative(), intBits, floatBits);
    }

    @Override
    public @NotNull InfiniFloat floor() {
        return new InfiniFloat(isNegative(), intBits);
    }

    @Override
    public @NotNull InfiniFloat ceil() {
        return !isInteger() ? isNegative() ?  (InfiniFloat) floor().substract(ONE) : (InfiniFloat) floor().add(ONE)
                : floor();
    }

    @Override
    public @NotNull InfiniFloat round() {
        if (!isInteger() && (floatBits[0] & 0x8000000000000000L) != 0) {
            if (isNegative()) {
                return (InfiniFloat) floor().substract(ONE);
            } else {
                return (InfiniFloat) floor().add(ONE);
            }
        } else {
            return floor();
        }
    }

    @Override
    public @NotNull InfiniFloat round(long decimalDigits) {
        InfiniNumber exponent = TEN.pow(decimalDigits);
        return (InfiniFloat) mul(exponent).round().div(exponent);
    }

    @Override
    public long size() {
        return isInteger() ? (long) intBits.length * Long.SIZE - Long.numberOfLeadingZeros(intBits[0])
                : ((long) intBits.length) * Long.SIZE - Long.numberOfLeadingZeros(intBits[0])
                - Long.numberOfTrailingZeros(floatBits[floatBits.length - 1]);
    }

    public boolean isNegative() {
        return sign;
    }

    public boolean isZero() {
        return isInteger() && intBits.length == 1 && intBits[0] == 0;
    }

    public boolean isInteger() {
        return floatBits.length == 0;
    }

    public static InfiniFloat valueOf(long i) {
        try {
            return LONG_VALUE_CACHE.computeIfAbsent(i, (j) -> {
                if (ComplexMathUtil.isNegative(j)) {
                    return new InfiniFloat(true, new long[]{-j});
                } else {
                    return new InfiniFloat(false, new long[]{j});
                }
            });
        } catch (ConcurrentModificationException e) {
            return valueOf(i);
        }
    }

    /**
     * Returns a InfiniNumber representation of the given double.
     * It is guaranteed that the result is equal to argument unless it NaN.
     *
     * <p>
     * Special cases:
     * <ul><li>If the argument is NaN, the result is ComplexMathUtil.NaN.
     * <li>If the argument is negative infinity, the result is ComplexMathUtil.NEGATIVE_INFINITY
     * <li>If the argument is positive infinity, the result is ComplexMathUtil.POSITIVE_INFINITY</ul>
     *
     * @param d the number as a double
     * @return the number as a InfiniNumber
     */
    public static InfiniNumber valueOf(double d) {
        try {
            return DOUBLE_VALUE_CACHE.computeIfAbsent(d, (e) -> {
                long longBits = Double.doubleToLongBits(e);
                long fraction = longBits & ComplexMathUtil.DOUBLE_FRACTION_MASK;
                int exponent = (int) (longBits & ComplexMathUtil.DOUBLE_EXPONENT_MASK
                        >>> ComplexMathUtil.DOUBLE_FRACTION_SIZE);
                long sign = longBits & ComplexMathUtil.DOUBLE_SIGN_MASK;

                if (exponent == 0) {
                    if (fraction == 0 && sign != 0) {
                        return NEGATIVE_ZERO;
                    }

                    return valueOf(sign | fraction).lShift(-1022);
                } else if (exponent == ComplexMathUtil.RAW_DOUBLE_MAX_EXPONENT) {
                    if (fraction == 0) {
                        return sign == 0 ? ComplexMathUtil.POSITIVE_INFINITY : ComplexMathUtil.NEGATIVE_INFINITY;
                    } else {
                        return ComplexMathUtil.NaN;
                    }
                } else {
                    return valueOf(sign | fraction | (1L << ComplexMathUtil.DOUBLE_FRACTION_SIZE))
                            .lShift(exponent + ComplexMathUtil.DOUBLE_EXPONENT_OFFSET);
                }
            });
        } catch (ConcurrentModificationException e) {
            return valueOf(d);
        }
    }

    public static InfiniFloat ofBits(boolean sign, long @NotNull [] intBits) {
        return ofBits(sign, intBits, LongArrays.EMPTY_ARRAY);
    }

    /**
     *
     * @param sign
     * @param intBits
     * @param floatBits
     * @return
     */
    public static InfiniFloat ofBits(boolean sign, long @NotNull [] intBits, long @NotNull [] floatBits) {
        intBits = intBits.length != 0 ? intBits.clone() : ZERO_ARRAY;
        floatBits = floatBits.length != 0 ? floatBits.clone() : LongArrays.EMPTY_ARRAY;

        int intBitsLength = intBits.length;
        int floatBitsLength = floatBits.length;
        int leadingZeroes = 0;
        int trailingZeroes = floatBitsLength;

        while (leadingZeroes < intBitsLength && intBits[leadingZeroes++] == 0);
        while (trailingZeroes-- > 0 && floatBits[trailingZeroes] == 0);

        if (leadingZeroes != 0) {
            intBits = leadingZeroes != intBitsLength ? ArrayUtil.copyAtEnd(intBits,
                    intBitsLength - leadingZeroes) : ZERO_ARRAY;
        }

        if (++trailingZeroes != floatBitsLength) {
            floatBits = trailingZeroes != 0 ? ArrayUtil.copyAtEnd(floatBits, trailingZeroes) : LongArrays.EMPTY_ARRAY;
        }

        return new InfiniFloat(sign, intBits, floatBits);
    }

    static {
        DOUBLE_VALUE_CACHE.put(-0.0D, NEGATIVE_ZERO);
        DOUBLE_VALUE_CACHE.put(Double.POSITIVE_INFINITY, ComplexMathUtil.POSITIVE_INFINITY);
        DOUBLE_VALUE_CACHE.put(Double.NEGATIVE_INFINITY, ComplexMathUtil.NEGATIVE_INFINITY);
        DOUBLE_VALUE_CACHE.put(Double.NaN, ComplexMathUtil.NaN);
    }
}
