package svegon.utils.math;

import org.jetbrains.annotations.Range;

public final class MathUtil {
    private MathUtil() {
        throw new AssertionError();
    }

    public static final float PI = (float) Math.PI;
    public static final float DEGREE_TO_RAD_RATIO = PI / 180F;

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static int squared(int d) {
        return d * d;
    }

    public static double squared(double d) {
        return d * d;
    }

    public static int cubed(int d) {
        return d * d * d;
    }

    public static double cubed(double d) {
        return d * d * d;
    }

    public static int positivePow(int base, @Range(from = 1, to = Integer.MAX_VALUE) final int power) {
        for (int i = 1; i < power; ++i) {
            base *= base;
        }

        return base;
    }

    public static double positivePow(double base, @Range(from = 1, to = Integer.MAX_VALUE) final int power) {
        for (int i = 1; i < power; ++i) {
            base *= base;
        }

        return base;
    }

    public static double pow(double base, final int power) {
        if (power == 0) {
            return base;
        } else if (power < 0) {
            return 1 / positivePow(base, -power);
        } else {
            return positivePow(base, power);
        }
    }

    public static long product(int... numbers) {
        long result = 1;

        for (int i : numbers) {
            result *= i;
        }

        return result;
    }

    public static int[] arrayIndexToMatrixIndices(@Range(from = 0, to = Integer.MAX_VALUE) int index,
                                                  @Range(from = 1, to = Integer.MAX_VALUE) int... dimensions) {
        int[] result = new int[dimensions.length];

        for (int i = 0; i < dimensions.length; ++i) {
            result[i] = Math.floorDiv(index, dimensions[i]);
            index = Math.floorMod(index, dimensions[i]);
        }

        return result;
    }

    /**
     * Incorporate a new double value using Kahan summation /
     * compensation summation.
     *
     * High-order bits of the sum are in intermediateSum[0], low-order
     * bits of the sum are in intermediateSum[1], any additional
     * elements are application-specific.
     *
     * @param intermediateSum the high-order and low-order words of the intermediate sum
     * @param value the name value to be included in the running sum
     */
    public static double[] sumWithCompensation(double[] intermediateSum, double value) {
        double tmp = value - intermediateSum[1];
        double sum = intermediateSum[0];
        double velvel = sum + tmp; // Little wolf of rounding error
        intermediateSum[1] = (velvel - sum) - tmp;
        intermediateSum[0] = velvel;
        return intermediateSum;
    }

    /**
     * If the compensated sum is spuriously NaN from accumulating one
     * or more same-signed infinite values, return the
     * correctly-signed infinity stored in the simple sum.
     */
    public static double computeFinalSum(double[] summands) {
        // Better error bounds to add both terms as the final sum
        double tmp = summands[0] + summands[1];
        double simpleSum = summands[summands.length - 1];
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
            return simpleSum;
        else
            return tmp;
    }
}
