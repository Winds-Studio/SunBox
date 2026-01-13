package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~4mins
Target patch: Lithium-fast-util.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class LithiumFastUtil {

    private double maxX;
    private double maxY;
    private double maxZ;

    private Direction.Axis axis;
    private static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();

    @Setup(Level.Iteration)
    public void setup() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        this.maxX = rnd.nextDouble();
        this.maxY = rnd.nextDouble();
        this.maxZ = rnd.nextDouble();
        this.axis = Direction.Axis.values()[rnd.nextInt(3)];
    }

    @Benchmark
    public double maxOptimized() {
        return switch (axis.ordinal()) {
            case 0 -> // X
                    maxX;
            case 1 -> // Y
                    maxY;
            case 2 -> // Z
                    maxZ;
            default -> throw new IllegalArgumentException();
        };
    }

    @Benchmark
    public Direction.Axis backwardCycleOptimized() {
        return switch (axis.ordinal()) {
            case 0 -> Direction.Axis.Z;
            case 1 -> Direction.Axis.X;
            case 2 -> Direction.Axis.Y;
            default -> throw new IllegalArgumentException();
        };
    }

    @Benchmark
    public double maxVanilla() {
        return axis.choose(maxX, maxY, maxZ);
    }

    @Benchmark
    public Direction.Axis backwardCycleVanilla() {
        return AXIS_VALUES[Math.floorMod(axis.ordinal() - 1, 3)];
    }

    // Mock
    public enum Direction {;
        public enum Axis {
            X {
                @Override
                public double choose(double x, double y, double z) {
                    return x;
                }
            },
            Y {
                @Override
                public double choose(double x, double y, double z) {
                    return y;
                }
            },
            Z {
                @Override
                public double choose(double x, double y, double z) {
                    return z;
                }
            };

            public abstract double choose(double x, double y, double z);
        }
    }
}
