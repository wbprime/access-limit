package com.bj58.arch.baseservice.accesslimit.demo;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;

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

        final int count;

        MethodRunnable(final CyclicBarrier barrier, final int repeated, final DemoService demoService) {
            this.cyclicBarrier = barrier;
            this.demoService = demoService;
            this.count = repeated;
        }

        void syncWait() {
//            try {
//                cyclicBarrier.await();
//            } catch (InterruptedException e) {
//                throw new IllegalStateException(e);
//            } catch (BrokenBarrierException e) {
//                throw new IllegalStateException(e);
//            }
        }

    }

    private static class Method1Runnable extends MethodRunnable implements Runnable {
        Method1Runnable(final CyclicBarrier barrier, final int repeated, final DemoService demoService) {
            super(barrier, repeated, demoService);
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                syncWait();
                demoService.demoMethod1(1, "2", ImmutableMap.<String, Long>of());
            }
        }
    }

    private static class Method2Runnable extends MethodRunnable implements Runnable {
        Method2Runnable(final CyclicBarrier barrier, final int repeated, final DemoService demoService) {
            super(barrier, repeated, demoService);
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                syncWait();
                demoService.demoMethod2((short)1, "2".getBytes(Charsets.UTF_8), ImmutableList.<Integer>of());
            }
        }
    }

    public static void main(final String[] args) {
//        final DemoService service = new AccessLimit_DemoServiceImpl();
        final DemoService service = new AccessLimitedDemoServiceImpl();

        final CyclicBarrier barrier = new CyclicBarrier(2);

        final Runnable runnable1 = new Method1Runnable(barrier, 5, service);
        final Runnable runnable2 = new Method2Runnable(barrier, 500, service);

        final Thread t1 = new Thread(runnable1);
        final Thread t2 = new Thread(runnable2);

        t1.setName("demoMethod1");
        t2.setName("demoMethod2");

        t1.start();
        t2.start();

        Uninterruptibles.joinUninterruptibly(t1);
        Uninterruptibles.joinUninterruptibly(t2);
    }
}
