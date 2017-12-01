package com.bj58.arch.baseservice.accesslimit.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
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
