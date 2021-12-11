package com.github.svegon.utils.collections;

import com.github.svegon.utils.collections.stream.*;
import com.github.svegon.utils.fast.util.bytes.ImprovedByteCollection;
import com.github.svegon.utils.fast.util.chars.ImprovedCharCollection;
import com.github.svegon.utils.fast.util.shorts.ImprovedShortCollection;
import org.jetbrains.annotations.Nullable;
import com.github.svegon.utils.fast.util.booleans.ImprovedBooleanCollection;
import com.github.svegon.utils.fast.util.floats.ImprovedFloatCollection;
import svegon.utils.collections.stream.*;
import com.github.svegon.utils.multithreading.ThreadUtil;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.booleans.*;
import it.unimi.dsi.fastutil.bytes.*;
import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.fastutil.floats.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.shorts.*;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.RandomAccess;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;

@NotThreadSafe
public final class ArrayUtil {
    private ArrayUtil() {
        throw new AssertionError();
    }

    private static final int PARALLEL_REVERSE_NO_FORK = 8192;

    /**
     * Creates a new array with the same component type and length as {@param array}.
     * @param componentType the component type as class of the resulting array
     * @param length length of the returning array
     * @param <T> component of {@param array}
     * @return a new array instance such that
     * {@return}.getClass().getComponentType() == {@param componentType}
     * && {@return}.length == {@param length}
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final @NotNull Class<T> componentType, int length) {
        return (T[]) Array.newInstance(componentType, length);
    }

    /**
     * Creates a new array with the same component type and length as {@param array}.
     * @param array the original array
     * @param length length of the returning array
     * @param <T> component of {@param array}
     * @return a new array instance such that
     * {@return}.getClass().getComponentType() == {@param array}.getClass().getComponentType()
     * && {@return}.length == {@param length}
     */
    public static <T> T[] newArray(final T @NotNull [] array, int length) {
        return com.google.common.collect.ObjectArrays.newArray(array, length);
    }

