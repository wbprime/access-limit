package com.bj58.arch.baseservice.accesslimit.processor.impl;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
final class Constants {
    private Constants() { throw new AssertionError("Construction forbidden"); }

    static final String GENERATED_FALLBACK_PACKAGE = "com.bj58.arch.baseservice.accesslimit.generated";

    static final String GENERATED_GROUPS_CLASS_NAME = "AccessLimit_Groups";

    static final String GENERATED_CLASS_PREFIX = "AccessLimit_";

    static final String GENERATED_ADAPTEE_VAR_NAME = "adaptee";
    static final String GENERATED_ACCESS_AWARE_VAR_NAME = "accessAware";
    static final String GENERATED_QPS_LIMITER_VAR_NAME = "qpsLimiters";

    static String qpsLimiterVarName(final int idx) {
        return GENERATED_QPS_LIMITER_VAR_NAME + "[" + idx + "]";
    }
}
