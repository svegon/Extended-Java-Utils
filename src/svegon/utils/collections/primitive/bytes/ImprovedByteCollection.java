package svegon.utils.collections.primitive.bytes;

import svegon.utils.collections.stream.ByteStream;
import svegon.utils.collections.stream.StreamUtil;
import it.unimi.dsi.fastutil.bytes.ByteCollection;

/**
 * @since 1.0.0
 */
public interface ImprovedByteCollection extends ByteCollection {
    /**
     * Return a primitive stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see ByteStream
     */
    default ByteStream byteStream() {
        return StreamUtil.byteStream(spliterator(), false);
    }

    /**
     * Return a primitive parallel stream over the elements, performing widening casts if
     * needed.
     *
     * @return a primitive stream over the elements.
     * @see java.util.Collection#stream()
     * @see ByteStream
     */
    default ByteStream parallelByteStream() {
        return StreamUtil.byteStream(spliterator(), true);
    }
}
