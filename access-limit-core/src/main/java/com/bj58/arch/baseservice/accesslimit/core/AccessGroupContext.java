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
