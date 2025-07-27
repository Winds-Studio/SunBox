package cn.dreeam.sunbox.benchmarks.jmh;

import cn.dreeam.sunbox.util.ChatFormatting;
import cn.dreeam.sunbox.util.TextColor;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
Estimate running time: ~4mins
See OptimizeTextColorMisc as well
Target patch: Optimise-TextColor.patch
*/
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class OptimizeTextColorMapGet {

    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR_IMMUTABLE_MAP = Stream.of(ChatFormatting.values())
            .filter(ChatFormatting::isColor)
            .collect(ImmutableMap.toImmutableMap(Function.identity(), formatting -> TextColor.of(formatting.getColor(), formatting.getName(), formatting))); // CraftBukkit
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR_FAST_UTIL = new Object2ObjectOpenHashMap<>(LEGACY_FORMAT_TO_COLOR_IMMUTABLE_MAP);
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR_ENUM_MAP =
            Stream.of(ChatFormatting.values())
                    .filter(ChatFormatting::isColor)
                    .collect(Collectors.toMap(
                            Function.identity(),
                            format -> TextColor.of(format.getColor(), format.getName(), format),
                            (a, b) -> a,
                            () -> new EnumMap<>(ChatFormatting.class)
                    ));

    private ChatFormatting chatFormatting;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random();
        chatFormatting = ChatFormatting.COLOR_VALUES[random.nextInt(ChatFormatting.COLOR_VALUES.length)];
    }

    @Benchmark
    public TextColor immutableMapGet() {
        return LEGACY_FORMAT_TO_COLOR_IMMUTABLE_MAP.get(chatFormatting);
    }

    @Benchmark
    public TextColor fastutilMapGet() {
        return LEGACY_FORMAT_TO_COLOR_FAST_UTIL.get(chatFormatting);
    }

    @Benchmark
    public TextColor enumMapGet() {
        return LEGACY_FORMAT_TO_COLOR_ENUM_MAP.get(chatFormatting);
    }

    @Benchmark
    public TextColor constantGet() {
        return chatFormatting.getTextColor();
    }
}
