package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~4mins
Target patch: Cache-chunk-key.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class CacheChunkKey {

    private class MockChunkPos {

        private final int x;
        private final int z;
        private final long longKey; // Paper

        public MockChunkPos(int x, int y) {
            this.x = x;
            this.z = y;
            this.longKey = asLong(this.x, this.z); // Paper // Leaf - Cache chunk key - diff on change
        }

        private static long asLong(int x, int z) {
            return x & 4294967295L | (z & 4294967295L) << 32; // Leaf - Cache chunk key - diff on change
        }
    }

    private MockChunkPos mockChunkPos;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random(12345); // FIXED SEED DO NOT CHANGE
        mockChunkPos = new MockChunkPos(random.nextInt(), random.nextInt());
    }

    @Benchmark
    public long getChunkKeySingleCallBefore() {
        return getChunkKey(mockChunkPos);
    }

    @Benchmark
    public long getChunkKeySingleCallAfter() {
        return mockChunkPos.longKey;
    }

    @Benchmark
    public void getChunkKeyBefore(Blackhole bh) {
        for (int i = 0; i < 50_000; i++) {
            bh.consume(getChunkKey(mockChunkPos));
        }
    }

    @Benchmark
    public void getChunkKeyAfter(Blackhole bh) {
        for (int i = 0; i < 50_000; i++) {
            bh.consume(mockChunkPos.longKey);
        }
    }

    public static long getChunkKey(final MockChunkPos pos) {
        return ((long) pos.z << 32) | (pos.x & 0xFFFFFFFFL); // Leaf - Cache chunk key
    }
}
