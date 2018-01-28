/*
 * Copyright (c) 2018 Vladimir Lysyy (mrbald@github)
 * ALv2 (http://www.apache.org/licenses/LICENSE-2.0)
 */

package net.bobah.ufwj.hash;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Fork(1)
@BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class FixedCapOpenHashBenchmark extends Main {
    private static final Random random = new Random(0);

    private static final List<Integer> keys
            = random.ints(1000).boxed().collect(toList());

    private static final List<Integer> positions
            = random.ints(1 << 8, 0, keys.size()).boxed().collect(toList());

    private static final int modOp = positions.size() - 1;

    private FixedCapOpenHash<Integer, Integer> openHash;

    private HashMap<Integer, Integer> jdkHash;

    private int posIdx = 0;

    private final Integer nextKey() {
        return keys.get(positions.get(posIdx = (posIdx + 1) & modOp));
    }

    @Setup(Level.Iteration)
    public void setUpIteration() {
        openHash = new FixedCapOpenHash<>(2000);
        jdkHash = new HashMap<>(2000);
    }

    @Benchmark
    public int writeOpenHash() {
        final Integer key = nextKey();
        final int pos = openHash.write(key);
        openHash.setValue(pos, key);
        return pos;
    }

    @Benchmark
    public Integer writeJdkHashMap() {
        final Integer key = nextKey();
        return jdkHash.put(key, key);
    }

    @Benchmark
    public Integer keyGenerationOverhead() {
        return nextKey();
    }

    public static void main(String[] argv) throws RunnerException, IOException {
        Main.main(new String[]{FixedCapOpenHashBenchmark.class.getName()});
    }
}

/*

# JMH version: 1.20
# VM version: JDK 1.8.0_112, VM 25.112-b16
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/jre/bin/java
# VM options: -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=54045:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8
# Warmup: 5 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: net.bobah.ufwj.hash.FixedCapOpenHashBenchmark.keyGenerationOverhead

...

# Run complete. Total time: 00:01:05

Benchmark                                                                        Mode     Cnt        Score     Error  Units
FixedCapOpenHashBenchmark.keyGenerationOverhead                                  avgt       5        5.541 ±   0.461  ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap                                        avgt       5       16.228 ±   2.301  ns/op
FixedCapOpenHashBenchmark.writeOpenHash                                          avgt       5       12.914 ±   0.466  ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead                                sample  115048      250.311 ± 253.700  ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.00    sample                4.000            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.50    sample               52.000            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.90    sample               65.000            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.95    sample               78.000            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.99    sample              530.510            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.999   sample            11545.264            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p0.9999  sample           160402.918            ns/op
FixedCapOpenHashBenchmark.keyGenerationOverhead:keyGenerationOverhead·p1.00    sample          7208960.000            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap                                      sample  140034      156.148 ± 109.657  ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.00                sample               15.000            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.50                sample               66.000            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.90                sample               85.000            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.95                sample              100.000            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.99                sample              466.000            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.999               sample             9274.400            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p0.9999              sample            93399.232            ns/op
FixedCapOpenHashBenchmark.writeJdkHashMap:writeJdkHashMap·p1.00                sample          4259840.000            ns/op
FixedCapOpenHashBenchmark.writeOpenHash                                        sample  140993      104.733 ±  12.288  ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.00                    sample               25.000            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.50                    sample               59.000            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.90                    sample               72.000            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.95                    sample               88.000            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.99                    sample              699.060            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.999                   sample             9026.112            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p0.9999                  sample            44373.005            ns/op
FixedCapOpenHashBenchmark.writeOpenHash:writeOpenHash·p1.00                    sample           241664.000            ns/op

 */