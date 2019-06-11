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
public abstract class AccessGroupContext {
    public abstract String id();
    public abstract long maxLimit();
    public abstract long periodInMicros();

    public static Builder builder() {
        return new AutoValue_AccessGroupContext.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder maxLimit(long maxLimit);

        public abstract Builder periodInMicros(long periodInMicros);

        public abstract AccessGroupContext build();
    }
}
