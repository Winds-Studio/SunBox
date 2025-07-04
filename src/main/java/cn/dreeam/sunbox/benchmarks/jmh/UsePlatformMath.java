package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~12mins
Target patch: Use-platform-math-functions.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class UsePlatformMath {

    private static float floatValue;
    private static double doubleValue;
    private static double doubleX;
    private static double doubleY;

    @Setup(Level.Iteration)
    public void setup() {
        floatValue = new Random().nextFloat();
        doubleValue = new Random().nextDouble();
        doubleX =  new Random().nextDouble();
        doubleY = new Random().nextDouble();
    }

    @Benchmark
    public static int floorFloatBefore() {
        return floorFloatBefore0(floatValue);
    }

    @Benchmark
    public static int floorFloatAfter() {
        return floorFloatAfter0(floatValue);
    }

    @Benchmark
    public static int floorDoubleBefore() {
        return floorDoubleBefore0(doubleValue);
    }

    @Benchmark
    public static int floorDoubleAfter() {
        return floorDoubleAfter0(doubleValue);
    }

    @Benchmark
    public static long lfloorBefore() {
        return lfloorBefore0(doubleValue);
    }

    @Benchmark
    public static long lfloorAfter() {
        return lfloorAfter0(doubleValue);
    }

    @Benchmark
    public static int ceilFloatBefore() {
        return ceilFloatBefore0(floatValue);
    }

    @Benchmark
    public static int ceilFloatAfter() {
        return ceilFloatAfter0(floatValue);
    }

    @Benchmark
    public static int ceilDoubleBefore() {
        return ceilDoubleBefore0(doubleValue);
    }

    @Benchmark
    public static int ceilDoubleAfter() {
        return ceilDoubleAfter0(doubleValue);
    }

    @Benchmark
    public static double absMaxBefore() {
        return absMaxBefore0(doubleX, doubleY);
    }

    @Benchmark
    public static double absMaxAfter() {
        return absMaxAfter0(doubleX, doubleY);
    }

    public static int floorFloatBefore0(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int floorFloatAfter0(float value) {
        return (int) Math.floor(value); // Gale - use platform  functions
    }

    public static int floorDoubleBefore0(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int floorDoubleAfter0(double value) {
        return (int) Math.floor(value); // Gale - use platform  functions
    }

    public static long lfloorBefore0(double value) {
        long l = (long) value;
        return value < l ? l - 1L : l;
    }

    public static long lfloorAfter0(double value) {
        return (long) Math.floor(value); // Gale - use platform  functions
    }

    public static int ceilFloatBefore0(float value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static int ceilFloatAfter0(float value) {
        return (int) Math.ceil(value); // Gale - use platform  functions
    }

    public static int ceilDoubleBefore0(double value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static int ceilDoubleAfter0(double value) {
        return (int) Math.ceil(value); // Gale - use platform  functions
    }

    public static double absMaxBefore0(double x, double y) {
        if (x < 0.0) {
            x = -x;
        }

        if (y < 0.0) {
            y = -y;
        }

        return Math.max(x, y);
    }

    public static double absMaxAfter0(double x, double y) {
        return Math.max(Math.abs(x), Math.abs(y)); // Gale - use platform  functions
    }
}
