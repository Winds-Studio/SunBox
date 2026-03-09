package cn.dreeam.sunbox.benchmarks.jmh;

import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.PriorityQueues;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/*
Estimate running time: ~4mins
Target class: gg.pufferfish.pufferfish.util.AsyncExecutor
*/
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Threads(1)
public class AsyncExecutorBench {

    private static final int TASK_COUNT = 100;

    private AsyncExecutorLeafNewDreeam asyncExecutorLeafNewDreeam;
    private AsyncExecutorLeafNewHaya asyncExecutorLeafNewHaya;
    private AsyncExecutorLeafOld asyncExecutorLeafOld;
    private AsyncExecutorPufferfish asyncExecutorPufferfish;

    @Setup(Level.Trial)
    public void setup() {
        asyncExecutorLeafNewDreeam = new AsyncExecutorLeafNewDreeam("Async Executor - leaf-new-dreeam");
        asyncExecutorLeafNewHaya = new AsyncExecutorLeafNewHaya("Async Executor - leaf-new-haya");
        asyncExecutorLeafOld = new AsyncExecutorLeafOld("Async Executor - leaf-old");
        asyncExecutorPufferfish = new AsyncExecutorPufferfish("Async Executor - pufferfish");

        asyncExecutorLeafNewDreeam.start();
        asyncExecutorLeafNewHaya.start();
        asyncExecutorLeafOld.start();
        asyncExecutorPufferfish.start();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        asyncExecutorLeafNewDreeam.join(2000);
        asyncExecutorLeafNewHaya.join(2000);
        asyncExecutorLeafOld.join(2000);
        asyncExecutorPufferfish.kill();
    }

    @Benchmark
    public void executorLeafNewDreeam(Blackhole blackhole) throws Exception {
        CountDownLatch latch = new CountDownLatch(TASK_COUNT);

        for (int i = 0; i < TASK_COUNT; i++) {
            asyncExecutorLeafNewDreeam.submit(() -> {
                blackhole.consume(1);
                latch.countDown();
            });
        }

        latch.await();
    }

    @Benchmark
    public void executorLeafNewHaya(Blackhole blackhole) throws Exception {
        CountDownLatch latch = new CountDownLatch(TASK_COUNT);

        for (int i = 0; i < TASK_COUNT; i++) {
            asyncExecutorLeafNewHaya.submit(() -> {
                blackhole.consume(1);
                latch.countDown();
            });
        }

        latch.await();
    }

    @Benchmark
    public void executorLeafOld(Blackhole blackhole) throws Exception {
        CountDownLatch latch = new CountDownLatch(TASK_COUNT);

        for (int i = 0; i < TASK_COUNT; i++) {
            asyncExecutorLeafOld.submit(() -> {
                blackhole.consume(1);
                latch.countDown();
            });
        }

        latch.await();
    }

    @Benchmark
    public void executorPufferfish(Blackhole blackhole) throws Exception {
        CountDownLatch latch = new CountDownLatch(TASK_COUNT);

        for (int i = 0; i < TASK_COUNT; i++) {
            asyncExecutorPufferfish.submit(() -> {
                blackhole.consume(1);
                latch.countDown();
            });
        }

        latch.await();
    }

    private static class AsyncExecutorLeafNewDreeam implements Runnable {

        private final PriorityQueue<Runnable> jobs = new ObjectArrayFIFOQueue<>();
        public final Thread thread;

        private volatile boolean killswitch = false;

        public AsyncExecutorLeafNewDreeam(String threadName) {
            this.thread = Thread.ofPlatform()
                    .name(threadName)
                    .priority(Thread.NORM_PRIORITY - 1)
                    .daemon(false)
                    //.uncaughtExceptionHandler(Util::onThreadException)
                    .unstarted(this);
        }

        public void start() {
            thread.start();
        }

        public void join(long millis) throws InterruptedException {
            killswitch = true;
            LockSupport.unpark(thread);
            thread.join(millis);
        }

        public void submit(Runnable runnable) {
            synchronized (jobs) {
                jobs.enqueue(runnable);
            }
            LockSupport.unpark(thread);
        }

