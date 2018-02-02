/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

/**
 * The Class UnsupportedBeanScopeException.
 */
public class UnsupportedBeanScopeException extends BeanRuleException {

    /** @serial */
    private static final long serialVersionUID = -5350555208208267662L;

    /**
     * Create a new UnsupportedBeanScopeException.
     *
     * @param scopeType the scope type
     * @param beanRule the bean rule
     */
    public UnsupportedBeanScopeException(ScopeType scopeType, BeanRule beanRule) {
        super("The " + scopeType + " scope is not available. beanRule", beanRule);
    }

}
