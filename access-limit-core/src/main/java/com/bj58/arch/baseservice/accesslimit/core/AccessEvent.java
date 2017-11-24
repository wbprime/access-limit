package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public final class AccessEvent {
    private final String theSourceId;

    private final long theMicros;
    private final int theCount;

    public AccessEvent(final String sourceId, long theMicros, int theCount) {
        this.theSourceId = sourceId;
        this.theMicros = theMicros;
        this.theCount = theCount;
    }

    public String sourceId() {
        return theSourceId;
    }

    public long micros() {
        return theMicros;
    }

    public int count() {
        return theCount;
    }
}
