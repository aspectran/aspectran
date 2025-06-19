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

import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;

/**
 * The Class ContentResult.
 *
 * <p>Created: 2008. 03. 23 PM 12:01:24</p>
 */
public class ContentResult extends ArrayList<ActionResult> {

    @Serial
    private static final long serialVersionUID = 7394299260107452305L;

    private final ProcessResult parent;

    private String name;

    private boolean explicit;

    public ContentResult(ProcessResult parent) {
        this(parent, 5);
    }

    public ContentResult(ProcessResult parent, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;

        if (parent != null) {
            parent.addContentResult(this);
            setExplicit(parent.isExplicit());
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

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public ActionResult getActionResult(String actionId) {
        if (actionId == null) {
            return null;
        }
        for (ListIterator<ActionResult> iter = listIterator(size()); iter.hasPrevious();) {
            ActionResult actionResult = iter.previous();
            if (actionId.equals(actionResult.getActionId())) {
                return actionResult;
            }
        }
        return null;
    }

    /**
     * Adds the action result.
     * @param actionResult the action result
     */
    public void addActionResult(@NonNull ActionResult actionResult) {
        ActionResult existing = getActionResult(actionResult.getActionId());
        if (existing != null &&
                existing.getResultValue() instanceof ResultValueMap resultValueMap &&
                actionResult.getResultValue() instanceof ResultValueMap) {
            resultValueMap.putAll((ResultValueMap)actionResult.getResultValue());
        } else {
            add(actionResult);
        }
    }

    public void addActionResult(Executable action, Object resultValue) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        ActionResult actionResult = new ActionResult();
        actionResult.setResultValue(action.getActionId(), resultValue);
        addActionResult(actionResult);
    }

    public void addActionResult(Executable parentAction, ProcessResult processResult) {
        if (parentAction == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        if (processResult == null) {
            throw new IllegalArgumentException("processResult must not be null");
        }
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
                    addActionResult(newActionResult);
                }
            }
        }
    }

    public String[] getActionIds() {
        Set<String> set = new LinkedHashSet<>();
        for (ActionResult actionResult : this) {
            if (actionResult.getActionId() != null) {
                set.add(actionResult.getActionId());
            }
        }
        return set.toArray(new String[0]);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("actionResults", this);
        return tsb.toString();
    }

}