        @Override
        public void run() {
            while (!killswitch) {
                try {
                    Runnable runnable = null;

                    synchronized (jobs) {
                        if (!jobs.isEmpty()) {
                            runnable = jobs.dequeue();
                        }
                    }

                    if (runnable == null) {
                        LockSupport.park();
                        continue;
                    }

                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class AsyncExecutorLeafNewHaya implements Runnable {

        private final PriorityQueue<Runnable> jobs = new ObjectArrayFIFOQueue<>();
        public final Thread thread;
        private volatile boolean killswitch = false;

        public AsyncExecutorLeafNewHaya(String threadName) {
            this.thread = Thread.ofPlatform()
                    .name(threadName)
                    .priority(Thread.NORM_PRIORITY - 1)
                    .daemon(false)
                    //.uncaughtExceptionHandler(Util::onThreadException)
                    .unstarted(this);
        }

        public void start() {
            thread.start();
        }

        public void join(long millis) throws InterruptedException {
            killswitch = true;
            LockSupport.unpark(thread);
            thread.join(millis);
        }

        public void submit(Runnable runnable) {
            synchronized (jobs) {
                jobs.enqueue(runnable);
            }
            LockSupport.unpark(thread);
        }

        @Override
        public void run() {
            while (!killswitch) {
                LockSupport.parkNanos(1); // TODO: IDK why
                //LockSupport.park();
                if (Thread.interrupted()) {
                    return;
                }
                try {
                    Runnable runnable;
                    synchronized (jobs) {
                        if (jobs.isEmpty()) {
                            continue;
                        }
                        runnable = jobs.dequeue();
                    }
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class AsyncExecutorLeafOld implements Runnable {

        private final PriorityQueue<Runnable> jobs = PriorityQueues.synchronize(new ObjectArrayFIFOQueue<>());
        public final Thread thread;
        private volatile boolean killswitch = false;

        public AsyncExecutorLeafOld(String threadName) {
            this.thread = Thread.ofPlatform()
                    .name(threadName)
                    .priority(Thread.NORM_PRIORITY - 1)
                    .daemon(false)
                    //.uncaughtExceptionHandler(Util::onThreadException)
                    .unstarted(this);
        }

        public void start() {
            thread.start();
        }

        public void join(long millis) throws InterruptedException {
            killswitch = true;
            LockSupport.unpark(thread);
            thread.join(millis);
        }

        public void submit(Runnable runnable) {
            jobs.enqueue(runnable);
            LockSupport.unpark(thread);
        }

        @Override
        public void run() {
            while (!killswitch) {
                try {
                    Runnable runnable;
                    try {
                        runnable = jobs.dequeue();
                    } catch (NoSuchElementException e) {
                        LockSupport.park();
                        continue;
                    }
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class AsyncExecutorPufferfish implements Runnable {

        private final Queue<Runnable> jobs = Queues.newArrayDeque();
        private final Lock mutex = new ReentrantLock();
        private final Condition cond = mutex.newCondition();
        private final Thread thread;
        private volatile boolean killswitch = false;

        public AsyncExecutorPufferfish(String threadName) {
            this.thread = new Thread(this, threadName);
        }

        public void start() {
            thread.start();
        }

        public void kill() {
            killswitch = true;
            cond.signalAll();
        }

        public void submit(Runnable runnable) {
            mutex.lock();
            try {
                jobs.offer(runnable);
                cond.signalAll();
            } finally {
                mutex.unlock();
            }
        }

        @Override
        public void run() {
            while (!killswitch) {
                try {
                    Runnable runnable = takeRunnable();
                    if (runnable != null) {
                        runnable.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private Runnable takeRunnable() throws InterruptedException {
            mutex.lock();
            try {
                while (jobs.isEmpty() && !killswitch) {
                    cond.await();
                }

                if (jobs.isEmpty()) return null; // We've set killswitch

                return jobs.remove();
            } finally {
                mutex.unlock();
            }
        }
    }
}
