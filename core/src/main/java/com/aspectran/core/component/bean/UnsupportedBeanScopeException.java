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
import com.aspectran.core.context.rule.type.ScopeType;

import java.io.Serial;

/**
 * Thrown when a bean is defined with a scope that is not available in the
 * current {@code Activity} context.
 * This typically happens when a bean is scoped as 'request' or 'session',
 * but the current activity does not have a {@code RequestAdapter} or
 * {@code SessionAdapter}.
 */
public class UnsupportedBeanScopeException extends BeanException {

    @Serial
    private static final long serialVersionUID = -5350555208208267662L;

    /**
     * Create a new UnsupportedBeanScopeException.
     * @param scopeType the unsupported scope
     * @param beanRule the bean rule
     */
    public UnsupportedBeanScopeException(ScopeType scopeType, BeanRule beanRule) {
        super("The " + scopeType + " scope is not available for bean " + beanRule);
    }

}
