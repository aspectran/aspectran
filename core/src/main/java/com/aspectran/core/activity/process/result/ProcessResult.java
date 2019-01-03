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
package com.aspectran.core.activity.process.result;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;

/**
 * The Class ProcessResult.
 * 
 * <p>Created: 2008. 06. 09 PM 4:13:40</p>
 */
public class ProcessResult extends ArrayList<ContentResult> {

    /** @serial */
    private static final long serialVersionUID = 4734650376929217378L;

    private String name;

    private boolean omittable;

    public ProcessResult() {
    }

    public ProcessResult(int initialCapacity) {
        super(initialCapacity);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOmittable() {
        return omittable;
    }

    public void setOmittable(boolean omittable) {
        this.omittable = omittable;
    }

    /**
     * Adds the content result.
     *
     * @param contentResult the content result
     */
    protected void addContentResult(ContentResult contentResult) {
        add(contentResult);
    }

    /**
     * Returns the result of the action as an {@code ActionResult}.
     *
     * @param actionId the action id
     * @return the result of the action
     */
    public ActionResult getActionResult(String actionId) {
        for (ContentResult contentResult : this) {
            ActionResult actionResult = contentResult.getActionResult(actionId);
            if (actionResult != null) {
                return actionResult;
            }
        }
        return null;
    }

    /**
     * Returns the result value of the action as an {@code Object}.
     *
     * @param actionId the action id
     * @return the result value of the action
     */
    public Object getResultValue(String actionId) {
        if (actionId == null) {
            return null;
        }
        if (!actionId.contains(ActivityContext.ID_SEPARATOR)) {
            ActionResult actionResult = getActionResult(actionId);
            return (actionResult != null ? actionResult.getResultValue() : null);
        } else {
            String[] ids = StringUtils.tokenize(actionId, ActivityContext.ID_SEPARATOR, true);
            if (ids.length == 1) {
                ActionResult actionResult = getActionResult(actionId);
                return (actionResult != null ? actionResult.getResultValue() : null);
            } else {
                ActionResult actionResult = getActionResult(ids[0]);
                if (actionResult == null || !(actionResult.getResultValue() instanceof ResultValueMap)) {
                    return null;
                }
                ResultValueMap resultValueMap = (ResultValueMap)actionResult.getResultValue();
                for (int i = 1; i < ids.length - 1; i++) {
                    Object value = resultValueMap.get(ids[i]);
                    if (!(value instanceof ResultValueMap)) {
                        return null;
                    }
                }
                return resultValueMap.get(ids[ids.length - 1]);
            }
        }
    }

    public String describe() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("omittable", omittable);
        tsb.appendSize("size", this);
        tsb.append("values", this);
        return tsb.toString();
    }

}
