package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@Deprecated
public class OpGroup implements OpItem {
    private final OpGroup parent;
    private final String name;

    private final List<OpItem> items;

    public OpGroup(
            final OpGroup group,
            final String name
    ) {
        this.parent = group;
        this.name = name;


        this.items = Lists.newArrayList();

        if (null != this.parent) {
            this.parent.addAdjustItem(this);
        }
    }

    @Override
    public double maxLimit() {
        double v = 0.0;
        for (final OpItem item : items) {
            v += item.maxLimit();
        }
        return v;
    }

    @Override
    public double minLimit() {
        double v = 0.0;
        for (final OpItem item : items) {
            v += item.minLimit();
        }
        return v;
    }

    @Override
    public double currentLimit() {
        double v = 0.0;
        for (final OpItem item : items) {
            v += item.currentLimit();
        }
        return v;
    }

    @Override
    public void setLimit(double limit) {
        final double max = maxLimit();
        final double min = minLimit();

        if (limit > max) {
            for (final OpItem item : items) {
                item.setLimit(item.maxLimit());
            }
        } else if (limit < min) {
            for (final OpItem item : items) {
                item.setLimit(item.minLimit());
            }
        } else {
            final double cur = currentLimit();
            final double percent = limit / cur;
            for (final OpItem item : items) {
                item.setLimit(item.currentLimit() * percent);
            }
        }
    }

    void addAdjustItem(final OpItem newItem) {
        double curLimit = 0.0;
        for (final OpItem item : items) {
            curLimit += item.currentLimit();
        }

        final double maxLimit = 0.0; // TODO

        final double limitLeft = maxLimit - curLimit;
        if (limitLeft >= newItem.maxLimit()) {
            newItem.setLimit(newItem.maxLimit());
        } else if (limitLeft >= newItem.minLimit()) {
            newItem.setLimit(limitLeft);
        } else {
            // Try to steal some limit from existing items

            double totalDiff = 0.0;
            for (final OpItem item : items) {
                totalDiff += (item.currentLimit() - item.minLimit());
            }

            checkState(limitLeft + totalDiff >= newItem.minLimit(), "Min limit for a group should cover its children but not");

            final double percent =
                    (newItem.minLimit() - limitLeft) / totalDiff;

            for (final OpItem item : items) {
                final double oldLimit = item.currentLimit();
                final double newLimit = oldLimit - percent * (oldLimit - item.minLimit());
                item.setLimit(newLimit);
            }
            newItem.setLimit(newItem.minLimit());
        }

        items.add(newItem);
    }

    private double sendRequestToParentIfExists(final OpItem opItem, double newVal) {
        if (null != this.parent) {
            double v = 0.0;
            for (final OpItem item : items) {
                if (item != opItem) {
                    v += item.currentLimit();
                } else {
                    v += newVal;
                }
            }

            return this.parent.sizeEvent(this, v);
        } else {
            return newVal;
        }
    }

    double sizeEvent(final OpItem opItem, double newVal) {

        final double oldVal = opItem.currentLimit();
        double delta = newVal - oldVal;

        // If not request more limit, do nothing
        if (delta <= 0.0) {
            return oldVal;
        }

        // First check if unused limit available
        {
            final double curLimit = currentLimit();
            final double maxLimit = maxLimit();
            if (maxLimit - curLimit >= delta) {
                return newVal;
            }
        }

        // If not, try to increase limit by send message to parent
        final double availLimit = sendRequestToParentIfExists(opItem, newVal);
        final double curLimit = currentLimit();
        if (availLimit - curLimit >= delta) {
            return newVal;
        }

        double totalDiff = 0.0;
        for (final OpItem item : items) {
            totalDiff += (item.currentLimit() - item.minLimit());
        }

        if (totalDiff < delta) delta = totalDiff;

        final double percent = delta / totalDiff;

        for (final OpItem item : items) {
            final double oldLimit = item.currentLimit();
            final double newLimit = oldLimit - percent * (oldLimit - item.minLimit());
            item.setLimit(newLimit);
        }

        opItem.setLimit(oldVal + delta);

        return 0.0; // TODO
    }

    @Override
    public String toString() {
        return "OpGroup{" +
                "parent=" + parent +
                ", name='" + name + '\'' +
                '}';
    }
}