    /**
     * Creates a new array with the same component type and length as {@param array}.
     * @param array the original array
     * @param <T> component of {@param array}
     * @return a new array instance such that
     * {@return}.getClass().getComponentType() == {@param array}.getClass().getComponentType()
     * && {@return}.length == {@param array}.length
     */
    public static <T> T[] newArray(final T @NotNull [] array) {
        return com.google.common.collect.ObjectArrays.newArray(array, array.length);
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @param <T> component type of the given array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static <T> T[] copyAtEnd(T @NotNull [] array, int newLength) {
        T[] newArray = newArray(array, newLength);

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static boolean[] copyAtEnd(boolean @NotNull [] array, int newLength) {
        boolean[] newArray = new boolean[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static byte[] copyAtEnd(byte @NotNull [] array, int newLength) {
        byte[] newArray = new byte[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static short[] copyAtEnd(short @NotNull [] array, int newLength) {
        short[] newArray = new short[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static int[] copyAtEnd(int @NotNull [] array, int newLength) {
        int[] newArray = new int[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static long[] copyAtEnd(long @NotNull [] array, int newLength) {
        long[] newArray = new long[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static char[] copyAtEnd(char @NotNull [] array, int newLength) {
        char[] newArray = new char[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static float[] copyAtEnd(float @NotNull [] array, int newLength) {
        float[] newArray = new float[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param array array of elements
     * @param newLength length of the return array
     * @return if newLength > array.length returns a new array
     *          with newLength - array.length elements appended to the beggining of the array;
     *          otherwise same return a new array with array.length - newLength elements
     *          removed from the beggining of the array
     * @throws NegativeArraySizeException if newLength < 0
     * @throws NullPointerException if array == null
     */
    public static double[] copyAtEnd(double @NotNull [] array, int newLength) {
        double[] newArray = new double[newLength];

        if (newLength < array.length) {
            System.arraycopy(array, array.length - newLength, newArray, 0, newLength);
        } else  {
            System.arraycopy(array, 0, newArray, newLength - array.length, array.length);
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @param <E> component type
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    @SafeVarargs
    public static <E> E[] merge(E @NotNull [] first, E @NotNull ... second) {
        E[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @param <E> component type of each array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new String[]{"a", "b", c"}, new String[]{"d", e", f"}) ->
     *          String[]{"a", "b", c", "d", e", f"}
     * @throws NullPointerException if arrays is null or if any of its elements is null
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] merge(E @NotNull [] @NotNull ... arrays) {
        E[] newArray = newArray((Class<E>) arrays.getClass().getComponentType().getComponentType(),
                Arrays.stream(arrays).mapToInt((a) -> a.length).sum());
        int offset = 0;

        for (E[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static boolean[] merge(boolean @NotNull [] first, boolean @NotNull ... second) {
        boolean[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new boolean[]{true, false, true}, new boolean[]{false, false}) ->
     *          boolean[]{true, false, true, false, false}
     * @throws NullPointerException if arrays is null or if any of its elements is null
     */
    public static boolean[] merge(boolean @NotNull [] @NotNull ... arrays) {
        boolean[] newArray = new boolean[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (boolean[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static byte[] merge(byte @NotNull [] first, byte @NotNull ... second) {
        byte[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new bytes[]{1, 2, 3}, new bytes[]{0, 0}) ->
     *          bytes[]{1, 2, 3, 0, 0}
     * @throws NullPointerException if arrays is null or if any of ots elements is null
     */
    public static byte[] merge(byte @NotNull [] @NotNull ... arrays) {
        byte[] newArray = new byte[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static short[] merge(short @NotNull [] first, short @NotNull ... second) {
        short[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new short[]{1, 2, 3}, new short[]{0, 0}) ->
     *          short[]{1, 2, 3, 0, 0}
     * @throws NullPointerException if arrays is null or if any of ots elements is null
     */
    public static short[] merge(short @NotNull [] @NotNull ... arrays) {
        short[] newArray = new short[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (short[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static int[] merge(int @NotNull [] first, int @NotNull ... second) {
        int[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new int[]{1, 2, 3}, new int[]{0, 0}) ->
     *          int[]{1, 2, 3, 0, 0}
     * @throws NullPointerException if arrays is null or if any of ots elements is null
     */
    public static int[] merge(int @NotNull [] @NotNull ... arrays) {
        int[] newArray = new int[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (int[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static long[] merge(long[] first, long... second) {
        long[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new long[]{1, 2, 3}, new long[]{0, 0}) ->
     *          long[]{1, 2, 3, 0, 0}
     * @throws NullPointerException if arrays is null or if any of ots elements is null
     */
    public static long[] merge(long[]... arrays) {
        long[] newArray = new long[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (long[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static float[] merge(float[] first, float... second) {
        float[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new float[]{0.4F, 0.5F, -3F}, new float[]{0F, Float.NaN}) ->
     *          float[]{0.4F, 0.5F, -3.0F, 0.0F, NaN}
     * @throws NullPointerException if arrays is null or if any of ots elements is null
     */
    public static float[] merge(float[]... arrays) {
        float[] newArray = new float[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (float[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * @param first first array to be merged
     * @param second second part of the merged array
     * @return new array including elements of both given array
     * @throws NullPointerException if first == null; if second == null
     */
    public static double[] merge(double[] first, double... second) {
        double[] newArray = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, newArray, first.length, second.length);

        return newArray;
    }

    /**
     * @param arrays array of arrays to be all merged into 1 array
     * @return a new array containing all elements of each array in array in their order in arrays;
     *          e.g. merge(new double[]{0.4F, 0.5F, -3F}, new double[]{0F, Double.NaN}) ->
     *          double[]{0.4F, 0.5F, -3.0F, 0.0F, NaN}
     * @throws NullPointerException if arrays is null or if any of ots elements is null
     */
    public static double[] merge(double[]... arrays) {
        double[] newArray = new double[Arrays.stream(arrays).mapToInt((a) -> a.length).sum()];
        int offset = 0;

        for (double[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }

    /**
     * Creates a new array with all elements at indexes marked with {@param indexesToRemove} as true
     * @param array the base array
     * @param indexesToRemove a BitSet determining which elements to remove
     * @param <E> component type of the arrays
     * @return a new array with length {@param array}.length - {@param indexesToRemove}.cardinality()
     * and containing all elements of {@param array} except those at indexes such that an index i
     * {@param indexesToRemove}.get(i) in the same order
     */
    public static <E> E[] filter(E @NotNull [] array, @NotNull BitSet indexesToRemove) {
        E[] newArray = newArray(array, array.length - indexesToRemove.cardinality());
        int filledSlots = 0;
        int startingIndexOfRange = indexesToRemove.get(0) ? 1 : 0;
        int endingIndexOfRange = 0;

        while ((endingIndexOfRange = indexesToRemove.nextSetBit(endingIndexOfRange)) >= 0
                && endingIndexOfRange <= array.length) {
            int length = endingIndexOfRange - startingIndexOfRange;

            System.arraycopy(array, startingIndexOfRange, newArray, filledSlots, length);

            filledSlots += length;
            startingIndexOfRange = endingIndexOfRange + 1;
        }

        System.arraycopy(array, startingIndexOfRange, newArray, filledSlots, newArray.length - filledSlots);

        return newArray;
    }

    /**
     * Creates a new array with elements not matching the {@param filter} removed.
     * @param array the base array
     * @param filter the predicate determining which elements to remove
     * @param <E> component type of the base and return array
     * @return an array which contains all elements such that
     * each element e returns {@code true} when calling {@param filter}.test(e)
     */
    public static <E> E[] filter(E @NotNull [] array, @NotNull Predicate<? super E> filter) {
        BitSet indexesToRemove = new BitSet(array.length);

        for (int i = 0; i < array.length; ++i) {
            if (!filter.test(array[i])) {
                indexesToRemove.set(i);
            }
        }

        return filter(array, indexesToRemove);
    }

    /**
     * Creates a new array with the elements at {@param index remove}
     * @param array the original array
     * @param index the index of the element to be removed
     * @param <E> component type of {@param array}
     * @return a new array with length {@param array}.length - 1
     * and all elements from {@param index} shifted 1 index to the left
     */
    public static <E> E[] pop(E @NotNull [] array, int index) {
        E[] newArray = newArray(array, array.length - 1);

        System.arraycopy(array, 0, newArray, 0, index);
        System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);

        return newArray;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @param <E> component type of the given array
     * @return the array after initializing
     * @since 1.0.0
     */
    public static <E> E[] fill(E @NotNull [] a, @Nullable E value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @param <E> component type of the given array
     * @return the array after initializing
     * @since 1.0.0
     */
    public static <E> E[] fill(E @NotNull [] a, int from, int to, @Nullable E value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static boolean[] fill(boolean @NotNull [] a, boolean value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static boolean[] fill(boolean @NotNull [] a, int from, int to, boolean value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static byte[] fill(byte @NotNull [] a, byte value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static byte[] fill(byte @NotNull [] a, int from, int to, byte value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static short[] fill(short @NotNull [] a, short value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static short[] fill(short @NotNull [] a, int from, int to, short value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static int[] fill(int @NotNull [] a, int value) {

        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static int[] fill(int @NotNull [] a, int from, int to, int value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static long[] fill(long @NotNull [] a, long value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static long[] fill(long @NotNull [] a, int from, int to, long value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static char[] fill(char @NotNull [] a, char value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static char[] fill(char @NotNull [] a, int from, int to, char value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static float[] fill(float @NotNull [] a, float value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static float[] fill(float @NotNull [] a, int from, int to, float value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing otherwise the results won't be reliable.
     *
     * @param a array to be initialized
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static double[] fill(double @NotNull [] a, double value) {
        a[0] = value;
        int i = 1;
        int half = a.length >>> 1;

        while (i < half) {
            System.arraycopy(a, 0, a, i, i);
            i <<= 1;
        }

        System.arraycopy(a, 0, a, i, a.length - i);
        return a;
    }

    /**
     * Set all elements of the specified array to the specified element.
     *
     * The effect is equal to using {@code Arrays.fill}
     * but this implementation uses {@code System.arraycopy} to perform faster.
     * This also means the array can't be accessed by external threads
     * while initializing to ensure correct results.
     *
     * @param a array to be initialized
     * @param from the first index to be initialized (inclusive)
     * @param to the last index to be initialized (exclusive)
     * @param value value of every element after initializing
     * @return the array after initializing
     * @since 1.0.0
     */
    public static double[] fill(double @NotNull [] a, int from, int to, double value) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        a[from] = value;
        int i = 1;
        int half = (to - from) >>> 1;

        while (i < half) {
            System.arraycopy(a, from, a, from + i, i);
            i <<= 1;
        }

        System.arraycopy(a, from, a,from + i, to - i - from);
        return a;
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @return the array after initializing
     * @since 1.0.0
     */
    public static boolean[] setAll(boolean @NotNull [] a, Int2BooleanFunction mapper) {
        return setRange(a, mapper, 0, a.length);
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @param from starting index of the range to be modified (inclusive)
     * @param to ending index of the range to be modified (exclusive)
     * @return the array after initializing
     * @since 1.0.0
     */
    public static boolean[] setRange(boolean @NotNull [] a, Int2BooleanFunction mapper, int from, int to) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        while (to-- != from) {
            a[to] = mapper.apply(to);
        }

        return a;
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @return the array after initializing
     * @since 1.0.0
     */
    public static byte[] setAll(byte @NotNull [] a, Int2ByteFunction mapper) {
        return setRange(a, mapper, 0, a.length);
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @param from starting index of the range to be modified (inclusive)
     * @param to ending index of the range to be modified (exclusive)
     * @return the array after initializing
     * @since 1.0.0
     */
    public static byte[] setRange(byte @NotNull [] a, Int2ByteFunction mapper, int from, int to) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        while (to-- > from) {
            a[to] = mapper.apply(to);
        }

        return a;
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @return the array after initializing
     * @since 1.0.0
     */
    public static short[] setAll(short @NotNull [] a, Int2ShortFunction mapper) {
        return setRange(a, mapper, 0, a.length);
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @param from starting index of the range to be modified (inclusive)
     * @param to ending index of the range to be modified (exclusive)
     * @return the array after initializing
     * @since 1.0.0
     */
    public static short[] setRange(short @NotNull [] a, Int2ShortFunction mapper, int from, int to) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        while (to-- > from) {
            a[to] = mapper.apply(to);
        }

        return a;
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @return the array after initializing
     * @since 1.0.0
     */
    public static char[] setAll(char @NotNull [] a, Int2CharFunction mapper) {
        return setRange(a, mapper, 0, a.length);
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @param from starting index of the range to be modified (inclusive)
     * @param to ending index of the range to be modified (exclusive)
     * @return the array after initializing
     * @since 1.0.0
     */
    public static char[] setRange(char @NotNull [] a, Int2CharFunction mapper, int from, int to) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        while (to-- > from) {
            a[to] = mapper.apply(to);
        }

        return a;
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @return the array after initializing
     * @since 1.0.0
     */
    public static float[] setAll(float @NotNull [] a, Int2FloatFunction mapper) {
        return setRange(a, mapper, 0, a.length);
    }

    /**
     * Set all elements of the specified array, using the provided generator function to compute each element.
     * If the generator function throws an exception, it is relayed to the caller
     * and the array is left in an indeterminate state.
     * @param a array to be initialized
     * @param mapper a function accepting an index and producing the desired value for that position
     * @param from starting index of the range to be modified (inclusive)
     * @param to ending index of the range to be modified (exclusive)
     * @return the array after initializing
     * @since 1.0.0
     */
    public static float[] setRange(float @NotNull [] a, Int2FloatFunction mapper, int from, int to) {
        Preconditions.checkPositionIndexes(from, to, a.length);

        while (to-- > from) {
            a[to] = mapper.apply(to);
        }

        return a;
    }

    public static void reverse(Swapper swapper, int start, int end, int fence) {
        Preconditions.checkPositionIndexes(start, end, fence);

        int length = end - start;
        int center = length >>> 1 - start;

        if ((length & 1) == 0) {
            center++;
        }

        for (int i = 0; i < center; i++) {
            swapper.swap(start + i, fence - i);
        }
    }

    /**
     * swaps each element on index i with an element end - i for every integer start <= i < end
     * @param swapper the swapper used to swap the elements
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     */
    public static void reverse(Swapper swapper, int start, int end) {
        reverse(swapper, start, end, end);
    }

    /**
     * performs the same as {@link #reverse(Swapper, int, int)} but in parallel if time effective
     * @param swapper the swapper used to swap the elements
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #reverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(Swapper swapper, int start, int end) {
        ForkJoinPool pool = ThreadUtil.getPool();

        if (pool.getParallelism() == 1 || end - start < PARALLEL_REVERSE_NO_FORK) {
            return pool.submit(new ParallelArrayReverseTask(start, end, Preconditions.checkNotNull(swapper)));
        } else {
            return pool.submit(new ForkJoinArrayReverseTask(start, end, end, Preconditions.checkNotNull(swapper)));
        }
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @param <E> component type of {@param array}
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static <E> ForkJoinTask<Void> parallelReverse(final E @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> ObjectArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final boolean @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> BooleanArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final byte @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> ByteArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final short @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> ShortArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final int @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> IntArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final long @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> LongArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final char @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> CharArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final float @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> FloatArrays.swap(array, i, j), start, end);
    }

    /**
     * Performs reversion in parallel in the same way as {@link #reverse(Swapper, int, int)}
     * @param array the array whose elements will be reversed
     * @param start the first index to be swapped
     * @param end the last index + 1 to be swapped
     * @return the {@link ForkJoinTask} performing the reversion
     * @see #parallelReverse(Swapper, int, int)
     */
    public static ForkJoinTask<Void> parallelReverse(final double @NotNull [] array, int start, int end) {
        Preconditions.checkPositionIndexes(start, end, array.length);

        return parallelReverse((i, j) -> DoubleArrays.swap(array, i, j), start, end);
    }

    public static int frequency(final boolean @NotNull [] a) {
        int frequency = 0;

        for (boolean b : a) {
            if (b) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final byte @NotNull [] a, final byte value) {
        int frequency = 0;

        for (byte b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final short @NotNull [] a, final short value) {
        int frequency = 0;

        for (short b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final int @NotNull [] a, final int value) {
        int frequency = 0;

        for (int b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final long @NotNull [] a, final long value) {
        int frequency = 0;

        for (long b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final char @NotNull [] a, final char value) {
        int frequency = 0;

        for (char b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final float @NotNull [] a, final float value) {
        int frequency = 0;

        for (float b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static int frequency(final double @NotNull [] a, final double value) {
        int frequency = 0;

        for (double b : a) {
            if (b == value) {
                frequency++;
            }
        }

        return frequency;
    }

    public static BooleanArrayAsList asList(boolean[] array) {
        return new BooleanArrayAsList(array);
    }

    public static ByteArrayAsList asList(byte[] array) {
        return new ByteArrayAsList(array);
    }

    public static ShortArrayAsList asList(short[] array) {
        return new ShortArrayAsList(array);
    }

    public static IntArrayAsList asList(int[] array) {
        return new IntArrayAsList(array);
    }

    public static LongArrayAsList asList(long[] array) {
        return new LongArrayAsList(array);
    }

    public static CharArrayAsList asList(char[] array) {
        return new CharArrayAsList(array);
    }

    public static FloatArrayAsList asList(float[] array) {
        return new FloatArrayAsList(array);
    }

    public static DoubleArrayAsList asList(double[] array) {
        return new DoubleArrayAsList(array);
    }

    public static BooleanStream stream(final boolean[] array) {
        return stream(array, 0, array.length);
    }

    public static BooleanStream stream(final boolean[] array, final int startingIndex, final int endingIndex) {
        return StreamUtil.booleanStream(spliterator(array, startingIndex, endingIndex), false);
    }

    public static ByteStream stream(final byte[] array) {
        return stream(array, 0, array.length);
    }

    public static ByteStream stream(final byte[] array, final int startingIndex, final int endingIndex) {
        return StreamUtil.byteStream(spliterator(array, startingIndex, endingIndex), false);
    }

    public static ShortStream stream(final short[] array) {
        return stream(array, 0, array.length);
    }

    public static ShortStream stream(final short[] array, final int startingIndex, final int endingIndex) {
        return StreamUtil.shortStream(spliterator(array, startingIndex, endingIndex), false);
    }

    public static CharStream stream(final char[] array) {
        return stream(array, 0, array.length);
    }

    public static CharStream stream(final char[] array, final int startingIndex, final int endingIndex) {
        return StreamUtil.charStream(spliterator(array, startingIndex, endingIndex), false);
    }

    public static FloatStream stream(final float[] array) {
        return stream(array, 0, array.length);
    }

    public static FloatStream stream(final float[] array, final int startingIndex, final int endingIndex) {
        return StreamUtil.floatStream(spliterator(array, startingIndex, endingIndex), false);
    }

    public static BooleanSpliterator spliterator(final boolean[] array, final int startInclusive,
                                                 final int endExclusive) {
        return BooleanSpliterators.wrap(array, startInclusive, endExclusive);
    }

    public static ByteSpliterator spliterator(final byte[] array, final int startInclusive, final int endExclusive) {
        return ByteSpliterators.wrap(array, startInclusive, endExclusive);
    }

    public static ShortSpliterator spliterator(final short[] array, final int startInclusive, final int endExclusive) {
        return ShortSpliterators.wrap(array, startInclusive, endExclusive);
    }

    public static CharSpliterator spliterator(final char[] array, final int startInclusive, final int endExclusive) {
        return CharSpliterators.wrap(array, startInclusive, endExclusive);
    }

    public static FloatSpliterator spliterator(final float[] array, final int startInclusive, final int endExclusive) {
        return FloatSpliterators.wrap(array, startInclusive, endExclusive);
    }

    public static final class BooleanArrayAsList extends AbstractBooleanList
            implements ImprovedBooleanCollection, RandomAccess {
        private final boolean[] array;

        public BooleanArrayAsList(boolean[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public BooleanSpliterator spliterator() {
            return ArrayUtil.spliterator(array, 0, array.length);
        }

        @Override
        public void getElements(int from, boolean @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, boolean @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull BooleanUnaryOperator operator) {
            setAll(array, (i) -> operator.apply(array[i]));
        }

        @Override
        public boolean getBoolean(int index) {
            return array[index];
        }

        @Override
        public void sort(BooleanComparator comparator) {
            BooleanArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(BooleanComparator comparator) {
            BooleanArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public boolean set(int index, boolean k) {
            boolean result = getBoolean(index);
            array[index] = k;
            return result;
        }

        public boolean[] getArray() {
            return array;
        }
    }

    public static final class ByteArrayAsList extends AbstractByteList
            implements ImprovedByteCollection, RandomAccess {
        private final byte[] array;

        public ByteArrayAsList(byte[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public ByteSpliterator spliterator() {
            return ArrayUtil.spliterator(array, 0, array.length);
        }

        @Override
        public void getElements(int from, byte @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, byte @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull ByteUnaryOperator operator) {
            setAll(array, (i) -> operator.apply(array[i]));
        }

        @Override
        public byte getByte(int index) {
            return array[index];
        }

        @Override
        public void sort(ByteComparator comparator) {
            ByteArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(ByteComparator comparator) {
            ByteArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public byte set(int index, byte k) {
            byte result = getByte(index);
            array[index] = k;
            return result;
        }

        public byte[] getArray() {
            return array;
        }
    }

    public static final class ShortArrayAsList extends AbstractShortList
            implements ImprovedShortCollection, RandomAccess {
        private final short[] array;

        public ShortArrayAsList(short[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public ShortSpliterator spliterator() {
            return ArrayUtil.spliterator(array, 0, array.length);
        }

        @Override
        public void getElements(int from, short @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, short @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull ShortUnaryOperator operator) {
            setAll(array, (i) -> operator.apply(array[i]));
        }

        @Override
        public short getShort(int index) {
            return array[index];
        }

        @Override
        public void sort(ShortComparator comparator) {
            ShortArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(ShortComparator comparator) {
            ShortArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public short set(int index, short k) {
            short result = getShort(index);
            array[index] = k;
            return result;
        }

        public short[] getArray() {
            return array;
        }
    }

    public static final class IntArrayAsList extends AbstractIntList implements RandomAccess {
        private final int[] array;

        public IntArrayAsList(int[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public IntSpliterator spliterator() {
            return IntSpliterators.wrap(array, 0, array.length);
        }

        @Override
        public void getElements(int from, int @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, int @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull IntUnaryOperator operator) {
            Arrays.setAll(array, (i) -> operator.applyAsInt(array[i]));
        }

        @Override
        public int getInt(int index) {
            return array[index];
        }

        @Override
        public void sort(IntComparator comparator) {
            IntArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(IntComparator comparator) {
            IntArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public int set(int index, int k) {
            int result = getInt(index);
            array[index] = k;
            return result;
        }

        public int[] getArray() {
            return array;
        }
    }

    public static final class LongArrayAsList extends AbstractLongList implements RandomAccess {
        private final long[] array;

        public LongArrayAsList(long[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public LongSpliterator spliterator() {
            return LongSpliterators.wrap(array, 0, array.length);
        }

        @Override
        public void getElements(int from, long @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, long @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull LongUnaryOperator operator) {
            Arrays.setAll(array, (i) -> operator.applyAsLong(array[i]));
        }

        @Override
        public long getLong(int index) {
            return array[index];
        }

        @Override
        public void sort(LongComparator comparator) {
            LongArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(LongComparator comparator) {
            LongArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public long set(int index, long k) {
            long result = getLong(index);
            array[index] = k;
            return result;
        }

        public long[] getArray() {
            return array;
        }
    }

    public static final class CharArrayAsList extends AbstractCharList
            implements ImprovedCharCollection, RandomAccess {
        private final char[] array;

        public CharArrayAsList(char[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public CharSpliterator spliterator() {
            return ArrayUtil.spliterator(array, 0, array.length);
        }

        @Override
        public void getElements(int from, char @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, char @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull CharUnaryOperator operator) {
            setAll(array, (i) -> operator.apply(array[i]));
        }

        @Override
        public char getChar(int index) {
            return array[index];
        }

        @Override
        public void sort(CharComparator comparator) {
            CharArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(CharComparator comparator) {
            CharArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public char set(int index, char k) {
            char result = getChar(index);
            array[index] = k;
            return result;
        }

        public char[] getArray() {
            return array;
        }
    }

    public static final class FloatArrayAsList extends AbstractFloatList
            implements ImprovedFloatCollection, RandomAccess {
        private final float[] array;

        public FloatArrayAsList(float[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public FloatSpliterator spliterator() {
            return ArrayUtil.spliterator(array, 0, array.length);
        }

        @Override
        public void getElements(int from, float @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, float @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull FloatUnaryOperator operator) {
            setAll(array, (i) -> operator.apply(array[i]));
        }

        @Override
        public float getFloat(int index) {
            return array[index];
        }

        @Override
        public void sort(FloatComparator comparator) {
            FloatArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(FloatComparator comparator) {
            FloatArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public float set(int index, float k) {
            float result = getFloat(index);
            array[index] = k;
            return result;
        }

        public float[] getArray() {
            return array;
        }
    }

    public static final class DoubleArrayAsList extends AbstractDoubleList implements RandomAccess {
        private final double[] array;

        public DoubleArrayAsList(double[] array) {
            this.array = Preconditions.checkNotNull(array);
        }

        @Override
        public DoubleSpliterator spliterator() {
            return DoubleSpliterators.wrap(array, 0, array.length);
        }

        @Override
        public void getElements(int from, double @NotNull [] a, int offset, int length) {
            System.arraycopy(array, from, a, offset, length);
        }

        @Override
        public void setElements(int index, double @NotNull [] a, int offset, int length) {
            System.arraycopy(a, offset, array, index, length);
        }

        @Override
        public void replaceAll(final @NotNull DoubleUnaryOperator operator) {
            Arrays.setAll(array, (i) -> operator.applyAsDouble(array[i]));
        }

        @Override
        public double getDouble(int index) {
            return array[index];
        }

        @Override
        public void sort(DoubleComparator comparator) {
            DoubleArrays.quickSort(array, comparator);
        }

        @Override
        public void unstableSort(DoubleComparator comparator) {
            DoubleArrays.unstableSort(array, comparator);
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public double set(int index, double k) {
            double result = getDouble(index);
            array[index] = k;
            return result;
        }

        public double[] getArray() {
            return array;
        }
    }

    private static final class ParallelArrayReverseTask extends RecursiveAction {
        private final int from;
        private final int to;
        private final Swapper swapper;

        private ParallelArrayReverseTask(int from, int to, Swapper swapper) {
            this.from = from;
            this.to = to;
            this.swapper = swapper;
        }

        @Override
        protected void compute() {
            reverse(swapper, from, to);
        }
    }

    private static final class ForkJoinArrayReverseTask extends RecursiveAction {
        private final int from;
        private final int to;
        private final int fence;
        private final Swapper swapper;

        private ForkJoinArrayReverseTask(int from, int to, int fence, Swapper swapper) {
            this.from = from;
            this.to = to;
            this.fence = fence;
            this.swapper = swapper;
        }

        @Override
        protected void compute() {
            ForkJoinPool pool = getPool();
            int length = to - from;

            if (pool.getParallelism() == 1 || length < PARALLEL_REVERSE_NO_FORK) {
                reverse(swapper, from, to, fence);
            } else {
                int i = from;
                int lastIndex = from + Math.floorDiv(to - from, PARALLEL_REVERSE_NO_FORK);

                while (i < lastIndex) {
                    pool.submit(new ForkJoinArrayReverseTask(i, i += PARALLEL_REVERSE_NO_FORK, fence, swapper));
                }

                pool.submit(new ForkJoinArrayReverseTask(i, to, fence, swapper));
            }
        }
    }
}
