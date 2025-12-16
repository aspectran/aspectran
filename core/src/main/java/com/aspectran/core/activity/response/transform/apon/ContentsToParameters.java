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
package com.aspectran.core.activity.response.transform.apon;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.apon.ObjectToParameters;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import org.jspecify.annotations.NonNull;

/**
 * Specializes {@link ObjectToParameters} to convert a {@link ProcessResult} object
 * into an APON {@link Parameters} object.
 *
 * <p>This class understands the hierarchical structure of {@code ProcessResult},
 * {@link ContentResult}, and {@link ActionResult}, mapping them into a corresponding
 * APON structure. Note that this implementation is designed for a one-way conversion
 * from a {@code ProcessResult} and does not support the general-purpose
 * `read(String, Object)` methods, which will throw {@link UnsupportedOperationException}.
 * </p>
 *
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class ContentsToParameters extends ObjectToParameters {

    public ContentsToParameters() {
        super();
    }

    @Override
    public <T extends Parameters> T read(String name, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Parameters> T read(String name, Object object, T container) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a container {@link Parameters} object to hold the converted results.
     * <p>As an optimization, if the {@code ProcessResult} contains a single, unnamed
     * {@link ContentResult} which in turn contains a single, unnamed {@link ActionResult}
     * whose value is already a {@link Parameters} object, that object is returned
     * directly. Otherwise, a new {@link VariableParameters} instance is created
     * and populated.</p>
     * @param object the object to be converted, expected to be a {@link ProcessResult}
     * @return a {@link Parameters} object representing the converted data
     */
    @NonNull
    @SuppressWarnings("unchecked")
    protected <T extends Parameters> T createContainer(Object object) {
        Assert.notNull(object, "object must not be null");
        if (object instanceof ProcessResult processResult) {
            if (processResult.size() == 1) {
                ContentResult contentResult = processResult.getFirst();
                if (contentResult.getName() == null && contentResult.size() == 1) {
                    ActionResult actionResult = contentResult.getFirst();
                    if (actionResult.getActionId() == null) {
                        Object resultValue = actionResult.getResultValue();
                        if (resultValue instanceof Parameters parameters) {
                            return (T)parameters;
                        }
                    }
                }
            }

            Parameters container = new VariableParameters();
            for (ContentResult contentResult : processResult) {
                if (contentResult != null) {
                    putValue(container, contentResult);
                }
            }
            return (T)container;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Populates the given container with the action results from a {@link ContentResult}.
     * If the {@code ContentResult} has a name, a nested {@link Parameters} object is
     * created for it.
     * @param container the parent {@link Parameters} container
     * @param contentResult the {@link ContentResult} to process
     */
    private void putValue(Parameters container, @NonNull ContentResult contentResult) {
        if (contentResult.getName() != null) {
            Parameters ps = new VariableParameters();
            container.putValue(contentResult.getName(), ps);
            container = ps;
        }
        for (ActionResult actionResult : contentResult) {
            String name = actionResult.getActionId();
            Object value = actionResult.getResultValue();
            if (name != null) {
                if (container.hasParameter(name)) {
                    container.removeParameter(name);
                }
                super.putValue(container, name, value);
            } else {
                super.putValue(container, value);
            }
        }
    }

    /**
     * Converts the given {@link ProcessResult} into an APON {@link Parameters} object.
     * @param processResult the process result to convert
     * @return the converted {@link Parameters} object
     */
    @NonNull
    public static Parameters from(ProcessResult processResult) {
        return new ContentsToParameters().read(processResult);
    }

    /**
     * Converts the given {@link ProcessResult} into an APON {@link Parameters} object
     * using the specified stringify context.
     * @param processResult the process result to convert
     * @param stringifyContext the context for custom string conversion
     * @return the converted {@link Parameters} object
     */
    @NonNull
    public static Parameters from(ProcessResult processResult, StringifyContext stringifyContext) {
        return new ContentsToParameters().apply(stringifyContext).read(processResult);
    }

}
