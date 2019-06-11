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
@Target(ElementType.METHOD)
@Documented
public @interface AccessLimit {
    /**
     * Parent group name.
     *
     * @return name of parent group
     */
    String group();

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
     * Permits per step (i.e., method invocation).
     *
     * @return permits per step
     */
    int weight() default 1;
}
