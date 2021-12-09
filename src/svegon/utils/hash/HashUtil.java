package svegon.utils.hash;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.booleans.BooleanHash;
import it.unimi.dsi.fastutil.bytes.ByteHash;
import it.unimi.dsi.fastutil.chars.CharHash;
import it.unimi.dsi.fastutil.doubles.DoubleHash;
import it.unimi.dsi.fastutil.floats.FloatHash;
import it.unimi.dsi.fastutil.ints.IntHash;
import it.unimi.dsi.fastutil.longs.LongHash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.ShortHash;
import svegon.utils.fast.util.ints.Object2IntOpenCostumWeakHashMap;

import java.util.Arrays;
import java.util.Objects;

public final class HashUtil {
    private HashUtil() {
        throw new AssertionError();
    }

    private static final Hash.Strategy<Object> ARRAY_REGARDING_STRATEGY = new Hash.Strategy<>() {
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
    private static final Hash.Strategy<String> CASE_IGNORING_STRATEGY = new CaseIgnoringStrategy();
    private static final Hash.Strategy<Object> DEFAULT_OBJECT_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(Object o) {
            return Objects.hashCode(o);
        }

        @Override
        public boolean equals(Object a, Object b) {
            return Objects.equals(a, b);
        }
    };
    private static final BooleanHash.Strategy DEFAULT_BOOLEAN_STRATEGY = new BooleanHash.Strategy() {
        @Override
        public int hashCode(boolean e) {
            return Boolean.hashCode(e);
        }

        @Override
        public boolean equals(boolean a, boolean b) {
            return a == b;
        }
    };
    private static final ByteHash.Strategy DEFAULT_BYTE_STRATEGY = new ByteHash.Strategy() {
        @Override
        public int hashCode(byte e) {
            return Byte.hashCode(e);
        }

        @Override
        public boolean equals(byte a, byte b) {
            return a == b;
        }
    };
    private static final ShortHash.Strategy DEFAULT_SHORT_STRATEGY = new ShortHash.Strategy() {
        @Override
        public int hashCode(short e) {
            return Short.hashCode(e);
        }

        @Override
        public boolean equals(short a, short b) {
            return a == b;
        }
    };
    private static final IntHash.Strategy DEFAULT_INT_STRATEGY = new IntHash.Strategy() {
        @Override
        public int hashCode(int e) {
            return Integer.hashCode(e);
        }

        @Override
        public boolean equals(int a, int b) {
            return a == b;
        }
    };
    private static final LongHash.Strategy DEFAULT_LONG_STRATEGY = new LongHash.Strategy() {
        @Override
        public int hashCode(long e) {
            return Long.hashCode(e);
        }

        @Override
        public boolean equals(long a, long b) {
            return a == b;
        }
    };
    private static final CharHash.Strategy DEFAULT_CHAR_STRATEGY =  new CharHash.Strategy() {
        @Override
        public int hashCode(char e) {
            return Character.hashCode(e);
        }

        @Override
        public boolean equals(char a, char b) {
            return a == b;
        }
    };
    private static final FloatHash.Strategy DEFAULT_FLOAT_STRATEGY = new FloatHash.Strategy() {
        @Override
        public int hashCode(float e) {
            return Float.hashCode(e);
        }

        @Override
        public boolean equals(float a, float b) {
            return Float.compare(a, b) == 0;
        }
    };
    private static final DoubleHash.Strategy DEFAULT_DOUBLE_STRATEGY = new DoubleHash.Strategy() {
        @Override
        public int hashCode(double e) {
            return Double.hashCode(e);
        }

        @Override
        public boolean equals(double a, double b) {
            return Double.compare(a, b) == 0;
        }
    };

    public static Hash.Strategy<Object> defaultStrategy() {
        return DEFAULT_OBJECT_STRATEGY;
    }

    public static BooleanHash.Strategy defaultBooleanStrategy() {
        return DEFAULT_BOOLEAN_STRATEGY;
    }

    public static ByteHash.Strategy defaultByteStrategy() {
        return DEFAULT_BYTE_STRATEGY;
    }

    public static ShortHash.Strategy defaultShortStrategy() {
        return DEFAULT_SHORT_STRATEGY;
    }

    public static IntHash.Strategy defaultIntStrategy() {
        return DEFAULT_INT_STRATEGY;
    }

    public static LongHash.Strategy defaultLongStrategy() {
        return DEFAULT_LONG_STRATEGY;
    }

    public static CharHash.Strategy defaultCharStrategy() {
        return DEFAULT_CHAR_STRATEGY;
    }

    public static FloatHash.Strategy defaultFloatStrategy() {
        return DEFAULT_FLOAT_STRATEGY;
    }

    public static DoubleHash.Strategy defaultDoubleStrategy() {
        return DEFAULT_DOUBLE_STRATEGY;
    }

    public static Hash.Strategy<Object> arrayRegardingStrategy() {
        return ARRAY_REGARDING_STRATEGY;
    }

    public static Hash.Strategy<Object> identityStrategy() {
        return IDENTITY_STRATEGY;
    }

    public static Hash.Strategy<String> caseIgnoringStringHash() {
        return CASE_IGNORING_STRATEGY;
    }

    private static final class CaseIgnoringStrategy implements Hash.Strategy<String> {
        final Object2IntMap<String> hashCodeCache = new Object2IntOpenCostumWeakHashMap<>(identityStrategy());

        private CaseIgnoringStrategy() {
            hashCodeCache.put(null, 0);
        }

        @Override
        public int hashCode(String o) {
            return hashCodeCache.computeIfAbsent(o, (String str) -> {
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
    }
}
