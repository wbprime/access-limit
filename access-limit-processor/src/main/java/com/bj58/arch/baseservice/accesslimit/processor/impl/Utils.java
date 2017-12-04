package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.common.MoreElements;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
final class Utils {
    private Utils() { throw new AssertionError("Construction forbidden"); }

    static String packageNameOf(final TypeElement type) {
        final PackageElement packageElement = MoreElements.getPackage(type);
        return packageElement.getQualifiedName().toString();
    }

    static String classNameOf(final TypeElement type) {
        final String name = type.getQualifiedName().toString();
        final String pkgName = packageNameOf(type);
        return pkgName.isEmpty() ? name : name.substring(pkgName.length() + 1);
    }
}
