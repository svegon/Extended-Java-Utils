package com.github.svegon.utils.vector;

import it.unimi.dsi.fastutil.ints.IntIntPair;

public class Vec2i extends Vecnt<int[], Vec2i> implements IntIntPair {
    public static final Vec2i ZERO = new Vec2i(0, 0);
    public static final Vec2i MIN = new Vec2i(Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final Vec2i MAX = new Vec2i(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int x;
    private final int y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Vec2i add(int x, int y) {
        return new Vec2i(getX() + x, getY() + y);
    }

    public Vec2i multiplyEach(int x, int y) {
        return new Vec2i(getX() * x, getY() * y);
    }

    @Override
    public int[] toArray() {
        return new int[]{getX(), getY()};
    }

    @Override
    public int[] toArray(int[] ints) {
        if (ints.length < 2) {
            return toArray();
        }

        ints[0] = getX();
        ints[1] = getY();

        return ints;
    }

    @Override
    public Vec2i add(Vec2i other) {
        return add(other.getX(), other.getY());
    }

    @Override
    public Vec2i substract(Vec2i other) {
        return add(-other.getX(), -other.getY());
    }

    @Override
    public Vec2i multiply(double multiplier) {
        return new Vec2i((int) (getX() * multiplier), (int) (getY() * multiplier));
    }

    @Override
    public Vec2i multiplyEach(Vec2i other) {
        return multiplyEach(other.getX(), other.getY());
    }

    @Override
    public Vec2i divide(double divider) {
        return new Vec2i((int) (getX() / divider), (int) (getY() / divider));
    }

    @Override
    public Vec2i neg() {
        return new Vec2i(-getX(), -getY());
    }

    @Override
    public int leftInt() {
        return getX();
    }

    @Override
    public int rightInt() {
        return getY();
    }

    @Override
    public String toString() {
        return getX() + " " + getY();
    }
}
