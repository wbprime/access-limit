package com.bj58.arch.baseservice.accesslimit.demo;

import java.util.List;
import java.util.Map;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
interface DemoService {
    void demoMethod1(final int arg1, final String arg2, final Map<String, Long> arg3);

    void demoMethod2(final short arg1, final byte[] arg2, final List<Integer> arg3);
}
