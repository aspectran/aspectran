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
import com.aspectran.utils.ExceptionUtils;

import java.io.Serial;

/**
 * Thrown when the container fails to create a bean instance.
 * Carries the associated {@link com.aspectran.core.context.rule.BeanRule}
 * for diagnostics.
 */
public class BeanCreationException extends BeanException {

    @Serial
    private static final long serialVersionUID = -4711272699122321571L;

    private final BeanRule beanRule;

    public BeanCreationException(BeanRule beanRule) {
        super("Cannot create a bean " + beanRule);
        this.beanRule = beanRule;
    }

    public BeanCreationException(String msg, BeanRule beanRule) {
        super(msg + " " + beanRule);
        this.beanRule = beanRule;
    }

    public BeanCreationException(BeanRule beanRule, Throwable cause) {
        super("Cannot create a bean " + beanRule, ExceptionUtils.unwrapThrowable(cause));
        this.beanRule = beanRule;
    }

    public BeanCreationException(String msg, BeanRule beanRule, Throwable cause) {
        super(msg + " " + beanRule, ExceptionUtils.unwrapThrowable(cause));
        this.beanRule = beanRule;
    }

    public BeanRule getBeanRule() {
        return beanRule;
    }

}
