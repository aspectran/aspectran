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
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
     */
    public ContentResult() {
        this(null, 5);
    }

    /**
     * Instantiates a new ContentResult with the specified initial capacity.
     * @param initialCapacity the initial capacity of the list
     */
    public ContentResult(int initialCapacity) {
        this(null, initialCapacity);
    }

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
    @Nullable
    public ProcessResult getParent() {
        return parent;
    }

    /**
     * Returns the name of this content group.
     * @return the name of the content group
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this content group.
     * @param name the name of the content group
     */
    public void setName(@Nullable String name) {
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
    @Nullable
    public ActionResult getActionResult(@Nullable String actionId) {
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
        Assert.notNull(actionResult, "actionResult must not be null");
        ActionResult existing = getActionResult(actionResult.getActionId());
        if (existing != null &&
                existing.getResultValue() instanceof ResultValueMap existingMap &&
                actionResult.getResultValue() instanceof ResultValueMap newMap) {
            existingMap.putAll(newMap);
        } else {
            add(actionResult);
        }
    }

    /**
     * A convenience method to create and add an {@link ActionResult}.
     * @param action the executed action
     * @param resultValue the value returned by the action
     */
    public void addActionResult(@NonNull Executable action, @Nullable Object resultValue) {
        Assert.notNull(action, "action must not be null");
        addActionResult(new ActionResult(action.getActionId(), resultValue));
    }

    /**
     * Adds all action results from a nested {@link ProcessResult}, prepending the
     * parent action's ID to each nested action ID to maintain a hierarchical structure.
     * @param parentAction the action that produced the nested process result
     * @param processResult the nested process result to import
     */
    public void addActionResult(@NonNull Executable parentAction, @NonNull ProcessResult processResult) {
        Assert.notNull(parentAction, "parentAction must not be null");
        Assert.notNull(processResult, "processResult must not be null");
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
                    addActionResult(new ActionResult(actionId, actionResult.getResultValue()));
                }
            }
        }
    }

    /**
     * Returns an array of all unique action IDs contained within this result.
     * @return an array of action IDs
     */
    @NonNull
    public String[] getActionIds() {
        Set<String> set = new LinkedHashSet<>();
        for (ActionResult actionResult : this) {
            if (actionResult.getActionId() != null) {
                set.add(actionResult.getActionId());
            }
        }
        return StringUtils.toStringArray(set);
    }

    @Override
    @NonNull
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("actionResults", this);
        return tsb.toString();
    }

}
