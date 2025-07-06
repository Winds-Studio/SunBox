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

        // Update predicate
        if (nextHalloweenEnd == 0 || currentEpochMillis >= nextHalloweenEnd) {
            java.time.OffsetDateTime currentDate = java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentEpochMillis), java.time.ZoneId.systemDefault())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0); // Adjust to directly start or end at zero o'clock

            java.time.OffsetDateTime thisHalloweenStart = currentDate.withMonth(halloweenStartMonthOfYear).withDayOfMonth(halloweenStartDayOfMonth);
            java.time.OffsetDateTime thisHalloweenEnd = currentDate.withMonth(halloweenEndMonthOfYear).withDayOfMonth(halloweenEndDayOfMonth);

            // Move to next year date if current passed
            if (currentDate.isAfter(thisHalloweenEnd)) {
                thisHalloweenStart = thisHalloweenStart.plusYears(1);
                thisHalloweenEnd = thisHalloweenEnd.plusYears(1);
            }

            nextHalloweenStart = thisHalloweenStart.toInstant().toEpochMilli();
            nextHalloweenEnd = thisHalloweenEnd.toInstant().toEpochMilli();
        }

        return currentEpochMillis >= nextHalloweenStart && currentEpochMillis < nextHalloweenEnd;
        // Gale end - predict Halloween
    }
}
