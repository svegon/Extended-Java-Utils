package svegon.utils.optional;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2DoubleFunction;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import svegon.utils.interfaces.function.Object2ByteFunction;
import svegon.utils.interfaces.function.Object2CharFunction;
import svegon.utils.interfaces.function.Object2FloatFunction;
import svegon.utils.interfaces.function.Object2ShortFunction;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class OptionalUtil {
    private OptionalUtil() {
        throw new UnsupportedOperationException();
    }

    public static <T> OptionalBoolean mapToBoolean(Optional<T> optional, Object2BooleanFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalBoolean.empty() : OptionalBoolean.of(mapper.apply(optional.get()));
    }

    public static <T> OptionalByte mapToByte(Optional<T> optional, Object2ByteFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalByte.empty() : OptionalByte.of(mapper.apply(optional.get()));
    }

    public static <T> OptionalShort mapToShort(Optional<T> optional, Object2ShortFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalShort.empty() : OptionalShort.of(mapper.apply(optional.get()));
    }

    public static <T> OptionalInt mapToInt(Optional<T> optional, Object2IntFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalInt.empty() : OptionalInt.of(mapper.applyAsInt(optional.get()));
    }

    public static <T> OptionalLong mapToLong(Optional<T> optional, Object2LongFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalLong.empty() : OptionalLong.of(mapper.applyAsLong(optional.get()));
    }

    public static <T> OptionalChar mapToChar(Optional<T> optional, Object2CharFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalChar.empty() : OptionalChar.of(mapper.apply(optional.get()));
    }

    public static <T> OptionalFloat mapToFloat(Optional<T> optional, Object2FloatFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalFloat.empty() : OptionalFloat.of(mapper.apply(optional.get()));
    }

    public static <T> OptionalDouble mapToDouble(Optional<T> optional, Object2DoubleFunction<? super T> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(mapper.applyAsDouble(optional.get()));
    }

    public static <T> OptionalBoolean flatMapToBoolean(Optional<T> optional,
                                                       Function<? super T, ? extends OptionalBoolean> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalBoolean.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalByte flatMapToByte(Optional<T> optional,
                                                 Function<? super T, ? extends OptionalByte> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalByte.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalShort flatMapToShort(Optional<T> optional,
                                                   Function<? super T, ? extends OptionalShort> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalShort.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalInt flatMapToInt(Optional<T> optional,
                                                   Function<? super T, ? extends OptionalInt> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalInt.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalLong flatMapToLong(Optional<T> optional,
                                                   Function<? super T, ? extends OptionalLong> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalLong.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalChar flatMapToChar(Optional<T> optional,
                                                   Function<? super T, ? extends OptionalChar> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalChar.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalFloat flatMapToFloat(Optional<T> optional,
                                                   Function<? super T, ? extends OptionalFloat> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalFloat.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }

    public static <T> OptionalDouble flatMapToDouble(Optional<T> optional,
                                                   Function<? super T, ? extends OptionalDouble> mapper) {
        Preconditions.checkNotNull(mapper);
        return optional.isEmpty() ? OptionalDouble.empty()
                : Preconditions.checkNotNull(mapper.apply(optional.get()));
    }
}
