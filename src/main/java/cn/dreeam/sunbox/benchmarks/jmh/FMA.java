package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~4-5 mins
Target patch: Optimize-Entity-distanceToSqr.patch

This benchmark includes two types of tests:
1. SingleOp: A microbenchmark measuring a single distance calculation.
2. Intensive: A more realistic benchmark that iterates over a list of 1000 entities.
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class FMA {

    private static final int ENTITY_COUNT = 1000;

    private static class MockEntity {
        final double x, y, z;

        MockEntity(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private List<MockEntity> mobEntities;
    private MockEntity playerEntity;

    private MockEntity singleEntity1;
    private MockEntity singleEntity2;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random(1234); // FIXED SEED DO NOT CHANGE
        mobEntities = new ArrayList<>(ENTITY_COUNT);
        playerEntity = new MockEntity(
                random.nextDouble() * 1000,
                random.nextDouble() * 1000,
                random.nextDouble() * 1000
        );

        for (int i = 0; i < ENTITY_COUNT; i++) {
            mobEntities.add(new MockEntity(
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000
            ));
        }
        singleEntity1 = new MockEntity(random.nextDouble() * 1000, random.nextDouble() * 1000, random.nextDouble() * 1000);
        singleEntity2 = new MockEntity(random.nextDouble() * 1000, random.nextDouble() * 1000, random.nextDouble() * 1000);
    }

    public static float Mth_sqrt(float value) {
        return (float)Math.sqrt(value);
    }

    @Benchmark
    public float distanceTo_SingleOp_Before() {
        float f = (float)(singleEntity1.x - singleEntity2.x);
        float f1 = (float)(singleEntity1.y - singleEntity2.y);
        float f2 = (float)(singleEntity1.z - singleEntity2.z);
        return Mth_sqrt(f * f + f1 * f1 + f2 * f2);
    }

    @Benchmark
    public double distanceToSqr_SingleOp_Before() {
        double d = singleEntity1.x - singleEntity2.x;
        double d1 = singleEntity1.y - singleEntity2.y;
        double d2 = singleEntity1.z - singleEntity2.z;
        return d * d + d1 * d1 + d2 * d2;
    }

    @Benchmark
    public float distanceTo_SingleOp_After_WithFMA() {
        final double dx = singleEntity1.x - singleEntity2.x;
        final double dy = singleEntity1.y - singleEntity2.y;
        final double dz = singleEntity1.z - singleEntity2.z;
        return (float) Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    @Benchmark
    public double distanceToSqr_SingleOp_After_WithFMA() {
        final double dx = singleEntity1.x - singleEntity2.x;
        final double dy = singleEntity1.y - singleEntity2.y;
        final double dz = singleEntity1.z - singleEntity2.z;
        return Math.fma(dx, dx, Math.fma(dy, dy, dz * dz));
    }


    @Benchmark
    public double distanceToSqr_Intensive_Before() {
        double total = 0;
        for (MockEntity mob : mobEntities) {
            double d = playerEntity.x - mob.x;
            double d1 = playerEntity.y - mob.y;
            double d2 = playerEntity.z - mob.z;
            total += d * d + d1 * d1 + d2 * d2;
        }
        return total;
    }

    @Benchmark
    public double distanceToSqr_Intensive_After_NoFMA() {
        double total = 0;
        for (MockEntity mob : mobEntities) {
            final double dx = playerEntity.x - mob.x;
            final double dy = playerEntity.y - mob.y;
            final double dz = playerEntity.z - mob.z;
            total += dx * dx + dy * dy + dz * dz;
        }
        return total;
    }

    @Benchmark
    public double distanceToSqr_Intensive_After_WithFMA() {
        double total = 0;
        for (MockEntity mob : mobEntities) {
            final double dx = playerEntity.x - mob.x;
            final double dy = playerEntity.y - mob.y;
            final double dz = playerEntity.z - mob.z;
            total += Math.fma(dx, dx, Math.fma(dy, dy, dz * dz));
        }
        return total;
    }
}