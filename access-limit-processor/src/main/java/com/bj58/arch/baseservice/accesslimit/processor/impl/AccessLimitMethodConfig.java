package com.bj58.arch.baseservice.accesslimit.processor.impl;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
final class AccessLimitMethodConfig {
    private final int index;
    private final int limit;
    private final int seconds;
    private final int weight;

    private AccessLimitMethodConfig(
            int index, int limit, int seconds, int weight
    ) {
        this.index = index;
        this.limit = limit;
        this.seconds = seconds;
        this.weight = weight;
    }

    int index() {
        return index;
    }

    int limit() {
        return limit;
    }

    int seconds() {
        return seconds;
    }

    int weight() {
        return weight;
    }

    @Override
    public String toString() {
        return "AccessLimitMethodConfig{"
                + "index=" + index + ", "
                + "limit=" + limit + ", "
                + "seconds=" + seconds + ", "
                + "weight=" + weight
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof AccessLimitMethodConfig) {
            AccessLimitMethodConfig that = (AccessLimitMethodConfig) o;
            return (this.index == that.index())
                    && (this.limit == that.limit())
                    && (this.seconds == that.seconds())
                    && (this.weight == that.weight());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.index;
        h *= 1000003;
        h ^= this.limit;
        h *= 1000003;
        h ^= this.seconds;
        h *= 1000003;
        h ^= this.weight;
        return h;
    }

    public static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private Integer index;
        private Integer limit;
        private Integer seconds;
        private Integer weight;
        Builder() {
        }

        public AccessLimitMethodConfig.Builder index(int index) {
            this.index = index;
            return this;
        }

        public AccessLimitMethodConfig.Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public AccessLimitMethodConfig.Builder seconds(int seconds) {
            this.seconds = seconds;
            return this;
        }

        public AccessLimitMethodConfig.Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public AccessLimitMethodConfig build() {
            String missing = "";
            if (this.index == null) {
                missing += " index";
            }
            if (this.limit == null) {
                missing += " limit";
            }
            if (this.seconds == null) {
                missing += " seconds";
            }
            if (this.weight == null) {
                missing += " weight";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            return new AccessLimitMethodConfig(
                    this.index,
                    this.limit,
                    this.seconds,
                    this.weight
            );
        }
    }
}
