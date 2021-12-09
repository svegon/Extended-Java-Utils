/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package svegon.utils.fuck_modifiers;

import svegon.utils.collections.ArrayUtil;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;

import java.util.*;
import java.util.function.*;

/**
 * An ordered collection of elements.  Elements can be added, but not removed.
 * Goes through a building phase, during which elements can be added, and a
 * traversal phase, during which elements can be traversed in order but no
 * further modifications are possible.
 *
 * <p> One or more arrays are used to store elements. The use of a multiple
 * arrays has better performance characteristics than a single array used by
 * {@link ArrayList}, as when the capacity of the list needs to be increased
 * no copying of elements is required.  This is usually beneficial in the case
 * where the results will be traversed a small number of times.
 *
 * @param <E> the type of elements in this list
 * @since 1.8
 */
public class SpinedBuffer<E> extends AbstractSpinedBuffer implements Consumer<E>, Iterable<E> {

    /*
     * We optimistically hope that all the data will fit into the first chunk,
     * so we try to avoid inflating the spine[] and priorElementCount[] arrays
     * prematurely.  So methods must be prepared to deal with these arrays being
     * null.  If spine is non-null, then spineIndex points to the current chunk
     * within the spine, otherwise it is zero.  The spine and priorElementCount
     * arrays are always the same size, and for any i <= spineIndex,
     * priorElementCount[i] is the sum of the sizes of all the prior chunks.
     *
     * The curChunk pointer is always valid.  The elementIndex is the index of
     * the next element to be written in curChunk; this may be past the end of
     * curChunk so we have to check before writing. When we inflate the spine
     * array, curChunk becomes the first element in it.  When we clear the
     * buffer, we discard all chunks except the first one, which we clear,
     * restoring it to the initial single-chunk state.
     */
    /**
     * Minimum power-of-two for the first chunk.
     */
    public static final int MIN_CHUNK_POWER = 4;

    /**
     * Minimum size for the first chunk.
     */
    public static final int MIN_CHUNK_SIZE = 1 << MIN_CHUNK_POWER;

    /**
     * Max power-of-two for chunks.
     */
    public static final int MAX_CHUNK_POWER = 30;

    /**
     * Minimum array size for array-of-chunks.
     */
    public static final int MIN_SPINE_SIZE = 8;


    /**
     * log2 of the size of the first chunk.
     */
    protected final int initialChunkPower;

    /**
     * Index of the *next* element to write; may point into, or just outside of,
     * the current chunk.
     */
    protected int elementIndex;

    /**
     * Index of the *current* chunk in the spine array, if the spine array is
     * non-null.
     */
    protected int spineIndex;

    /**
     * Count of elements in all prior chunks.
     */
    protected long[] priorElementCount;

    /**
     * Is the buffer currently empty?
     */
    public boolean isEmpty() {
        return (spineIndex == 0) && (elementIndex == 0);
    }

    /**
     * How many elements are currently in the buffer?
     */
    public long count() {
        return (spineIndex == 0)
                ? elementIndex
                : priorElementCount[spineIndex] + elementIndex;
    }

    /**
     * How big should the nth chunk be?
     */
    protected int chunkSize(int n) {
        int power = (n == 0 || n == 1)
                ? initialChunkPower
                : Math.min(initialChunkPower + n - 1, MAX_CHUNK_POWER);
        return 1 << power;
    }

    /**
     * Chunk that we're currently writing into; may or may not be aliased with
     * the first element of the spine.
     */
    protected E[] curChunk;

    /**
     * All chunks, or null if there is only one chunk.
     */
    protected E[][] spine;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    @SuppressWarnings("unchecked")
    SpinedBuffer(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);

