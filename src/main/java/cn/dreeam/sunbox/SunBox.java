package cn.dreeam.sunbox;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class SunBox {

    public static void main(String[] args) {
        try {
            runJMH();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void runJMH() throws Exception {
        Options opt = new OptionsBuilder()
                // TODO: Notice: Include benchmark class you want
                .include(cn.dreeam.sunbox.benchmarks.jmh.UsePlatformMath.class.getSimpleName())
                .forks(1)
                .warmupIterations(3)
                .measurementIterations(3)
                .build();

        new Runner(opt).run();
    }
}
