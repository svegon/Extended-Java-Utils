package svegon.utils.vector;

import it.unimi.dsi.fastutil.floats.FloatFloatPair;

public final class Vec2f extends Vecnt<float[], Vec2f> implements FloatFloatPair {
    public static final Vec2f ZERO = new Vec2f(0, 0);
    public static final Vec2f MIN = new Vec2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    public static final Vec2f MAX = new Vec2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    public static final Vec2f NAN = new Vec2f(Float.NaN, Float.NaN);

    private final float x;
    private final float y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vec2f add(float x, float y) {
        return new Vec2f(getX() + x, getY() + y);
    }

    public Vec2f multiplyEach(float x, float y) {
        return new Vec2f(getX() * x, getY() * y);
    }

    @Override
    public float[] toArray() {
        return new float[]{getX(), getY()};
    }

    @Override
    public float[] toArray(float[] floats) {
        if (floats.length < 2) {
            return toArray();
        }

        floats[0] = getX();
        floats[1] = getY();

        return floats;
    }

    @Override
    public Vec2f add(Vec2f other) {
        return add(other.getX(), other.getY());
    }

    @Override
    public Vec2f substract(Vec2f other) {
        return add(-other.getX(), -other.getY());
    }

    @Override
    public Vec2f multiply(double multiplier) {
        return new Vec2f((float) (getX() * multiplier), (float) (getY() * multiplier));
    }

    @Override
    public Vec2f multiplyEach(Vec2f other) {
        return multiplyEach(other.getX(), other.getY());
    }

    @Override
    public Vec2f divide(double divider) {
        return new Vec2f((float) (getX() / divider), (float) (getY() / divider));
    }

    @Override
    public Vec2f neg() {
        return new Vec2f(-getX(), -getY());
    }

    @Override
    public float leftFloat() {
        return getX();
    }

    @Override
    public float rightFloat() {
        return getY();
    }

    @Override
    public String toString() {
        return getX() + " " + getY();
    }
}
