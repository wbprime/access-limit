package com.bj58.arch.baseservice.accesslimit.core;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
interface SleepingTimer {
    void sleep(final long duration, final TimeUnit unit);
}
