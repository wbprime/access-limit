package com.bj58.arch.baseservice.accesslimit.core;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class QpsLimiter implements QpsAdjustable {
    private final WalkingClock clock;
    private final SleepingTimer timer;

    private final long microsPerMeasure;

    private volatile int permitsPerMeasure;

    private final Object mutex = new Object();
    private volatile long nextFreeMicros;

    private volatile int freePermits;

    public QpsLimiter(final int seconds, final int permits) {
        this(seconds, permits, SystemWalkingClock.instance(), SystemSleepingTimer.instance());
    }

    QpsLimiter(
            final int seconds, final int permits,
            final WalkingClock clock,
            final SleepingTimer timer
    ) {
        this.clock = clock;
        this.timer = timer;
        this.permitsPerMeasure = permits;
        this.microsPerMeasure = TimeUnit.SECONDS.toMicros(seconds);
    }

    @Override
    public void adjust(final double qps) {
        permitsPerMeasure = (int) (qps * TimeUnit.SECONDS.toMicros(1) * microsPerMeasure);
    }

    public void acquire(final int val) {
        final long sleepingMicros = reserve(val);
        timer.sleep(sleepingMicros, TimeUnit.MICROSECONDS);
    }

    public void release(final int val) {
        /* Do nothing */
    }

    private long reserve(final int required) {
        synchronized (mutex) {
            final long curMicros = clock.readCurrentMicros();

            resync(curMicros);
            return reserveAndGetWaitMicros(required, curMicros);
        }
    }

    private long reserveAndGetWaitMicros(final int required, final long curMicros) {
        final int availPermits = freePermits;
        final long nextMicros = (availPermits <= 0) ? nextFreeMicros : curMicros;

        final int takenPermits = Math.min(availPermits, required);
        freePermits -= takenPermits;

        final int freshPermits = required - takenPermits;
        final long waitMicros = (freshPermits == 0) ? 0L :
                (long) (freshPermits * 1.0 * microsPerMeasure / permitsPerMeasure);
        nextFreeMicros += waitMicros;

        return Math.max(nextMicros - curMicros, 0L);
    }

    private void resync(final long curMicros) {
        if (nextFreeMicros < curMicros) {
            nextFreeMicros = curMicros + microsPerMeasure;
            freePermits = permitsPerMeasure;
        }
    }
}
