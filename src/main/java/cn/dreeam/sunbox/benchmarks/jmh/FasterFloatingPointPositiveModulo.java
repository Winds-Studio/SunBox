package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~4mins
Target patch: Faster-floating-point-positive-modulo.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class FasterFloatingPointPositiveModulo {

    private static float floatNumerator;
    private static float floatDenominator;
    private static double doubleNumerator;
    private static double doubleDenominator;

    @Setup(Level.Iteration)
    public void setup() {
        floatNumerator = new Random().nextFloat();
        floatDenominator = new Random().nextFloat();
        doubleNumerator =  new Random().nextDouble();
        doubleDenominator = new Random().nextDouble();
    }

    @Benchmark
    public static float floatBefore() {
        return positiveModuloForAnyDenominator(floatNumerator, floatDenominator);
    }

    @Benchmark
    public static float forPositiveIntegerDenominatorFloatAfter() {
        return positiveModuloForPositiveIntegerDenominator(floatNumerator, floatDenominator);
    }

    @Benchmark
    public static double doubleBefore() {
        return positiveModuloForAnyDenominator(doubleNumerator, doubleDenominator);
    }

    @Benchmark
    public static double forPositiveIntegerDenominatorDoubleAfter() {
        return positiveModuloForPositiveIntegerDenominator(doubleNumerator, doubleDenominator);
    }

    public static float positiveModuloForAnyDenominator(float numerator, float denominator) {
        return (numerator % denominator + denominator) % denominator;
    }

    public static double positiveModuloForAnyDenominator(double numerator, double denominator) {
        return (numerator % denominator + denominator) % denominator;
    }

    // Gale start - faster floating-point positive modulo
    public static float positiveModuloForPositiveIntegerDenominator(float numerator, float denominator) {
        var modulo = numerator % denominator;
        return modulo < 0 ? modulo + denominator : modulo;
    }

    public static double positiveModuloForPositiveIntegerDenominator(double numerator, double denominator) {
        var modulo = numerator % denominator;
        return modulo < 0 ? modulo + denominator : modulo;
    }
    // Gale end - faster floating-point positive modulo
}
