/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.ObjectToParameters;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

/**
 * Converts a ProcessResult object to an APON object.
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

    @NonNull
    @SuppressWarnings("unchecked")
    protected <T extends Parameters> T createContainer(Object object) {
        Assert.notNull(object, "object must not be null");
        if (object instanceof ProcessResult processResult) {
            if (processResult.size() == 1) {
                ContentResult contentResult = processResult.get(0);
                if (contentResult.getName() == null && contentResult.size() == 1) {
                    ActionResult actionResult = contentResult.get(0);
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
                    container.removeValue(name);
                }
                super.putValue(container, name, value);
            } else {
                super.putValue(container, value);
            }
        }
    }

    @NonNull
    public static Parameters from(ProcessResult processResult) {
        return new ContentsToParameters().read(processResult);
    }

    @NonNull
    public static Parameters from(ProcessResult processResult, StringifyContext stringifyContext) {
        return new ContentsToParameters().apply(stringifyContext).read(processResult);
    }

}
