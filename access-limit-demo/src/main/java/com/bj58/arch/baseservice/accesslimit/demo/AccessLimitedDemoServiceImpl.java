package com.bj58.arch.baseservice.accesslimit.demo;

import com.bj58.arch.baseservice.accesslimit.core.AccessEvent;
import com.bj58.arch.baseservice.accesslimit.core.AccessManagers;
import com.bj58.arch.baseservice.accesslimit.core.QpsLimiter;
import com.bj58.arch.baseservice.accesslimit.core.QpsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class AccessLimitedDemoServiceImpl implements DemoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLimitedDemoServiceImpl.class);

    private final DemoService adapteeService;

    private final QpsManager qpsManager;
    private final QpsLimiter accessLimiter4DemoMethod1;
    private final QpsLimiter accessLimiter4DemoMethod2;

    AccessLimitedDemoServiceImpl(
            final DemoService adaptee,
            final QpsLimiter accessLimiter1,
            final QpsLimiter accessLimiter2
    ) {
        this.adapteeService = adaptee;

        this.accessLimiter4DemoMethod1 = accessLimiter1;
        this.accessLimiter4DemoMethod2 = accessLimiter2;

        this.qpsManager = AccessManagers.std();
        this.qpsManager.register("accessLimiter4DemoMethod1", this.accessLimiter4DemoMethod1);
        this.qpsManager.register("accessLimiter4DemoMethod2", this.accessLimiter4DemoMethod2);
    }

    @Override
    public void demoMethod1(int arg1, String arg2, Map<String, Long> arg3) {
        qpsManager.onEvent(new AccessEvent("accessLimiter4DemoMethod1", TimeUnit.NANOSECONDS.toMicros(System.nanoTime()), 100));
        LOGGER.debug("demoMethod1 acquiring");
        accessLimiter4DemoMethod1.acquire(100);
        LOGGER.debug("demoMethod1 acquired");
        try {
            adapteeService.demoMethod1(arg1, arg2, arg3);
        } finally {
            accessLimiter4DemoMethod1.release(100);
        }
    }

    @Override
    public void demoMethod2(short arg1, byte[] arg2, List<Integer> arg3) {
        LOGGER.info("demoMethod2 acquiring");
        accessLimiter4DemoMethod2.acquire(50);
        LOGGER.info("demoMethod2 acquired");
        try {
            adapteeService.demoMethod2(arg1, arg2, arg3);
        } finally {
            accessLimiter4DemoMethod2.release(50);
        }
    }
}
