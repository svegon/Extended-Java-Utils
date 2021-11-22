package svegon.utils.math.really_big_math;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Pair;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import svegon.utils.annotations.ToDo;

@ToDo
@Immutable
public final class ComplexNumber extends InfiniNumber {
    public static final ComplexNumber ONE = new ComplexNumber(InfiniFloat.ONE, InfiniFloat.ZERO);

    private final InfiniFloat realPart;
    private final InfiniFloat imagPart;

    ComplexNumber(InfiniFloat realPart, InfiniFloat imagPart) {
        this.realPart = realPart;
        this.imagPart = imagPart;
    }

    ComplexNumber(InfiniFloat realPart) {
        this(realPart, InfiniFloat.ZERO);
    }

        @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @NotNull InfiniNumber add(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber substract(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber mul(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber div(@NotNull Number divider) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber floorDiv(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber pow(@NotNull Number exp) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber log() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber mod(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull Pair<InfiniNumber, InfiniNumber> divMod(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber lShift(long by) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber rShift(long by) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber and(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber or(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber xor(@NotNull Number other) {
        return null;
    }

    @Override
    public @NotNull InfiniNumber invert() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber sign() {
        return 0;
    }

    @Override
    public @NotNull InfiniNumber abs() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber neg() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber floor() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber ceil() {
        return null;
    }

    @Override
    public @NotNull InfiniNumber round(long decimalLength) {
        return null;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public int compareTo(@NotNull Number o) {
        return 0;
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }

    public static ComplexNumber valueOf(InfiniFloat real, InfiniFloat imag) {
        return new ComplexNumber(Preconditions.checkNotNull(real), Preconditions.checkNotNull(imag));
    }

    public static ComplexNumber valueOf(InfiniFloat real) {
        return new ComplexNumber(Preconditions.checkNotNull(real), InfiniFloat.ZERO);
    }
}
