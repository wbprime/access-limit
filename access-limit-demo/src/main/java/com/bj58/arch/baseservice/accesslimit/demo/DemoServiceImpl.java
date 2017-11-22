package com.bj58.arch.baseservice.accesslimit.demo;

import com.bj58.arch.baseservice.accesslimit.api.AccessLimit;
import com.bj58.arch.baseservice.accesslimit.api.EnableAccessLimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@EnableAccessLimit
public class DemoServiceImpl implements DemoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    @AccessLimit(limit = 30)
    @Deprecated
    public void demoMethod1(int arg1, String arg2, Map<String, Long> arg3) {
        LOGGER.info("DemoServiceImpl#demoMethod1 invoked");
    }

    @Override
    @AccessLimit(limit = 30, weight = 16)
    public void demoMethod2(short arg1, byte[] arg2, List<Integer> arg3) {
        LOGGER.info("DemoServiceImpl#demoMethod2 invoked");
    }
}
