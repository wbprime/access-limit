package com.bj58.arch.baseservice.accesslimit.core;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
public interface QpsManageGroup extends QpsManageNode, QpsLimitRequestAware {
    void addChild(final QpsManageLeaf node);

    AccessGroupContext context();
}
