/*
 * Copyright (c) 2008-present The Aspectran Project
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
 */
package com.aspectran.core.component.bean;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;

/**
 * Thrown when more than one bean matches a requested type.
 * Provides the candidate {@link com.aspectran.core.context.rule.BeanRule}
 * descriptors for diagnostics.
 */
public class NoUniqueBeanException extends BeanException {

    @Serial
    private static final long serialVersionUID = 8350428939010030065L;

    private final Class<?> type;

    private final BeanRule[] beanRules;

    public NoUniqueBeanException(Class<?> type, @NonNull BeanRule[] beanRules) {
        super("No qualifying bean of type '" + type + "' is defined: expected single matching bean but found " +
                beanRules.length + ": [" + getBeanDescriptions(beanRules) + "]");
        this.type = type;
        this.beanRules = beanRules;
    }

    public Class<?> getType() {
        return type;
    }

    public BeanRule[] getBeanRules() {
        return beanRules;
    }

    public static String getBeanDescriptions(@NonNull BeanRule[] beanRules) {
        String[] describes = new String[beanRules.length];
        for (int i = 0; i < describes.length; i++) {
            describes[i] = beanRules[i].toString();
        }
        return StringUtils.joinWithCommas(describes);
    }

}
