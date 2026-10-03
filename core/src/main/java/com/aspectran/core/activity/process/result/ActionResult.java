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
package com.aspectran.core.activity.process.result;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Encapsulates the result of executing a single {@link com.aspectran.core.activity.process.action.Executable}
 * action. This class is the most granular unit in the structured result hierarchy.
 *
 * <p>It stores the action's unique identifier ({@code actionId}) and the value returned
 * by its execution. A collection of these objects is held by a {@link ContentResult} to
 * represent the outcomes of a group of actions. If the {@code actionId} contains
 * an ID separator (e.g., "."), the result value can be parsed into a nested
 * {@link ResultValueMap} to represent complex data structures.</p>
 *
 * <p>Created: 2008. 03. 23 PM 12:01:24</p>
 */
public class ActionResult {

    private String actionId;

    private Object resultValue;

    /**
     * Instantiates a new ActionResult.
     */
    public ActionResult() {
    }

    /**
     * Instantiates a new ActionResult with the given action ID and result value.
     * @param actionId the unique identifier for the action
     * @param resultValue the value returned by the action
     */
    public ActionResult(String actionId, Object resultValue) {
        setResultValue(actionId, resultValue);
    }

    /**
     * Returns the unique identifier of the action.
     * @return the action ID
     */
    @Nullable
    public String getActionId() {
        return actionId;
    }

    /**
     * Returns the value produced by the action's execution.
     * @return the result value of the action
     */
    @Nullable
    public Object getResultValue() {
        return resultValue;
    }

    /**
     * Sets the result value for the action. If the provided {@code actionId} contains
     * an ID separator, it will be parsed into a nested {@link ResultValueMap}.
     * @param actionId the unique identifier for the action
     * @param resultValue the value returned by the action
     */
    public void setResultValue(@Nullable String actionId, @Nullable Object resultValue) {
        if (actionId == null || !actionId.contains(ActivityContext.ID_SEPARATOR)) {
            this.actionId = actionId;
            this.resultValue = resultValue;
        } else {
            String[] ids = StringUtils.tokenize(actionId, ActivityContext.ID_SEPARATOR, true);
            if (ids.length == 0) {
                this.actionId = null;
                this.resultValue = resultValue;
            } else if (ids.length == 1) {
                this.actionId = ids[0];
                this.resultValue = resultValue;
            } else {
                ResultValueMap rootMap = new ResultValueMap();
                ResultValueMap currentMap = rootMap;
                for (int i = 1; i < ids.length - 1; i++) {
                    ResultValueMap nextMap = new ResultValueMap();
                    currentMap.put(ids[i], nextMap);
                    currentMap = nextMap;
                }
                currentMap.put(ids[ids.length - 1], resultValue);
                this.actionId = ids[0];
                this.resultValue = rootMap;
            }
        }
    }

    @Override
    @NonNull
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionId", actionId);
        tsb.append("resultValue", resultValue);
        return tsb.toString();
    }

}
