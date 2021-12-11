package com.github.svegon.utils.vector;

public class Vec3f extends Vecnt<float[], Vec3f> {
    public static final Vec3f ZERO = new Vec3f(0, 0, 0);
    public static final Vec3f MIN = new Vec3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY);
    public static final Vec3f MAX = new Vec3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY);
    public static final Vec3f NAN = new Vec3f(Float.NaN, Float.NaN, Float.NaN);

    private final float x;
    private final float y;
    private final float z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Vec3f add(float x, float y, float z) {
        return new Vec3f(getX() + x, getY() + y, getZ() + z);
    }

    public Vec3f multiplyEach(float x, float y, float z) {
        return new Vec3f(getX() * x, getY() * y, getZ() * z);
    }

    @Override
    public float[] toArray() {
        return new float[]{getX(), getY(), getZ()};
    }

    @Override
    public float[] toArray(float[] floats) {
        if (floats.length < 3) {
            return toArray();
        }

        floats[0] = getX();
        floats[1] = getY();
        floats[2] = getZ();

        return floats;
    }

    @Override
    public Vec3f add(Vec3f other) {
        return add(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3f substract(Vec3f other) {
        return add(-other.getX(), -other.getY(), -other.getZ());
    }

    @Override
    public Vec3f multiply(double multiplier) {
        return new Vec3f((float) (getX() * multiplier), (float) (getY() * multiplier), (float) (getZ() * multiplier));
    }

    @Override
    public Vec3f multiplyEach(Vec3f other) {
        return multiplyEach(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public Vec3f divide(double divider) {
        return new Vec3f((float) (getX() / divider), (float) (getY() / divider), (float) (getZ() / divider));
    }

    @Override
    public Vec3f neg() {
        return new Vec3f(-getX(), -getY(), -getZ());
    }

    @Override
    public String toString() {
        return getX() + " " + getY() + " " + getZ();
    }
}
