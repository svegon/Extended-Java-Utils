package svegon.utils.vector;

import it.unimi.dsi.fastutil.longs.LongLongPair;

public final class Vec2l extends Vecnt<long[], Vec2l> implements LongLongPair {
    public static final Vec2l ZERO = new Vec2l(0L, 0L);
    public static final Vec2l MIN = new Vec2l(Long.MIN_VALUE, Long.MIN_VALUE);
    public static final Vec2l MAX = new Vec2l(Long.MAX_VALUE, Long.MAX_VALUE);

    private final long x;
    private final long y;

    public Vec2l(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public Vec2l add(long x, long y) {
        return new Vec2l(getX() + x, getY() + y);
    }

    public Vec2l multiplyEach(long x, long y) {
        return new Vec2l(getX() * x, getY() * y);
    }

    @Override
    public long[] toArray() {
        return new long[]{getX(), getY()};
    }

    @Override
    public long[] toArray(long[] longs) {
        if (longs.length < 2) {
            return toArray();
        }

        longs[0] = getX();
        longs[1] = getY();

        return longs;
    }

    @Override
    public Vec2l add(Vec2l other) {
        return add(other.getX(), other.getY());
    }

    @Override
    public Vec2l substract(Vec2l other) {
        return add(-other.getX(), -other.getY());
    }

    @Override
    public Vec2l multiply(double multiplier) {
        return new Vec2l((long) (getX() * multiplier), (long) (getY() * multiplier));
    }

    @Override
    public Vec2l multiplyEach(Vec2l other) {
        return multiplyEach(other.getX(), other.getY());
    }

    @Override
    public Vec2l divide(double divider) {
        return new Vec2l((long) (getX() / divider), (long) (getY() / divider));
    }

    @Override
    public Vec2l neg() {
        return new Vec2l(-getX(), -getY());
    }

    @Override
    public long leftLong() {
        return getX();
    }

    @Override
    public long rightLong() {
        return getY();
    }

    @Override
    public String toString() {
        return getX() + " " + getY();
    }
}
