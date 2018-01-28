/*
 * Copyright (c) 2018 Vladimir Lysyy (mrbald@github)
 * ALv2 (http://www.apache.org/licenses/LICENSE-2.0)
 */

package net.bobah.ufwj.queue;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WatermarkQueueBenchmark {
    private static final Object DUMMY = new Object();

    @Fork(1)
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @Warmup(iterations = 5, time = 3)
    @Measurement(iterations = 5, time = 3)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @State(Scope.Group)
    public static class Watermarked {
        private WatermarkQueue<Object> queue
                = new WatermarkQueue<>(2000, 8000, 1000L, ()->{}, ()->{});

        @Benchmark
        @Group("watermarked")
        @GroupThreads(1)
        public void produce() throws InterruptedException {
            while (queue.isProducerSuspended()) {
                Thread.yield();
            }
            queue.add(DUMMY);
        }

        @Benchmark
        @Group("watermarked")
        @GroupThreads(1)
        public Object consume() throws InterruptedException {
            return queue.poll();
        }
    }

    public static void main(String[] argv) throws RunnerException, IOException {
        Main.main(new String[]{WatermarkQueueBenchmark.class.getName()});
    }
}

/*

# JMH version: 1.20
# VM version: JDK 1.8.0_112, VM 25.112-b16
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/bin/java
# VM options: -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=54306:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8
# Warmup: 5 iterations, 3 s each
# Measurement: 5 iterations, 3 s each
# Timeout: 10 min per iteration
# Threads: 2 threads (1 group; 1x "consume", 1x "produce" in each group), will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: net.bobah.ufwj.queue.WatermarkQueueBenchmark.Watermarked.watermarked

...

# Run complete. Total time: 00:01:02

Benchmark                                                              Mode     Cnt        Score    Error  Units
WatermarkQueueBenchmark.Watermarked.watermarked                        avgt       5      126.417 ± 42.975  ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume                avgt       5      126.416 ± 42.971  ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce                avgt       5      126.419 ± 42.980  ns/op
WatermarkQueueBenchmark.Watermarked.watermarked                      sample  736669     1097.825 ± 46.252  ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume              sample  368294     1177.954 ± 58.397  ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.00        sample                6.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.50        sample               79.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.90        sample             1194.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.95        sample             2936.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.99        sample            24193.600           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.999       sample            46573.120           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p0.9999      sample           146106.368           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:consume·p1.00        sample          5218304.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce              sample  368375     1017.713 ± 71.737  ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.00        sample                2.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.50        sample              100.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.90        sample              902.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.95        sample             2304.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.99        sample            21216.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.999       sample            45735.936           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p0.9999      sample           301042.893           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:produce·p1.00        sample          6643712.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.00    sample                2.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.50    sample               97.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.90    sample              986.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.95    sample             2592.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.99    sample            21888.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.999   sample            46208.000           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p0.9999  sample           282282.496           ns/op
WatermarkQueueBenchmark.Watermarked.watermarked:watermarked·p1.00    sample          6643712.000           ns/op

 */