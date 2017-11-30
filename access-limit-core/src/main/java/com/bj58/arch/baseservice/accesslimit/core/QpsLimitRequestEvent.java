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
public abstract class QpsLimitRequestEvent {
    public abstract String sourceId();

    public abstract long currentLimit();

    public abstract long expectedLimit();

    public static Builder builder() {
        return new AutoValue_QpsLimitRequestEvent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sourceId(final String sourceId);

        public abstract Builder currentLimit(long curLimit);

        public abstract Builder expectedLimit(long expectedLimit);

        public abstract QpsLimitRequestEvent build();
    }
}
