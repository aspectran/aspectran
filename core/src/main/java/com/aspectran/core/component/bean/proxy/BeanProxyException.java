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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.component.bean.BeanException;
import com.aspectran.core.context.rule.BeanRule;

import java.io.Serial;

/**
 * Exception thrown when creation or invocation of a proxy bean fails.
 * Carries the associated {@link com.aspectran.core.context.rule.BeanRule}
 * for diagnostics.
 */
public class BeanProxyException extends BeanException {

    @Serial
    private static final long serialVersionUID = -3560168431550039638L;

    private final BeanRule beanRule;

    /**
     * Creates a new BeanProxyException with the specified bean rule and cause.
     * @param beanRule the bean rule associated with the proxy creation failure
     * @param cause the cause of the exception
     */
    public BeanProxyException(BeanRule beanRule, Throwable cause) {
        super("Could not instantiate proxy bean " + beanRule, cause);
        this.beanRule = beanRule;
    }

    /**
     * Returns the bean rule associated with this exception.
     * @return the bean rule
     */
    public BeanRule getBeanRule() {
        return beanRule;
    }

}
