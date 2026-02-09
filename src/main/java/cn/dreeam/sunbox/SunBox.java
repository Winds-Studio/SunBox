package cn.dreeam.sunbox;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class SunBox {

    public static void main(String[] args) {
        runNanoTime();
        try {
            runJMH();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void runJMH() throws Exception {
        Options opt = new OptionsBuilder()
                // TODO: Notice: Include benchmark class name you want
                .include("BiomeLookupBenchmark")
                .forks(1)
                .warmupIterations(4)
                .measurementIterations(10)
                .warmupTime(TimeValue.seconds(5))
                .measurementTime(TimeValue.seconds(4))
                .build();

        new Runner(opt).run();
    }

    private static void runNanoTime() {
        // TODO: Notice: Include benchmark class you want
        //cn.dreeam.sunbox.benchmarks.nanoTime.OptimizeTextColor.run();
    }
}
