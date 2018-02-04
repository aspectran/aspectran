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
package com.aspectran.core.component.template;

import com.aspectran.core.context.rule.TemplateRule;

/**
 * This class is the basic exception that gets thrown from the template package.
 * 
 * <p>Created: 2016. 01. 15.</p>
 */
public class TemplateProcessorException extends TemplateRuleException {

    /** @serial */
    private static final long serialVersionUID = -1495281620922964138L;

    /**
     * Instantiates a new TemplateProcessorException.
     *
     * @param msg the detail message
     * @param templateRule the template rule
     */
    public TemplateProcessorException(String msg, TemplateRule templateRule) {
        super(msg, templateRule);
    }

    /**
     * Instantiates a new TemplateProcessorException.
     *
     * @param msg the detail message
     * @param templateRule the template rule
     * @param cause the root cause
     */
    public TemplateProcessorException(String msg, TemplateRule templateRule, Throwable cause) {
        super(msg, templateRule, cause);
    }

}
