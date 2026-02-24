package cn.dreeam.sunbox.benchmarks.jmh;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~36mins
Target patch: Paper PR: Fix MC-117075: Block Entities Unload Lag Spike
Target PR: https://github.com/Winds-Studio/Leaf/pull/649
Original Implementation: https://github.com/PaperMC/Paper/pull/9970
*/
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BlockEntityRemovalBenchmark {

    @Param({"10000", "50000"})
    public int listSize;

    @Param({"0.01", "0.10", "0.50"})
    public double removeFraction;

    @Param({"RANDOM", "CONTIGUOUS"})
    public String removePattern;

    private MockBlockEntity[] sourceArray;
    private List<MockBlockEntity> paperList;
    private OldBlockEntityTickersList leafOldList;
    private BlockEntityTickersList leafNewList;

    @Setup(Level.Trial)
    public void setupTrial() {
        sourceArray = new MockBlockEntity[listSize];
        for (int i = 0; i < listSize; i++) {
            sourceArray[i] = new MockBlockEntity();
        }

        int removeCount = (int) (listSize * removeFraction);

        if ("CONTIGUOUS".equals(removePattern)) {
            int start = listSize / 2 - removeCount / 2;
            for (int i = start; i < start + removeCount; i++) {
                sourceArray[i].removed = true;
            }
        } else {
            Random r = new Random(947);
            int removed = 0;
            while (removed < removeCount) {
                int idx = r.nextInt(listSize);
                if (!sourceArray[idx].removed) {
                    sourceArray[idx].removed = true;
                    removed++;
                }
            }
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        paperList = new ArrayList<>(listSize);
        paperList.addAll(Arrays.asList(sourceArray));
        leafOldList = new OldBlockEntityTickersList(Arrays.asList(sourceArray));
        leafNewList = new BlockEntityTickersList(Arrays.asList(sourceArray));
    }

    @Benchmark
    public void testPaper(Blackhole bh) {
        ReferenceOpenHashSet<MockBlockEntity> toRemove = new ReferenceOpenHashSet<>();
        toRemove.add(null);

        for (int i = 0; i < paperList.size(); i++) {
            MockBlockEntity entity = paperList.get(i);
            if (entity.isRemoved()) {
                toRemove.add(entity);
            } else {
                entity.tick();
            }
        }
        paperList.removeAll(toRemove);
        bh.consume(paperList);
    }

    @Benchmark
    public void testLeafIntArrayListImpl(Blackhole bh) {
        for (int i = 0; i < leafNewList.size(); i++) {
            MockBlockEntity entity = leafNewList.get(i);
            if (entity.isRemoved()) {
                leafNewList.markAsRemoved(i);
            } else {
                entity.tick();
            }
        }
        leafNewList.removeMarkedEntries();
        bh.consume(leafNewList);
    }

    @Benchmark
    public void testLeafIntHashSetImpl(Blackhole bh) {
        for (int i = 0; i < leafOldList.size(); i++) {
            MockBlockEntity entity = leafOldList.get(i);
            if (entity.isRemoved()) {
                leafOldList.markAsRemoved(i);
            } else {
                entity.tick();
            }
        }
        leafOldList.removeMarkedEntries();
        bh.consume(leafOldList);
    }

    public static class MockBlockEntity {
        public boolean removed = false;
        public boolean isRemoved() { return removed; }
        public void tick() {}
    }

    public static final class BlockEntityTickersList extends ReferenceArrayList<MockBlockEntity> {
        private final IntArrayList toRemove = new IntArrayList();
        private int minRemovalIndex = Integer.MAX_VALUE;
        private int lastAddedRemoveIndex = -1;
        private boolean isSorted = true;

        public BlockEntityTickersList() { super(); }
        public BlockEntityTickersList(final Collection<? extends MockBlockEntity> c) { super(c); }

        public void markAsRemoved(final int index) {
            if (index < this.minRemovalIndex) this.minRemovalIndex = index;
            if (index < this.lastAddedRemoveIndex) this.isSorted = false;
            this.lastAddedRemoveIndex = index;
            this.toRemove.add(index);
        }

        public void removeMarkedEntries() {
            if (this.toRemove.isEmpty()) return;
            if (!this.isSorted) {
                IntArrays.quickSort(this.toRemove.elements(), 0, this.toRemove.size());
                minRemovalIndex = this.toRemove.getInt(0);
            }
            removeBySortedIndices();
            this.toRemove.clear();
            this.minRemovalIndex = Integer.MAX_VALUE;
            this.lastAddedRemoveIndex = -1;
            this.isSorted = true;
        }

        private void removeBySortedIndices() {
            if (this.minRemovalIndex >= this.size) return;
            final int[] removeIndices = this.toRemove.elements();
            final int removeCount = this.toRemove.size();
            final Object[] backingArray = this.a;
            int writeIndex = this.minRemovalIndex;
            int prevRemoveIndex = this.minRemovalIndex;

            for (int i = 1; i < removeCount; i++) {
                int currRemoveIndex = removeIndices[i];
                if (currRemoveIndex == prevRemoveIndex) continue;
                int length = currRemoveIndex - (prevRemoveIndex + 1);
                if (length > 0) {
                    System.arraycopy(backingArray, prevRemoveIndex + 1, backingArray, writeIndex, length);
                    writeIndex += length;
                }
                prevRemoveIndex = currRemoveIndex;
            }

            int tailLength = this.size - (prevRemoveIndex + 1);
            if (tailLength > 0) {
                System.arraycopy(backingArray, prevRemoveIndex + 1, backingArray, writeIndex, tailLength);
                writeIndex += tailLength;
            }

            Arrays.fill(backingArray, writeIndex, this.size, null);
            this.size = writeIndex;
        }

        @Override
        public void clear() {
            super.clear();
            this.toRemove.clear();
            this.minRemovalIndex = Integer.MAX_VALUE;
            this.lastAddedRemoveIndex = -1;
            this.isSorted = true;
        }
    }

    public static final class OldBlockEntityTickersList extends ObjectArrayList<MockBlockEntity> {

        private final IntOpenHashSet toRemove = new IntOpenHashSet();
        private int startSearchFromIndex = -1;

        public OldBlockEntityTickersList() {
            super();
        }

        public OldBlockEntityTickersList(final Collection<? extends MockBlockEntity> c) {
            super(c);
        }

        public void markAsRemoved(final int index) {
            // The block entities list always loop starting from 0, so we only need to check if the startSearchFromIndex is -1 and that's it
            if (this.startSearchFromIndex == -1)
                this.startSearchFromIndex = index;

            this.toRemove.add(index);
        }

        public void removeMarkedEntries() {
            if (this.startSearchFromIndex == -1) // No entries in the list, skip
                return;

            removeAllByIndex(startSearchFromIndex, toRemove);
            toRemove.clear();
            this.startSearchFromIndex = -1; // Reset the start search index
        }

        private void removeAllByIndex(final int startSearchFromIndex, final IntOpenHashSet c) { // can't use Set<Integer> because we want to avoid autoboxing when using contains
            final int requiredMatches = c.size();
            if (requiredMatches == 0)
                return; // exit early, we don't need to do anything

            final Object[] a = this.a;
            int writeIndex = startSearchFromIndex;
            int lastCopyIndex = startSearchFromIndex;
            int matches = 0;

            for (int readIndex = startSearchFromIndex; readIndex < size; readIndex++) {
                if (c.contains(readIndex)) {
                    matches++;
                    final int blockLength = readIndex - lastCopyIndex;
                    if (blockLength > 0) {
                        System.arraycopy(a, lastCopyIndex, a, writeIndex, blockLength);
                        writeIndex += blockLength;
                    }
                    lastCopyIndex = readIndex + 1;

                    if (matches == requiredMatches) {
                        break;
                    }
                }
            }

            final int finalBlockLength = size - lastCopyIndex;
            if (finalBlockLength > 0) {
                System.arraycopy(a, lastCopyIndex, a, writeIndex, finalBlockLength);
                writeIndex += finalBlockLength;
            }

            if (writeIndex < size) {
                Arrays.fill(a, writeIndex, size, null);
            }
            size = writeIndex;
        }
    }
}