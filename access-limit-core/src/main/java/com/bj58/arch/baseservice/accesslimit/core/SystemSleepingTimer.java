package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
class SystemSleepingTimer implements SleepingTimer {
    private SystemSleepingTimer() {
        /* To conform SINGLETON convention */
    }

    private static class SingletonHolder {
        static SystemSleepingTimer TIMER = new SystemSleepingTimer();

        static SystemSleepingTimer holding() {
            return TIMER;
        }
    }

    static SystemSleepingTimer instance() {
        return SingletonHolder.holding();
    }

    @Override
    public void sleep(long duration, TimeUnit unit) {
        if (duration > 0L) {
            Uninterruptibles.sleepUninterruptibly(duration, unit);
        }
    }
}
