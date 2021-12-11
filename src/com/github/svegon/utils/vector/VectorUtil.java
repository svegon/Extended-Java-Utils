package com.github.svegon.utils.vector;

import com.github.svegon.utils.math.MathUtil;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public final class VectorUtil {
    private VectorUtil() {
        throw new AssertionError();
    }

    public static double squaredDistance(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Vectors don't match in dimensions.");
        }

        int length = first.length;
        double result = 0;

        for (int i = 0; i < length; i++) {
            result += MathUtil.squared(first[i] - second[i]);
        }

        return result;
    }


    public static double distance(double[] first, double[] second) {
        return Math.sqrt(squaredDistance(first, second));
    }

    /**
     * @param first first vector
     * @param second second vector
     * @return a new array with each element result[i] == first[i] + second[i] for any int i; i >= 0 && i < result.length
     */
    public static float[] vectorSum(float[] first, float[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Vectors have different dimensions.");
        }

        float[] result = first.clone();
        int length = result.length;

        for (int i = 0; i < length; i++) {
            result[i] += second[i];
        }

        return result;
    }

    /**
     * @param first first vector
     * @param second second vector
     * @return a new array with each element result[i] == first[i] - second[i] for any int i; i >= 0 && i < result.length
     */
    public static float[] vectorSubstract(float[] first, float[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Vectors have different dimensions.");
        }

        float[] result = first.clone();
        int length = result.length;

        for (int i = 0; i < length; i++) {
            result[i] -= second[i];
        }

        return result;
    }

    /**
     * @param first first vector
     * @param second second vector
     * @return a new array with each element result[i] == first[i] + second[i] for any int i; i >= 0 && i < result.length
     */
    public static double[] vectorSum(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Vectors have different dimensions.");
        }

        double[] result = first.clone();
        int length = result.length;

        for (int i = 0; i < length; i++) {
            result[i] += second[i];
        }

        return result;
    }

    /**
     * @param first first vector
     * @param second second vector
     * @return a new array with each element result[i] == first[i] - second[i] for any int i; i >= 0 && i < result.length
     */
    public static double[] vectorSubstract(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Vectors have different dimensions.");
        }

        double[] result = first.clone();
        int length = result.length;

        for (int i = 0; i < length; i++) {
            result[i] -= second[i];
        }

        return result;
    }
}
