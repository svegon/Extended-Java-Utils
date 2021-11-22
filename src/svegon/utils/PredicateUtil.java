package svegon.utils;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import it.unimi.dsi.fastutil.bytes.ByteArraySet;
import it.unimi.dsi.fastutil.bytes.BytePredicate;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteSets;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatPredicate;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortPredicate;
import it.unimi.dsi.fastutil.shorts.ShortSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public final class PredicateUtil {
    private PredicateUtil() {
        throw new AssertionError();
    }

    private static final Predicate<String> INTEGER_PREDICATE = s -> {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    };

    public static <T> Predicate<T> duplicationPredicate() {
        return new Predicate<>() {
            private final Set<T> presentElements = new HashSet<>();

            @Override
            public boolean test(T o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static BooleanPredicate booleanDuplicationPredicate() {
        return new BooleanPredicate() {
            boolean _false;
            boolean _true;

            @Override
            public boolean test(boolean bl) {
                if (bl) {
                    if (_true) {
                        return true;
                    } else {
                        _true = true;
                        return false;
                    }
                } else {
                    if (_false) {
                        return true;
                    } else {
                        _false = true;
                        return false;
                    }
                }
            }
        };
    }

    public static BytePredicate byteDuplicationPredicate() {
        return new BytePredicate() {
            private final ByteSet presentElements = new ByteArraySet(256);

            @Override
            public boolean test(byte o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static ShortPredicate shortDuplicationPredicate() {
        return new ShortPredicate() {
            private final ShortSet presentElements = new ShortOpenHashSet();

            @Override
            public boolean test(short o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static IntPredicate intDuplicationPredicate() {
        return new IntPredicate() {
            private final IntSet presentElements = new IntOpenHashSet();

            @Override
            public boolean test(int o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static LongPredicate longDuplicationPredicate() {
        return new LongPredicate() {
            private final LongSet presentElements = new LongOpenHashSet();

            @Override
            public boolean test(long o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static CharPredicate charDuplicationPredicate() {
        return new CharPredicate() {
            private final CharSet presentElements = new CharOpenHashSet();

            @Override
            public boolean test(char o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static FloatPredicate floatDuplicationPredicate() {
        return new FloatPredicate() {
            private final FloatSet presentElements = new FloatOpenHashSet();

            @Override
            public boolean test(float o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static DoublePredicate doubleDuplicationPredicate() {
        return new DoublePredicate() {
            private final DoubleSet presentElements = new DoubleOpenHashSet();

            @Override
            public boolean test(double o) {
                if (presentElements.contains(o)) {
                    return true;
                }

                presentElements.add(o);
                return false;
            }
        };
    }

    public static <T> Predicate<T> uniquenessPredicate() {
        return new Predicate<>() {
            private final Set<T> presentElements = Sets.newHashSet();

            @Override
            public boolean test(T o) {
                if (presentElements.contains(o)) {
                    return false;
                }

                presentElements.add(o);
                return true;
            }
        };
    }

    public static CharPredicate charUniquenessPredicate() {
        return new CharPredicate() {
            private final CharSet presentElements = new CharOpenHashSet();

            @Override
            public boolean test(char o) {
                if (presentElements.contains(o)) {
                    return false;
                }

                presentElements.add(o);
                return true;
            }
        };
    }

    public static Predicate<String> integerPredicate() {
        return INTEGER_PREDICATE;
    }
}
