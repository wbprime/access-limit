package com.bj58.arch.baseservice.accesslimit.demo;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class DemoMain {
    private static abstract class MethodRunnable implements Runnable {
        final CyclicBarrier cyclicBarrier;
        final DemoService demoService;

        MethodRunnable(final CyclicBarrier barrier, final DemoService demoService) {
            this.cyclicBarrier = barrier;
            this.demoService = demoService;
        }

        void syncWait() {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (BrokenBarrierException e) {
                throw new IllegalStateException(e);
            }
        }

    }

    private static class Method1Runnable extends MethodRunnable implements Runnable {
        Method1Runnable(final CyclicBarrier barrier, final DemoService demoService) {
            super(barrier, demoService);
        }

        @Override
        public void run() {
            syncWait();
            demoService.demoMethod1(1, "2", ImmutableMap.<String, Long>of());
            syncWait();
            demoService.demoMethod1(2, "20", ImmutableMap.<String, Long>of());
            syncWait();
            demoService.demoMethod1(3, "200", ImmutableMap.<String, Long>of());
            syncWait();
            demoService.demoMethod1(4, "2000", ImmutableMap.<String, Long>of());
            syncWait();
            demoService.demoMethod1(5, "20000", ImmutableMap.<String, Long>of());
        }
    }

    private static class Method2Runnable extends MethodRunnable implements Runnable {
        Method2Runnable(final CyclicBarrier barrier, final DemoService demoService) {
            super(barrier, demoService);
        }

        @Override
        public void run() {
            syncWait();
            demoService.demoMethod2((short)1, "2".getBytes(Charsets.UTF_8), ImmutableList.<Integer>of());
            syncWait();
            demoService.demoMethod2((short)2, "20".getBytes(Charsets.UTF_8), ImmutableList.<Integer>of());
            syncWait();
            demoService.demoMethod2((short)3, "200".getBytes(Charsets.UTF_8), ImmutableList.<Integer>of());
            syncWait();
            demoService.demoMethod2((short)4, "2000".getBytes(Charsets.UTF_8), ImmutableList.<Integer>of());
            syncWait();
            demoService.demoMethod2((short)5, "20000".getBytes(Charsets.UTF_8), ImmutableList.<Integer>of());
        }
    }

    public static void main(final String[] args) {
        final DemoService service = new AccessLimit_DemoServiceImpl();
//        final DemoService service = new AccessLimitedDemoServiceImpl(
//                new DemoServiceImpl(), new QpsLimiter(1, 100), new QpsLimiter(1, 200)
//        );

        final CyclicBarrier barrier = new CyclicBarrier(2);

        final Runnable runnable1 = new Method1Runnable(barrier, service);
        final Runnable runnable2 = new Method2Runnable(barrier, service);

        final Thread t1 = new Thread(runnable1);
        final Thread t2 = new Thread(runnable2);

        t1.start();
        t2.start();

        Uninterruptibles.joinUninterruptibly(t1);
        Uninterruptibles.joinUninterruptibly(t2);
    }
}
