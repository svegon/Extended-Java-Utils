package com.github.svegon.utils.collections.spliterator;

import it.unimi.dsi.fastutil.booleans.AbstractBooleanSpliterator;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterators;
import it.unimi.dsi.fastutil.bytes.AbstractByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterators;
import it.unimi.dsi.fastutil.chars.AbstractCharSpliterator;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.chars.CharSpliterator;
import it.unimi.dsi.fastutil.chars.CharSpliterators;
import it.unimi.dsi.fastutil.floats.*;
import it.unimi.dsi.fastutil.shorts.AbstractShortSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import it.unimi.dsi.fastutil.shorts.ShortSpliterator;
import it.unimi.dsi.fastutil.shorts.ShortSpliterators;

import java.util.Objects;
import java.util.Spliterator;

/**
 * @since 1.0.0
 */
public final class SpliteratorUtil {
    private SpliteratorUtil() {
        throw new AssertionError();
    }

    public static BooleanSpliterator emptyBooleanSpliterator() {
        return BooleanSpliterators.EMPTY_SPLITERATOR;
    }

    public static ByteSpliterator emptyByteSpliterator() {
        return ByteSpliterators.EMPTY_SPLITERATOR;
    }

    public static ShortSpliterator emptyShortSpliterator() {
        return ShortSpliterators.EMPTY_SPLITERATOR;
    }

    public static CharSpliterator emptyCharSpliterator() {
        return CharSpliterators.EMPTY_SPLITERATOR;
    }

    public static FloatSpliterator emptyFloatSpliterator() {
        return FloatSpliterators.EMPTY_SPLITERATOR;
    }

    public abstract static class BooleanSpliteratorImpl extends AbstractBooleanSpliterator {
        public static final int MAX_BATCH = 1 << 10;
        public static final int BATCH_UNIT = 1 << 25;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected BooleanSpliteratorImpl(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED : additionalCharacteristics;
        }

        public static final class HoldingBooleanConsumer implements BooleanConsumer {
            public boolean value;

            @Override
            public void accept(boolean value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public BooleanSpliterator trySplit() {
            HoldingBooleanConsumer holder = new HoldingBooleanConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                boolean[] a = new boolean[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return BooleanSpliterators.wrap(a, 0, j, characteristics());
            }

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    public abstract static class ByteSpliteratorImpl extends AbstractByteSpliterator {
        public static final int MAX_BATCH = 1 << 10;
        public static final int BATCH_UNIT = 1 << 25;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected ByteSpliteratorImpl(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED : additionalCharacteristics;
        }

        public static final class HoldingByteConsumer implements ByteConsumer {
            public byte value;

            @Override
            public void accept(byte value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public ByteSpliterator trySplit() {
            HoldingByteConsumer holder = new HoldingByteConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                byte[] a = new byte[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return ByteSpliterators.wrap(a, 0, j, characteristics());
            }

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    public abstract static class ShortSpliteratorImpl extends AbstractShortSpliterator {
        public static final int MAX_BATCH = 1 << 10;
        public static final int BATCH_UNIT = 1 << 25;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected ShortSpliteratorImpl(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        public static final class HoldingShortConsumer implements ShortConsumer {
            public short value;

            @Override
            public void accept(short value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public ShortSpliterator trySplit() {
            HoldingShortConsumer holder = new HoldingShortConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                short[] a = new short[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return ShortSpliterators.wrap(a, 0, j, characteristics());
            }

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    public abstract static class CharSpliteratorImpl extends AbstractCharSpliterator {
        public static final int MAX_BATCH = 1 << 10;
        public static final int BATCH_UNIT = 1 << 25;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected CharSpliteratorImpl(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        public static final class HoldingCharConsumer implements CharConsumer {
            public char value;

            @Override
            public void accept(char value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public CharSpliterator trySplit() {
            HoldingCharConsumer holder = new HoldingCharConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                char[] a = new char[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return CharSpliterators.wrap(a, 0, j, characteristics());
            }

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    public abstract static class FloatSpliteratorImpl extends AbstractFloatSpliterator {
        public static final int MAX_BATCH = 1 << 10;
        public static final int BATCH_UNIT = 1 << 25;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected FloatSpliteratorImpl(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        public static final class HoldingFloatConsumer implements FloatConsumer {
            public float value;

            @Override
            public void accept(float value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public FloatSpliterator trySplit() {
            HoldingFloatConsumer holder = new HoldingFloatConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                float[] a = new float[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new FloatArraySpliterator(a, 0, j, characteristics());
            }

            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    public static final class FloatArraySpliterator implements FloatSpliterator {
        private final float[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public FloatArraySpliterator(float[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public FloatArraySpliterator(float[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    | Spliterator.ORDERED | Spliterator.NONNULL;
        }

        @Override
        public FloatSpliterator trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid) ? null : new FloatArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(FloatConsumer action) {
            Objects.requireNonNull(action);

            float[] a; int i, hi; // hoist accesses and checks from loop

            if ((a = array).length >= (hi = fence) && (i = index) >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(FloatConsumer action) {
            Objects.requireNonNull(action);

            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() { return fence - index; }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public FloatComparator getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;

            throw new IllegalStateException();
        }
    }
}
