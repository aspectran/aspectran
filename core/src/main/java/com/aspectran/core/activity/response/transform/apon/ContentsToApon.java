/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.apon.ObjectToApon;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

/**
 * Converts a ProcessResult object to an APON object.
 * 
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class ContentsToApon {

    public static Parameters from(ProcessResult processResult) {
        if (processResult == null) {
            throw new IllegalArgumentException("processResult must not be null");
        }

        if (processResult.size() == 1) {
            ContentResult contentResult = processResult.get(0);
            if (contentResult.getName() == null && contentResult.size() == 1) {
                ActionResult actionResult = contentResult.get(0);
                if (actionResult.getActionId() == null) {
                    Object resultValue = actionResult.getResultValue();
                    if (resultValue instanceof Parameters) {
                        return (Parameters)resultValue;
                    }
                }
            }
        }

        Parameters container = new VariableParameters();
        for (ContentResult contentResult : processResult) {
            read(contentResult, container);
        }
        return container;
    }

    private static void read(ContentResult contentResult, Parameters container) {
        if (contentResult.getName() != null) {
            Parameters p = new VariableParameters();
            container.putValue(contentResult.getName(), p);
            container = p;
        }
        for (ActionResult actionResult : contentResult) {
            ObjectToApon.putValue(container, actionResult.getActionId(), actionResult.getResultValue());
        }
    }

}
