package com.bj58.arch.baseservice.accesslimit.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
@Ignore
public class QpsLimiterTest {
    private QpsLimiter limiter;

    private WalkingClock mockedClock;
    private SleepingTimer mockedTimer;

    private int seconds;
    private int permits;
    private long curMicro;

    @Before
    public void setUp() throws Exception {
        final Random rnd = new Random();

        seconds = rnd.nextInt(1000) + 1;
        permits = rnd.nextInt(1000) + 10;
        curMicro = Math.abs(rnd.nextLong());

        mockedClock = mock(WalkingClock.class);
        when(mockedClock.readCurrentMicros()).thenReturn(curMicro);

        mockedTimer = mock(SleepingTimer.class);
        doNothing().when(mockedTimer).sleep(anyLong(), any(TimeUnit.class));

        limiter = new StdQpsLimiter(seconds, permits, mockedClock, mockedTimer);
    }

    @Test
    public void test_once_lessThanLimit() throws Exception {
        final int diffCnt = -1;
        limiter.acquire(permits + diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);
    }

    @Test
    public void test_once_greaterThanLimit() throws Exception {
        final int diffCnt = 1;
        limiter.acquire(permits + diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);
    }

    @Test
    public void test_twice_lessThanLimit() throws Exception {
        final int diffCnt = -1;
        limiter.acquire(permits + diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);

        clearInvocations(mockedClock);
        clearInvocations(mockedTimer);

        final int cnt = Math.abs(diffCnt * 2);
        limiter.acquire(cnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);
    }

    @Test
    public void test_twice_greaterThanLimit() throws Exception {
        final int diffCnt = 1;
        limiter.acquire(permits + diffCnt);

        final int diffAbs = Math.abs(diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);

        clearInvocations(mockedClock);
        clearInvocations(mockedTimer);

        final int cnt = Math.abs(diffCnt * 2);
        limiter.acquire(cnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep((long) (seconds * 1000000.0 * (1 + diffAbs * 1.0 / permits)), TimeUnit.MICROSECONDS);
    }

    @Test
    public void test_thrice_lessThanLimit() throws Exception {
        final int diffCnt = -1;
        limiter.acquire(permits + diffCnt);

        final int diffAbs = Math.abs(diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);

        clearInvocations(mockedClock);
        clearInvocations(mockedTimer);

        final int cnt1 = diffAbs * 2;
        limiter.acquire(cnt1);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);

        clearInvocations(mockedClock);
        clearInvocations(mockedTimer);

        final int cnt2 = permits;
        limiter.acquire(cnt2);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep((long) (seconds * 1000000.0 * (1 + diffAbs * 1.0 / permits)), TimeUnit.MICROSECONDS);
    }

    @Test
    public void test_thrice_greaterThanLimit() throws Exception {
        final int diffCnt = 1;
        limiter.acquire(permits + diffCnt);

        final int diffAbs = Math.abs(diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);

        clearInvocations(mockedClock);
        clearInvocations(mockedTimer);

        final int cnt1 = diffAbs * 2;
        limiter.acquire(cnt1);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep((long) (seconds * 1000000.0 * (1 + diffAbs * 1.0 / permits)), TimeUnit.MICROSECONDS);

        clearInvocations(mockedClock);
        clearInvocations(mockedTimer);

        final int cnt2 = permits;
        limiter.acquire(cnt2);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep((long) (seconds * 1000000.0 * (1 + diffAbs * 3.0 / permits)), TimeUnit.MICROSECONDS);
    }

    @Test
    public void longPause() throws Exception {
        final int diffCnt = 1;
        limiter.acquire(permits + diffCnt);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);

        reset(mockedClock, mockedTimer);

        when(mockedClock.readCurrentMicros()).thenReturn(curMicro + seconds * 1000000 * 2);

        final int cnt1 = permits + diffCnt;
        limiter.acquire(cnt1);

        verify(mockedClock).readCurrentMicros();
        verify(mockedTimer).sleep(0L, TimeUnit.MICROSECONDS);
    }
}