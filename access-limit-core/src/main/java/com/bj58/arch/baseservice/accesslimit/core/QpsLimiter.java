package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
public interface QpsLimiter {
    void acquire(final int required);

    void release(final int released);

    QpsLimiter limitUpdated(long newLimit);
}
