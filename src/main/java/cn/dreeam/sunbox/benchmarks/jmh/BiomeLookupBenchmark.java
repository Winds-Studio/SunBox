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
    private static final int SIZE = 4096;

    @Setup
    public void setup() {
        inputData = new int[SIZE];
        Random rand = new Random(12345L);
        for (int i = 0; i < SIZE; i++) {
            inputData[i] = rand.nextInt();
        }
    }

    @Benchmark
    public void testFpDivision(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += (double) (inputData[i] & 3) / 4.0;
        }
        bh.consume(sum);
    }

    @Benchmark
    public void testTableLookup(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += QUART_OFFSETS[inputData[i] & 3];
        }
        bh.consume(sum);
    }
}