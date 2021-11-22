package svegon.utils.collections.primitive.chars;

import svegon.utils.collections.stream.CharStream;
import svegon.utils.collections.stream.StreamUtil;
import it.unimi.dsi.fastutil.chars.CharCollection;

/**
 * @since 1.0.0
 */
public interface ImprovedCharCollection extends CharCollection {
    /**
     * Return a primitive stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see CharStream
     */
    default CharStream charStream() {
        return StreamUtil.charStream(spliterator(), false);
    }

    /**
     * Return a primitive parallel stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see CharStream
     */
    default CharStream parallelCharStream() {
        return StreamUtil.charStream(spliterator(), true);
    }
}