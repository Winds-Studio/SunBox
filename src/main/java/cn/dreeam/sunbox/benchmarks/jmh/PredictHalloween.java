package cn.dreeam.sunbox.benchmarks.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~2mins
Target patch: Predict-Halloween.patch
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class PredictHalloween {

    private static final int halloweenStartMonthOfYear = 10;
    private static final int halloweenStartDayOfMonth = 20;
    private static final int halloweenEndMonthOfYear = 11;
    private static final int halloweenEndDayOfMonth = 4;
    private static long nextHalloweenStart = 0;
    private static long nextHalloweenEnd = 0;

    @Benchmark
    public static boolean isHalloweenBefore() {
        LocalDate localDate = LocalDate.now();
        int i = localDate.get(ChronoField.DAY_OF_MONTH);
        int i1 = localDate.get(ChronoField.MONTH_OF_YEAR);
        return i1 == 10 && i >= 20 || i1 == 11 && i <= 3;
    }

    @Benchmark
    public static boolean isHalloweenAfter() {
        // Gale start - predict Halloween
        long currentEpochMillis = System.currentTimeMillis();

        if (currentEpochMillis > nextHalloweenEnd) {
            // Update prediction

            java.time.OffsetDateTime currentDate = java.time.OffsetDateTime.now();
            int currentMonthOfYear = currentDate.getMonth().getValue();
            int currentDayOfMonth = currentDate.getDayOfMonth();

            java.time.OffsetDateTime nextHalloweenStartDate = currentDate.withMonth(halloweenStartMonthOfYear).withDayOfMonth(halloweenStartDayOfMonth)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0); // Adjust to directly start or end at zero o'clock

            if (currentMonthOfYear >= halloweenEndMonthOfYear && currentDayOfMonth >= halloweenEndDayOfMonth) {
                nextHalloweenStartDate = nextHalloweenStartDate.plusYears(1);
            }

            nextHalloweenStart = nextHalloweenStartDate.toInstant().toEpochMilli();
            nextHalloweenEnd = nextHalloweenStartDate.withMonth(halloweenEndMonthOfYear).withDayOfMonth(halloweenEndDayOfMonth).toInstant().toEpochMilli();
        }

        return currentEpochMillis >= nextHalloweenStart;
        // Gale end - predict Halloween
    }
}
