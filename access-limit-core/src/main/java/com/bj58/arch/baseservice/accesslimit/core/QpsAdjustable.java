package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public interface QpsAdjustable {
    /**
     * Adjust to QPS value {@code qps}.
     *
     * @param qps QPS value
     */
    void adjust(final double qps);
}
