package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.primitives.Longs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class StdQpsManageLeaf implements QpsManageLeaf {
    private static final Logger LOGGER = LoggerFactory.getLogger(StdQpsManageLeaf.class);

    private final QpsManageGroup parentGroup;

    private final AccessMethodContext context;

    // The soft limit for min/max limit
    private long theMinLimit;
    private long theLimit;

    private final Object mutex = new Object();
    private final long[] lastAccessArr;
    private int accessArrIdx;

    public StdQpsManageLeaf(
            final QpsManageGroup group,
            final AccessMethodContext context
    ) {
        this(group, context, 2);
    }

    public StdQpsManageLeaf(
            final QpsManageGroup group,
            final AccessMethodContext context,
            final int arrLen
    ) {
        this.context = context;
        this.theMinLimit = context.minLimit();
        this.theLimit = context.maxLimit();

        group.addChild(this);
        this.parentGroup = group;

        this.lastAccessArr = new long[arrLen];
        for (int i = 0; i < this.lastAccessArr.length; i++) {
            this.lastAccessArr[i] = this.theLimit;
        }
        this.accessArrIdx = 0;
    }

    @Override
    public void onQpsChanged(final QpsChangeEvent event) {
        synchronized (mutex) {
            if (event.periodInMicros() != 0L) {
                accessArrIdx = (accessArrIdx + 1) % lastAccessArr.length;
                lastAccessArr[accessArrIdx] =
                        (long) (event.permits() * 1.0 / event.periodInMicros() * parentGroup.context().periodInMicros());
            } else {
                lastAccessArr[accessArrIdx] += event.permits();
            }

            adjustMinLimitIfNeeded();

            final long expected = adjustLimitIfNeeded();

            if (expected > theLimit) {
                parentGroup.onQpsLimitRequested(
                        QpsLimitRequestEvent.builder()
                                .sourceId(event.sourceId())
                                .currentLimit(theLimit)
                                .expectedLimit(expected)
                                .build()
                );
            }
        }
    }

    @Override
    public void adjustMaxQpsLimit(final long permits) {
        final double oldLimit = theLimit;
        if (permits > context.maxLimit()) {
            theLimit = context.maxLimit();
        } else if (permits < theMinLimit) {
            theLimit = theMinLimit;
        } else {
            theLimit = permits;
        }
        LOGGER.debug("QPS limit max for \"{}\" changed: [{}] -> [{}]", context.id(), oldLimit, theLimit);
    }

    private void adjustMinLimitIfNeeded() {
        double v = 0.0;
        for (long qps : lastAccessArr) {
            v += qps;
        }

        final long avg = (long)(v / lastAccessArr.length);

        if (avg > theMinLimit)  {
            // Increase theMinLimit to the average of lastAccessArr elements
            // if the average value in lastAccessArr was greater than current theMinLimit
            theMinLimit = Math.min(avg, theLimit);
        } else {
            // Decrease theMinLimit to the max of lastAccessArr elements
            // only when all values in lastAccessArr were less than current theMinLimit
            long max = avg;
            boolean isGreaterThan = false;
            for (long qps : lastAccessArr) {
                if (qps > theMinLimit) {
                    isGreaterThan = true;
                    break;
                }

                if (qps > max) max = qps;
            }

            if (! isGreaterThan) {
                theMinLimit = Math.max(max, context.minLimit());
            }
        }
    }

    private long adjustLimitIfNeeded() {
        long curLimit = theLimit;

        long max = Longs.max(lastAccessArr);

        if (max >= curLimit) {
            curLimit = growLimit(curLimit, max);
        }

        return Math.min(curLimit, context.maxLimit());
    }

    // TODO refactor to a Strategy
    private long growLimit(final long cur, final long expected) {
        return growLimit2(cur, expected);
    }

    private long growLimit2(final long cur, final long expected) {
        return context.maxLimit();
    }

    // TODO refactor to a Strategy
    private long growLimit1(final long cur, final long expected) {
        final double ratio = expected * 1.0 / context.maxLimit();

        double growFactor;
        if (ratio < 0.3) {
            growFactor = 2.0;
        } else if (ratio < 0.5) {
            growFactor = 1.5;
        } else if (ratio < 0.7) {
            growFactor = 1.2;
        } else {
            growFactor = 1.1;
        }

        return (long)(expected * growFactor);
    }

    @Override
    public long currentQpsLimit(){
        return theLimit;
    }

    @Override
    public long qpsLimitMin() {
        return theMinLimit;
    }

    @Override
    public AccessMethodContext context() {
        return context;
    }

    @Override
    public String id() {
        return context.id();
    }
}

