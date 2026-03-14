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

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.context.rule.BeanRule;

import java.io.Serial;

/**
 * Exception thrown when a {@link FactoryBean} is not yet fully initialized,
 * for example because it is involved in a circular reference.
 *
 * @since 2.0.0
 */
public class FactoryBeanNotInitializedException extends BeanCreationException {

    @Serial
    private static final long serialVersionUID = 5961471681939634699L;

    /**
     * Create a new FactoryBeanNotInitializedException.
     * @param beanRule the bean rule
     */
    public FactoryBeanNotInitializedException(BeanRule beanRule) {
        super(beanRule, "FactoryBean is not fully initialized yet");
    }

    /**
     * Create a new FactoryBeanNotInitializedException.
     * @param beanRule the bean rule
     * @param msg the detail message
     */
    public FactoryBeanNotInitializedException(BeanRule beanRule, String msg) {
        super(beanRule, msg);
    }

}
