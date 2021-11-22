package svegon.utils.vector;

public class Vec3i extends Vecnt<int[], Vec3i> {
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    public static final Vec3i MIN = new Vec3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final Vec3i MAX = new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int x;
    private final int y;
    private final int z;

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vec3i add(int x, int y, int z) {
        return new Vec3i(getX() + x, getY() + y, getZ() + z);
    }

    public Vec3i multiplyEach(int x, int y, int z) {
        return new Vec3i(getX() * x, getY() * y, getZ() * z);
    }

    @Override
    public int[] toArray() {
        return new int[]{getX(), getY(), getZ()};
    }

    @Override
    public int[] toArray(int[] ints) {
        if (ints.length < 3) {
            return toArray();
        }

        ints[0] = getX();
        ints[1] = getY();
        ints[2] = getZ();

        return ints;
    }

    @Override
    public Vec3i add(Vec3i other) {
        return add(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3i substract(Vec3i other) {
        return add(-other.getX(), -other.getY(), -other.getZ());
    }

    @Override
    public Vec3i multiply(double multiplier) {
        return new Vec3i((int) (getX() * multiplier), (int) (getY() * multiplier), (int) (getZ() * multiplier));
    }

    @Override
    public Vec3i multiplyEach(Vec3i other) {
        return multiplyEach(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3i divide(double divider) {
        return new Vec3i((int) (getX() / divider), (int) (getY() / divider), (int) (getZ() / divider));
    }

    @Override
    public Vec3i neg() {
        return new Vec3i(-getX(), -getY(), -getZ());
    }

    @Override
    public String toString() {
        return getX() + " " + getY() + " " + getZ();
    }
}
