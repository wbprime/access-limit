package com.bj58.arch.baseservice.accesslimit.core;

import com.google.auto.value.AutoValue;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@AutoValue
public abstract class AccessEvent {
    public abstract String sourceId();

    public abstract long timeStampInMicros();

    public abstract int count();

    public static Builder builder() {
        return new AutoValue_AccessEvent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sourceId(final String sourceId);

        public abstract Builder timeStampInMicros(long micros);

        public abstract Builder count(int count);

        public abstract AccessEvent build();
    }
}
