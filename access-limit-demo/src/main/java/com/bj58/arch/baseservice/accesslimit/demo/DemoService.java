package com.bj58.arch.baseservice.accesslimit.demo;

import java.util.List;
import java.util.Map;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
interface DemoService {
    void demoMethod1(final int arg1, final String arg2, final Map<String, Long> arg3);

    void demoMethod2(final short arg1, final byte[] arg2, final List<Integer> arg3);
}
