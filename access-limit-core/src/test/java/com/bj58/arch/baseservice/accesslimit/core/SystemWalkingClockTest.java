package com.bj58.arch.baseservice.accesslimit.core;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
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