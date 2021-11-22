package svegon.utils.collections.primitive.booleans;

import svegon.utils.collections.stream.BooleanStream;
import svegon.utils.collections.stream.StreamUtil;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;

public interface ImprovedBooleanCollection extends BooleanCollection {
    /**
    * Return a primitive stream over the elements, performing widening casts if
    * needed.
    *
    * @return a primitive stream over the elements.
    * @see java.util.Collection#stream()
    * @see BooleanStream
    */
    default BooleanStream booleanStream() {
        return StreamUtil.booleanStream(spliterator(), false);
    }

    /**
     * Return a primitive parallel stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see BooleanStream
     */
    default BooleanStream parallelBooleanStream() {
        return StreamUtil.booleanStream(spliterator(), true);
    }
}
