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
package com.aspectran.core.activity.process.result;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;

/**
 * Holds the result of an action's execution.
 *
 * <p>Created: 2008. 03. 23 PM 12:01:24</p>
 */
public class ActionResult {

    private String actionId;

    private Object resultValue;

    /**
     * Gets the action id.
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Gets the result value of the action.
     * @return the result value of the action
     */
    public Object getResultValue() {
        return resultValue;
    }

    /**
     * Sets the result value of the action.
     * @param actionId the new action id
     * @param resultValue the new result value of the action
     */
    public void setResultValue(String actionId, Object resultValue) {
        if (actionId == null || !actionId.contains(ActivityContext.ID_SEPARATOR)) {
            this.actionId = actionId;
            this.resultValue = resultValue;
        } else {
            String[] ids = StringUtils.tokenize(actionId, ActivityContext.ID_SEPARATOR, true);
            if (ids.length == 1) {
                this.actionId = null;
                this.resultValue = resultValue;
            } else if (ids.length == 2) {
                ResultValueMap resultValueMap = new ResultValueMap();
                resultValueMap.put(ids[1], resultValue);
                this.actionId = ids[0];
                this.resultValue = resultValueMap;
            } else {
                ResultValueMap resultValueMap = new ResultValueMap();
                for (int i = 1; i < ids.length - 1; i++) {
                    ResultValueMap resultValueMap2 = new ResultValueMap();
                    resultValueMap.put(ids[i], resultValueMap2);
                    resultValueMap = resultValueMap2;
                }
                resultValueMap.put(ids[ids.length - 1], resultValue);
                this.actionId = actionId;
                this.resultValue = resultValueMap;
            }
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionId", actionId);
        tsb.append("resultValue", resultValue);
        return tsb.toString();
    }

}
