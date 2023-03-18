/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.util.StringUtils;

/**
 * The Class NoUniqueBeanException.
 */
public class NoUniqueBeanException extends BeanException {

    private static final long serialVersionUID = 8350428939010030065L;

    private final Class<?> type;

    private final BeanRule[] beanRules;

    /**
     * Instantiates a new NoUniqueBeanException.
     * @param type the required type of the missing bean
     * @param beanRules the bean rules
     */
    public NoUniqueBeanException(Class<?> type, BeanRule[] beanRules) {
        super("No qualifying bean of type '" + type + "' is defined: expected single matching bean but found " +
                beanRules.length + ": [" + getBeanDescriptions(beanRules) + "]");
        this.type = type;
        this.beanRules = beanRules;
    }

    /**
     * Returns the type required type of the missing bean.
     * @return the required type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets the bean rules.
     * @return the bean rules
     */
    public BeanRule[] getBeanRules() {
        return beanRules;
    }

    public static String getBeanDescriptions(BeanRule[] beanRules) {
        String[] describes = new String[beanRules.length];
        for (int i = 0; i < describes.length; i++) {
            describes[i] = beanRules[i].toString();
        }
        return StringUtils.joinCommaDelimitedList(describes);
    }

}
