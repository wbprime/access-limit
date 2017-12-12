package com.bj58.arch.baseservice.accesslimit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
final class StdQpsLimiter implements QpsLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(StdQpsLimiter.class);

    private final WalkingClock clock;
    private final SleepingTimer timer;

    private final long microsPerMeasure;

    private long permitsPerMeasure;

    private final Object mutex = new Object();
    private long nextFreeMicros;

    private long freePermits;

    StdQpsLimiter(
            final long microsPerMeasure,
            final long permitsPerMeasure,
            final WalkingClock clock,
            final SleepingTimer timer
    ) {
        this.clock = clock;
        this.timer = timer;
        this.microsPerMeasure = microsPerMeasure;
        this.permitsPerMeasure = permitsPerMeasure;
    }

    @Override
    public void acquire(final int required) {
        final long sleepingMicros = reserve(required);

        if (LOGGER.isDebugEnabled()) {
            final long nano = System.nanoTime();
            LOGGER.debug("[{}] Begin to sleep {} us", nano, sleepingMicros);
            timer.sleep(sleepingMicros, TimeUnit.MICROSECONDS);
            LOGGER.debug("[{}] Finish to sleep {} us", nano, sleepingMicros);
        } else {
            timer.sleep(sleepingMicros, TimeUnit.MICROSECONDS);
        }
    }

    @Override
    public void release(final int released) {
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
        final long availPermits = freePermits;
        final long nextMicros = (availPermits <= 0) ? nextFreeMicros : curMicros;

        final long takenPermits = Math.min(availPermits, required);
        freePermits -= takenPermits;

        final long freshPermits = required - takenPermits;
        final long waitMicros = (long) (freshPermits * 1.0 * microsPerMeasure / permitsPerMeasure);
        nextFreeMicros += waitMicros;

        return Math.max(nextMicros - curMicros, 0L);
    }

    private void resync(final long curMicros) {
        if (nextFreeMicros < curMicros) {
            nextFreeMicros = curMicros + microsPerMeasure;
            freePermits = permitsPerMeasure;
        }
    }

    @Override
    public synchronized StdQpsLimiter limitUpdated(final long limit) {
        final long oldLimit = permitsPerMeasure;
        permitsPerMeasure = limit;

        LOGGER.debug("Permits changed: [{}] -> [{}]", oldLimit, limit);

        return this;
    }

    @Override
    public String toString() {
        return "StdQpsLimiter{" +
                ", clock=" + clock +
                ", timer=" + timer +
                ", microsPerMeasure=" + microsPerMeasure +
                ", permitsPerMeasure=" + permitsPerMeasure +
                ", nextFreeMicros=" + nextFreeMicros +
                ", freePermits=" + freePermits +
                '}';
    }
}
