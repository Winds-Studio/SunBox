package cn.dreeam.sunbox;

import cn.dreeam.sunbox.benchmarks.jmh.FMA;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

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
                .include(FMA.class.getSimpleName())
                .forks(1)
                .warmupIterations(4)
                .measurementIterations(10)
                .warmupTime(TimeValue.seconds(5))
                .measurementTime(TimeValue.seconds(4))
                .build();

        new Runner(opt).run();
    }
}
