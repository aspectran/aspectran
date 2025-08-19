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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;

/**
 * An exception that is thrown when a parameter binding fails.
 *
 * <p>This exception is typically thrown when there is an error during the process of
 * binding request parameters or other data sources to the arguments of a method
 * being invoked by an action.</p>
 *
 * <p>Created: 2009. 02. 26</p>
 */
public class ParameterBindingException extends ProcessException {

    @Serial
    private static final long serialVersionUID = 1094222427227812288L;

    private final ParameterBindingRule parameterBindingRule;

    /**
     * Creates a new ParameterBindingException.
     * @param parameterBindingRule the parameter binding rule that caused the exception
     * @param cause the root cause
     */
    public ParameterBindingException(
            @NonNull ParameterBindingRule parameterBindingRule, @NonNull Throwable cause) {
        super("Failed to bind request parameter to action method parameter " +
                parameterBindingRule + "; Cause: " +
            ExceptionUtils.getRootCauseSimpleMessage(cause), cause);

        this.parameterBindingRule = parameterBindingRule;
    }

    /**
     * Returns the parameter binding rule that caused this exception.
     * @return the parameter binding rule
     */
    @NonNull
    public ParameterBindingRule getParameterBindingRule() {
        return parameterBindingRule;
    }

}
