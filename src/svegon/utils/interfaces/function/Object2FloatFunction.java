package svegon.utils.interfaces.function;

import java.util.function.Function;

@FunctionalInterface
public interface Object2FloatFunction<T> extends Function<T, Float> {
    float applyToFloat(T o);

    @Override
    default Float apply(T t) {
        return applyToFloat(t);
    }
}
