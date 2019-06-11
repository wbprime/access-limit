package com.bj58.arch.baseservice.accesslimit.core;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
class SystemWalkingClock implements WalkingClock {
    private static final long NANOS_PER_MICRO = TimeUnit.MICROSECONDS.toNanos(1);

    private SystemWalkingClock() {
        /* To conform SINGLETON convention */
    }

    private static class SingletonHolder {
        static SystemWalkingClock CLOCK = new SystemWalkingClock();

        static SystemWalkingClock holding() {
            return CLOCK;
        }
    }

    static SystemWalkingClock instance() {
        return SingletonHolder.holding();
    }

    @Override
    public long readCurrentMicros() {
        return System.nanoTime() / NANOS_PER_MICRO;
    }

    @Override
    public String toString() {
        return "SystemWalkingClock{}";
    }
}
