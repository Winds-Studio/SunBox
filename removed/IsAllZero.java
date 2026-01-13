package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

/*
Estimate running time: ~10mins
Target patch: Optimise-chunkUnloads.patch
(This change has been removed since Leaf 1.21.11)
*/
@Deprecated(forRemoval = true, since = "1.21.11")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class IsAllZero {

    public static final int ARRAY_SIZE = 16 * 16 * 16 / (8/4); // blocks / bytes per block

    byte[] dataAllZero;
    byte[] dataOneNonZero;

    private static final VarHandle LONG_VIEW =
            MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder());

    private static final Unsafe UNSAFE;
    private static final long BYTE_ARRAY_BASE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            BYTE_ARRAY_BASE = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Setup
    public void setup() {
        dataAllZero = new byte[ARRAY_SIZE];
        dataOneNonZero = new byte[ARRAY_SIZE];
        dataOneNonZero[ARRAY_SIZE / 2] = 1;
    }

    @Benchmark
    public boolean baseline_byteLoop_zero() {
        for (byte b : dataAllZero) {
            if (b != 0) return false;
        }
        return true;
    }

    @Benchmark
    public boolean byteBuffer_zero() {
        ByteBuffer buf = ByteBuffer.wrap(dataAllZero);
        while (buf.remaining() >= Long.BYTES) {
            if (buf.getLong() != 0L) return false;
        }
        return true;
    }

    @Benchmark
    public boolean varHandle_zero() {
        int len = dataAllZero.length >> 3;
        for (int i = 0; i < len; i++) {
            if ((long) LONG_VIEW.get(dataAllZero, i << 3) != 0L) {
                return false;
            }
        }
        return true;
    }

    @Benchmark
    public boolean unsafe_zero() {
        int len = dataAllZero.length >> 3;
        long offset = BYTE_ARRAY_BASE;
        for (int i = 0; i < len; i++) {
            if (UNSAFE.getLong(dataAllZero, offset) != 0L) {
                return false;
            }
            offset += Long.BYTES;
        }
        return true;
    }

    @Benchmark
    public boolean memorySegment_zero() {
        MemorySegment seg = MemorySegment.ofArray(dataAllZero);
        int len = dataAllZero.length >> 3;
        for (int i = 0; i < len; i++) {
            if (seg.get(ValueLayout.JAVA_LONG_UNALIGNED, (long) i << 3) != 0L) {
                return false;
            }
        }
        return true;
    }

    @Benchmark
    public boolean taiyouVer_zero() {
        // Leaf start - Optimize chunkUnload
        // Check in 8-byte chunks
        final int longLength = ARRAY_SIZE >>> 3;
        for (int i = 0; i < longLength; i++) {
            long value = 0;
            final int baseIndex = i << 3;
            // Combine 8 bytes into a long
            for (int j = 0; j < 8; j++) {
                value |= ((long) (dataAllZero[baseIndex + j] & 0xFF)) << (j << 3);
            }
            if (value != 0) {
                return false;
            }
        }

        // Check remaining bytes
        for (int i = longLength << 3; i < ARRAY_SIZE; i++) {
            if (dataAllZero[i] != 0) {
                return false;
            }
        }
        // Leaf end - Optimize chunkUnload

        return true;
    }

    // ===== non-zero short-circuit cases =====

    @Benchmark
    public boolean varHandle_nonZero() {
        int len = dataOneNonZero.length >> 3;
        for (int i = 0; i < len; i++) {
            if ((long) LONG_VIEW.get(dataOneNonZero, i << 3) != 0L) {
                return false;
            }
        }
        return true;
    }

    @Benchmark
    public boolean unsafe_nonZero() {
        int len = dataOneNonZero.length >> 3;
        long offset = BYTE_ARRAY_BASE;
        for (int i = 0; i < len; i++) {
            if (UNSAFE.getLong(dataOneNonZero, offset) != 0L) {
                return false;
            }
            offset += Long.BYTES;
        }
        return true;
    }

    @Benchmark
    public boolean memorySegment_nonZero() {
        MemorySegment seg = MemorySegment.ofArray(dataOneNonZero);
        int len = dataOneNonZero.length >> 3;
        for (int i = 0; i < len; i++) {
            if (seg.get(ValueLayout.JAVA_LONG_UNALIGNED, (long) i << 3) != 0L) {
                return false;
            }
        }
        return true;
    }

    @Benchmark
    public boolean taiyouVer_nonZero() {
        // Leaf start - Optimize chunkUnload
        // Check in 8-byte chunks
        final int longLength = ARRAY_SIZE >>> 3;
        for (int i = 0; i < longLength; i++) {
            long value = 0;
            final int baseIndex = i << 3;
            // Combine 8 bytes into a long
            for (int j = 0; j < 8; j++) {
                value |= ((long) (dataOneNonZero[baseIndex + j] & 0xFF)) << (j << 3);
            }
            if (value != 0) {
                return false;
            }
        }

        // Check remaining bytes
        for (int i = longLength << 3; i < ARRAY_SIZE; i++) {
            if (dataOneNonZero[i] != 0) {
                return false;
            }
        }
        // Leaf end - Optimize chunkUnload

        return true;
    }
}
