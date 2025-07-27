package cn.dreeam.sunbox.benchmarks.nanoTime;

import java.util.Random;

/*
Target patch: Optimise-TextColor.patch
*/
public class OptimizeTextColor {

    private static final Random RANDOM = new Random();
    private static final int WARMUP_ITERATIONS = 5_000_000;
    private static final int BENCH_ITERATIONS = 10_000_000;

    private static int colorValue;
    private static String colorString;

    public static void run() {
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runOnce(); // JIT warm-up
        }

        System.out.println("Running benchmark...");

        long afterStart = System.nanoTime();
        for (int i = 0; i < BENCH_ITERATIONS; i++) {
            parseColorAfter();
        }
        long afterEnd = System.nanoTime();

        long beforeStart = System.nanoTime();
        for (int i = 0; i < BENCH_ITERATIONS; i++) {
            parseColorBefore();
        }
        long beforeEnd = System.nanoTime();

        System.out.printf("startsWith(\"#\") total: %f ms%n", (beforeEnd - beforeStart) / 1_000_000.0);
        System.out.printf("charAt(0) == '#' total: %f ms%n", (afterEnd - afterStart) /1_000_000.0);
    }

    private static void runOnce() {
        colorValue = RANDOM.nextInt(0xFFFFFF + 1);
        colorString = "#" + String.format("%06X", colorValue);
        parseColorBefore();
        parseColorAfter();
    }

    public static boolean parseColorBefore() {
        return colorString.startsWith("#");
    }

    public static boolean parseColorAfter() {
        return colorString.charAt(0) == '#';
    }
}
