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
public abstract class QpsChangeEvent {
    public abstract String sourceId();

    public abstract long periodInMicros();

    public abstract long permits();

    public static Builder builder() {
        return new AutoValue_QpsChangeEvent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sourceId(String sourceId);

        public abstract Builder periodInMicros(long periodInMicros);

        public abstract Builder permits(long permits);

        public abstract QpsChangeEvent build();
    }
}
