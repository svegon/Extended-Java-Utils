package com.github.svegon.utils.vector;

import java.util.Arrays;

public abstract class Vecnt<A, T extends Vecnt<A, T>> {
    public abstract A toArray();

    public abstract A toArray(A a);

    public abstract T add(T other);

    public abstract T substract(T other);

    public abstract T multiply(double mutliplier);

    public abstract T multiplyEach(T other);

    public T divide(double divider) {
        return multiply(1 / divider);
    }

    public T neg() {
        return multiply(-1);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Vecnt<?, ?> vecnt)) {
            return false;
        }

        return Arrays.deepEquals(new Object[]{toArray()}, new Object[]{vecnt.toArray()});
    }

    @Override
    public final int hashCode() {
        return Arrays.deepHashCode(new Object[]{toArray()});
    }
}
