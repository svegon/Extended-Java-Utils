package svegon.utils.vector;

public class Vec3l extends Vecnt<long[], Vec3l> {
    public static final Vec3l ZERO = new Vec3l(0, 0, 0);
    public static final Vec3l MIN = new Vec3l(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
    public static final Vec3l MAX = new Vec3l(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

    private final long x;
    private final long y;
    private final long z;

    public Vec3l(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getZ() {
        return z;
    }

    public Vec3l add(long x, long y, long z) {
        return new Vec3l(getX() + x, getY() + y, getZ() + z);
    }

    public Vec3l multiplyEach(long x, long y, long z) {
        return new Vec3l(getX() * x, getY() * y, getZ() * z);
    }

    @Override
    public long[] toArray() {
        return new long[]{getX(), getY(), getZ()};
    }

    @Override
    public long[] toArray(long[] longs) {
        if (longs.length < 3) {
            return toArray();
        }

        longs[0] = getX();
        longs[1] = getY();
        longs[2] = getZ();

        return longs;
    }

    @Override
    public Vec3l add(Vec3l other) {
        return add(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3l substract(Vec3l other) {
        return add(-other.getX(), -other.getY(), -other.getZ());
    }

    @Override
    public Vec3l multiply(double multiplier) {
        return new Vec3l((long) (getX() * multiplier), (long) (getY() * multiplier), (long) (getZ() * multiplier));
    }

    @Override
    public Vec3l multiplyEach(Vec3l other) {
        return multiplyEach(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3l divide(double divider) {
        return new Vec3l((long) (getX() / divider), (long) (getY() / divider), (long) (getZ() / divider));
    }

    @Override
    public Vec3l neg() {
        return new Vec3l(-getX(), -getY(), -getZ());
    }

    @Override
    public String toString() {
        return getX() + " " + getY() + " " + getZ();
    }
}
