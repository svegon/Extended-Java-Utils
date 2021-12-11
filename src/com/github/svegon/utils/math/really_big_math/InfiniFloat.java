package com.github.svegon.utils.math.really_big_math;

import com.github.svegon.utils.collections.ArrayUtil;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
    public static final InfiniFloat ZERO_POINT_ONE = (InfiniFloat) ONE.div(TEN);

    private static final long[] ZERO_ARRAY = ZERO.intBits;

    public static final InfiniFloat NEGATIVE_ZERO = new InfiniFloat(true, ZERO_ARRAY);

    private final boolean sign;
    private final long[] intBits;
    private final long[] floatBits;
    private double doubleValue = Double.NaN;
    private int hashCode;
    private boolean initHashCode = true;

    InfiniFloat(boolean sign, long[] intBits, long[] floatBits) {
        this.sign = sign;
        this.intBits = intBits;
        this.floatBits = floatBits;
    }

    InfiniFloat(boolean sign, long[] intBits) {
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

        if (!(o instanceof InfiniFloat other)) {
            return -ComplexMathUtil.cast(o).compareTo(this);
        }

        if (other.isZero()) {
            if (isZero()) {
                return 0;
            }

            return isNegative() ? -1 : 1;
        }

        if (isNegative() != other.isNegative()) {
            return isNegative() ? -1 : 1;
        }

        if (intBits.length != other.intBits.length) {
            return intBits.length < other.intBits.length == isNegative() ? -1 : 1;
        }

        int length = intBits.length;
        int c;

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
        if (Double.isNaN(doubleValue)) {
            doubleValue = initDouble();
        }

        return doubleValue;
    }

    private double initDouble() {
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
        if (initHashCode) {
            hashCode = 31 * Arrays.hashCode(intBits) + Arrays.hashCode(floatBits);
            initHashCode = false;
        }

        return hashCode;
    }

    @Override
    public @NotNull InfiniNumber add(@NotNull Number other) {
        if (other instanceof Infinity) {
            return (InfiniNumber) other;
        }

        if (other instanceof ComplexNumber) {
            return new ComplexNumber(this).add(other);
        }

        if (!(other instanceof InfiniFloat infiniFloat)) {
            return add(ComplexMathUtil.cast(other));
        }

        if (infiniFloat.isZero()) {
            return this;
        }

        if (infiniFloat.isNegative()) {
            if (isNegative()) {
                return neg().add(infiniFloat.neg()).neg();
            }

            long[] resultIntBits;
            long[] resultFloatBits;
            boolean resultSign = false;

            boolean bit64 = false;

            int thisBitsIndex = floatBits.length;
            int otherBitsIndex = infiniFloat.floatBits.length;

            if (thisBitsIndex > otherBitsIndex) {
                resultFloatBits = new long[thisBitsIndex];

                System.arraycopy(floatBits, otherBitsIndex, resultFloatBits, otherBitsIndex,
                        thisBitsIndex - otherBitsIndex);
            } else {
                resultFloatBits = new long[otherBitsIndex];

                if (otherBitsIndex != thisBitsIndex) {
                    while (otherBitsIndex-- > thisBitsIndex) {
                        long second = infiniFloat.floatBits[otherBitsIndex];

                        if (second == 0) {
                            if (bit64) {
                                resultFloatBits[otherBitsIndex] = -1;
                            }
                        } else {
                            if (bit64) {
                                resultFloatBits[otherBitsIndex] = -second - 1;
                            } else {
                                resultFloatBits[otherBitsIndex] = -second;
                                bit64 = true;
                            }
                        }
                    }
                }
            }

            while (otherBitsIndex-- > 0) {
                long first = floatBits[otherBitsIndex];
                long second = infiniFloat.floatBits[otherBitsIndex];

                if (bit64) {
                    resultFloatBits[thisBitsIndex] = first - second - 1;
                } else {
                    resultFloatBits[thisBitsIndex] = first - second;
                }

                bit64 = Long.compareUnsigned(first, second) < 0;
            }

            thisBitsIndex = intBits.length;
            otherBitsIndex = infiniFloat.intBits.length;

            if (thisBitsIndex > otherBitsIndex) {
                resultIntBits = new long[thisBitsIndex];

                while (otherBitsIndex-- > 0) {
                    thisBitsIndex--;

                    long first = intBits[thisBitsIndex];
                    long second = infiniFloat.intBits[otherBitsIndex];

                    if (bit64) {
                        resultIntBits[thisBitsIndex] = first - second - 1;
                    } else {
                        resultIntBits[thisBitsIndex] = first - second;
                    }

                    bit64 = Long.compareUnsigned(first, second) < 0;
                }

                while (bit64) {
                    long first = intBits[thisBitsIndex];

                    resultIntBits[thisBitsIndex--] = first - 1;
                    bit64 = first == 0;
                }

                System.arraycopy(intBits, 0, resultIntBits, 0, thisBitsIndex);
            } else {
                resultIntBits = new long[otherBitsIndex];

                while (thisBitsIndex-- > 0) {
                    otherBitsIndex--;

                    long first = intBits[thisBitsIndex];
                    long second = infiniFloat.intBits[otherBitsIndex];

                    if (bit64) {
                        resultIntBits[thisBitsIndex] = first - second - 1;
                    } else {
                        resultIntBits[thisBitsIndex] = first - second;
                    }

                    bit64 = Long.compareUnsigned(first, second) < 0;
                }

                while (otherBitsIndex-- > 0) {
                    long temp = infiniFloat.intBits[otherBitsIndex];

                    if (bit64) {
                        resultIntBits[otherBitsIndex] = -temp - 1;
                        bit64 = false;
                    } else {
                        resultIntBits[otherBitsIndex] = -temp;
                    }
                }

                resultSign = bit64 || infiniFloat.intBits.length > intBits.length;
            }

            return new InfiniFloat(resultSign, resultIntBits, resultFloatBits);
        }

        if (isNegative()) {
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

            while (bit64) {
                if (longerBitsIndex < 0) {
                    if (resultIntBits.length + 1 > it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                        throw new OutOfMemoryError("InfiniFloat size out of range");
                    }

                    resultIntBits = ArrayUtil.merge(new long[]{1}, resultIntBits);
                    break;
                }

                temp = longerIntBits[longerBitsIndex];
                resultIntBits[longerBitsIndex] = temp + 1;
                bit64 = temp == Long.MAX_VALUE;
                longerBitsIndex--;
            }

            System.arraycopy(longerIntBits, 0, resultIntBits, 0, longerBitsIndex);
        } else {
            resultIntBits = longerIntBits;
        }

        return new InfiniFloat(false, resultIntBits, resultFloatBits);
    }

    @Override
    public @NotNull InfiniNumber mul(@NotNull Number other) {
        if (!(other instanceof InfiniNumber infiniNumber)) {
            return mul(ComplexMathUtil.cast(other));
        }

        if (!(other instanceof InfiniFloat infiniFloat)) {
            return infiniNumber.mul(this);
        }

        int arrayLength = infiniFloat.intBits.length;

        if (arrayLength > intBits.length) {
            return infiniFloat.mul(this);
        }

        if (infiniFloat.compareTo(ONE) == 0) {
            return this;
        }

        InfiniFloat abs = abs();
        InfiniFloat multiplier = infiniFloat.abs();
        InfiniNumber result = ZERO;

        for (int i = 0; i < arrayLength; i++) {
            if (multiplier.intBits[i] != 0) {
                long setBits = multiplier.intBits[i];
                int offset = (arrayLength - i) * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfTrailingZeros(setBits)) != 64) {
                    result = result.add(abs.lShift(offset + setBit));
                    setBits &= ~(1L << setBit);
                }
            }
        }

        arrayLength = multiplier.floatBits.length;

        for (int i = 0; i < arrayLength; i++) {
            if (multiplier.floatBits[i] != 0) {
                long setBits = multiplier.intBits[i];
                int offset = i * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    result = result.add(abs.rShift(offset + setBit));
                    setBits &= ~(0x8000000000000000L >> setBit);
                }
            }
        }

        if (isNegative() != infiniFloat.isNegative()) {
            result = result.neg();
        }

        return result;
    }

    @Override
    public @NotNull InfiniNumber div(@NotNull Number divider) {
        if (!(divider instanceof InfiniNumber infiniNumber)) {
            return floorDiv(ComplexMathUtil.cast(divider));
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
                int offset = i * Long.SIZE + 1;
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
            return pow(ComplexMathUtil.cast(exp));
        }

        if (exp instanceof InfiniFloat other && other.isInteger()) {
            if (other.isZero()) {
                return ONE;
            }

            if (other.compareTo(ONE) == 0) {
                return this;
            }

            if (other.isNegative()) {
                return ONE.div(pow(other.neg()));
            }

            InfiniNumber result = ONE;

            while (!other.isZero()) {
                other = (InfiniFloat) other.substract(ONE);
                result = result.mul(this);
            }

            return result;
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
            return pow(ComplexMathUtil.cast(exp), mod);
        }

        if (other.isInteger()) {
            if (other.isZero()) {
                return ONE.mod(mod);
            }

            if (other.compareTo(ONE) == 0) {
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
            return new ComplexNumber((InfiniFloat) neg().log(), (InfiniFloat) valueOf(Math.PI));
        }

        final InfiniFloat base = (InfiniFloat) substract(ONE);

        return ComplexMathUtil.sum(1, ComplexMathUtil.MAX_CALCULABLE_FACTORIAL, (int i) -> {
            InfiniNumber result = ComplexMathUtil.factorial(i).div(base.pow(-i));

            if ((i & 1) == 0) {
                result = result.neg();
            }

            return result;
        });
    }

    @Override
    public @NotNull InfiniNumber floorDiv(@NotNull Number other) {
        if (!(other instanceof InfiniNumber infiniNumber)) {
            return floorDiv(ComplexMathUtil.cast(other));
        }

        if (other instanceof Infinity) {
            if (other == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return ZERO;
        }

        return divMod(other).first();
    }

    @Override
    public @NotNull InfiniNumber mod(@NotNull Number other) {
        if (other instanceof Infinity) {
            if (other == ComplexMathUtil.NaN) {
                return ComplexMathUtil.NaN;
            }

            return this;
        }

        if (other instanceof ComplexNumber) {
            return new ComplexNumber(this).mod(other);
        }

        if (!(other instanceof InfiniFloat infiniFloat)) {
            return mod(ComplexMathUtil.cast(other));
        }

        infiniFloat = infiniFloat.abs();
        int cmp = abs().compareTo(infiniFloat);

        // check if the this and modulo equal and thus the result has to be 0
        if (cmp == 0) {
            return ZERO;
        }

        // check if modulo is this object (possibly negative and if so convert to positive)
        if (cmp < 0) {
            return isNegative() ? infiniFloat.add(this) : this;
        }

        if (infiniFloat.isZero()) {
            throw new ArithmeticException("modulo by 0");
        }

        if (infiniFloat.isInteger() && Long.bitCount(infiniFloat.intBits[0]) == 1
                && Arrays.stream(intBits, 1, infiniFloat.intBits.length).allMatch((i) -> i == 0)) {
            return and(infiniFloat.abs().substract(ONE).neg());
        }

        return divMod(other).right();
    }

    @Override
    public @NotNull ObjectObjectImmutablePair<@NotNull InfiniNumber, @NotNull InfiniNumber>
    divMod(@NotNull Number other) {
        if (other instanceof Infinity) {
            return new ObjectObjectImmutablePair<>(floorDiv(other), ComplexMathUtil.NaN);
        }

        if (other instanceof ComplexNumber) {
            return new ComplexNumber(this).divMod(other);
        }

        if (!(other instanceof InfiniFloat infiniFloat)) {
            return divMod(ComplexMathUtil.cast(other));
        }

        int arrayLength = infiniFloat.intBits.length;
        InfiniNumber flooredDiv = ZERO;
        InfiniFloat remainder = this;

        for (int i = 0; i < arrayLength; i++) {
            if (infiniFloat.intBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                int offset = (arrayLength - i) * Long.SIZE;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    InfiniFloat resultMember = rShift(offset + setBit);
                    InfiniFloat flooredMember = resultMember.floor();

                    if (flooredMember.isZero()) {
                        return new ObjectObjectImmutablePair<>(flooredDiv, remainder);
                    }

                    flooredDiv = flooredDiv.add(flooredMember);
                    remainder = (InfiniFloat) remainder.substract(resultMember.mul(infiniFloat));
                    setBits &= ~(1L << setBit);
                }
            }
        }

        arrayLength = infiniFloat.floatBits.length;

        for (int i = 0; i < arrayLength && !remainder.isZero(); i++) {
            if (infiniFloat.floatBits[i] != 0) {
                long setBits = infiniFloat.intBits[i];
                long offset = (long) i * Long.SIZE + 1;
                int setBit;

                while ((setBit = Long.numberOfLeadingZeros(setBits)) != 64) {
                    InfiniFloat resultMember = lShift(offset + setBit);
                    InfiniFloat flooredMember = resultMember.floor();

                    if (flooredMember.isZero()) {
                        return new ObjectObjectImmutablePair<>(flooredDiv, remainder);
                    }

                    flooredDiv = flooredDiv.add(flooredMember);
                    remainder = (InfiniFloat) remainder.substract(resultMember.mul(infiniFloat));
                    setBits &= ~(0x8000000000000000L >> setBit);
                }
            }
        }

        return new ObjectObjectImmutablePair<>(flooredDiv, remainder);
    }

    @Override
    public @NotNull InfiniFloat lShift(long by) {
        if (by == 0 || isZero()) {
            return this;
        }

        if (ComplexMathUtil.isNegative(by)) {
            long newSize = (((long) floatBits.length + 1) * Long.SIZE
                    - Long.numberOfTrailingZeros(floatBits[floatBits.length - 1]) - by - 1) / Long.SIZE;

            if (newSize > it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
                throw new OutOfMemoryError("InfiniFloat float bits size out of range");
            }

            long bitROffset = by & (Long.SIZE - 1); // a programming expression for modulo by a power of 2
            long bitLOffset = Long.SIZE - bitROffset;
            long[] resultIntBits = ZERO_ARRAY;
            long[] resultFloatBits = LongArrays.EMPTY_ARRAY;
            int i = 0;
            int index = 0;
            int length = (int) (((long) intBits.length * Long.SIZE - Long.numberOfLeadingZeros(intBits[0]) + by
                    + Long.SIZE - 1) / Long.SIZE);

            if (bitROffset == 0) {
                if (newSize != 0) {
                    resultFloatBits = ArrayUtil.copyAtEnd(floatBits, (int) newSize);
                }

                if (length != 0) {
                    resultIntBits = Arrays.copyOf(intBits, length);

                    System.arraycopy(intBits, length, resultFloatBits, 0, intBits.length - length);
                }

                return new InfiniFloat(isNegative(), resultIntBits, resultFloatBits);
            }

            if (length != 0) {
                resultIntBits = new long[length--];

                if (intBits[0] >> bitROffset == 0) {
                    resultIntBits[i++] = intBits[0] << bitLOffset;
                }

                while (i < length) {
                    long var = intBits[index++];
                    resultIntBits[i++] |= var >> bitROffset;
                    resultIntBits[i] = var << bitLOffset;
                }

                if (index < intBits.length) {
                    resultIntBits[i] |= intBits[index] >> bitROffset;
                } else {
                    resultIntBits[i] |= floatBits[0] >> bitROffset;
                }
            }

            if (newSize != 0) {
                resultFloatBits = new long[(int) newSize];
                i = 0;
                length = intBits.length;

                while (index < length) {
                    long var = intBits[index++];
                    resultFloatBits[i++] |= var >> bitROffset;
                    resultFloatBits[i] = var << bitLOffset;
                }

                length = floatBits.length;
                index = 0;

                while (index < length) {
                    long var = floatBits[index++];
                    resultFloatBits[i++] |= var >> bitROffset;
                    resultFloatBits[i] = var << bitLOffset;
                }
            }

            return new InfiniFloat(isNegative(), resultIntBits, resultFloatBits);
        }

        int leadingZeroes = Long.numberOfLeadingZeros(intBits[0]);
        long newSize = (((long) intBits.length + 1) * Long.SIZE - leadingZeroes + by - 1)/ Long.SIZE;

        if (newSize > it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE) {
            throw new OutOfMemoryError("InfiniFloat int bits size out of range");
        }

        int length = intBits.length;
        long bitLOffset = by & (Long.SIZE - 1); // a programming expression for modulo by a power of 2
        long bitROffset = Long.SIZE - bitLOffset;
        long[] resultIntBits = ZERO_ARRAY;
        long[] resultFloatBits = LongArrays.EMPTY_ARRAY;
        int i = 0;

        if (bitLOffset == 0) {
            if (newSize != 0) {
                resultIntBits = Arrays.copyOf(intBits, (int) newSize);
            }

            resultFloatBits = ArrayUtil.copyAtEnd(floatBits, floatBits.length - ((int) newSize)
                    + intBits.length);

            System.arraycopy(floatBits, 0, resultIntBits, length, floatBits.length);

            return new InfiniFloat(isNegative(), resultIntBits, resultFloatBits);
        }

        if (newSize != 0) {
            resultIntBits = new long[(int) newSize];

            if (intBits[0] >> bitROffset != 0) {
                int maxIndex = length - 1;

                while (i < maxIndex) {
                    long var = intBits[i];
                    resultIntBits[i] |= var >> bitROffset;
                    resultIntBits[++i] = var << bitLOffset;
                }
            } else {
                resultIntBits[i++] = intBits[0] << bitLOffset;

                while (i < length) {
                    long var = intBits[i];
                    resultIntBits[i - 1] |= var >> bitROffset;
                    resultIntBits[i++] = var << bitLOffset;
                }
            }
        }

        length = floatBits.length;

        if (length != 0) {
            int maxIndex = resultIntBits.length - 1;
            int fi = 0;

            while (i < maxIndex && fi < length) {
                long var = floatBits[fi++];
                resultIntBits[i] |= var >> bitROffset;
                resultIntBits[++i] = var << bitLOffset;
            }

            if (fi < maxIndex) {
                resultIntBits[resultIntBits.length - 1] |= floatBits[fi] >> bitROffset;
                length = floatBits.length - fi;
                resultFloatBits = new long[length--];
                resultFloatBits[0] = floatBits[fi++] << bitLOffset;
                i = 0;

                while (i < length) {
                    long var = floatBits[fi++];
                    resultIntBits[i] |= var >> bitROffset;
                    resultIntBits[++i] = var << bitLOffset;
                }

                if (resultFloatBits[length] == 0) {
                    resultFloatBits = Arrays.copyOf(resultFloatBits, length);
                }
            }
        }

        return new InfiniFloat(isNegative(), resultIntBits, resultFloatBits);
    }

    @Override
    public @NotNull InfiniFloat rShift(long by) {
        return lShift(-by);
    }

    @Override
    public @NotNull InfiniNumber and(@NotNull Number other) {
        InfiniNumber infiniNumber = ComplexMathUtil.cast(other);

        if (!(infiniNumber instanceof InfiniFloat n)) {
            return infiniNumber.and(this);
        }

        long[] shorterIntBits = intBits;
        long[] longerIntBits = n.intBits;
        long[] shorterFloatBits = floatBits;
        long[] longerFloatBits = n.floatBits;

        if (shorterIntBits.length > longerIntBits.length) {
            shorterIntBits = longerIntBits;
            longerIntBits = intBits;
        }

        if (shorterFloatBits.length > longerFloatBits.length) {
            shorterFloatBits = longerFloatBits;
            longerFloatBits = floatBits;
        }

        long[] resultIntBits = new long[shorterIntBits.length];
        long[] resultFloatBits = shorterFloatBits.length != 0 ? new long[shorterFloatBits.length]
                : shorterFloatBits;
        int i = shorterIntBits.length;
        int j = longerIntBits.length;
        int length;

        while (i-- != 0) {
            j--;
            resultIntBits[i] = shorterIntBits[i] & longerIntBits[j];
        }

        for (length = resultFloatBits.length; i != length; i++) {
            resultFloatBits[i] = shorterFloatBits[i] & longerFloatBits[i];
        }

        return new InfiniFloat(isNegative() && n.isNegative(), trimIntbits(resultIntBits),
                trimFloatbits(resultFloatBits));
    }

    @Override
    public @NotNull InfiniNumber or(@NotNull Number other) {
        InfiniNumber infiniNumber = ComplexMathUtil.cast(other);

        if (!(infiniNumber instanceof InfiniFloat n)) {
            return infiniNumber.or(this);
        }

        long[] shorterIntBits = intBits;
        long[] longerIntBits = n.intBits;
        long[] shorterFloatBits = floatBits;
        long[] longerFloatBits = n.floatBits;

        if (shorterIntBits.length > longerIntBits.length) {
            shorterIntBits = longerIntBits;
            longerIntBits = intBits;
        }

        if (shorterFloatBits.length > longerFloatBits.length) {
            shorterFloatBits = longerFloatBits;
            longerFloatBits = floatBits;
        }

        long[] resultIntBits = new long[longerIntBits.length];
        long[] resultFloatBits = longerFloatBits.length != 0 ? new long[longerFloatBits.length]
                : longerFloatBits;
        int i = shorterIntBits.length;
        int j = longerIntBits.length;
        int length;

        while (i-- != 0) {
            j--;
            resultIntBits[i] = shorterIntBits[i] | longerIntBits[j];
        }

        for (length = shorterFloatBits.length; i != length; i++) {
            resultFloatBits[i] = shorterFloatBits[i] | longerFloatBits[i];
        }

        System.arraycopy(longerIntBits, 0, resultIntBits, 0, j);
        System.arraycopy(longerFloatBits, i, resultFloatBits, i, resultFloatBits.length - i);

        return new InfiniFloat(isNegative() || n.isNegative(), resultIntBits, resultFloatBits);
    }

    @Override
    public @NotNull InfiniNumber xor(@NotNull Number other) {
        InfiniNumber infiniNumber = ComplexMathUtil.cast(other);

        if (!(infiniNumber instanceof InfiniFloat n)) {
            return infiniNumber.or(this);
        }

        long[] shorterIntBits = intBits;
        long[] longerIntBits = n.intBits;
        long[] shorterFloatBits = floatBits;
        long[] longerFloatBits = n.floatBits;

        if (shorterIntBits.length > longerIntBits.length) {
            shorterIntBits = longerIntBits;
            longerIntBits = intBits;
        }

        if (shorterFloatBits.length > longerFloatBits.length) {
            shorterFloatBits = longerFloatBits;
            longerFloatBits = floatBits;
        }

        long[] resultIntBits = new long[longerIntBits.length];
        long[] resultFloatBits = longerFloatBits.length != 0 ? new long[longerFloatBits.length]
                : longerFloatBits;
        int i = shorterIntBits.length;
        int j = longerIntBits.length;
        int length;

        while (i-- != 0) {
            j--;
            resultIntBits[i] = shorterIntBits[i] ^ longerIntBits[j];
        }

        for (length = shorterFloatBits.length; i != length; i++) {
            resultFloatBits[i] = shorterFloatBits[i] ^ longerFloatBits[i];
        }

        System.arraycopy(longerIntBits, 0, resultIntBits, 0, j);
        System.arraycopy(longerFloatBits, i, resultFloatBits, i, resultFloatBits.length - i);

        return new InfiniFloat(isNegative() ^ n.isNegative(), resultIntBits, resultFloatBits);
    }

    @Override
    public @NotNull InfiniFloat invert() {
        long[] resultIntBits = new long[intBits.length];
        long[] resultFloatBits = new long[floatBits.length];
        int length = resultIntBits.length;

        for (int i = 0; i != length; i++) {
            resultIntBits[i] = ~intBits[i];
        }

        length = resultFloatBits.length;

        for (int i = 0; i != length; i++) {
            resultFloatBits[i] = ~floatBits[i];
        }

        return new InfiniFloat(!isNegative(), trimIntbits(resultIntBits), trimFloatbits(resultFloatBits));
    }

    @Override
    public @NotNull InfiniNumber sign() {
        if (isNegative()) {
            return NEGATIVE_ONE;
        }

        return isZero() ? ZERO : ONE;
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
                : this;
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
        InfiniNumber exponent = TEN.pow(valueOf(decimalDigits));
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
        return isInteger() && intBits[0] == 0;
    }

    public boolean isInteger() {
        return floatBits.length == 0;
    }

    private static long[] trimIntbits(long[] bits) {
        int i = 0;
        int length = bits.length;

        while (i != length && bits[i] == 0) {
            i++;
        }

        if (i == length) {
            return ZERO_ARRAY;
        } else if (i != 0) {
            return ArrayUtil.copyAtEnd(bits, length - i);
        } else {
            return bits;
        }
    }

    private static long[] trimFloatbits(long[] bits) {
        int length = bits.length;
        int i = length;

        while (i != 0 && bits[--i] == 0);

        if (i == 0) {
            return LongArrays.EMPTY_ARRAY;
        } else if (++i != length) {
            return Arrays.copyOf(bits, i);
        } else {
            return bits;
        }
    }

    public static InfiniFloat valueOf(long i) {
        return LONG_VALUE_CACHE.computeIfAbsent(i, (j) -> {
            if (ComplexMathUtil.isNegative(j)) {
                return new InfiniFloat(true, new long[]{-j});
            } else {
                return new InfiniFloat(false, new long[]{j});
            }
        });
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
    }

    /**
     * Creates a new instance of {@code InfiniFloat} array specifying the instance.
     * The result with have 0 float bits (will be an integer).
     * For access safety copies of the actual inputted array may be used in result's
     * fields.
     *
     * @param sign true if the result is supposed to be negative
     * @param intBits the array of longs signifying the integer part of the InfiniFloat
     * @return a new instance of {@code InfiniFloat} array specifying the instance
     */
    public static InfiniFloat ofBits(boolean sign, long @NotNull [] intBits) {
        return ofBits(sign, intBits, LongArrays.EMPTY_ARRAY);
    }

    /**
     * Creates a new instance of {@code InfiniFloat} arrays specifying the instance.
     * For access safety copies of the actual inputted arrays may be used in result's
     * fields.
     *
     * @param sign true if the result is supposed to be negative
     * @param intBits the array of longs signifying the integer part of the InfiniFloat
     * @param floatBits the array of longs signifying the decimal part of the InfiniFloat
     * @return a new instance of {@code InfiniFloat} arrays specifying the instance
     */
    public static InfiniFloat ofBits(boolean sign, long @NotNull [] intBits, long @NotNull [] floatBits) {
        return new InfiniFloat(sign, intBits.length != 0 ? trimIntbits(intBits.clone()) : ZERO_ARRAY,
                floatBits.length != 0 ? trimFloatbits(floatBits.clone()) : LongArrays.EMPTY_ARRAY);
    }

    static {
        DOUBLE_VALUE_CACHE.put(-0.0D, NEGATIVE_ZERO);
        DOUBLE_VALUE_CACHE.put(Double.POSITIVE_INFINITY, ComplexMathUtil.POSITIVE_INFINITY);
        DOUBLE_VALUE_CACHE.put(Double.NEGATIVE_INFINITY, ComplexMathUtil.NEGATIVE_INFINITY);
        DOUBLE_VALUE_CACHE.put(Double.NaN, ComplexMathUtil.NaN);
    }
}
