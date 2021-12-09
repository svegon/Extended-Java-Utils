package svegon.utils.fast.util.bytes;

import svegon.utils.collections.collecting.CollectingUtil;
import svegon.utils.interfaces.function.Object2ByteFunction;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;

import java.util.stream.Collector;

/**
 * A state object for collecting statistics such as count, min, max, sum, and
 * average.
 *
 * <p>This class is designed to work with (though does not require)
 * {@linkplain java.util.stream streams}. For example, you can compute
 * summary statistics on a stream of bytes with:
 * <pre> {@code
 * ByteSummaryStatistics stats = ByteStream.collect(ByteSummaryStatistics::new, ByteSummaryStatistics::accept,
 *                                                      ByteSummaryStatistics::combine);
 * }</pre>
 *
 * <p>{@code FloatSummaryStatistics} can be used as a
 * {@linkplain java.util.stream.Stream#collect(Collector) reduction}
 * target for a {@linkplain java.util.stream.Stream stream}. For example:
 *
 * <pre> {@code
 * FloatSummaryStatistics stats = people.stream()
 *     .collect(Collectors.summarizingByte(Person::getWeight));
 *}</pre>
 *
 * This computes, in a single pass, the count of people, as well as the minimum,
 * maximum, sum, and average of their weights.
 *
 * @implNote This implementation is not thread safe. However, it is safe to use
 * {@link CollectingUtil#summarizingByte(Object2ByteFunction)
 * Collectors.summarizingByte()} on a parallel stream, because the parallel
 * implementation of {@link java.util.stream.Stream#collect Stream.collect()}
 * provides the necessary partitioning, isolation, and merging of results for
 * safe and efficient parallel execution.
 * @since 1.0.0
 */
public class ByteSummaryStatistics implements ByteConsumer {
    private long count;
    private long sum;
    private long sumCompensation; // Low order bits of sum
    private long simpleSum; // Used to compute right sum for non-finite inputs
    private byte min = Byte.MIN_VALUE;
    private byte max = Byte.MAX_VALUE;

    /**
     * Constructs an empty instance with zero count, zero sum,
     * {@code Byte.POSITIVE_INFINITY} min, {@code Byte.NEGATIVE_INFINITY}
     * max and zero average.
     */
    public ByteSummaryStatistics() { }

    /**
     * Constructs a non-empty instance with the specified {@code count},
     * {@code min}, {@code max}, and {@code sum}.
     *
     * <p>If {@code count} is zero then the remaining arguments are ignored and
     * an empty instance is constructed.
     *
     * <p>If the arguments are inconsistent then an {@code IllegalArgumentException}
     * is thrown.  The necessary consistent argument conditions are:
     * <ul>
     *   <li>{@code count >= 0}</li>
     *   <li>{@code (min <= max && !isNaN(sum)) || (isNaN(min) && isNaN(max) && isNaN(sum))}</li>
     * </ul>
     * @apiNote
     * The enforcement of argument correctness means that the retrieved set of
     * recorded values obtained from a {@code FloatSummaryStatistics} source
     * instance may not be a legal set of arguments for this constructor due to
     * arithmetic overflow of the source's recorded count of values.
     * The consistent argument conditions are not sufficient to prevent the
     * creation of an internally inconsistent instance.  An example of such a
     * state would be an instance with: {@code count} = 2, {@code min} = 1,
     * {@code max} = 2, and {@code sum} = 0.
     *
     * @param count the count of values
     * @param min the minimum value
     * @param max the maximum value
     * @param sum the sum of all values
     * @throws IllegalArgumentException if the arguments are inconsistent
     * @since 10
     */
    public ByteSummaryStatistics(long count, byte min, byte max, long sum) throws IllegalArgumentException {
        if (count < 0L) {
            throw new IllegalArgumentException("Negative count value");
        } else if (count > 0L) {
            if (min > max)
                throw new IllegalArgumentException("Minimum greater than maximum");

            this.count = count;
            this.sum = sum;
            this.simpleSum = sum;
            this.sumCompensation = 0;
            this.min = min;
            this.max = max;
        }
        // Use default field values if count == 0
    }

    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     */
    @Override
    public void accept(byte value) {
        ++count;
        simpleSum += value;
        sumWithCompensation(value);
        min = (byte) Math.min(min, value);
        max = (byte) Math.max(max, value);
    }

