package com.bj58.arch.baseservice.accesslimit.demo;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class DemoMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoMain.class);

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
            LOGGER.info("Thread finished");
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
            LOGGER.info("Thread finished");
        }
    }

    public static void main(final String[] args) throws Exception {
//        final DemoService service = new AccessLimit_DemoServiceImpl();
        final DemoService service = new AccessLimitedDemoServiceImpl();

        final CyclicBarrier barrier = new CyclicBarrier(2);

        final ExecutorService executorService = Executors.newFixedThreadPool(6);

        final List<Future<?>> list = Lists.newArrayList();

        {
            final Runnable runnable = new Method1Runnable(barrier, 5, service);

            final Future<?> future = executorService.submit(runnable);
            list.add(future);
        }
        {
            final Runnable runnable = new Method2Runnable(barrier, 50, service);

            final Future<?> future = executorService.submit(runnable);
            list.add(future);
        }
        {
            final Runnable runnable = new Method1Runnable(barrier, 50, service);

            final Future<?> future = executorService.submit(runnable);
            list.add(future);
        }
        {
            final Runnable runnable = new Method2Runnable(barrier, 5, service);

            final Future<?> future = executorService.submit(runnable);
            list.add(future);
        }
        {
            final Runnable runnable = new Method1Runnable(barrier, 500, service);

            final Future<?> future = executorService.submit(runnable);
            list.add(future);
        }
        {
            final Runnable runnable = new Method2Runnable(barrier, 500, service);

            final Future<?> future = executorService.submit(runnable);
            list.add(future);
        }

        // Wait all to finish
        for (final Future<?> future : list) {
            Uninterruptibles.getUninterruptibly(future);
        }

        executorService.shutdown();
    }
}
