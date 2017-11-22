package com.bj58.arch.baseservice.accesslimit.core;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
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
}
