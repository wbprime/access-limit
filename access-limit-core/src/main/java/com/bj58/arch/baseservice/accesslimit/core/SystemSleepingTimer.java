package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
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

    @Override
    public String toString() {
        return "SystemSleepingTimer{}";
    }
}
