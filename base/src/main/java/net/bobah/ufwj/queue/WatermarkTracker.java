/*
 * Copyright (c) 2018 Vladimir Lysyy (mrbald@github)
 * ALv2 (http://www.apache.org/licenses/LICENSE-2.0)
 */

package net.bobah.ufwj.queue;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A blocking queue wrapper implementing watermarks-based back pressure propagation
 * from the queue sink to one or more logical sources.
 *
 * @param <E> payload type
 * @param <S> logical source key type
 *
 * @author Vladimir Lysyy (mrbald@github)
 */
public class WatermarkTracker<E,S> {
    private final BlockingQueue<E> queue;
    private final long lowerWatermark;
    private final long upperWatermark;
    private final Consumer<S> onLowerWatermarkCrossed;
    private final Consumer<S> onUpperWatermarkCrossed;
    private final Function<E, S> classifier;
    private final Function<S, StreamTracker> trackerSupplier;

    class StreamTracker {
        private final S key;
        long counter = 0;
        private boolean suspended = false;

        StreamTracker(S key) {
            this.key = key;
        }

        synchronized void incoming(int n) {
            if ((counter += n) >= upperWatermark && !suspended) {
                suspended = true;
                onUpperWatermarkCrossed.accept(key);
            }
        }

        synchronized void outgoing(int n) {
            if ((counter -= n) == lowerWatermark && suspended) {
                suspended = false;
                onLowerWatermarkCrossed.accept(key);
            }
        }

        synchronized boolean isSuspended() {
            return suspended;
        }
    }

    public static <E, Void> WatermarkTracker<E, Void> newMono(
            BlockingQueue<E> queue,
            long lowerWatermark, long upperWatermark,
            Runnable onLowerWatermarkCrossed, Runnable onUpperWatermarkCrossed) {
        return new WatermarkTracker<E, Void>(queue, lowerWatermark, upperWatermark, onLowerWatermarkCrossed, onUpperWatermarkCrossed);
    }

    public static <E, S> WatermarkTracker<E, S> newMulti(
            BlockingQueue<E> queue,
            long lowerWatermark, long upperWatermark,
            Function<E, S> classifier,
            Consumer<S> onLowerWatermarkCrossed, Consumer<S> onUpperWatermarkCrossed) {
        return new WatermarkTracker<>(queue, lowerWatermark, upperWatermark, classifier, onLowerWatermarkCrossed, onUpperWatermarkCrossed);
    }

    private WatermarkTracker(
            BlockingQueue<E> queue,
            long lowerWatermark, long upperWatermark,
            Function<E, S> classifier,
            Consumer<S> onLowerWatermarkCrossed, Consumer<S> onUpperWatermarkCrossed) {
        assert lowerWatermark >= 0 && lowerWatermark < upperWatermark;

        this.queue = queue;
        this.lowerWatermark = lowerWatermark;
        this.upperWatermark = upperWatermark;
        this.classifier = classifier;
        this.onLowerWatermarkCrossed = onLowerWatermarkCrossed;
        this.onUpperWatermarkCrossed = onUpperWatermarkCrossed;

        final Map<S, StreamTracker> trackerMap = new ConcurrentHashMap<>();

        this.trackerSupplier = key -> trackerMap.computeIfAbsent(key, StreamTracker::new);
    }

    private WatermarkTracker(
            BlockingQueue<E> queue,
            long lowerWatermark, long upperWatermark,
            Runnable onLowerWatermarkCrossed, Runnable onUpperWatermarkCrossed) {
        assert lowerWatermark >= 0 && lowerWatermark < upperWatermark;

        this.queue = queue;
        this.lowerWatermark = lowerWatermark;
        this.upperWatermark = upperWatermark;
        this.classifier = x -> null;
        this.onLowerWatermarkCrossed = x -> onLowerWatermarkCrossed.run();
        this.onUpperWatermarkCrossed = x -> onUpperWatermarkCrossed.run();

        final StreamTracker streamTracker = new StreamTracker(null);

        this.trackerSupplier = key -> streamTracker;
    }

    public void add(E e) {
        queue.offer(e);
        trackerForPayload(e).incoming(1);
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        final E e = queue.poll(timeout, unit);

        if (e != null) {
            trackerForPayload(e).outgoing(1);
        }

        return e;
    }

    public boolean isSuspended(S key) {
        return trackerForStream(key).isSuspended();
    }

    public boolean isSuspended() {
        return isSuspended(null);
    }

    StreamTracker trackerForPayload(E e) {
        return trackerForStream(classifier.apply(e));
    }

    StreamTracker trackerForStream(S s) {
        return trackerSupplier.apply(s);
    }

}
