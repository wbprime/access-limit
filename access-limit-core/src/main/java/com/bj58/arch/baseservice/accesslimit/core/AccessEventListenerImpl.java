package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.collect.ImmutableMap;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
final class AccessEventListenerImpl implements QpsManager {
    private static final long MICROS_PER_SECOND = TimeUnit.SECONDS.toMicros(1);

    private ImmutableMap<String, QpsCalculator> calculators = ImmutableMap.of();

    private ImmutableMap<String, QpsLimitAdjustable> items = ImmutableMap.of();

    @Override
    public void onEvent(final AccessEvent event) {
        final QpsCalculator calculator = calculators.get(event.sourceId());

        // TODO maybe NPE is a better choice
        if (null == calculator) return;

        final boolean updated = calculator.add(event.micros(), event.count());
        if (updated) {
            final QpsLimitAdjustable adjustable = items.get(event.sourceId());
            adjustable.adjust(calculator.lastQps());
        }
    }

    @Override
    public synchronized void register(final String name, final QpsLimitAdjustable item) {
        checkState(! (calculators.containsKey(name) || items.containsKey(name)),
                "QpsLimitAdjustable with name \"%s\" was already registered");

        items = ImmutableMap.<String, QpsLimitAdjustable>builder()
                .putAll(items).put(name, item).build();

        calculators = ImmutableMap.<String, QpsCalculator>builder()
                .putAll(calculators)
                .put(name, new QpsCalculator((int)MICROS_PER_SECOND))
                .build();
    }

    private static class QpsCalculator {

        private final double lastQpsArr[];
        private int idxOfQpsArr;

        private final int microsPerMeasure;

        private long curMeasureBegMicros;

        private int curMeasureCount;


        QpsCalculator(int microsPerMeasure) {
            this.microsPerMeasure = microsPerMeasure;

            this.curMeasureBegMicros = 0L;
            this.curMeasureCount = 0;

            this.lastQpsArr = new double[5];
            this.idxOfQpsArr = 0;
        }

        // Add access data and return if update a last qps value
        synchronized boolean add(final long micros, final int count) {
            if (micros >= curMeasureBegMicros + microsPerMeasure) {
                lastQpsArr[idxOfQpsArr++ % lastQpsArr.length] = 1.0 * curMeasureCount * MICROS_PER_SECOND / microsPerMeasure;

                curMeasureBegMicros = micros;
                curMeasureCount = count;

                return true;
            } else {
                curMeasureCount += count;

                return false;
            }
        }

        double lastQps() {
            return lastQpsArr[idxOfQpsArr % lastQpsArr.length];
        }
    }
}
