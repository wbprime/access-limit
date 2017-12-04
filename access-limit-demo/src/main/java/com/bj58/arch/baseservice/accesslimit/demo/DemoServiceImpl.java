package com.bj58.arch.baseservice.accesslimit.demo;

import com.bj58.arch.baseservice.accesslimit.processor.AccessGroup;
import com.bj58.arch.baseservice.accesslimit.processor.AccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@EnableAccessLimit
//@EnableScfSupport
@AccessGroup(name = "parent", max = 1000)
public class DemoServiceImpl implements DemoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoServiceImpl.class);

    @AccessGroup(name = "test", max = 100)
    private static class TestGroup {}

    @AccessGroup(name = "product", max = 1000)
    private static class ParentGroup {}

    @Override
    @AccessLimit(group = "product", max = 20, min = 10)
    @Deprecated
    public void demoMethod1(int arg1, String arg2, @Nonnull Map<String, Long> arg3) {
        LOGGER.info("{}#demoMethod1 invoked", this.getClass());
    }

    @Override
    @AccessLimit(group = "test", max = 30, min = 10, weight = 16)
    public void demoMethod2(@CheckForSigned short arg1, @Nullable @CheckForNull byte[] arg2, List<Integer> arg3) {
        LOGGER.info("{}#demoMethod2 invoked", this.getClass());
    }

    @AccessLimit(group = "test", max = 30, min = 10, weight = 16)
    public void demoMethod2(@CheckForSigned @CheckForNull byte[] arg1, List<Integer> arg2) {

    }
}
