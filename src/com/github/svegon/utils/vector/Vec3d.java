package com.github.svegon.utils.vector;

public final class Vec3d extends Vecnt<double[], Vec3d> {
    public static final Vec3d ZERO = new Vec3d(0, 0, 0);
    public static final Vec3d MIN = new Vec3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY);
    public static final Vec3d MAX = new Vec3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY);
    public static final Vec3d NAN = new Vec3d(Double.NaN, Double.NaN, Double.NaN);

    private final double x;
    private final double y;
    private final double z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vec3d add(double x, double y, double z) {
        return new Vec3d(getX() + x, getY() + y, getZ() + z);
    }

    public Vec3d multiplyEach(double x, double y, double z) {
        return new Vec3d(getX() * x, getY() * y, getZ() * z);
    }

    @Override
    public double[] toArray() {
        return new double[]{getX(), getY(), getZ()};
    }

    @Override
    public double[] toArray(double[] doubles) {
        if (doubles.length < 3) {
            return toArray();
        }

        doubles[0] = getX();
        doubles[1] = getY();
        doubles[2] = getZ();

        return doubles;
    }

    @Override
    public Vec3d add(Vec3d other) {
        return add(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3d substract(Vec3d other) {
        return add(-other.getX(), -other.getY(), -other.getZ());
    }

    @Override
    public Vec3d multiply(double multiplier) {
        return new Vec3d(getX() * multiplier, getY() * multiplier, getZ() * multiplier);
    }

    @Override
    public Vec3d multiplyEach(Vec3d other) {
        return multiplyEach(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3d divide(double divider) {
        return new Vec3d(getX() / divider, getY() / divider, getZ() / divider);
    }

    @Override
    public Vec3d neg() {
        return new Vec3d(-getX(), -getY(), -getZ());
    }

    @Override
    public String toString() {
        return getX() + " " + getY() + " " + getZ();
    }
}
