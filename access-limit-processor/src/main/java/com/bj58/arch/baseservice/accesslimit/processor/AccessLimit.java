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
@Target(ElementType.METHOD)
@Documented
public @interface AccessLimit {
    /**
     * Max permits for {@code seconds} seconds, i.e. max qps = {@code max / seconds}.
     *
     * @return max permits
     */
    int max();

    /**
     * Min permits for {@code seconds} seconds, i.e. min qps = {@code max / seconds}.
     *
     * @return min permits
     */
    int min() default 0;

    /**
     * Measure time unit for QPS
     *
     * @return seconds
     */
    int seconds() default 1;

    /**
     * Permits per step (i.e., method invocation).
     *
     * @return permits per step
     */
    int weight() default 1;
}