        this.initialChunkPower = Math.max(MIN_CHUNK_POWER,
                Integer.SIZE - Integer.numberOfLeadingZeros(initialCapacity - 1));
        curChunk = (E[]) new Object[1 << initialChunkPower];
    }

    /**
     * Constructs an empty list with an initial capacity of sixteen.
     */
    @SuppressWarnings("unchecked")
    SpinedBuffer() {
        this.initialChunkPower = MIN_CHUNK_POWER;
        curChunk = (E[]) new Object[1 << initialChunkPower];
    }

    /**
     * Returns the current capacity of the buffer
     */
    protected long capacity() {
        return (spineIndex == 0)
                ? curChunk.length
                : priorElementCount[spineIndex] + spine[spineIndex].length;
    }

    @SuppressWarnings("unchecked")
    private void inflateSpine() {
        if (spine == null) {
            spine = (E[][]) new Object[MIN_SPINE_SIZE][];
            priorElementCount = new long[MIN_SPINE_SIZE];
            spine[0] = curChunk;
        }
    }

    /**
     * Ensure that the buffer has at least capacity to hold the target size
     */
    @SuppressWarnings("unchecked")
    protected final void ensureCapacity(long targetSize) {
        long capacity = capacity();
        if (targetSize > capacity) {
            inflateSpine();
            for (int i=spineIndex+1; targetSize > capacity; i++) {
                if (i >= spine.length) {
                    int newSpineSize = spine.length * 2;
                    spine = Arrays.copyOf(spine, newSpineSize);
                    priorElementCount = Arrays.copyOf(priorElementCount, newSpineSize);
                }
                int nextChunkSize = chunkSize(i);
                spine[i] = (E[]) new Object[nextChunkSize];
                priorElementCount[i] = priorElementCount[i-1] + spine[i-1].length;
                capacity += nextChunkSize;
            }
        }
    }

    /**
     * Force the buffer to increase its capacity.
     */
    protected void increaseCapacity() {
        ensureCapacity(capacity() + 1);
    }

    /**
     * Retrieve the element at the specified index.
     */
    public E get(long index) {
        // @@@ can further optimize by caching last seen spineIndex,
        // which is going to be right most of the time

        // Casts to int are safe since the spine array index is the index minus
        // the prior element count from the current spine
        if (spineIndex == 0) {
            if (index < elementIndex)
                return curChunk[((int) index)];
            else
                throw new IndexOutOfBoundsException(Long.toString(index));
        }

        if (index >= count())
            throw new IndexOutOfBoundsException(Long.toString(index));

        for (int j=0; j <= spineIndex; j++)
            if (index < priorElementCount[j] + spine[j].length)
                return spine[j][((int) (index - priorElementCount[j]))];

        throw new IndexOutOfBoundsException(Long.toString(index));
    }

    /**
     * Copy the elements, starting at the specified offset, into the specified
     * array.
     */
    public void copyInto(E[] array, int offset) {
        long finalOffset = offset + count();
        if (finalOffset > array.length || finalOffset < offset) {
            throw new IndexOutOfBoundsException("does not fit");
        }

        if (spineIndex == 0)
            System.arraycopy(curChunk, 0, array, offset, elementIndex);
        else {
            // full chunks
            for (int i=0; i < spineIndex; i++) {
                System.arraycopy(spine[i], 0, array, offset, spine[i].length);
                offset += spine[i].length;
            }
            if (elementIndex > 0)
                System.arraycopy(curChunk, 0, array, offset, elementIndex);
        }
    }

    /**
     * Create a new array using the specified array factory, and copy the
     * elements into it.
     */
    public E[] asArray(IntFunction<E[]> arrayFactory) {
        long size = count();
        if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        E[] result = arrayFactory.apply((int) size);
        copyInto(result, 0);
        return result;
    }

    public void clear() {
        if (spine != null) {
            curChunk = spine[0];
            for (int i=0; i<curChunk.length; i++)
                curChunk[i] = null;
            spine = null;
            priorElementCount = null;
        }
        else {
            for (int i=0; i<elementIndex; i++)
                curChunk[i] = null;
        }
        elementIndex = 0;
        spineIndex = 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        // completed chunks, if any
        for (int j = 0; j < spineIndex; j++)
            for (E t : spine[j])
                consumer.accept(t);

        // current chunk
        for (int i=0; i<elementIndex; i++)
            consumer.accept(curChunk[i]);
    }

    @Override
    public void accept(E e) {
        if (elementIndex == curChunk.length) {
            inflateSpine();
            if (spineIndex+1 >= spine.length || spine[spineIndex+1] == null)
                increaseCapacity();
            elementIndex = 0;
            ++spineIndex;
            curChunk = spine[spineIndex];
        }
        curChunk[elementIndex++] = e;
    }

    @Override
    public String toString() {
        List<E> list = new ArrayList<>();
        forEach(list::add);
        return "SpinedBuffer:" + list.toString();
    }

    private static final int SPLITERATOR_CHARACTERISTICS
            = Spliterator.SIZED | Spliterator.ORDERED | Spliterator.SUBSIZED;

    /**
     * Return a {@link Spliterator} describing the contents of the buffer.
     */
    public Spliterator<E> spliterator() {
        class Splitr implements Spliterator<E> {
            // The current spine index
            int splSpineIndex;

            // Last spine index
            final int lastSpineIndex;

            // The current element index into the current spine
            int splElementIndex;

            // Last spine's last element index + 1
            final int lastSpineElementFence;

            // When splSpineIndex >= lastSpineIndex and
            // splElementIndex >= lastSpineElementFence then
            // this spliterator is fully traversed
            // tryAdvance can set splSpineIndex > spineIndex if the last spine is full

            // The current spine array
            E[] splChunk;

            public Splitr(int firstSpineIndex, int lastSpineIndex,
                   int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert spine != null || firstSpineIndex == 0 && lastSpineIndex == 0;
                splChunk = (spine == null) ? curChunk : spine[firstSpineIndex];
            }

            @Override
            public long estimateSize() {
                return (splSpineIndex == lastSpineIndex)
                        ? (long) lastSpineElementFence - splElementIndex
                        : // # of elements prior to end -
                        priorElementCount[lastSpineIndex] + lastSpineElementFence -
                                // # of elements prior to current
                                priorElementCount[splSpineIndex] - splElementIndex;
            }

            @Override
            public int characteristics() {
                return SPLITERATOR_CHARACTERISTICS;
            }

            @Override
            public boolean tryAdvance(Consumer<? super E> consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    consumer.accept(splChunk[splElementIndex++]);

                    if (splElementIndex == splChunk.length) {
                        splElementIndex = 0;
                        ++splSpineIndex;
                        if (spine != null && splSpineIndex <= lastSpineIndex)
                            splChunk = spine[splSpineIndex];
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super E> consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    int i = splElementIndex;
                    // completed chunks, if any
                    for (int sp = splSpineIndex; sp < lastSpineIndex; sp++) {
                        E[] chunk = spine[sp];
                        for (; i < chunk.length; i++) {
                            consumer.accept(chunk[i]);
                        }
                        i = 0;
                    }
                    // last (or current uncompleted) chunk
                    E[] chunk = (splSpineIndex == lastSpineIndex) ? splChunk : spine[lastSpineIndex];
                    int hElementIndex = lastSpineElementFence;
                    for (; i < hElementIndex; i++) {
                        consumer.accept(chunk[i]);
                    }
                    // mark consumed
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = lastSpineElementFence;
                }
            }

            @Override
            public Spliterator<E> trySplit() {
                if (splSpineIndex < lastSpineIndex) {
                    // split just before last chunk (if it is full this means 50:50 split)
                    Spliterator<E> ret = new Splitr(splSpineIndex, lastSpineIndex - 1,
                            splElementIndex, spine[lastSpineIndex-1].length);
                    // position to start of last chunk
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = 0;
                    splChunk = spine[splSpineIndex];
                    return ret;
                }
                else if (splSpineIndex == lastSpineIndex) {
                    int t = (lastSpineElementFence - splElementIndex) / 2;
                    if (t == 0)
                        return null;
                    else {
                        Spliterator<E> ret = Arrays.spliterator(splChunk, splElementIndex, splElementIndex + t);
                        splElementIndex += t;
                        return ret;
                    }
                }
                else {
                    return null;
                }
            }
        }
        return new Splitr(0, spineIndex, 0, elementIndex);
    }

    /**
     * An ordered collection of fast values.  Elements can be added, but
     * not removed. Goes through a building phase, during which elements can be
     * added, and a traversal phase, during which elements can be traversed in
     * order but no further modifications are possible.
     *
     * <p> One or more arrays are used to store elements. The use of a multiple
     * arrays has better performance characteristics than a single array used by
     * {@link ArrayList}, as when the capacity of the list needs to be increased
     * no copying of elements is required.  This is usually beneficial in the case
     * where the results will be traversed a small number of times.
     *
     * @param <E> the wrapper type for this fast type
     * @param <T_ARR> the array type for this fast type
     * @param <T_CONS> the Consumer type for this fast type
     */
    public abstract static class OfPrimitive<E, T_ARR, T_CONS> extends SpinedBuffer<E> implements Iterable<E> {

        /*
         * We optimistically hope that all the data will fit into the first chunk,
         * so we try to avoid inflating the spine[] and priorElementCount[] arrays
         * prematurely.  So methods must be prepared to deal with these arrays being
         * null.  If spine is non-null, then spineIndex points to the current chunk
         * within the spine, otherwise it is zero.  The spine and priorElementCount
         * arrays are always the same size, and for any i <= spineIndex,
         * priorElementCount[i] is the sum of the sizes of all the prior chunks.
         *
         * The curChunk pointer is always valid.  The elementIndex is the index of
         * the next element to be written in curChunk; this may be past the end of
         * curChunk so we have to check before writing. When we inflate the spine
         * array, curChunk becomes the first element in it.  When we clear the
         * buffer, we discard all chunks except the first one, which we clear,
         * restoring it to the initial single-chunk state.
         */

        // The chunk we're currently writing into
        T_ARR curChunk;

        // All chunks, or null if there is only one chunk
        T_ARR[] spine;

        /**
         * Constructs an empty list with the specified initial capacity.
         *
         * @param  initialCapacity  the initial capacity of the list
         * @throws IllegalArgumentException if the specified initial capacity
         *         is negative
         */
        OfPrimitive(int initialCapacity) {
            super(initialCapacity);
            curChunk = newArray(1 << initialChunkPower);
        }

        /**
         * Constructs an empty list with an initial capacity of sixteen.
         */
        OfPrimitive() {
            super();
            curChunk = newArray(1 << initialChunkPower);
        }

        @Override
        public abstract Iterator<E> iterator();

        @Override
        public abstract void forEach(Consumer<? super E> consumer);

        /** Create a new array-of-array of the proper type and size */
        protected abstract T_ARR[] newArrayArray(int size);

        /** Create a new array of the proper type and size */
        public abstract T_ARR newArray(int size);

        /** Get the length of an array */
        protected abstract int arrayLength(T_ARR array);

        /** Iterate an array with the provided consumer */
        protected abstract void arrayForEach(T_ARR array, int from, int to,
                                             T_CONS consumer);

        protected long capacity() {
            return (spineIndex == 0)
                    ? arrayLength(curChunk)
                    : priorElementCount[spineIndex] + arrayLength(spine[spineIndex]);
        }

        private void inflateSpine() {
            if (spine == null) {
                spine = newArrayArray(MIN_SPINE_SIZE);
                priorElementCount = new long[MIN_SPINE_SIZE];
                spine[0] = curChunk;
            }
        }

        protected void increaseCapacity() {
            ensureCapacity(capacity() + 1);
        }

        protected int chunkFor(long index) {
            if (spineIndex == 0) {
                if (index < elementIndex)
                    return 0;
                else
                    throw new IndexOutOfBoundsException(Long.toString(index));
            }

            if (index >= count())
                throw new IndexOutOfBoundsException(Long.toString(index));

            for (int j=0; j <= spineIndex; j++)
                if (index < priorElementCount[j] + arrayLength(spine[j]))
                    return j;

            throw new IndexOutOfBoundsException(Long.toString(index));
        }

        public void copyInto(T_ARR array, int offset) {
            long finalOffset = offset + count();
            if (finalOffset > arrayLength(array) || finalOffset < offset) {
                throw new IndexOutOfBoundsException("does not fit");
            }

            if (spineIndex == 0)
                System.arraycopy(curChunk, 0, array, offset, elementIndex);
            else {
                // full chunks
                for (int i=0; i < spineIndex; i++) {
                    System.arraycopy(spine[i], 0, array, offset, arrayLength(spine[i]));
                    offset += arrayLength(spine[i]);
                }
                if (elementIndex > 0)
                    System.arraycopy(curChunk, 0, array, offset, elementIndex);
            }
        }

        public T_ARR asPrimitiveArray() {
            long size = count();
            if (size >= it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            T_ARR result = newArray((int) size);
            copyInto(result, 0);
            return result;
        }

        protected void preAccept() {
            if (elementIndex == arrayLength(curChunk)) {
                inflateSpine();
                if (spineIndex+1 >= spine.length || spine[spineIndex+1] == null)
                    increaseCapacity();
                elementIndex = 0;
                ++spineIndex;
                curChunk = spine[spineIndex];
            }
        }

        public void clear() {
            if (spine != null) {
                curChunk = spine[0];
                spine = null;
                priorElementCount = null;
            }
            elementIndex = 0;
            spineIndex = 0;
        }

        @SuppressWarnings("overloads")
        public void forEach(T_CONS consumer) {
            // completed chunks, if any
            for (int j = 0; j < spineIndex; j++)
                arrayForEach(spine[j], 0, arrayLength(spine[j]), consumer);

            // current chunk
            arrayForEach(curChunk, 0, elementIndex, consumer);
        }

        public abstract class BaseSpliterator<T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>>
                implements Spliterator.OfPrimitive<E, T_CONS, T_SPLITR> {
            // The current spine index
            public int splSpineIndex;

            // Last spine index
            public final int lastSpineIndex;

            // The current element index into the current spine
            public int splElementIndex;

            // Last spine's last element index + 1
            public final int lastSpineElementFence;

            // When splSpineIndex >= lastSpineIndex and
            // splElementIndex >= lastSpineElementFence then
            // this spliterator is fully traversed
            // tryAdvance can set splSpineIndex > spineIndex if the last spine is full

            // The current spine array
            public T_ARR splChunk;

            public BaseSpliterator(int firstSpineIndex, int lastSpineIndex,
                            int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert spine != null || firstSpineIndex == 0 && lastSpineIndex == 0;
                splChunk = (spine == null) ? curChunk : spine[firstSpineIndex];
            }

            public abstract T_SPLITR newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                             int firstSpineElementIndex, int lastSpineElementFence);

            public abstract void arrayForOne(T_ARR array, int index, T_CONS consumer);

            public abstract T_SPLITR arraySpliterator(T_ARR array, int offset, int len);

            @Override
            public long estimateSize() {
                return (splSpineIndex == lastSpineIndex)
                        ? (long) lastSpineElementFence - splElementIndex
                        : // # of elements prior to end -
                        priorElementCount[lastSpineIndex] + lastSpineElementFence -
                                // # of elements prior to current
                                priorElementCount[splSpineIndex] - splElementIndex;
            }

            @Override
            public int characteristics() {
                return SPLITERATOR_CHARACTERISTICS;
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    arrayForOne(splChunk, splElementIndex++, consumer);

                    if (splElementIndex == arrayLength(splChunk)) {
                        splElementIndex = 0;
                        ++splSpineIndex;
                        if (spine != null && splSpineIndex <= lastSpineIndex)
                            splChunk = spine[splSpineIndex];
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    int i = splElementIndex;
                    // completed chunks, if any
                    for (int sp = splSpineIndex; sp < lastSpineIndex; sp++) {
                        T_ARR chunk = spine[sp];
                        arrayForEach(chunk, i, arrayLength(chunk), consumer);
                        i = 0;
                    }
                    // last (or current uncompleted) chunk
                    T_ARR chunk = (splSpineIndex == lastSpineIndex) ? splChunk : spine[lastSpineIndex];
                    arrayForEach(chunk, i, lastSpineElementFence, consumer);
                    // mark consumed
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = lastSpineElementFence;
                }
            }

            @Override
            public T_SPLITR trySplit() {
                if (splSpineIndex < lastSpineIndex) {
                    // split just before last chunk (if it is full this means 50:50 split)
                    T_SPLITR ret = newSpliterator(splSpineIndex, lastSpineIndex - 1,
                            splElementIndex, arrayLength(spine[lastSpineIndex - 1]));
                    // position us to start of last chunk
                    splSpineIndex = lastSpineIndex;
                    splElementIndex = 0;
                    splChunk = spine[splSpineIndex];
                    return ret;
                }
                else if (splSpineIndex == lastSpineIndex) {
                    int t = (lastSpineElementFence - splElementIndex) / 2;
                    if (t == 0)
                        return null;
                    else {
                        T_SPLITR ret = arraySpliterator(splChunk, splElementIndex, t);
                        splElementIndex += t;
                        return ret;
                    }
                }
                else {
                    return null;
                }
            }
        }
    }

    /**
     * An ordered collection of {@code double} values.
     */
    public static class OfBoolean extends OfPrimitive<Boolean, boolean[], BooleanConsumer> implements BooleanConsumer {
        public OfBoolean() { }

        public OfBoolean(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Boolean> consumer) {
            if (consumer instanceof BooleanConsumer) {
                forEach((BooleanConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected boolean[][] newArrayArray(int size) {
            return new boolean[size][];
        }

        @Override
        public boolean[] newArray(int size) {
            return new boolean[size];
        }

        @Override
        protected int arrayLength(boolean[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(boolean[] array, int from, int to, BooleanConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(boolean i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Boolean get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);

            if (spineIndex == 0 && ch == 0) {
                return curChunk[(int) index];
            } else {
                return spine[ch][(int) (index - priorElementCount[ch])];
            }
        }

        @Override
        public BooleanIterator iterator() {
            return new Itr(this);
        }

        public BooleanSpliterator spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            boolean[] array = asPrimitiveArray();

            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            } else {
                boolean[] array2 = Arrays.copyOf(array, 200);

                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public static final class Itr implements BooleanIterator, BooleanConsumer {
            private final BooleanSpliterator spliterator;
            private boolean valueReady = false;
            private boolean nextElement;

            private Itr(OfBoolean of) {
                this.spliterator = Objects.requireNonNull(of.spliterator());
            }

            @Override
            public void accept(boolean t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public boolean nextBoolean() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        public final class Splitr extends BaseSpliterator<BooleanSpliterator> implements BooleanSpliterator {
            public Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex,
                          int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                         int firstSpineElementIndex, int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(boolean[] array, int index, BooleanConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public BooleanSpliterator arraySpliterator(boolean[] array, int offset, int len) {
                return ArrayUtil.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code double} values.
     */
    public static class OfByte extends OfPrimitive<Byte, byte[], ByteConsumer> implements ByteConsumer {
        public OfByte() { }

        public OfByte(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Byte> consumer) {
            if (consumer instanceof ByteConsumer) {
                forEach((ByteConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected byte[][] newArrayArray(int size) {
            return new byte[size][];
        }

        @Override
        public byte[] newArray(int size) {
            return new byte[size];
        }

        @Override
        protected int arrayLength(byte[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(byte[] array, int from, int to, ByteConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(byte i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Byte get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);

            if (spineIndex == 0 && ch == 0) {
                return curChunk[(int) index];
            } else {
                return spine[ch][(int) (index - priorElementCount[ch])];
            }
        }

        @Override
        public ByteIterator iterator() {
            return new Itr(this);
        }

        public ByteSpliterator spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            byte[] array = asPrimitiveArray();

            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            } else {
                byte[] array2 = Arrays.copyOf(array, 200);

                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public static final class Itr implements ByteIterator, ByteConsumer {
            private final ByteSpliterator spliterator;
            private boolean valueReady = false;
            private byte nextElement;

            private Itr(OfByte of) {
                this.spliterator = Objects.requireNonNull(of.spliterator());
            }

            @Override
            public void accept(byte t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady) {
                    spliterator.tryAdvance(this);
                }

                return valueReady;
            }

            @Override
            public byte nextByte() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        public final class Splitr extends BaseSpliterator<ByteSpliterator> implements ByteSpliterator {
            public Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex,
                          int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex,
                                         int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(byte[] array, int index, ByteConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public ByteSpliterator arraySpliterator(byte[] array, int offset, int len) {
                return ArrayUtil.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code double} values.
     */
    public static class OfShort extends OfPrimitive<Short, short[], ShortConsumer> implements ShortConsumer {
        public OfShort() { }

        public OfShort(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Short> consumer) {
            if (consumer instanceof ShortConsumer) {
                forEach((ShortConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected short[][] newArrayArray(int size) {
            return new short[size][];
        }

        @Override
        public short[] newArray(int size) {
            return new short[size];
        }

        @Override
        protected int arrayLength(short[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(short[] array, int from, int to, ShortConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(short i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        @Override
        public Short get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);

            if (spineIndex == 0 && ch == 0) {
                return curChunk[(int) index];
            } else {
                return spine[ch][(int) (index - priorElementCount[ch])];
            }
        }

        @Override
        public ShortIterator iterator() {
            return new Itr(this);
        }

        public ShortSpliterator spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            short[] array = asPrimitiveArray();

            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            } else {
                short[] array2 = Arrays.copyOf(array, 200);

                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public static final class Itr implements ShortIterator, ShortConsumer {
            private final ShortSpliterator spliterator;
            private boolean valueReady = false;
            private short nextElement;

            private Itr(OfShort of) {
                this.spliterator = Objects.requireNonNull(of.spliterator());
            }

            @Override
            public void accept(short t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public short nextShort() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        public final class Splitr extends BaseSpliterator<ShortSpliterator> implements ShortSpliterator {
            public Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex,
                          int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex,
                                         int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(short[] array, int index, ShortConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public ShortSpliterator arraySpliterator(short[] array, int offset, int len) {
                return ArrayUtil.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code int} values.
     */
    public static class OfInt extends OfPrimitive<Integer, int[], IntConsumer>
            implements IntConsumer {
        OfInt() { }

        OfInt(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
            }
            else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected int[][] newArrayArray(int size) {
            return new int[size][];
        }

        @Override
        public int[] newArray(int size) {
            return new int[size];
        }

        @Override
        protected int arrayLength(int[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(int[] array,
                                    int from, int to,
                                    IntConsumer consumer) {
            for (int i = from; i < to; i++)
                consumer.accept(array[i]);
        }

        @Override
        public void accept(int i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Integer get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);
            if (spineIndex == 0 && ch == 0)
                return curChunk[(int) index];
            else
                return spine[ch][(int) (index - priorElementCount[ch])];
        }

        @Override
        public PrimitiveIterator.OfInt iterator() {
            return Spliterators.iterator(spliterator());
        }

        public Spliterator.OfInt spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            int[] array = asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            }
            else {
                int[] array2 = Arrays.copyOf(array, 200);
                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public final class Splitr extends BaseSpliterator<Spliterator.OfInt>
                implements Spliterator.OfInt {
            public Splitr(int firstSpineIndex, int lastSpineIndex,
                   int firstSpineElementIndex, int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                  int firstSpineElementIndex, int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(int[] array, int index, IntConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public OfInt arraySpliterator(int[] array, int offset, int len) {
                return Arrays.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code long} values.
     */
    static class OfLong extends OfPrimitive<Long, long[], LongConsumer>
            implements LongConsumer {
        OfLong() { }

        OfLong(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
            }
            else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected long[][] newArrayArray(int size) {
            return new long[size][];
        }

        @Override
        public long[] newArray(int size) {
            return new long[size];
        }

        @Override
        protected int arrayLength(long[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(long[] array,
                                    int from, int to,
                                    LongConsumer consumer) {
            for (int i = from; i < to; i++)
                consumer.accept(array[i]);
        }

        @Override
        public void accept(long i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Long get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);
            if (spineIndex == 0 && ch == 0)
                return curChunk[(int) index];
            else
                return spine[ch][(int) (index - priorElementCount[ch])];
        }

        @Override
        public PrimitiveIterator.OfLong iterator() {
            return Spliterators.iterator(spliterator());
        }


        public Spliterator.OfLong spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            long[] array = asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            }
            else {
                long[] array2 = Arrays.copyOf(array, 200);
                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public final class Splitr extends BaseSpliterator<Spliterator.OfLong>
                implements Spliterator.OfLong {
            public Splitr(int firstSpineIndex, int lastSpineIndex,
                          int firstSpineElementIndex, int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                         int firstSpineElementIndex, int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(long[] array, int index, LongConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public OfLong arraySpliterator(long[] array, int offset, int len) {
                return Arrays.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code double} values.
     */
    public static class OfChar extends OfPrimitive<Character, char[], CharConsumer> implements CharConsumer {
        public OfChar() { }

        public OfChar(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Character> consumer) {
            if (consumer instanceof CharConsumer) {
                forEach((CharConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected char[][] newArrayArray(int size) {
            return new char[size][];
        }

        @Override
        public char[] newArray(int size) {
            return new char[size];
        }

        @Override
        protected int arrayLength(char[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(char[] array, int from, int to, CharConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(char i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Character get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);

            if (spineIndex == 0 && ch == 0) {
                return curChunk[(int) index];
            } else {
                return spine[ch][(int) (index - priorElementCount[ch])];
            }
        }

        @Override
        public CharIterator iterator() {
            return new Itr(this);
        }

        public CharSpliterator spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            char[] array = asPrimitiveArray();

            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            } else {
                char[] array2 = Arrays.copyOf(array, 200);

                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public static final class Itr implements CharIterator, CharConsumer {
            private final CharSpliterator spliterator;
            private boolean valueReady = false;
            private char nextElement;

            private Itr(OfChar of) {
                this.spliterator = Objects.requireNonNull(of.spliterator());
            }

            @Override
            public void accept(char t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady) {
                    spliterator.tryAdvance(this);
                }

                return valueReady;
            }

            @Override
            public char nextChar() {
                if (!valueReady && !hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        public final class Splitr extends BaseSpliterator<CharSpliterator> implements CharSpliterator {
            public Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex,
                          int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                         int firstSpineElementIndex, int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(char[] array, int index, CharConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public CharSpliterator arraySpliterator(char[] array, int offset, int len) {
                return ArrayUtil.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code double} values.
     */
    public static class OfFloat extends OfPrimitive<Float, float[], FloatConsumer>
            implements FloatConsumer {
        public OfFloat() { }

        public OfFloat(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Float> consumer) {
            if (consumer instanceof FloatConsumer) {
                forEach((FloatConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected float[][] newArrayArray(int size) {
            return new float[size][];
        }

        @Override
        public float[] newArray(int size) {
            return new float[size];
        }

        @Override
        protected int arrayLength(float[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(float[] array, int from, int to, FloatConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(float i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Float get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);

            if (spineIndex == 0 && ch == 0) {
                return curChunk[(int) index];
            } else {
                return spine[ch][(int) (index - priorElementCount[ch])];
            }
        }

        @Override
        public FloatIterator iterator() {
            return new Itr(this);
        }

        public FloatSpliterator spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            float[] array = asPrimitiveArray();

            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            } else {
                float[] array2 = Arrays.copyOf(array, 200);

                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public static final class Itr implements FloatIterator, FloatConsumer {
            private final FloatSpliterator spliterator;
            private boolean valueReady = false;
            private float nextElement;

            private Itr(OfFloat of) {
                this.spliterator = Objects.requireNonNull(of.spliterator());
            }

            @Override
            public void accept(float t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public float nextFloat() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        public final class Splitr extends BaseSpliterator<FloatSpliterator>
                implements FloatSpliterator {
            public Splitr(int firstSpineIndex, int lastSpineIndex,
                          int firstSpineElementIndex, int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                         int firstSpineElementIndex, int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(float[] array, int index, FloatConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public FloatSpliterator arraySpliterator(float[] array, int offset, int len) {
                return ArrayUtil.spliterator(array, offset, offset+len);
            }
        }
    }

    /**
     * An ordered collection of {@code double} values.
     */
    public static class OfDouble
            extends OfPrimitive<Double, double[], DoubleConsumer>
            implements DoubleConsumer {
        public OfDouble() { }

        public OfDouble(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
            }
            else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        protected double[][] newArrayArray(int size) {
            return new double[size][];
        }

        @Override
        public double[] newArray(int size) {
            return new double[size];
        }

        @Override
        protected int arrayLength(double[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(double[] array,
                                    int from, int to,
                                    DoubleConsumer consumer) {
            for (int i = from; i < to; i++)
                consumer.accept(array[i]);
        }

        @Override
        public void accept(double i) {
            preAccept();
            curChunk[elementIndex++] = i;
        }

        public Double get(long index) {
            // Casts to int are safe since the spine array index is the index minus
            // the prior element count from the current spine
            int ch = chunkFor(index);
            if (spineIndex == 0 && ch == 0)
                return curChunk[(int) index];
            else
                return spine[ch][(int) (index - priorElementCount[ch])];
        }

        @Override
        public PrimitiveIterator.OfDouble iterator() {
            return Spliterators.iterator(spliterator());
        }

        public Spliterator.OfDouble spliterator() {
            return new Splitr(0, spineIndex, 0, elementIndex);
        }

        @Override
        public String toString() {
            double[] array = asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array));
            }
            else {
                double[] array2 = Arrays.copyOf(array, 200);
                return String.format("%s[length=%d, chunks=%d]%s...",
                        getClass().getSimpleName(), array.length,
                        spineIndex, Arrays.toString(array2));
            }
        }

        public final class Splitr extends BaseSpliterator<Spliterator.OfDouble> implements Spliterator.OfDouble {
            public Splitr(int firstSpineIndex, int lastSpineIndex,
                          int firstSpineElementIndex, int lastSpineElementFence) {
                super(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                         int firstSpineElementIndex, int lastSpineElementFence) {
                return new Splitr(firstSpineIndex, lastSpineIndex,
                        firstSpineElementIndex, lastSpineElementFence);
            }

            @Override
            public void arrayForOne(double[] array, int index, DoubleConsumer consumer) {
                consumer.accept(array[index]);
            }

            @Override
            public OfDouble arraySpliterator(double[] array, int offset, int len) {
                return Arrays.spliterator(array, offset, offset+len);
            }
        }
    }
}

