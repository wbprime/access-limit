package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public final class NoopQpsAdjustStrategy implements QpsAdjustStrategy {
    @Override
    public boolean isAdjustable(double newQps) {
        // Never adjust
        return false;
    }
}
