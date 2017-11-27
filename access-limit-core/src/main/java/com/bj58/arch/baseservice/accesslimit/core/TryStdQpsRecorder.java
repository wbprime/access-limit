package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.base.Optional;

import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class TryStdQpsRecorder implements TryQpsRecorder {
    private static final long MICROS_PER_SECOND = TimeUnit.SECONDS.toMicros(1);

    private final TryQpsManageGroup parent;

    private final TryQpsManageLeaf leaf;

    private final QpsCalculator calculator;

    public TryStdQpsRecorder(
            final TryQpsManageLeaf attachedLeaf,
            final TryQpsManageGroup parentGroup
    ) {
        this.parent = parentGroup;
        this.leaf = attachedLeaf;
        this.calculator = new QpsCalculator((int)MICROS_PER_SECOND);
    }

    @Override
    public void onEvent(final AccessEvent event) {
        final Optional<Double> updated = calculator.add(event.micros(), event.count());
        if (updated.isPresent()) {
            parent.onQpsChanged(leaf.id(), updated.get());
        }
    }

    private static class QpsCalculator {
        private final int microsPerMeasure;

        private long curMeasureBegMicros;

        private int curMeasureCount;

        private double theLastQps;

        QpsCalculator(int microsPerMeasure) {
            this.microsPerMeasure = microsPerMeasure;

            this.curMeasureBegMicros = 0L;
            this.curMeasureCount = 0;

            this.theLastQps = 0.0;
        }

        // Add access data and return if update a last qps value
        synchronized Optional<Double> add(final long micros, final int count) {
            if (micros >= curMeasureBegMicros + microsPerMeasure) {
                theLastQps = 1.0 * curMeasureCount * MICROS_PER_SECOND / microsPerMeasure;

                curMeasureBegMicros = micros;
                curMeasureCount = count;

                return Optional.of(theLastQps);
            } else {
                curMeasureCount += count;

                return Optional.absent();
            }
        }
    }
}
