package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~4mins
See OptimizeTextColorMapGet as well
Target patch: Optimise-TextColor.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class OptimizeTextColorMisc {

    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private int colorValue;
    private String colorString;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random();
        colorValue = random.nextInt(0xFFFFFF + 1);
        colorString = "#" + String.format("%06X", colorValue); // e.g. "#FFAABB"
    }

    @Benchmark
    public String formatValueBefore() {
        return formatValueBefore0(colorValue);
    }

    @Benchmark
    public String formatValueAfter() {
        return formatValueAfter0(colorValue);
    }

    @Benchmark
    public boolean parseColorBefore() {
        return colorString.startsWith("#");
    }

    @Benchmark
    public boolean parseColorAfter() {
        return colorString.charAt(0) == '#';
    }

    private String formatValueBefore0(int value) {
        return String.format(Locale.ROOT, "#%06X", value);
    }

    private String formatValueAfter0(int value) {
        return new String(new char[]{
                '#',
                HEX_DIGITS[(value >> 20) & 0xF],
                HEX_DIGITS[(value >> 16) & 0xF],
                HEX_DIGITS[(value >> 12) & 0xF],
                HEX_DIGITS[(value >> 8) & 0xF],
                HEX_DIGITS[(value >> 4) & 0xF],
                HEX_DIGITS[value & 0xF]
        });
    }
}
