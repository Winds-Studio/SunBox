package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class BiomeLookupBenchmark {

    private static final double[] QUART_OFFSETS = {0.0D, 0.25D, 0.5D, 0.75D};

    private int[] inputData;
    private static final int SIZE = 4096; // Size of input array
    private static final Random rand = new Random(12345L);

    @Setup
    public void setup() {
        inputData = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            inputData[i] = rand.nextInt();
        }
    }

    @Benchmark
    public void testFpDivision(Blackhole bh) {
        for (int i = 0; i < SIZE; i++) {
            int val = inputData[i];
            bh.consume((double) (val & 3) / 4.0);
        }
    }

    @Benchmark
    public void testTableLookup(Blackhole bh) {
        for (int i = 0; i < SIZE; i++) {
            int val = inputData[i];
            bh.consume(QUART_OFFSETS[val & 3]);
        }
    }
}