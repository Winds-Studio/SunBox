package cn.dreeam.sunbox.benchmarks.jmh;

import cn.dreeam.sunbox.util.CraftMapColorCache;
import cn.dreeam.sunbox.util.MapPalette;
import cn.dreeam.sunbox.util.VectorMapPalette;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
Estimate running time: ~3mins
Target patch: Vectorized-map-color-conversion.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class MapColorConversion {

    private static final int IMAGE_SIZE = 128 * 128;

    private static boolean initCache;

    private int[] pixels;
    private byte[] result;

    @Setup(Level.Iteration)
    public void setup() {
        if (!initCache) {
            MapPalette.setMapColorCache(new CraftMapColorCache(Logger.getLogger("SunBox")));
            initCache = true;
        }
        pixels = new int[IMAGE_SIZE];
        result = new byte[IMAGE_SIZE];
        Random random = new Random();

        // Generate random argb pixel array
        for (int i = 0; i < pixels.length; i++) {
            int a = random.nextInt(256);
            int r = random.nextInt(256); // 0–255
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    @Benchmark
    public byte[] matchColor() {
        for (int i = 0; i < pixels.length; i++) {
            result[i] = MapPalette.matchColor(new Color(pixels[i], false), false);
        }
        return result;
    }

    @Benchmark
    public byte[] matchColorWithCache() {
        for (int i = 0; i < pixels.length; i++) {
            result[i] = MapPalette.matchColor(new Color(pixels[i], true), true);
        }
        return result;
    }

    @Benchmark
    public byte[] matchColorVectorized() {
        VectorMapPalette.matchColorVectorized(pixels, result); // Gale - Pufferfish - vectorized map color conversion
        return result;
    }
}
