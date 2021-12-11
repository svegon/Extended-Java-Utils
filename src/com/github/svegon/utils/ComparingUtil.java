package com.github.svegon.utils;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2FloatFunction;

import java.util.Comparator;
import java.util.function.Predicate;

public final class ComparingUtil {
    private ComparingUtil() {
        throw new AssertionError();
    }

    // comparing as true > false
    public static <T> Comparator<T> comparingBoolean(final Predicate<T> keyExtractor) {
        Preconditions.checkNotNull(keyExtractor);
        return (o1, o2) -> Boolean.compare(keyExtractor.test(o1), keyExtractor.test(o2));
    }

    public static <T> Comparator<T> comparingFloat(final Object2FloatFunction<T> keyExtractor) {
        Preconditions.checkNotNull(keyExtractor);
        return (o1, o2) -> Float.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
    }
}
