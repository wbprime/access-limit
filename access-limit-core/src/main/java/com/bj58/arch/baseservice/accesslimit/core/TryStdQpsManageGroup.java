package com.bj58.arch.baseservice.accesslimit.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
public class TryStdQpsManageGroup implements TryQpsManageGroup {
    private final String theId;

    private ImmutableMap<String, TryQpsManageLeaf> leaves = ImmutableMap.of();

    public TryStdQpsManageGroup(final String theId) {
        this.theId = theId;
    }

    @Override
    public String id() {
        return theId;
    }

    @Override
    public void onQpsChanged(final String id, double v) {
        final TryQpsManageLeaf leaf = leaves.get(id);
        if (null != leaf) {
            // Try to adjust QPS for target child item
            final double oldChildLimit = leaf.currentQpsLimit();
            leaf.adjust(v);
            final double newChildLimit = leaf.currentQpsLimit();

            final double changing = newChildLimit - oldChildLimit;
            if (changing > 0.0 && leaves.size() > 1) {
                double toChange = changing;

                final List<TryQpsManageLeaf> leavesToChange = Lists.newLinkedList();
                for (final TryQpsManageLeaf leafToChange : leaves.values()) {
                    if (! leafToChange.id().equals(id)) leavesToChange.add(leafToChange);
                }

                while (toChange > 0.0 && ! leavesToChange.isEmpty()) {
                    final int totalCount = leavesToChange.size();

                    final double eachChange = toChange / totalCount;

                    int idx = totalCount - 1;
                    while (idx >= 0) {
                        final TryQpsManageLeaf leafToChange = leavesToChange.get(idx);

                        final double oldLimit = leafToChange.currentQpsLimit();
                        final double newLimit = leafToChange.changeQpsLimit(oldLimit - eachChange);

                        final double changed = newLimit - oldLimit;
                        if (changed < eachChange) {
                            leavesToChange.remove(leafToChange);
                        }

                        toChange = toChange - changed;

                        idx--;
                    }
                }

                if (toChange > 0.0) {
                    // TODO error or warning
                }
            }
        } else {
            // TODO error or warning
        }
    }
}
