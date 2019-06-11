package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
@AutoValue
abstract class AccessLimitGroupConfig {
    abstract String name();

    abstract int maxPermits();

    abstract long micros();

    abstract ImmutableMap<String, AccessLimitMethodConfig> methodConfigs();

    static Builder builder() {
        return new AutoValue_AccessLimitGroupConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder maxPermits(int maxLimit);

        public abstract Builder micros(long micros);

        abstract ImmutableMap.Builder<String, AccessLimitMethodConfig> methodConfigsBuilder();
        public final Builder addMethodConfig(
                final String methodName, final AccessLimitMethodConfig config
        ) {
            methodConfigsBuilder().put(methodName, config);
            return this;
        }

        public abstract AccessLimitGroupConfig build();
    }
}
