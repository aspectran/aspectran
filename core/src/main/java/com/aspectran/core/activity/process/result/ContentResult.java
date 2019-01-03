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

import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;

/**
 * The Class ContentResult.
 * 
 * <p>Created: 2008. 03. 23 PM 12:01:24</p>
 */
public class ContentResult extends ArrayList<ActionResult> {

    /** @serial */
    private static final long serialVersionUID = 7394299260107452305L;

    private final ProcessResult parent;

    private String name;

    private boolean omittable;

    public ContentResult(ProcessResult parent, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;

        if (parent != null) {
            parent.addContentResult(this);
        }
    }

    public ProcessResult getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult getActionResult(String actionId) {
        if (actionId == null) {
            return null;
        }
        for (ActionResult actionResult : this) {
            if (actionId.equals(actionResult.getActionId())) {
                return actionResult;
            }
        }
        return null;
    }

    public boolean isOmittable() {
        return omittable;
    }

    public void setOmittable(boolean omittable) {
        this.omittable = omittable;
    }

    /**
     * Adds the action result.
     *
     * @param actionResult the action result
     */
    public void addActionResult(ActionResult actionResult) {
        ActionResult existActionResult = getActionResult(actionResult.getActionId());
        if (existActionResult != null &&
                existActionResult.getResultValue() instanceof ResultValueMap &&
                actionResult.getResultValue() instanceof ResultValueMap) {
            ResultValueMap resultValueMap = (ResultValueMap)existActionResult.getResultValue();
            resultValueMap.putAll((ResultValueMap)actionResult.getResultValue());
        } else {
            add(actionResult);
        }
    }

    public void addActionResult(Executable action, Object resultValue) {
        ActionResult actionResult = new ActionResult();
        actionResult.setResultValue(action.getActionId(), resultValue);
        actionResult.setHidden(action.isHidden());
        addActionResult(actionResult);
    }

    public void addActionResult(Executable parentAction, ProcessResult processResult) {
        for (ContentResult contentResult : processResult) {
            for (ActionResult actionResult : contentResult) {
                if (actionResult.getActionId() != null) {
                    String actionId;
                    if (parentAction.getActionId() != null) {
                        actionId = parentAction.getActionId() + ActivityContext.ID_SEPARATOR +
                                actionResult.getActionId();
                    } else {
                        actionId = actionResult.getActionId();
                    }
                    ActionResult newActionResult = new ActionResult();
                    newActionResult.setResultValue(actionId, actionResult.getResultValue());
                    newActionResult.setHidden(parentAction.isHidden());
                    addActionResult(newActionResult);
                }
            }
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("values", this);
        return tsb.toString();
    }

}
