package svegon.utils.fast.util.shorts;

import svegon.utils.collections.stream.ShortStream;
import svegon.utils.collections.stream.StreamUtil;
import it.unimi.dsi.fastutil.shorts.ShortCollection;

/**
 * @since 1.0.0
 */
public interface ImprovedShortCollection extends ShortCollection {
    /**
     * Return a fast stream over the elements, performing widening casts if
     * needed.
     *
     * @return a fast stream over the elements.
     * @see java.util.Collection#stream()
     * @see ShortStream
     */
    default ShortStream shortStream() {
        return StreamUtil.shortStream(spliterator(), false);
    }

    /**
     * Return a fast parallel stream over the elements, performing widening casts if
     * needed.
     *
     * @return a fast stream over the elements.
     * @see java.util.Collection#stream()
     * @see ShortStream
     */
    default ShortStream parallelShortStream() {
        return StreamUtil.shortStream(spliterator(), true);
    }
}
