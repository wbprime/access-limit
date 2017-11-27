package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public interface QpsManageLeaf extends QpsManageNode, QpsAdjustable {
    double qpsLimitMax();

    double qpsLimitMin();

    /**
     * Try to change the cur limit to newVal.
     *
     * Return the actual new limit, may not equal to newVal.
     *
     * @param newVal
     * @return
     */
    double changeQpsLimit(double newVal);

    double currentQpsLimit();
}
