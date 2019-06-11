package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
final class ComposedAccessMonitor implements AccessAware {
    private final ImmutableMap<String, AccessAware> map;

    ComposedAccessMonitor(final Map<String, AccessAware> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public void onAccessed(final AccessEvent event) {
        for (final Map.Entry<String, AccessAware> entry : map.entrySet()) {
            final String id = entry.getKey();
            final AccessAware accessAware = entry.getValue();

            if (id.equals(event.sourceId())) {
                accessAware.onAccessed(event);
            } else {
                accessAware.onAccessed(
                        AccessEvent.builder()
                                .sourceId(id)
                                .timeStampInMicros(event.timeStampInMicros())
                                .count(0) // Just to mark and tick
                                .build()
                );
            }
        }
    }

    @Override
    public String toString() {
        return "ComposedAccessMonitor{" +
                "map=" + map +
                '}';
    }
}
