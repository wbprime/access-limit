package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class StdQpsManageGroup implements Supplier<AccessAware>, QpsManageGroup {
    private static final Logger LOGGOR = LoggerFactory.getLogger(StdQpsManageGroup.class);

    private final AccessGroupContext context;

    private ImmutableMap<String, QpsManageLeaf> leaves = ImmutableMap.of();

    public StdQpsManageGroup(final AccessGroupContext context) {
        this.context = context;
    }

    @Override
    public AccessAware get() {
        final Map<String, AccessAware> map = Maps.newHashMapWithExpectedSize(leaves.size());
        for (final Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
            map.put(entry.getKey(), new StdAccessMonitor(context, entry.getValue()));
        }

        return new ComposedAccessMonitor(map);
    }

    @Override
    public synchronized void addChild(final QpsManageLeaf node) {
        // Check sum min limit with group max limit
        {
            long v = 0L;
            for (final Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
                final QpsManageLeaf leaf = entry.getValue();
                v += leaf.qpsLimitMin();
            }

            checkState(
                    v + node.qpsLimitMin() <= context.maxLimit(),
                    "Min limit is not enough"
            );
        }

        long existedCurLimit = 0L;
        for (final Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
            final QpsManageLeaf leaf = entry.getValue();
            existedCurLimit += leaf.currentQpsLimit();
        }

        final long limitLeft = context.maxLimit() - existedCurLimit;
        if (limitLeft >= node.currentQpsLimit()) {
            node.adjustMaxQpsLimit(node.currentQpsLimit());
        } else if (limitLeft >= node.qpsLimitMin()) {
            node.adjustMaxQpsLimit(limitLeft);
        } else {
            final long limitForNode = node.qpsLimitMin();
            final long limitForOthers = limitForNode - limitLeft;

            for (final Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
                existedCurLimit -= entry.getValue().qpsLimitMin();
            }

            final double percent = limitForOthers * 1.0 / existedCurLimit;
            for (final Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
                final QpsManageLeaf leaf = entry.getValue();
                final long curLimit = leaf.currentQpsLimit();
                leaf.adjustMaxQpsLimit((long) (curLimit - (curLimit - leaf.qpsLimitMin()) * percent));
            }

            node.adjustMaxQpsLimit(node.qpsLimitMin());
        }

        final Map<String, QpsManageLeaf> map = Maps.newHashMap();
        map.putAll(leaves);
        map.put(node.context().id(), node);
        this.leaves = ImmutableMap.copyOf(map);
    }

    @Override
    public String id() {
        return context.id();
    }

    @Override
    public AccessGroupContext context() {
        return context;
    }

    @Override
    public synchronized void onQpsLimitRequested(final QpsLimitRequestEvent event) {
        if (LOGGOR.isDebugEnabled()) {
            for (Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
                final QpsManageLeaf leaf = entry.getValue();
                LOGGOR.debug("Before id(\"{}\");min({});max({})", leaf.id(), leaf.qpsLimitMin(), leaf.currentQpsLimit());
            }
            LOGGOR.debug("onQpsLimitRequested {}", event);
        }

        final String id = event.sourceId();

        final QpsManageLeaf curLeaf = leaves.get(id);
        if (null == curLeaf) return;

        final long changing = event.expectedLimit() - event.currentLimit();
        if (changing > 0L && !leaves.isEmpty()) {
            long toChange = changing;

            final List<QpsManageLeaf> leavesToChange = Lists.newLinkedList();
            for (final QpsManageLeaf leafToChange : leaves.values()) {
                if (!leafToChange.id().equals(id)) leavesToChange.add(leafToChange);
            }

            while (toChange > 0L && !leavesToChange.isEmpty()) {
                final int totalCount = leavesToChange.size();

                final long eachChange = toChange / totalCount;

                int idx = totalCount - 1;
                while (idx >= 0) {
                    final QpsManageLeaf leafToChange = leavesToChange.get(idx);

                    final long oldLimit = leafToChange.currentQpsLimit();
                    leafToChange.adjustMaxQpsLimit(oldLimit - eachChange);
                    final long newLimit = leafToChange.currentQpsLimit();

                    final long changed = oldLimit - newLimit;
                    if (changed < eachChange) {
                        leavesToChange.remove(leafToChange);
                    }

                    toChange = toChange - changed;

                    idx--;
                }
            }

            curLeaf.adjustMaxQpsLimit(event.currentLimit() + (changing - toChange));
        }

        if (LOGGOR.isDebugEnabled()) {
            for (Map.Entry<String, QpsManageLeaf> entry : leaves.entrySet()) {
                final QpsManageLeaf leaf = entry.getValue();
                LOGGOR.debug("After id(\"{}\");min({});max({})", leaf.id(), leaf.qpsLimitMin(), leaf.currentQpsLimit());
            }
        }
    }
}
