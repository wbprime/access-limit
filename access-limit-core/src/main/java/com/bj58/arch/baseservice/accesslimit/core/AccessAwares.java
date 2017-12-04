package com.bj58.arch.baseservice.accesslimit.core;

import java.util.Map;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public final class AccessAwares {
    private AccessAwares() { throw new AssertionError("Construction forbidden"); }

    public static AccessAware composed(final Map<String, AccessAware> accessAwares) {
        return new ComposedAccessMonitor(accessAwares);
    }

    public static AccessAware create(
            final AccessGroupContext groupContext,
            final QpsChangeAware aware
    ) {
        return new StdAccessMonitor(groupContext, aware);
    }
}
