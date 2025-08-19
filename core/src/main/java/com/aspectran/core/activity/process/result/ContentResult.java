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
 * Represents a container for the results of a logically grouped set of actions.
 * Typically, an instance of this class corresponds to a {@code <contents>}
 * block within a translet rule.
 *
 * <p>It holds a collection of {@link ActionResult} objects, each representing the
 * outcome of a single action. This class provides a mid-level grouping in the
 * result hierarchy ({@link ProcessResult} -> {@code ContentResult} -> {@link ActionResult}),
 * enabling structured access to the results of a specific action group.</p>
 *
 * <p>Created: 2008. 03. 23 PM 12:01:24</p>
 */
public class ContentResult extends ArrayList<ActionResult> {

    @Serial
    private static final long serialVersionUID = 7394299260107452305L;

    private final ProcessResult parent;

    private String name;

    private boolean explicit;

    /**
     * Instantiates a new ContentResult with a default initial capacity.
     * @param parent the parent {@link ProcessResult} that will contain this result
     */
    public ContentResult(ProcessResult parent) {
        this(parent, 5);
    }

    /**
     * Instantiates a new ContentResult with the specified initial capacity.
     * @param parent the parent {@link ProcessResult} that will contain this result
     * @param initialCapacity the initial capacity of the list
     */
    public ContentResult(ProcessResult parent, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;

        if (parent != null) {
            parent.addContentResult(this);
            setExplicit(parent.isExplicit());
        }
    }

    /**
     * Returns the parent {@link ProcessResult} that contains this result.
     * @return the parent process result
     */
    public ProcessResult getParent() {
        return parent;
    }

    /**
     * Returns the name of this content group.
     * @return the name of the content group
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this content group.
     * @param name the name of the content group
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this content group was explicitly defined in the configuration.
     * @return true if the content group was explicit, false otherwise
     */
    public boolean isExplicit() {
        return explicit;
    }

    /**
     * Sets whether this content group was explicitly defined.
     * @param explicit true if the content group was explicit, false otherwise
     */
    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    /**
     * Retrieves an {@link ActionResult} by its action ID.
     * It searches backwards from the end of the list.
     * @param actionId the ID of the action to find
     * @return the corresponding {@link ActionResult}, or {@code null} if not found
     */
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
     * Adds an {@link ActionResult} to this content group. If an action result with the
     * same ID already exists and both are map-like, their values are merged.
     * @param actionResult the action result to add
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

    /**
     * A convenience method to create and add an {@link ActionResult}.
     * @param action the executed action
     * @param resultValue the value returned by the action
     */
    public void addActionResult(Executable action, Object resultValue) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        ActionResult actionResult = new ActionResult();
        actionResult.setResultValue(action.getActionId(), resultValue);
        addActionResult(actionResult);
    }

    /**
     * Adds all action results from a nested {@link ProcessResult}, prepending the
     * parent action's ID to each nested action ID to maintain a hierarchical structure.
     * @param parentAction the action that produced the nested process result
     * @param processResult the nested process result to import
     */
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

    /**
     * Returns an array of all unique action IDs contained within this result.
     * @return an array of action IDs
     */
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
