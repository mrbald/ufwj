/*
 * Copyright (c) 2018 Vladimir Lysyy (mrbald@github)
 * ALv2 (http://www.apache.org/licenses/LICENSE-2.0)
 */

package net.bobah.ufwj.queue;

import java.util.*;

public final class WatermarkQueue<E> extends AbstractQueue<E> {
    private final Queue<E> queue;

    private final int lowerWatermark;
    private final int upperWatermark;
    private final long maxWaitMs;

    private final Runnable onSuspend;
    private final Runnable onResume;

    private boolean producerSuspended = false;

    public WatermarkQueue(int lowerWatermark, int upperWatermark, long maxWaitMs,
                          Runnable onSuspend, Runnable onResume) {
        this.queue = new ArrayDeque<>(lowerWatermark + upperWatermark);
        this.lowerWatermark =lowerWatermark;
        this.upperWatermark = upperWatermark;
        this.maxWaitMs = maxWaitMs;
        this.onSuspend = onSuspend;
        this.onResume = onResume;
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return new ArrayList<>(queue).iterator();
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized boolean offer(E e) {
        final int queueSizeWas = queue.size();

        queue.offer(e);
        if (queueSizeWas == 0) {
            notify();
        }

        if (!producerSuspended && queueSizeWas == upperWatermark - 1) {
            producerSuspended = true;
            onSuspend.run();
        }

        return true;
    }

    @Override
    public synchronized E poll() {
        final int queueSizeWas = queue.size();

        if (queueSizeWas == 0) {
            try {
                wait(maxWaitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        final E e = queue.poll();
        if (e != null) {
            if (producerSuspended && queueSizeWas == lowerWatermark + 1) {
                producerSuspended = false;
                onResume.run();
            }
        }

        return e;
    }

    @Override
    public synchronized E peek() {
        return queue.peek();
    }

    public synchronized boolean isProducerSuspended() {
        return producerSuspended;
    }
}