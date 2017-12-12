package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.collect.ImmutableMap;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
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
