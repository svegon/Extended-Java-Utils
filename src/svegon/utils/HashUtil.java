package svegon.utils;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import java.util.Arrays;

public final class HashUtil {
    private HashUtil() {
        throw new AssertionError();
    }

    private static final Hash.Strategy<Object> DEFAULT_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(Object o) {
            return Arrays.deepHashCode(new Object[]{o});
        }

        @Override
        public boolean equals(Object a, Object b) {
            return Arrays.deepEquals(new Object[]{a}, new Object[]{b});
        }
    };

    private static final Hash.Strategy<Object> IDENTITY_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(Object o) {
            return System.identityHashCode(o);
        }

        @Override
        public boolean equals(Object a, Object b) {
            return a == b;
        }
    };

    private static final Hash.Strategy<String> CASE_IGNORING_STRATEGY = new Hash.Strategy<>() {
        private final Object2IntMap<String> hashCodeCache = new Object2IntOpenCustomHashMap<>(identityStrategy());

        @Override
        public int hashCode(String o) {
            return hashCodeCache.computeIfAbsent(o, (String str) -> {
                if (str == null) {
                    return 0;
                }

                int length = str.length();
                int h = 0;

                for (int i = 0; i < length; i++) {
                    h = 31 * h + Character.toUpperCase(str.codePointAt(i));
                }
                return h;
            });
        }

        @Override
        public boolean equals(String a, String b) {
            return a == b || (a != null && a.equalsIgnoreCase(b));
        }
    };

    public static Hash.Strategy<Object> arrayRegardingStrategy() {
        return DEFAULT_STRATEGY;
    }

    public static Hash.Strategy<Object> identityStrategy() {
        return IDENTITY_STRATEGY;
    }

    public static Hash.Strategy<String> caseIgnoringStringHash() {
        return CASE_IGNORING_STRATEGY;
    }
}
