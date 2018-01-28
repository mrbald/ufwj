/*
 * Copyright (c) 2018 Vladimir Lysyy (mrbald@github)
 * ALv2 (http://www.apache.org/licenses/LICENSE-2.0)
 */

package net.bobah.ufwj.queue;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.concurrent.*;

public class WatermarkTrackerBenchmark extends Main {
    private static final Object DUMMY = new Object();

    @Fork(1)
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @Warmup(iterations = 5, time = 3)
    @Measurement(iterations = 5, time = 3)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @State(Scope.Group)
    public static class Mono {
        private BlockingQueue<Object> queue = new LinkedBlockingDeque<Object>();
        private WatermarkTracker<Object, String> tracker
                = WatermarkTracker.newMono(queue, 2000, 8000, ()->{}, ()->{});

        @Benchmark
        @Group("mono")
        @GroupThreads(1)
        public void produce() throws InterruptedException {
            while (tracker.isSuspended(null)) {
                Thread.yield();
            }
            tracker.add(DUMMY);
        }

        @Benchmark
        @Group("mono")
        @GroupThreads(1)
        public Object consume() throws InterruptedException {
            return tracker.poll(1, TimeUnit.HOURS);
        }
    }



    @Fork(1)
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @Warmup(iterations = 5, time = 3)
    @Measurement(iterations = 5, time = 3)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @State(Scope.Group)
    public static class Multi {
        private static final String DUMMY_KEY = "42";

        private BlockingQueue<Object> queue = new LinkedBlockingDeque<Object>();
        private WatermarkTracker<Object, String> tracker
                = WatermarkTracker.newMulti(queue, 2000, 8000, x->DUMMY_KEY, x->{}, x->{});

        @Benchmark
        @Group("multi")
        @GroupThreads(1)
        public void produce() throws InterruptedException {
            while (tracker.isSuspended(DUMMY_KEY)) {
                Thread.yield();
            }
            tracker.add(DUMMY);
        }

        @Benchmark
        @Group("multi")
        @GroupThreads(1)
        public Object consume() throws InterruptedException {
            return tracker.poll(1, TimeUnit.HOURS);
        }
    }

    public static void main(String[] argv) throws RunnerException, IOException {
        Main.main(new String[]{WatermarkTrackerBenchmark.class.getName()});
    }
}

/*

# JMH version: 1.20
# VM version: JDK 1.8.0_112, VM 25.112-b16
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/bin/java
# VM options: -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=54292:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8
# Warmup: 5 iterations, 3 s each
# Measurement: 5 iterations, 3 s each
# Timeout: 10 min per iteration
# Threads: 2 threads (1 group; 1x "consume", 1x "produce" in each group), will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: net.bobah.ufwj.queue.WatermarkTrackerBenchmark.Mono.mono

...

# Benchmark: net.bobah.ufwj.queue.WatermarkTrackerBenchmark.Multi.multi

...

# Run complete. Total time: 00:02:04

Benchmark                                                Mode     Cnt        Score    Error  Units
WatermarkTrackerBenchmark.Mono.mono                      avgt       5      298.764 ± 19.920  ns/op
WatermarkTrackerBenchmark.Mono.mono:consume              avgt       5      298.753 ± 19.917  ns/op
WatermarkTrackerBenchmark.Mono.mono:produce              avgt       5      298.775 ± 19.923  ns/op
WatermarkTrackerBenchmark.Multi.multi                    avgt       5      312.544 ±  9.693  ns/op
WatermarkTrackerBenchmark.Multi.multi:consume            avgt       5      312.526 ±  9.664  ns/op
WatermarkTrackerBenchmark.Multi.multi:produce            avgt       5      312.562 ±  9.723  ns/op
WatermarkTrackerBenchmark.Mono.mono                    sample  802297      509.114 ± 39.619  ns/op
WatermarkTrackerBenchmark.Mono.mono:consume            sample  401528      481.194 ± 66.722  ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.00      sample               28.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.50      sample              104.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.90      sample              472.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.95      sample              641.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.99      sample             9088.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.999     sample            34496.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p0.9999    sample            90044.288           ns/op
WatermarkTrackerBenchmark.Mono.mono:consume·p1.00      sample          5529600.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.00         sample                3.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.50         sample              119.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.90         sample              526.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.95         sample              760.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.99         sample             9808.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.999        sample            34348.928           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p0.9999       sample            85415.757           ns/op
WatermarkTrackerBenchmark.Mono.mono:mono·p1.00         sample          5529600.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce            sample  400769      537.086 ± 42.684  ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.00      sample                3.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.50      sample              122.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.90      sample              597.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.95      sample              860.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.99      sample            10896.000           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.999     sample            34190.720           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p0.9999    sample            82097.152           ns/op
WatermarkTrackerBenchmark.Mono.mono:produce·p1.00      sample          4050944.000           ns/op
WatermarkTrackerBenchmark.Multi.multi                  sample  793106      458.434 ± 14.643  ns/op
WatermarkTrackerBenchmark.Multi.multi:consume          sample  397023      437.476 ± 21.038  ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.00    sample                9.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.50    sample              108.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.90    sample              448.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.95    sample              587.800           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.99    sample             8544.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.999   sample            33280.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p0.9999  sample            85196.186           ns/op
WatermarkTrackerBenchmark.Multi.multi:consume·p1.00    sample          1468416.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.00      sample                1.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.50      sample              119.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.90      sample              540.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.95      sample              744.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.99      sample             8528.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.999     sample            30592.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p0.9999    sample            82056.691           ns/op
WatermarkTrackerBenchmark.Multi.multi:multi·p1.00      sample          1513472.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce          sample  396083      479.441 ± 20.370  ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.00    sample                1.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.50    sample              128.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.90    sample              630.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.95    sample              843.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.99    sample             8512.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.999   sample            28288.000           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p0.9999  sample            75898.624           ns/op
WatermarkTrackerBenchmark.Multi.multi:produce·p1.00    sample          1513472.000           ns/op

 */