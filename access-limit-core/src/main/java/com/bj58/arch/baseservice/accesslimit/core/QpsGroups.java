package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.collect.ImmutableMap;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
public final class QpsGroups {
    private static ImmutableMap<String, QpsManageGroup> GROUPS = ImmutableMap.of();

    private QpsGroups() { throw new AssertionError("Construction forbidden"); }

    public synchronized static void register(final QpsManageGroup group) {
        // register
        final String groupName = group.id();

        if (GROUPS.containsKey(groupName)) {
            throw new IllegalStateException("Duplicated group \"" + groupName + "\"defined");
        }

        GROUPS = ImmutableMap.<String, QpsManageGroup>builder()
                .putAll(GROUPS)
                .put(groupName, group)
                .build();
    }

    public static QpsManageGroup get(final String groupName) {
        return GROUPS.get(groupName);
    }
}
