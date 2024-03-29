package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.base.Optional;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
final class StdAccessMonitor implements AccessAware {
    private final QpsChangeAware qpsChangeAware;

    private final AccessGroupContext groupContext;

    private long curMeasureBegMicros;
    private long curMeasureCount;

    StdAccessMonitor(
            final AccessGroupContext groupContext,
            final QpsChangeAware aware
    ) {
        this.groupContext = groupContext;

        this.qpsChangeAware = aware;

        this.curMeasureBegMicros = 0L;
        this.curMeasureCount = 0L;
    }

    @Override
    public void onAccessed(final AccessEvent event) {
        final Optional<QpsChangeEvent> updated = add(event);
        if (updated.isPresent()) {
            qpsChangeAware.onQpsChanged(updated.get());
        }
    }

    // Add access data and return if update a last qps value
    synchronized Optional<QpsChangeEvent> add(final AccessEvent event) {
        final long micros = event.timeStampInMicros();
        final int count = event.count();
        if (0L == curMeasureBegMicros) {
            curMeasureBegMicros = micros;
            curMeasureCount = count;
            return Optional.absent();
        } else if (micros >= curMeasureBegMicros + groupContext.periodInMicros()) {
            final long existedMicros = micros - curMeasureBegMicros;
            final long existedCount = curMeasureCount;

            curMeasureBegMicros = micros;
            curMeasureCount = count;

            return Optional.of(
                    QpsChangeEvent.builder()
                            .sourceId(event.sourceId())
                            .periodInMicros(existedMicros)
                            .permits(existedCount)
                            .build()
            );
        } else {
            curMeasureCount += count;

            return Optional.absent();
        }
    }

    @Override
    public String toString() {
        return "StdAccessMonitor{" +
                "groupContext=" + groupContext +
                ", curMeasureBegMicros=" + curMeasureBegMicros +
                ", curMeasureCount=" + curMeasureCount +
                '}';
    }
}
