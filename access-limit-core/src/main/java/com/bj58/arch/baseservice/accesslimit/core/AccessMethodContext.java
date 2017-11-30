package com.bj58.arch.baseservice.accesslimit.core;

import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@AutoValue
public abstract class AccessMethodContext {
    public abstract String id();
    public abstract long maxLimit();
    public abstract long minLimit();
    public abstract int weight();

    public static Builder builder() {
        return new AutoValue_AccessMethodContext.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(final String id);

        abstract Builder maxLimit(long maxLimit);
        abstract Builder minLimit(long minLimit);
        public final Builder limit(final long max, final long min) {
            checkArgument(max >= min, "Max permits (%s) should be greater than min permits (%s) but not", max, min);
            checkArgument(min >= 0, "Min permits (%s) should be non-negative integer but not", min);
            return maxLimit(max).minLimit(min);
        }

        public abstract Builder weight(int weight);

        public abstract AccessMethodContext build();
    }
}
