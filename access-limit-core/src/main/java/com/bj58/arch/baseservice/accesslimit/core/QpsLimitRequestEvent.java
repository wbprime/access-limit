package com.bj58.arch.baseservice.accesslimit.core;

import com.google.auto.value.AutoValue;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
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
