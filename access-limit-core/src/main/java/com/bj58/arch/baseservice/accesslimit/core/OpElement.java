package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@Deprecated
public class OpElement implements OpItem {
    private final OpGroup parent;
    private final String name;

    private final double maxOfQpsLimit;
    private final double minOfQpsLimit;
    private double curQpsLimit;

    public OpElement(
            final OpGroup group,
            final String name,
            double maxOfQpsLimit,
            double minOfQpsLimit
    ) {
        this.parent = group;
        this.name = name;
        this.maxOfQpsLimit = maxOfQpsLimit;
        this.minOfQpsLimit = minOfQpsLimit;

        if (null != this.parent) {
            this.parent.addAdjustItem(this);
        }
    }

    @Override
    public double maxLimit() {
        return maxOfQpsLimit;
    }

    @Override
    public double minLimit() {
        return minOfQpsLimit;
    }

    @Override
    public double currentLimit() {
        return curQpsLimit;
    }

    @Override
    public void setLimit(final double limit) {
        this.curQpsLimit = limit;
    }

    public void resize(final double newLimit) {
        double newVal = newLimit;
        if (newVal > maxOfQpsLimit) newVal = maxOfQpsLimit;
        if (newVal < minOfQpsLimit) newVal = minOfQpsLimit;

        if (null != parent && newVal != curQpsLimit) {
            parent.sizeEvent(this, newVal);
        }
    }

    @Override
    public String toString() {
        return "OpElement{" +
                "parent=" + parent +
                ", name='" + name + '\'' +
                ", maxOfQpsLimit=" + maxOfQpsLimit +
                ", minOfQpsLimit=" + minOfQpsLimit +
                ", curQpsLimit=" + curQpsLimit +
                '}';
    }
}
