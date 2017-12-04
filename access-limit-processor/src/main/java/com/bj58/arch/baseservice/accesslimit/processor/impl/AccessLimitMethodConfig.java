package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.value.AutoValue;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@AutoValue
abstract class AccessLimitMethodConfig {
    abstract String groupName();

    abstract String methodName();

    abstract int methodIndex();

    abstract int maxPermits();

    abstract int minPermits();

    abstract int weight();

    static Builder builder() {
        return new AutoValue_AccessLimitMethodConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder groupName(final String name);

        public abstract Builder methodName(final String name);

        public abstract Builder methodIndex(final int index);

        public abstract Builder maxPermits(int maxLimit);

        public abstract Builder minPermits(int minLimit);

        public abstract Builder weight(int weight);

        public abstract AccessLimitMethodConfig build();
    }
}
