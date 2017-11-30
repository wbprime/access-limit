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
    abstract int index();

    abstract int maxLimit();

    abstract int minLimit();

    abstract int seconds();

    abstract int weight();

    static Builder builder() {
        return new AutoValue_AccessLimitMethodConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder index(int index);

        public abstract Builder maxLimit(int maxLimit);

        public abstract Builder minLimit(int minLimit);

        public abstract Builder seconds(int seconds);

        public abstract Builder weight(int weight);

        public abstract AccessLimitMethodConfig build();
    }
}
