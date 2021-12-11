package com.github.svegon.utils;

import com.github.svegon.utils.collections.StringAsList;
import com.github.svegon.utils.collections.collecting.CollectingUtil;
import com.github.svegon.utils.collections.stream.StreamUtil;
import com.github.svegon.utils.math.really_big_math.ComplexMathUtil;
import com.github.svegon.utils.math.really_big_math.InfiniFloat;
import com.github.svegon.utils.math.really_big_math.InfiniNumber;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import java.util.List;

public final class StringUtil {
    private StringUtil() {
        throw new AssertionError();
    }

    public static final String NaN = "NaN";
    public static final String POSITIVE_INFINITY = "\u221e";
    public static final String NEGATIVE_INFINITY = "-" + POSITIVE_INFINITY;

    public static final List<String> DECIMAL_DIGITS = ImmutableList.of("0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9");
    public static final String DEFAULT_POINTER = ".";
    public static final Pair<String, String> STANDARD_SIGNS = new ObjectObjectImmutablePair<>("", "-");
    public static final CharList DECIMAL_DIGIT_CHARS = StreamUtil.mapToChar(DECIMAL_DIGITS.stream(),
            (s) -> s.charAt(0)).collect(CollectingUtil.toImmutableCharList());

    public static String toString(double d, List<String> digitSet, String pointer, Pair<String, String> signs) {
        if (Double.isNaN(d)) {
            return NaN;
        }

        if (d == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        }

        if (d == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        }

        StringBuilder builder = new StringBuilder();

        if (d < 0) {
            d = -d;
            builder.append(signs.right());
        } else {
            builder.append(signs.left());
        }

        int base = digitSet.size();
        int intPart = (int) d;
        double floatPart = d - intPart;

        if (intPart == 0) {
            builder.append(digitSet.get(0));
        } else {
            while (intPart != 0) {
                builder.insert(0, digitSet.get(Math.floorMod(intPart, base)));
                intPart = Math.floorDiv(intPart, base);
            }
        }

        if (floatPart == 0) {
            return builder.toString();
        }

        builder.append(pointer);

        while (floatPart != 0) {
            floatPart *= base;
            intPart = (int) floatPart;
            builder.append(digitSet.get(intPart));
            floatPart = floatPart - intPart;
        }

        return builder.toString();
    }

    public static String toString(@NotNull InfiniNumber n, List<String> digitSet, String pointer, Pair<String,
            String> signs) {
        StringBuilder builder = new StringBuilder();

        if (n.compareTo(0) < 0) {
            n = n.neg();
            builder.append(signs.right());
        } else {
            builder.append(signs.left());
        }

        int base = digitSet.size();
        Pair<InfiniNumber, InfiniNumber> floatingPart = n.divMod(1);
        InfiniNumber intPart = floatingPart.left();
        InfiniNumber floatPart = floatingPart.right();
        Pair<InfiniNumber, InfiniNumber> charAndRemainder;

        if (intPart.compareTo(0) == 0) {
            builder.append(digitSet.get(0));
        } else {
            while (intPart.compareTo(0) != 0) {
                charAndRemainder = intPart.divMod(base);
                builder.insert(0, digitSet.get(charAndRemainder.right().intValue()));
                intPart = charAndRemainder.left();
            }
        }

        if (floatPart.compareTo(0) == 0) {
            return builder.toString();
        }

        builder.append(pointer);

        while (floatPart.compareTo(0) != 0) {
            charAndRemainder = floatPart.mul(base).divMod(1);
            builder.append(digitSet.get(charAndRemainder.left().intValue()));
            floatPart = charAndRemainder.right();
        }

        return builder.toString();
    }

    public static String toString(InfiniNumber n, List<String> charSet, String pointer) {
        return toString(n, charSet, pointer, STANDARD_SIGNS);
    }

    public static String toString(InfiniNumber n, List<String> charSet) {
        return toString(n, charSet, DEFAULT_POINTER);
    }

    public static String toString(InfiniNumber n) {
        return toString(n, DECIMAL_DIGITS);
    }

    public static CharList asList(String string) {
        return StringAsList.of(string);
    }

    public static InfiniNumber parseInfiniNumber(String s, CharList charSet, String pointer, Pair<String,
            String> signs) {
        if (s.equals(NaN)) {
            return ComplexMathUtil.NaN;
        }

        if (s.equals(POSITIVE_INFINITY) || s.equals(String.valueOf(Double.POSITIVE_INFINITY))) {
            return ComplexMathUtil.POSITIVE_INFINITY;
        }

        if (s.equals(NEGATIVE_INFINITY) || s.equals(String.valueOf(Double.NEGATIVE_INFINITY))) {
            return ComplexMathUtil.NEGATIVE_INFINITY;
        }

        boolean sign;

        if (s.startsWith(signs.first())) {
            sign = false;
            s = s.substring(signs.first().length());
        } else if (s.startsWith(signs.right())) {
            sign = true;
            s = s.substring(signs.right().length());
        } else {
            throw new NumberFormatException("invalid signum");
        }

        String[] var = s.split(pointer);

        switch (var.length) {
            case 0 -> {
                return InfiniFloat.ZERO;
            }

            case 1 -> {
                InfiniFloat base = InfiniFloat.valueOf(charSet.size());
                int length = s.length();
                InfiniNumber result = InfiniFloat.ZERO;

                for (int i = 0; i < length; i++) {
                    int digit = charSet.indexOf(s.charAt(i));

                    if (digit < 0) {
                        throw new NumberFormatException();
                    }

                    result = result.add(InfiniFloat.valueOf(digit)
                            .mul(base.pow(InfiniFloat.valueOf(length - i - 1))));
                    // invert is a programming shortcut for -1-i
                }

                return sign ? result.neg() : result;
            }

            case 2 -> {
                InfiniNumber result = InfiniFloat.ZERO;
                int length = s.length();
                InfiniFloat base = InfiniFloat.valueOf(charSet.size());
                s = var[1];

                for (int i = 0; i < length; i++) {
                    int digit = charSet.indexOf(s.charAt(i));

                    if (digit < 0) {
                        throw new NumberFormatException();
                    }

                    result = result.add(InfiniFloat.valueOf(digit)
                            .mul(base.pow(InfiniFloat.valueOf(~i)))); // invert is a programming shortcut for -1-i
                }

                result = result.add(parseInfiniNumber(var[0], charSet, pointer, signs));

                if (sign) {
                    result = result.neg();
                }

                return result;
            }

            default -> throw new NumberFormatException();
        }
    }

    public static InfiniNumber parseInfiniNumber(String s, CharList charSet, String pointer) {
        return parseInfiniNumber(s, charSet, pointer, STANDARD_SIGNS);
    }

    public static InfiniNumber parseInfiniNumber(String s, CharList charSet) {
        return parseInfiniNumber(s, charSet, DEFAULT_POINTER);
    }

    public static InfiniNumber parseInfiniNumber(String s) {
        return parseInfiniNumber(s, DECIMAL_DIGIT_CHARS);
    }
}
