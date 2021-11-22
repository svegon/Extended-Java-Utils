package svegon.utils;

import svegon.utils.collections.StringList;
import svegon.utils.math.really_big_math.InfiniNumber;
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

    public static String toString(double d, List<String> charSet, String pointer, Pair<String, String> signs) {
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

        int base = charSet.size();
        int intPart = (int) d;
        double floatPart = d - intPart;

        if (intPart == 0) {
            builder.append(charSet.get(0));
        } else {
            while (intPart != 0) {
                builder.insert(0, charSet.get(Math.floorMod(intPart, base)));
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
            builder.append(charSet.get(intPart));
            floatPart = floatPart - intPart;
        }

        return builder.toString();
    }

    public static String toString(InfiniNumber n, List<String> charSet, String pointer, Pair<String, String> signs) {
        StringBuilder builder = new StringBuilder();

        if (n.compareTo(0) < 0) {
            n = n.neg();
            builder.append(signs.right());
        } else {
            builder.append(signs.left());
        }

        int base = charSet.size();
        Pair<InfiniNumber, InfiniNumber> floatingPart = n.divMod(1);
        InfiniNumber intPart = floatingPart.left();
        InfiniNumber floatPart = floatingPart.right();
        Pair<InfiniNumber, InfiniNumber> charAndRemainder;

        if (intPart.compareTo(0) == 0) {
            builder.append(charSet.get(0));
        } else {
            while (intPart.compareTo(0) != 0) {
                charAndRemainder = intPart.divMod(base);
                builder.insert(0, charSet.get(charAndRemainder.right().intValue()));
                intPart = charAndRemainder.left();
            }
        }

        if (floatPart.compareTo(0) == 0) {
            return builder.toString();
        }

        builder.append(pointer);

        while (floatPart.compareTo(0) != 0) {
            charAndRemainder = floatPart.mul(base).divMod(1);
            builder.append(charSet.get(charAndRemainder.left().intValue()));
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

    public static CharList asList(final String string) {
        return StringList.of(string);
    }
}
