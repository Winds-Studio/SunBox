package cn.dreeam.sunbox.benchmarks.jmh;

import cn.dreeam.sunbox.util.OptimizedNonNullListArrayList;
import com.google.common.collect.Lists;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
Estimate running time: ~15-20 mins
Target patch: Custom-NonNullList.patch

This benchmark includes tests for NonNullList.get() operations:
1. SingleOp: Microbenchmark measuring single get operations
2. Sequential: Sequential access patterns (cache-friendly)
3. Random: Random access patterns (cache-unfriendly)
4. Intensive: High-volume operations with different list sizes
5. Mixed: Mixed access patterns simulating real-world usage

If it takes too long use the values below:

    private static void runJMH() throws Exception {
        Options opt = new OptionsBuilder()
                .include("NonNullListGetBenchmark")
                .forks(1)
                .warmupIterations(3)
                .measurementIterations(10)
                .warmupTime(TimeValue.seconds(2))
                .measurementTime(TimeValue.seconds(2))
                .build();

        new Runner(opt).run();
    }

*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class NonNullListGetBenchmark {

    // Test different list sizes to stress different scenarios
    private static final int SMALL_SIZE = 36;      // Typical inventory size
    private static final int MEDIUM_SIZE = 256;    // Larger container
    private static final int LARGE_SIZE = 1024;    // Very large container
    private static final int HUGE_SIZE = 4096;     // Stress test size

    // Number of operations for intensive tests
    private static final int OPERATIONS_COUNT = 10_000;

    // Mock item class to simulate Minecraft ItemStack
    private static class MockItem {
        private final String name;
        private final int count;
        private final int durability;

        MockItem(String name, int count, int durability) {
            this.name = name;
            this.count = count;
            this.durability = durability;
        }

        @Override
        public String toString() {
            return name + ":" + count + ":" + durability;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof MockItem)) return false;
            MockItem other = (MockItem) obj;
            return count == other.count &&
                    durability == other.durability &&
                    Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, count, durability);
        }
    }

    // Optimized NonNullList implementation
    public static class OptimizedNonNullList<E> extends AbstractList<E> {
        private final OptimizedNonNullListArrayList<E> list;
        @Nullable
        private final E defaultValue;

        public static <E> OptimizedNonNullList<E> create() {
            return new OptimizedNonNullList<>(new OptimizedNonNullListArrayList<>(null), null);
        }

        public static <E> OptimizedNonNullList<E> createWithCapacity(int initialCapacity) {
            return new OptimizedNonNullList<>(new OptimizedNonNullListArrayList<>(initialCapacity, null), null);
        }

        public static <E> OptimizedNonNullList<E> withSize(int size, E defaultValue) {
            Objects.requireNonNull(defaultValue, "Default value cannot be null");

            @SuppressWarnings("unchecked")
            E[] objects = (E[])new Object[size];
            Arrays.fill(objects, defaultValue);
            return new OptimizedNonNullList<>(new OptimizedNonNullListArrayList<>(objects, defaultValue), defaultValue);
        }

        @SafeVarargs
        public static <E> OptimizedNonNullList<E> of(E defaultValue, E... elements) {
            Objects.requireNonNull(elements, "Elements array cannot be null");
            return new OptimizedNonNullList<>(new OptimizedNonNullListArrayList<>(elements, defaultValue), defaultValue);
        }

        protected OptimizedNonNullList(OptimizedNonNullListArrayList<E> list, @Nullable E defaultValue) {
            this.list = list;
            this.defaultValue = defaultValue;
        }

        @Nonnull
        @Override
        public E get(int index) {
            return this.list.get(index);
        }

        @Override
        public E set(int index, E value) {
            Objects.requireNonNull(value, "Value cannot be null");
            return this.list.set(index, value);
        }

        @Override
        public void add(int index, E value) {
            Objects.requireNonNull(value, "Value cannot be null");
            this.list.add(index, value);
        }

        @Override
        public E remove(int index) {
            return this.list.remove(index);
        }

        @Override
        public int size() {
            return this.list.size();
        }

        @Override
        public void clear() {
            if (this.defaultValue == null) {
                this.list.clear();
            } else {
                this.list.fillWithDefault();
            }
        }
    }

    // Original NonNullList implementation for comparison
    public static class OriginalNonNullList<E> extends AbstractList<E> {
        private final List<E> list;
        @Nullable
        private final E defaultValue;

        public static <E> OriginalNonNullList<E> create() {
            return new OriginalNonNullList<>(Lists.newArrayList(), null);
        }

        public static <E> OriginalNonNullList<E> createWithCapacity(int initialCapacity) {
            return new OriginalNonNullList<>(Lists.newArrayListWithCapacity(initialCapacity), null);
        }

        public static <E> OriginalNonNullList<E> withSize(int size, E defaultValue) {
            Objects.requireNonNull(defaultValue, "Default value cannot be null");
            Object[] objects = new Object[size];
            Arrays.fill(objects, defaultValue);
            return new OriginalNonNullList<>(Arrays.asList((E[])objects), defaultValue);
        }

        @SafeVarargs
        public static <E> OriginalNonNullList<E> of(E defaultValue, E... elements) {
            Objects.requireNonNull(elements, "Elements array cannot be null");
            return new OriginalNonNullList<>(Arrays.asList(elements), defaultValue);
        }

        protected OriginalNonNullList(List<E> list, @Nullable E defaultValue) {
            this.list = list;
            this.defaultValue = defaultValue;
        }

        @Nonnull
        @Override
        public E get(int index) {
            return this.list.get(index);
        }

        @Override
        public E set(int index, E value) {
            Objects.requireNonNull(value, "Value cannot be null");
            return this.list.set(index, value);
        }

        @Override
        public void add(int index, E value) {
            Objects.requireNonNull(value, "Value cannot be null");
            this.list.add(index, value);
        }

        @Override
        public E remove(int index) {
            return this.list.remove(index);
        }

        @Override
        public int size() {
            return this.list.size();
        }

        @Override
        public void clear() {
            if (this.defaultValue == null) {
                this.list.clear();
            } else {
                for (int i = 0; i < this.size(); i++) {
                    this.list.set(i, this.defaultValue);
                }
            }
        }
    }

    // Test data
    private OptimizedNonNullList<MockItem> optimizedSmall;
    private OptimizedNonNullList<MockItem> optimizedMedium;
    private OptimizedNonNullList<MockItem> optimizedLarge;
    private OptimizedNonNullList<MockItem> optimizedHuge;

    private OriginalNonNullList<MockItem> originalSmall;
    private OriginalNonNullList<MockItem> originalMedium;
    private OriginalNonNullList<MockItem> originalLarge;
    private OriginalNonNullList<MockItem> originalHuge;

    // Access patterns
    private int[] randomIndicesSmall;
    private int[] randomIndicesMedium;
    private int[] randomIndicesLarge;
    private int[] randomIndicesHuge;

    // Single operation indices
    private int singleIndexSmall;
    private int singleIndexMedium;
    private int singleIndexLarge;
    private int singleIndexHuge;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random(42); // Fixed seed for reproducibility

        // Create default item
        MockItem defaultItem = new MockItem("empty", 0, 0);


        optimizedSmall = OptimizedNonNullList.withSize(SMALL_SIZE, defaultItem);
        optimizedMedium = OptimizedNonNullList.withSize(MEDIUM_SIZE, defaultItem);
        optimizedLarge = OptimizedNonNullList.withSize(LARGE_SIZE, defaultItem);
        optimizedHuge = OptimizedNonNullList.withSize(HUGE_SIZE, defaultItem);


        originalSmall = OriginalNonNullList.withSize(SMALL_SIZE, defaultItem);
        originalMedium = OriginalNonNullList.withSize(MEDIUM_SIZE, defaultItem);
        originalLarge = OriginalNonNullList.withSize(LARGE_SIZE, defaultItem);
        originalHuge = OriginalNonNullList.withSize(HUGE_SIZE, defaultItem);


        fillOptimizedList(optimizedSmall, random);
        fillOptimizedList(optimizedMedium, random);
        fillOptimizedList(optimizedLarge, random);
        fillOptimizedList(optimizedHuge, random);

        fillOriginalList(originalSmall, random);
        fillOriginalList(originalMedium, random);
        fillOriginalList(originalLarge, random);
        fillOriginalList(originalHuge, random);


        randomIndicesSmall = generateRandomIndices(SMALL_SIZE, OPERATIONS_COUNT, random);
        randomIndicesMedium = generateRandomIndices(MEDIUM_SIZE, OPERATIONS_COUNT, random);
        randomIndicesLarge = generateRandomIndices(LARGE_SIZE, OPERATIONS_COUNT, random);
        randomIndicesHuge = generateRandomIndices(HUGE_SIZE, OPERATIONS_COUNT, random);


        singleIndexSmall = random.nextInt(SMALL_SIZE);
        singleIndexMedium = random.nextInt(MEDIUM_SIZE);
        singleIndexLarge = random.nextInt(LARGE_SIZE);
        singleIndexHuge = random.nextInt(HUGE_SIZE);
    }

    private void fillOptimizedList(OptimizedNonNullList<MockItem> list, Random random) {
        for (int i = 0; i < list.size(); i++) {
            if (random.nextDouble() < 0.3) { // 30% chance of non-default item
                list.set(i, new MockItem(
                        "item_" + random.nextInt(100),
                        random.nextInt(64) + 1,
                        random.nextInt(1000)
                ));
            }
        }
    }

    private void fillOriginalList(OriginalNonNullList<MockItem> list, Random random) {
        for (int i = 0; i < list.size(); i++) {
            if (random.nextDouble() < 0.3) { // 30% chance of non-default item
                list.set(i, new MockItem(
                        "item_" + random.nextInt(100),
                        random.nextInt(64) + 1,
                        random.nextInt(1000)
                ));
            }
        }
    }

    private int[] generateRandomIndices(int size, int count, Random random) {
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = random.nextInt(size);
        }
        return indices;
    }

    // === SINGLE OPERATION BENCHMARKS ===

    @Benchmark
    public MockItem optimized_SingleOp_Small() {
        return optimizedSmall.get(singleIndexSmall);
    }

    @Benchmark
    public MockItem original_SingleOp_Small() {
        return originalSmall.get(singleIndexSmall);
    }

    @Benchmark
    public MockItem optimized_SingleOp_Medium() {
        return optimizedMedium.get(singleIndexMedium);
    }

    @Benchmark
    public MockItem original_SingleOp_Medium() {
        return originalMedium.get(singleIndexMedium);
    }

    @Benchmark
    public MockItem optimized_SingleOp_Large() {
        return optimizedLarge.get(singleIndexLarge);
    }

    @Benchmark
    public MockItem original_SingleOp_Large() {
        return originalLarge.get(singleIndexLarge);
    }

    @Benchmark
    public MockItem optimized_SingleOp_Huge() {
        return optimizedHuge.get(singleIndexHuge);
    }

    @Benchmark
    public MockItem original_SingleOp_Huge() {
        return originalHuge.get(singleIndexHuge);
    }

    // === SEQUENTIAL ACCESS BENCHMARKS ===

    @Benchmark
    public void optimized_Sequential_Small(Blackhole bh) {
        for (int i = 0; i < optimizedSmall.size(); i++) {
            bh.consume(optimizedSmall.get(i));
        }
    }

    @Benchmark
    public void original_Sequential_Small(Blackhole bh) {
        for (int i = 0; i < originalSmall.size(); i++) {
            bh.consume(originalSmall.get(i));
        }
    }

    @Benchmark
    public void optimized_Sequential_Medium(Blackhole bh) {
        for (int i = 0; i < optimizedMedium.size(); i++) {
            bh.consume(optimizedMedium.get(i));
        }
    }

    @Benchmark
    public void original_Sequential_Medium(Blackhole bh) {
        for (int i = 0; i < originalMedium.size(); i++) {
            bh.consume(originalMedium.get(i));
        }
    }

    @Benchmark
    public void optimized_Sequential_Large(Blackhole bh) {
        for (int i = 0; i < optimizedLarge.size(); i++) {
            bh.consume(optimizedLarge.get(i));
        }
    }

    @Benchmark
    public void original_Sequential_Large(Blackhole bh) {
        for (int i = 0; i < originalLarge.size(); i++) {
            bh.consume(originalLarge.get(i));
        }
    }

    // === RANDOM ACCESS BENCHMARKS ===

    @Benchmark
    public void optimized_Random_Small(Blackhole bh) {
        for (int index : randomIndicesSmall) {
            bh.consume(optimizedSmall.get(index));
        }
    }

    @Benchmark
    public void original_Random_Small(Blackhole bh) {
        for (int index : randomIndicesSmall) {
            bh.consume(originalSmall.get(index));
        }
    }

    @Benchmark
    public void optimized_Random_Medium(Blackhole bh) {
        for (int index : randomIndicesMedium) {
            bh.consume(optimizedMedium.get(index));
        }
    }

    @Benchmark
    public void original_Random_Medium(Blackhole bh) {
        for (int index : randomIndicesMedium) {
            bh.consume(originalMedium.get(index));
        }
    }

    @Benchmark
    public void optimized_Random_Large(Blackhole bh) {
        for (int index : randomIndicesLarge) {
            bh.consume(optimizedLarge.get(index));
        }
    }

    @Benchmark
    public void original_Random_Large(Blackhole bh) {
        for (int index : randomIndicesLarge) {
            bh.consume(originalLarge.get(index));
        }
    }

    // === INTENSIVE BENCHMARKS ===

    @Benchmark
    public void optimized_Intensive_Huge_Sequential(Blackhole bh) {
        // Multiple passes over the huge list
        for (int pass = 0; pass < 10; pass++) {
            for (int i = 0; i < optimizedHuge.size(); i++) {
                bh.consume(optimizedHuge.get(i));
            }
        }
    }

    @Benchmark
    public void original_Intensive_Huge_Sequential(Blackhole bh) {
        // Multiple passes over the huge list
        for (int pass = 0; pass < 10; pass++) {
            for (int i = 0; i < originalHuge.size(); i++) {
                bh.consume(originalHuge.get(i));
            }
        }
    }

    @Benchmark
    public void optimized_Intensive_Huge_Random(Blackhole bh) {
        for (int index : randomIndicesHuge) {
            bh.consume(optimizedHuge.get(index));
        }
    }

    @Benchmark
    public void original_Intensive_Huge_Random(Blackhole bh) {
        for (int index : randomIndicesHuge) {
            bh.consume(originalHuge.get(index));
        }
    }

    // === MIXED ACCESS PATTERN BENCHMARKS ===

    @Benchmark
    public void optimized_Mixed_Access_Pattern(Blackhole bh) {
        // Simulate real-world access: some sequential, some random
        // Sequential access (like iterating through inventory)
        for (int i = 0; i < Math.min(optimizedMedium.size(), 50); i++) {
            bh.consume(optimizedMedium.get(i));
        }

        // Random access (like checking specific slots)
        for (int i = 0; i < 100; i++) {
            int index = randomIndicesMedium[i % randomIndicesMedium.length];
            bh.consume(optimizedMedium.get(index));
        }

        // Hotbar access simulation (first 9 items accessed frequently)
        for (int i = 0; i < 9 && i < optimizedMedium.size(); i++) {
            for (int j = 0; j < 5; j++) { // Access each hotbar slot 5 times
                bh.consume(optimizedMedium.get(i));
            }
        }
    }

    @Benchmark
    public void original_Mixed_Access_Pattern(Blackhole bh) {
        // Simulate real-world access: some sequential, some random
        // Sequential access (like iterating through inventory)
        for (int i = 0; i < Math.min(originalMedium.size(), 50); i++) {
            bh.consume(originalMedium.get(i));
        }

        // Random access (like checking specific slots)
        for (int i = 0; i < 100; i++) {
            int index = randomIndicesMedium[i % randomIndicesMedium.length];
            bh.consume(originalMedium.get(index));
        }

        // Hotbar access simulation (first 9 items accessed frequently)
        for (int i = 0; i < 9 && i < originalMedium.size(); i++) {
            for (int j = 0; j < 5; j++) { // Access each hotbar slot 5 times
                bh.consume(originalMedium.get(i));
            }
        }
    }

    // === BOUNDARY CONDITION BENCHMARKS ===

    @Benchmark
    public void optimized_Boundary_Access(Blackhole bh) {
        // Test first and last elements repeatedly (common edge cases)
        for (int i = 0; i < 1000; i++) {
            bh.consume(optimizedLarge.get(0)); // First element
            bh.consume(optimizedLarge.get(optimizedLarge.size() - 1)); // Last element
        }
    }

    @Benchmark
    public void original_Boundary_Access(Blackhole bh) {
        // Test first and last elements repeatedly (common edge cases)
        for (int i = 0; i < 1000; i++) {
            bh.consume(originalLarge.get(0)); // First element
            bh.consume(originalLarge.get(originalLarge.size() - 1)); // Last element
        }
    }

    // === CACHE BEHAVIOR BENCHMARKS ===

    @Benchmark
    public void optimized_Cache_Friendly_Strided(Blackhole bh) {
        // Access every 8th element (still relatively cache-friendly)
        for (int i = 0; i < optimizedLarge.size(); i += 8) {
            bh.consume(optimizedLarge.get(i));
        }
    }

    @Benchmark
    public void original_Cache_Friendly_Strided(Blackhole bh) {
        // Access every 8th element (still relatively cache-friendly)
        for (int i = 0; i < originalLarge.size(); i += 8) {
            bh.consume(originalLarge.get(i));
        }
    }

    @Benchmark
    public void optimized_Cache_Unfriendly_Strided(Blackhole bh) {
        // Access every 128th element (cache-unfriendly)
        for (int i = 0; i < optimizedHuge.size(); i += 128) {
            bh.consume(optimizedHuge.get(i));
        }
    }

    @Benchmark
    public void original_Cache_Unfriendly_Strided(Blackhole bh) {
        // Access every 128th element (cache-unfriendly)
        for (int i = 0; i < originalHuge.size(); i += 128) {
            bh.consume(originalHuge.get(i));
        }
    }

    // === STRESS TEST BENCHMARKS ===

    @Benchmark
    public void optimized_Stress_Test_Multi_Pattern(Blackhole bh) {
        // Combine multiple access patterns in one test
        final int iterations = 100;

        for (int iter = 0; iter < iterations; iter++) {
            // Sequential burst
            for (int i = 0; i < Math.min(20, optimizedMedium.size()); i++) {
                bh.consume(optimizedMedium.get(i));
            }

            // Random burst
            for (int i = 0; i < 10; i++) {
                int index = randomIndicesMedium[(iter * 10 + i) % randomIndicesMedium.length];
                bh.consume(optimizedMedium.get(index));
            }

            // Boundary access
            if (optimizedMedium.size() > 0) {
                bh.consume(optimizedMedium.get(0));
                bh.consume(optimizedMedium.get(optimizedMedium.size() - 1));
            }
        }
    }

    @Benchmark
    public void original_Stress_Test_Multi_Pattern(Blackhole bh) {
        // Combine multiple access patterns in one test
        final int iterations = 100;

        for (int iter = 0; iter < iterations; iter++) {
            // Sequential burst
            for (int i = 0; i < Math.min(20, originalMedium.size()); i++) {
                bh.consume(originalMedium.get(i));
            }

            // Random burst
            for (int i = 0; i < 10; i++) {
                int index = randomIndicesMedium[(iter * 10 + i) % randomIndicesMedium.length];
                bh.consume(originalMedium.get(index));
            }

            // Boundary access
            if (originalMedium.size() > 0) {
                bh.consume(originalMedium.get(0));
                bh.consume(originalMedium.get(originalMedium.size() - 1));
            }
        }
    }
}