package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~4mins
Target patch: optimize-movement-vector-normalization.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class EntityFallClipVector {

    private Vec3 pos;
    private Vec3[] smallVecs;
    private Vec3[] largeVecs;
    
    private int index = 0;
    private static final int DATA_SIZE = 4096;
    private static final int MASK = DATA_SIZE - 1;

    @Setup(Level.Trial)
    public void setup() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        this.pos = new Vec3(100, 100, 100);

        this.smallVecs = new Vec3[DATA_SIZE];
        this.largeVecs = new Vec3[DATA_SIZE];

        for (int i = 0; i < DATA_SIZE; i++) {
            this.smallVecs[i] = randomVec(rnd, 1.0, 8.0);
            this.largeVecs[i] = randomVec(rnd, 8.1, 50.0);
        }
    }

    @Benchmark
    public Vec3 smallVanilla() {
        Vec3 vec = smallVecs[(index++) & MASK];
        double min = Math.min(vec.length(), 8.0);
        return pos.add(vec.normalize().scale(min));
    }

    @Benchmark
    public Vec3 smallOptimized() {
        Vec3 vec = smallVecs[(index++) & MASK];
        double d = vec.lengthSqr();
        if (d > 64.0) {
            double scale = 8.0 / Math.sqrt(d);
            return pos.add(vec.x * scale, vec.y * scale, vec.z * scale);
        } else {
            return pos.add(vec.x, vec.y, vec.z);
        }
    }

    @Benchmark
    public Vec3 largeVanilla() {
        Vec3 vec = largeVecs[(index++) & MASK];
        double min = Math.min(vec.length(), 8.0);
        return pos.add(vec.normalize().scale(min));
    }

    @Benchmark
    public Vec3 largeOptimized() {
        Vec3 vec = largeVecs[(index++) & MASK];
        double d = vec.lengthSqr();
        if (d > 64.0) {
            double scale = 8.0 / Math.sqrt(d);
            return pos.add(vec.x * scale, vec.y * scale, vec.z * scale);
        } else {
            return pos.add(vec.x, vec.y, vec.z);
        }
    }

    private Vec3 randomVec(ThreadLocalRandom rnd, double minLen, double maxLen) {
        double x = rnd.nextDouble() - 0.5;
        double y = rnd.nextDouble() - 0.5;
        double z = rnd.nextDouble() - 0.5;
        double len = Math.sqrt(x * x + y * y + z * z);
        double targetLen = minLen + (maxLen - minLen) * rnd.nextDouble();
        return new Vec3((x / len) * targetLen, (y / len) * targetLen, (z / len) * targetLen);
    }

    public static class Vec3 {
        public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);
        public final double x;
        public final double y;
        public final double z;

        public Vec3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double lengthSqr() {
            return this.x * this.x + this.y * this.y + this.z * this.z;
        }

        public double length() {
            return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }

        public Vec3 normalize() {
            double squareRoot = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
            return squareRoot < 1.0E-5F ? ZERO : new Vec3(this.x / squareRoot, this.y / squareRoot, this.z / squareRoot);
        }

        public Vec3 scale(double factor) {
            return this.multiply(factor, factor, factor);
        }

        public Vec3 multiply(double factorX, double factorY, double factorZ) {
            return new Vec3(this.x * factorX, this.y * factorY, this.z * factorZ);
        }

        public Vec3 add(Vec3 vec) {
            return this.add(vec.x, vec.y, vec.z);
        }

        public Vec3 add(double x, double y, double z) {
            return new Vec3(this.x + x, this.y + y, this.z + z);
        }
    }
}
