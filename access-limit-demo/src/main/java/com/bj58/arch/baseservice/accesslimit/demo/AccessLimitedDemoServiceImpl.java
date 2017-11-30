package com.bj58.arch.baseservice.accesslimit.demo;

import com.bj58.arch.baseservice.accesslimit.core.AccessAware;
import com.bj58.arch.baseservice.accesslimit.core.AccessEvent;
import com.bj58.arch.baseservice.accesslimit.core.AccessGroupContext;
import com.bj58.arch.baseservice.accesslimit.core.AccessMethodContext;
import com.bj58.arch.baseservice.accesslimit.core.QpsLimiter;
import com.bj58.arch.baseservice.accesslimit.core.QpsLimiters;
import com.bj58.arch.baseservice.accesslimit.core.StdQpsManageGroup;
import com.bj58.arch.baseservice.accesslimit.core.StdQpsManageLeaf;

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

    // To manage limit update strategy
    private final AccessMethodContext accessMethodContext1;
    private final AccessMethodContext accessMethodContext2;

    // To perform rate limit
    private final QpsLimiter accessLimiter4m1;
    private final QpsLimiter accessLimiter4m2;

    // To record QPS and update QPS limit
    private final AccessAware accessAware;

    AccessLimitedDemoServiceImpl() {
        this.adapteeService = new DemoServiceImpl();

        final AccessGroupContext groupContext = AccessGroupContext.builder()
                .id("accessManageGroup")
                .maxLimit(1000L)
                .periodInMicros(TimeUnit.SECONDS.toMicros(1))
                .build();

        final StdQpsManageGroup qpsManageGroup = new StdQpsManageGroup(groupContext);

        accessMethodContext1 = AccessMethodContext.builder()
                .id("demoMethod1")
                .limit(1000L, 10L)
                .weight(1)
                .build();

        final StdQpsManageLeaf manageLeaf4m1 = new StdQpsManageLeaf(qpsManageGroup, accessMethodContext1);

        accessMethodContext2 = AccessMethodContext.builder()
                .id("demoMethod2")
                .limit(1000L, 10L)
                .weight(1)
                .build();

        final StdQpsManageLeaf manageLeaf4m2 = new StdQpsManageLeaf(qpsManageGroup, accessMethodContext2);

        this.accessLimiter4m1 = QpsLimiters.create(groupContext, accessMethodContext1, manageLeaf4m1);
        this.accessLimiter4m2 = QpsLimiters.create(groupContext, accessMethodContext2, manageLeaf4m2);

        this.accessAware = qpsManageGroup.get();
    }

    @Override
    public void demoMethod1(int arg1, String arg2, Map<String, Long> arg3) {
        accessAware.onAccessed(
                AccessEvent.builder().sourceId("demoMethod1")
                        .timeStampInMicros(TimeUnit.NANOSECONDS.toMicros(System.nanoTime()))
                        .count(accessMethodContext1.weight())
                        .build()
        );

        LOGGER.debug("demoMethod1 acquiring");

        final QpsLimiter qpsLimiter = accessLimiter4m1;
        qpsLimiter.acquire(accessMethodContext1.weight());

        LOGGER.debug("demoMethod1 acquired");

        try {
            adapteeService.demoMethod1(arg1, arg2, arg3);
        } finally {
            qpsLimiter.release(accessMethodContext1.weight());
        }
    }

    @Override
    public void demoMethod2(short arg1, byte[] arg2, List<Integer> arg3) {
        accessAware.onAccessed(
                AccessEvent.builder().sourceId("demoMethod2")
                        .timeStampInMicros(TimeUnit.NANOSECONDS.toMicros(System.nanoTime()))
                        .count(accessMethodContext2.weight())
                        .build()
        );

        LOGGER.info("demoMethod2 acquiring");

        final QpsLimiter qpsLimiter = accessLimiter4m2;
        qpsLimiter.acquire(accessMethodContext2.weight());

        LOGGER.info("demoMethod2 acquired");

        try {
            adapteeService.demoMethod2(arg1, arg2, arg3);
        } finally {
            qpsLimiter.release(accessMethodContext2.weight());
        }
    }
}
