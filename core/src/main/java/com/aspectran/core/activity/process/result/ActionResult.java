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
package com.aspectran.core.activity.process.result;

import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class ActionResult.
 * 
 * <p>Created: 2008. 03. 23 PM 12:01:24</p>
 */
public class ActionResult {

    public static final Object NO_RESULT = new Object();

    private final ContentResult parent;

    private String actionId;

    private Object resultValue;

    private boolean hidden;

    /**
     * Instantiates a new Action result.
     *
     * @param parent the parent result
     */
    public ActionResult(ContentResult parent) {
        this.parent = parent;

        if (parent != null) {
            parent.addActionResult(this);
        }
    }

    /**
     * Gets the parent.
     *
     * @return the parent result
     */
    public ContentResult getParent() {
        return parent;
    }

    /**
     * Gets the action id.
     *
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the action id.
     *
     * @param actionId the new action id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets the result value of an action.
     *
     * @return the result value of an action
     */
    public Object getResultValue() {
        return resultValue;
    }

    /**
     * Sets the result value of an action.
     *
     * @param resultValue the new result value of an action
     */
    public void setResultValue(Object resultValue) {
        this.resultValue = resultValue;
    }

    /**
     * Returns whether or not to expose this action's result.
     *
     * @return true if hide this action result; false otherwise
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets whether or not to hide the result of this action.
     *
     * @param hidden whether to hide this action result
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionId", actionId);
        tsb.append("resultValue", resultValue);
        tsb.append("hidden", hidden);
        return tsb.toString();
    }

}
