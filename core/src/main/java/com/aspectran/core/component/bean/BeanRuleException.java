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
import com.aspectran.core.context.rule.IllegalRuleException;

import java.io.Serial;

/**
 * Exception indicating an invalid or inconsistent {@link BeanRule}.
 * <p>
 * Thrown during analysis of annotation/XML configuration when a bean
 * definition cannot be resolved (e.g., missing methods, duplicate lifecycle
 * declarations, or invalid factory settings).
 * </p>
 */
public class BeanRuleException extends IllegalRuleException {

    @Serial
    private static final long serialVersionUID = -8362121026201328444L;

    private final BeanRule beanRule;

    /**
     * Instantiates a new BeanRuleException.
     * @param beanRule the bean rule
     */
    public BeanRuleException(BeanRule beanRule) {
        super("Invalid bean " + beanRule);
        this.beanRule = beanRule;
    }

    /**
     * Instantiates a new BeanRuleException.
     * @param beanRule the bean rule
     * @param cause the root cause
     */
    public BeanRuleException(BeanRule beanRule, Throwable cause) {
        super("Invalid bean " + beanRule, cause);
        this.beanRule = beanRule;
    }

    /**
     * Instantiates a new BeanRuleException.
     * @param msg the detail message
     * @param beanRule the bean rule
     */
    public BeanRuleException(String msg, BeanRule beanRule) {
        super(msg + " for bean rule " + beanRule);
        this.beanRule = beanRule;
    }

    /**
     * Instantiates a new BeanRuleException.
     * @param msg the detail message
     * @param beanRule the bean rule
     * @param cause the root cause
     */
    public BeanRuleException(String msg, BeanRule beanRule, Throwable cause) {
        super(msg + " for bean rule " + beanRule, cause);
        this.beanRule = beanRule;
    }

    /**
     * Gets bean rule.
     * @return the bean rule
     */
    public BeanRule getBeanRule() {
        return beanRule;
    }

}
