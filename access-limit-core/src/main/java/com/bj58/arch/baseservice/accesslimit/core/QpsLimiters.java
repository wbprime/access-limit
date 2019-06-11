package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
public final class QpsLimiters {
    public static QpsLimiter create(
            final long microsPerMeasure, final long maxLimit
    ) {
        return new StdQpsLimiter(
                microsPerMeasure, maxLimit,
                SystemWalkingClock.instance(),
                SystemSleepingTimer.instance()
        );
    }

    public static QpsLimiter create(
            final AccessGroupContext groupContext, final AccessMethodContext context
    ) {
        return create(groupContext.periodInMicros(), context.maxLimit());
    }

    private static class LeafAdaptedQpsLimiter implements QpsLimiter {
        private final QpsLimiter adaptee;
        private final QpsManageLeaf leaf;

        LeafAdaptedQpsLimiter(final QpsLimiter adaptee, final QpsManageLeaf leaf) {
            this.adaptee = adaptee;
            this.leaf = leaf;
        }

        @Override
        public void acquire(int required) {
            adaptee.limitUpdated(leaf.currentQpsLimit()).acquire(required);
        }

        @Override
        public void release(int released) {
            adaptee.limitUpdated(leaf.currentQpsLimit()).release(released);
        }

        @Override
        public QpsLimiter limitUpdated(long newLimit) {
            throw new UnsupportedOperationException("Method limitUpdated not implemented in class LeafAdaptedQpsLimiter");
        }
    }

    public static QpsLimiter create(
            final AccessGroupContext groupContext, final QpsManageLeaf leaf
    ) {
        return new LeafAdaptedQpsLimiter(
                create(groupContext, leaf.context()), leaf
        );
    }
}
