package com.bj58.arch.baseservice.accesslimit.processor.impl;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
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
