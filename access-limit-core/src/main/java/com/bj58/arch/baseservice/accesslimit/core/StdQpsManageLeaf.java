package com.bj58.arch.baseservice.accesslimit.core;

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
    private long theMaxLimit;

    // The actual limit
    // Rule: theLimitMin <= theMinLimit <= theLimit <= theLimitMax
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
        this.theLimit = context.maxLimit();
        this.theMinLimit = context.minLimit();
        this.theMaxLimit = context.maxLimit();

        this.parentGroup = group;
        if (null != this.parentGroup) {
            this.parentGroup.addChild(this);
        }

        this.lastAccessArr = new long[arrLen];
        for (int i = 0; i < this.lastAccessArr.length; i++) {
            this.lastAccessArr[i] = this.theLimit;
        }
        this.accessArrIdx = 0;
    }

    @Override
    public void onQpsChanged(final QpsChangeEvent event) {
        if (null != parentGroup && event.periodInMicros() != 0L) {
            synchronized (mutex) {
                accessArrIdx = (accessArrIdx + 1) % lastAccessArr.length;
                lastAccessArr[accessArrIdx] =
                        (long) (event.permits() * 1.0 / event.periodInMicros() * parentGroup.context().periodInMicros());

                adjustMinLimitIfNeeded();

                final long expected = adjustLimitIfNeeded();

                if (expected > theMaxLimit) {
                    parentGroup.onQpsLimitRequested(
                            QpsLimitRequestEvent.builder()
                                    .sourceId(event.sourceId())
                                    .currentLimit(theMaxLimit)
                                    .expectedLimit(expected)
                                    .build()
                    );
                }
            }
        }
    }

    @Override
    public void adjustMaxQpsLimit(final long permits) {
        final double oldLimit = theMaxLimit;
        if (permits > context.maxLimit()) {
            theMaxLimit = context.maxLimit();
        } else if (permits < theMinLimit) {
            theMaxLimit = theLimit;
        } else {
            theMaxLimit = permits;
        }
        LOGGER.debug("QPS limit max changed: [{}] -> [{}]", oldLimit, theMaxLimit);

        LOGGER.debug("QPS limit changed: [{}] -> [{}]", theLimit, theMaxLimit);
        theLimit = theMaxLimit;
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

        long max = curLimit;
        for (long qps : lastAccessArr) {
            if (qps > max) max = qps;
        }

        if (max >= curLimit) {
            curLimit = growLimit(curLimit, max);
        }

        final long wanted = curLimit;

        if (curLimit < theMinLimit) {
            curLimit = theMinLimit;
        }

        if (curLimit > theMaxLimit) {
            curLimit = theMaxLimit;
        }

        theLimit = curLimit;

        return wanted;
    }

    private long growLimit(final long cur, final long expected) {
        final double ratio = expected * 1.0 / context.maxLimit();

        double growFactor;
        if (ratio < 0.2) {
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
    public long currentQpsLimit() {
        return theLimit;
    }

    @Override
    public long qpsLimitMax() {
        return theMaxLimit;
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

