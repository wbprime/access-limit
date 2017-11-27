package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.base.Supplier;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class TryQpsLimiterFactory implements Supplier<QpsLimiter>, TryQpsManageLeaf {
    private final String theId;

    private final Object mutex = new Object();
    private final double[] lastQpsArr;
    private int qpsArrIdx;

    private double theQpsLimit;
    private double theLimitMax;
    private double theLimitMin;

    public TryQpsLimiterFactory(final String theId) {
        this.theId = theId;

        this.theLimitMax = 0.0;
        this.theQpsLimit = 0.0;
        this.theLimitMin = 0.0;

        this.lastQpsArr = new double[5];
        for (int i = 0; i < this.lastQpsArr.length; i++) {
            this.lastQpsArr[i] = this.theQpsLimit;
        }
        this.qpsArrIdx = 0;
    }

    @Override
    public String id() {
        return theId;
    }

    @Override
    public QpsLimiter get() {
        return null;
    }

    @Override
    public void adjust(final double newQps) {
        synchronized (mutex) {
            qpsArrIdx = (qpsArrIdx + 1) % lastQpsArr.length;
            lastQpsArr[qpsArrIdx] = newQps;

            adjustMinLimitIfNeeded();
            adjustLimitIfNeeded();
        }
    }

    private void adjustMinLimitIfNeeded() {
        double v = 0.0;
        for (double qps : lastQpsArr) {
            v += qps;
        }

        final double avg = v / lastQpsArr.length;

        if (avg > theLimitMin)  {
            // Increase theLimitMin to the average of lastQpsArr elements
            // if the average value in lastQpsArr was greater than current theLimitMin
            theLimitMin = avg;
        } else {
            // Decrease theLimitMin to the max of lastQpsArr elements
            // only when all values in lastQpsArr were less than current theLimitMin
            double max = avg;
            boolean isGreaterThan = false;
            for (double qps : lastQpsArr) {
                if (qps > theLimitMin) {
                    isGreaterThan = true;
                    break;
                }

                if (qps > max) max = qps;
            }

            if (! isGreaterThan) {
                theLimitMin = max;
            }
        }
    }

    private void adjustLimitIfNeeded() {
        double curLimit = theQpsLimit;

        double max = curLimit - 1;
        for (double qps : lastQpsArr) {
            if (qps > max) max = qps;
        }

        final double growFactor = 1.2;
        if (max > curLimit) {
            curLimit = max * growFactor;
        }

        if (curLimit < theLimitMin) {
            curLimit = theLimitMin;
        }

        if (curLimit > theLimitMax) {
            curLimit = theLimitMax;
        }

        theQpsLimit = curLimit;
    }

    @Override
    public double changeQpsLimit(final double newVal) {
        if (newVal > theLimitMax) {
            theQpsLimit = theLimitMax;
        } else if (newVal < theLimitMin) {
            theQpsLimit = theLimitMin;
        } else {
            theQpsLimit = newVal;
        }

        return theQpsLimit;
    }

    @Override
    public double currentQpsLimit() {
        return theQpsLimit;
    }

    @Override
    public double qpsLimitMax() {
        return theLimitMax;
    }

    @Override
    public double qpsLimitMin() {
        return theLimitMin;
    }
}

