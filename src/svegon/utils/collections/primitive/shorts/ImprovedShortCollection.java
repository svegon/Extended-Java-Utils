package svegon.utils.collections.primitive.shorts;

import svegon.utils.collections.stream.ShortStream;
import svegon.utils.collections.stream.StreamUtil;
import it.unimi.dsi.fastutil.shorts.ShortCollection;

/**
 * @since 1.0.0
 */
public interface ImprovedShortCollection extends ShortCollection {
    /**
     * Return a primitive stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see ShortStream
     */
    default ShortStream shortStream() {
        return StreamUtil.shortStream(spliterator(), false);
    }

    /**
     * Return a primitive parallel stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see ShortStream
     */
    default ShortStream parallelShortStream() {
        return StreamUtil.shortStream(spliterator(), true);
    }
}
