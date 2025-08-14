package cn.dreeam.sunbox.benchmarks.jmh;

import com.google.common.collect.AbstractIterator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~2mins
Target patch: Sakura-Optimise-check-inside-blocks-and-traverse-blo.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class BetterBlocksTraverse {

    private static final int SIZE = 5; // AABB size

    private MockAABB testAABB;

    @Setup(Level.Iteration)
    public void setup() {
        testAABB = new MockAABB(0, 0, 0, SIZE, SIZE, SIZE);
    }

    @Benchmark
    public void iterableAABB(Blackhole bh) {
        for (MockBlockPos pos : iterable(testAABB)) {
            bh.consume(pos);
        }
    }

    @Benchmark
    public void betweenClosedAABB(Blackhole bh) {
        for (MockBlockPos pos : betweenClosed(testAABB)) {
            bh.consume(pos);
        }
    }

    public static class MockBlockPos {
        protected int x, y, z;

        public MockBlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public static class MutableBlockPos extends MockBlockPos {
            public MutableBlockPos() {
                this(0, 0, 0);
            }

            public MutableBlockPos(int x, int y, int z) {
                super(x, y, z);
            }

            public MockBlockPos.MutableBlockPos set(int x, int y, int z) {
                this.x = x; // Paper - Perf: Manually inline methods in BlockPosition
                this.y = y; // Paper - Perf: Manually inline methods in BlockPosition
                this.z = z; // Paper - Perf: Manually inline methods in BlockPosition
                return this;
            }
        }
    }

    public static class MockAABB {
        private final double minX, minY, minZ, maxX, maxY, maxZ;

        public MockAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }

    public static class Mth {
        public static int floor(double v) {
            return (int) Math.floor(v);
        }
    }

    // Sakura start - optimise check inside blocks
    public Iterable<MockBlockPos> iterable(MockAABB bb) {
        return () -> new BlockPosIterator(bb);
    }

    public final class BlockPosIterator extends AbstractIterator<MockBlockPos> {

        private final int startX;
        private final int startY;
        private final int startZ;
        private final int endX;
        private final int endY;
        private final int endZ;
        private MockBlockPos.MutableBlockPos pos = null;

        public BlockPosIterator(MockAABB bb) {
            this.startX = Mth.floor(bb.minX);
            this.startY = Mth.floor(bb.minY);
            this.startZ = Mth.floor(bb.minZ);
            this.endX = Mth.floor(bb.maxX);
            this.endY = Mth.floor(bb.maxY);
            this.endZ = Mth.floor(bb.maxZ);
        }

        @Override
        protected MockBlockPos computeNext() {
            MockBlockPos.MutableBlockPos pos = this.pos;
            if (pos == null) {
                return this.pos = new MockBlockPos.MutableBlockPos(this.startX, this.startY, this.startZ);
            }
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            if (y < this.endY) {
                y += 1;
            } else if (x < this.endX) {
                x += 1;
                y = this.startY;
            } else if (z < this.endZ) {
                z += 1;
                x = this.startX;
                y = this.startY; // Reset y also!
            } else {
                return this.endOfData();
            }

            pos.set(x, y, z);
            return pos;
        }
    }
    // Sakura end - optimise check inside blocks

    // Copy from origin
    public static MockBlockPos containing(double x, double y, double z) {
        return new MockBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static Iterable<MockBlockPos> betweenClosed(MockAABB box) {
        MockBlockPos blockPos = containing(box.minX, box.minY, box.minZ);
        MockBlockPos blockPos1 = containing(box.maxX, box.maxY, box.maxZ);
        return betweenClosed(blockPos, blockPos1);
    }

    public static Iterable<MockBlockPos> betweenClosed(MockBlockPos firstPos, MockBlockPos secondPos) {
        return betweenClosed(
                Math.min(firstPos.getX(), secondPos.getX()),
                Math.min(firstPos.getY(), secondPos.getY()),
                Math.min(firstPos.getZ(), secondPos.getZ()),
                Math.max(firstPos.getX(), secondPos.getX()),
                Math.max(firstPos.getY(), secondPos.getY()),
                Math.max(firstPos.getZ(), secondPos.getZ())
        );
    }

    public static Iterable<MockBlockPos> betweenClosed(int x1, int y1, int z1, int x2, int y2, int z2) {
        int i = x2 - x1 + 1;
        int i1 = y2 - y1 + 1;
        int i2 = z2 - z1 + 1;
        int i3 = i * i1 * i2;
        return () -> new AbstractIterator<>() {
            private final MockBlockPos.MutableBlockPos cursor = new MockBlockPos.MutableBlockPos();
            private int index;

            @Override
            protected MockBlockPos computeNext() {
                if (this.index == i3) {
                    return this.endOfData();
                } else {
                    int i4 = this.index % i;
                    int i5 = this.index / i;
                    int i6 = i5 % i1;
                    int i7 = i5 / i1;
                    this.index++;
                    return this.cursor.set(x1 + i4, y1 + i6, z1 + i7);
                }
            }
        };
    }
}
