package cn.dreeam.sunbox.benchmarks.jmh;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
Estimate running time: ~3mins
Target patch: Optimize-player-list-for-sending-player-info.patch
(This patch has been removed since Leaf 1.21.8)
*/
@Deprecated(forRemoval = true, since = "1.21.8")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class CollectListWithCondition {

    private static final int PLAYER_COUNT = 500;

    private static class MockPlayer {
        boolean predicate = Math.random() > 0.5;
    }

    private List<MockPlayer> players;
    private Predicate<MockPlayer> condition;

    @Setup(Level.Iteration)
    public void setup() {
        players = new ArrayList<>(PLAYER_COUNT);
        for (int i = 0; i < PLAYER_COUNT; ++i) {
            players.add(new MockPlayer());
        }

        condition = p -> p.predicate;
    }

    @Benchmark
    public List<MockPlayer> filterManually() {
        List<MockPlayer> manualList = new ArrayList<>(players.size());
        for (int index = 0, size = players.size(); index < size; index++) {
            MockPlayer player = players.get(index);
            if (condition.test(player)) {
                manualList.add(player);
            }
        }
        return manualList;
    }

    @Benchmark
    public List<MockPlayer> filterStream() {
        return players.stream().filter(new java.util.function.Predicate<MockPlayer>() {
            @Override
            public boolean test(MockPlayer input) {
                return condition.test(input);
            }
        }).collect(Collectors.toList());
    }

    @Benchmark
    public Collection<MockPlayer> filterGuavaCollections2() {
        return Collections2.filter(players, condition);
    }
}
