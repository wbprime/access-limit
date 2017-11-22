package com.bj58.arch.baseservice.accesslimit.core;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class SystemWalkingClockTest {
    @Test
    public void readCurrentMicros() throws Exception {
        final SystemWalkingClock clock = SystemWalkingClock.instance();

        final long nano1 = System.nanoTime();

        final long micro = clock.readCurrentMicros();

        final long nano2 = System.nanoTime();

        Assertions.assertThat(micro * 1000).isGreaterThanOrEqualTo(nano1).isLessThanOrEqualTo(nano2);
    }
}