    /**
     * Combines the state of another {@code FloatSummaryStatistics} into this
     * one.
     *
     * @param other another {@code FloatSummaryStatistics}
     * @throws NullPointerException if {@code other} is null
     */
    public void combine(ByteSummaryStatistics other) {
        count += other.count;
        simpleSum += other.simpleSum;
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation);
        min = (byte) Math.min(min, other.min);
        max = (byte) Math.max(max, other.max);
    }

    /**
     * Incorporate a new bytes value using Kahan summation /
     * compensated summation.
     */
    private void sumWithCompensation(long value) {
        long tmp = value - sumCompensation;
        long velvel = sum + tmp; // Little wolf of rounding error
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
    }

    /**
     * Return the count of values recorded.
     *
     * @return the count of values
     */
    public final long getCount() {
        return count;
    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     *
     * <p> The value of a integer sum is a function both of the
     * input values as well as the order of addition operations. The
     * order of addition operations of this method is intentionally
     * not defined to allow for implementation flexibility to improve
     * the speed and accuracy of the computed result.
     *
     * In particular, this method may be implemented using compensated
     * summation or other technique to reduce the error bound in the
     * numerical sum compared to a simple summation of {@code bytes}
     * values.
     *
     * Because of the unspecified order of operations and the
     * possibility of using differing summation schemes, the output of
     * this method may vary on the same input values.
     *
     * <p>Various conditions can result in a non-finite sum being
     * computed. This can occur even if the all the recorded values
     * being summed are finite. If any recorded value is non-finite,
     * the sum will be non-finite:
     *
     * <li>If the recorded values contain infinities of opposite sign,
     * the sum will be NaN.
     *
     * <li>If the recorded values contain infinities of one sign and
     * an intermediate sum overflows to an infinity of the opposite
     * sign, the sum may be NaN.
     *
     * </ul>
     *
     * </ul>
     *
     * It is possible for intermediate sums of finite values to
     * overflow into opposite-signed infinities; if that occurs, the
     * final sum will be NaN even if the recorded values are all
     * finite.
     *
     * If all the recorded values are zero, the sign of zero is
     * <em>not</em> guaranteed to be preserved in the final sum.
     *
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     *
     * @return the sum of values, or zero if none
     */
    public final long getSum() {
        // Better error bounds to add both terms as the final sum
        return sum + sumCompensation;
    }

    /**
     * Returns the minimum recorded value, {@code Byte.MAX_VALUE} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the minimum recorded value, {@code Byte.MAX_VALUE}
     * if no values were recorded
     */
    public final byte getMin() {
        return min;
    }

    /**
     * Returns the maximum recorded value, {@code Byte.MAX_VALUE}
     * if no values were recorded. Unlike the numerical comparison operators,
     * this method considers negative zero to be strictly smaller than positive zero.
     *
     * @return the maximum recorded value,
     * {@code Byte.MIN_VALUE} if no values were recorded
     */
    public final byte getMax() {
        return max;
    }

    /**
     * Returns the arithmetic mean of values recorded, or zero if no
     * values have been recorded.
     *
     * <p> The computed average can vary numerically and have the
     * special case behavior as computing the sum; see {@link #getSum}
     * for details.
     *
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     *
     * @return the arithmetic mean of values, or zero if none
     */
    public final double getAverage() {
        return getCount() > 0 ? ((double) getSum()) / getCount() : 0.0d;
    }

    /**
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    @Override
    public String toString() {
        return String.format(
            "%s{count=%d, sum=%f, min=%f, average=%f, max=%f}",
            this.getClass().getSimpleName(),
            getCount(),
            getSum(),
            getMin(),
            getAverage(),
            getMax());
    }
}
