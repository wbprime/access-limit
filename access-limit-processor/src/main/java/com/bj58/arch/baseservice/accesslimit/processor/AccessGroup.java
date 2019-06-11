package com.bj58.arch.baseservice.accesslimit.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface AccessGroup {
    /**
     * Group name
     *
     * @return name
     */
    String name();

    /**
     * Max permits in {@code seconds} seconds for methods in this group, i.e. max qps = {@code max / seconds}.
     *
     * @return max permits
     */
    int max();

    /**
     * Measure time unit for QPS
     *
     * @return seconds
     */
    int seconds() default 1;
}
