package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~6mins
Target patch: Reduce-array-allocations.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class CachedEnumValuesForLoop {

    // 10 enums
    enum MockEnum {
        A, B, C, D, E, F, G, H, I, J
    }

    // Simulate different call density of scenarios
    @Param({"1000", "50000"})
    private static int ITERATE_COUNT;

    private static final List<MockEnum> valuesList = List.of(MockEnum.values());
    private static final MockEnum[] valuesArray = MockEnum.values();

    @Benchmark
    public void valuesIterating(Blackhole bh) {
        for (int i = 0; i < ITERATE_COUNT; i++) {
            for (MockEnum o : MockEnum.values()) {
                bh.consume(o);
            }
        }
    }

    @Benchmark
    public void valuesListIterating(Blackhole bh) {
        for (int i = 0; i < ITERATE_COUNT; i++) {
            for (MockEnum o : valuesList) {
                bh.consume(o);
            }
        }
    }

    @Benchmark
    public void valuesArrayIterating(Blackhole bh) {
        for (int i = 0; i < ITERATE_COUNT; i++) {
            for (MockEnum o : valuesArray) {
                bh.consume(o);
            }
        }
    }
}
