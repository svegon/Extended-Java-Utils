package svegon.utils.collections.primitive.floats;

import svegon.utils.collections.stream.FloatStream;
import svegon.utils.collections.stream.StreamUtil;
import it.unimi.dsi.fastutil.floats.FloatCollection;

/**
 * @since 1.0.0
 */
public interface ImprovedFloatCollection extends FloatCollection {
    /**
     * Return a primitive stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see FloatStream
     */
    default FloatStream floatStream() {
        return StreamUtil.floatStream(spliterator(), false);
    }

    /**
     * Return a primitive parallel stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see FloatStream
     */
    default FloatStream parallelFloatStream() {
        return StreamUtil.floatStream(spliterator(), true);
    }
}
