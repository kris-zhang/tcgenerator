/*
 * (C) Copyright 2017 kriszhang (http://kriszhang.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.kriszhang.tcgenerator;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.common.reflect.ClassPath.ClassInfo;
import com.kriszhang.tcgenerator.Configuration.FilterConfig;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.reflect.Modifier.isPublic;

/**
 * @author gongzuo.zy
 * @version $Id: ClassFilter.java, v0.1 2017-06-03 09:24  gongzuo.zy Exp $
 */
class InputFilter {

    private static final InputFilter INSTANCE = new InputFilter();

    static InputFilter getInstance() {
        return INSTANCE;
    }

    private InputFilter() {}

    Collection<Class> filter(Collection<ClassInfo> classInfo) {
        return classInfo.stream().map(ClassInfo::load).filter(InputFilter::filter).collect(Collectors.toList());
    }

    private static boolean filter(Class c) {
        FilterConfig filter = Configuration.getFilterConfig();

        if (filter.getAnonymousClass() != null) {
            if (filter.getAnonymousClass() && !c.isAnonymousClass()) {
                return false;
            }
            if (!filter.getAnonymousClass() && c.isAnonymousClass()) {
                return false;
            }
        }

        if (filter.getInterfaceClass() != null) {
            if (filter.getInterfaceClass() && !c.isInterface()) {
                return false;
            }
            if (!filter.getInterfaceClass() && c.isInterface()) {
                return false;
            }
        }

        if (filter.getLocalClass() != null) {
            if (filter.getLocalClass() && !c.isLocalClass()) {
                return false;
            }
            if (!filter.getLocalClass() && c.isLocalClass()) {
                return false;
            }
        }

        if (filter.getPublicClass() != null) {
            if (filter.getPublicClass() && !isPublic(c.getModifiers())) {
                return false;
            }
            if (!filter.getPublicClass() && isPublic(c.getModifiers())) {
                return false;
            }
        }

        if (!isNullOrEmpty(filter.getNamePattern()) && !c.getSimpleName().matches(filter.getNamePattern())) {
            return false;
        }

        if (!isNullOrEmpty(filter.getCustomize())) {
            boolean evalResult = ScriptEngine.getInstance().eval(filter.getCustomize(), c);
            if (!evalResult) {
                return false;
            }
        }

        return true;
    }

}
