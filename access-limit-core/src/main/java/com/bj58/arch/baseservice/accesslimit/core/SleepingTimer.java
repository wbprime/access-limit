package com.bj58.arch.baseservice.accesslimit.core;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
interface SleepingTimer {
    void sleep(final long duration, final TimeUnit unit);
}
