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

import java.io.Serial;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the top-level container for all execution results within a single
 * {@link com.aspectran.core.activity.Activity} lifecycle. It provides a structured
 * view of the activity's outcome by holding a collection of {@link ContentResult} objects.
 *
 * <p>Each {@code ContentResult} corresponds to a logical grouping of actions (e.g., a
 * {@code <contents>} block), allowing for an organized, hierarchical representation of
 * what occurred during the request processing. This class serves as the final, aggregated
 * result of a translet execution.</p>
 *
 * <p>Created: 2008. 06. 09 PM 4:13:40</p>
 */
public class ProcessResult extends ArrayList<ContentResult> {

    @Serial
    private static final long serialVersionUID = 4734650376929217378L;

    private String name;

    private boolean explicit;

    /**
     * Instantiates a new ProcessResult with a default initial capacity.
     */
    public ProcessResult() {
        this(5);
    }

    /**
     * Instantiates a new ProcessResult with the specified initial capacity.
     * @param initialCapacity the initial capacity of the list
     */
    public ProcessResult(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Returns the name of this process result.
     * @return the name of the process result
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this process result.
     * @param name the name of the process result
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this process result was explicitly defined in the configuration.
     * @return true if the process result was explicit, false otherwise
     */
    public boolean isExplicit() {
        return explicit;
    }

    /**
     * Sets whether this process result was explicitly defined.
     * @param explicit true if the process result was explicit, false otherwise
     */
    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    /**
     * Adds a {@link ContentResult} to this process result. This method is intended
     * for internal use by the framework.
     * @param contentResult the content result to add
     */
    protected void addContentResult(ContentResult contentResult) {
        add(contentResult);
    }

    /**
     * Retrieves a {@link ContentResult} by its name.
     * @param name the name of the content group to find
     * @return the corresponding {@link ContentResult}, or {@code null} if not found
     */
    public ContentResult getContentResult(String name) {
        for (ContentResult contentResult : this) {
            if (Objects.equals(name, contentResult.getName())) {
                return contentResult;
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link ContentResult} by its name and explicit flag.
     * @param name the name of the content group to find
     * @param explicit the explicit flag of the content group
     * @return the corresponding {@link ContentResult}, or {@code null} if not found
     */
    public ContentResult getContentResult(String name, boolean explicit) {
        for (ContentResult contentResult : this) {
            if (Objects.equals(name, contentResult.getName()) && contentResult.isExplicit() == explicit) {
                return contentResult;
            }
        }
        return null;
    }

    /**
     * Returns the last {@link ContentResult} in this process result.
     * @return the last added {@link ContentResult}, or {@code null} if this result is empty
     */
    public ContentResult lastContentResult() {
        return (isEmpty() ? null : get(size() - 1));
    }

    /**
     * Retrieves an {@link ActionResult} by its action ID, searching through all contained
     * content results.
     * @param actionId the ID of the action to find
     * @return the corresponding {@link ActionResult}, or {@code null} if not found
     */
    public ActionResult getActionResult(String actionId) {
        if (actionId == null) {
            return null;
        }
        for (ListIterator<ContentResult> iter = listIterator(size()); iter.hasPrevious();) {
            ContentResult contentResult = iter.previous();
            ActionResult actionResult = contentResult.getActionResult(actionId);
            if (actionResult != null) {
                return actionResult;
            }
        }
        return null;
    }

    /**
     * Retrieves the value of an action's result by its ID. This method can also
     * access nested values if the action ID contains separators.
     * @param actionId the ID of the action whose result value is to be returned
     * @return the result value, or {@code null} if not found
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
                if (actionResult == null || !(actionResult.getResultValue() instanceof Map<?, ?> valueMap)) {
                    return null;
                }
                for (int i = 1; i < ids.length - 1; i++) {
                    Object value = valueMap.get(ids[i]);
                    if (!(value instanceof Map)) {
                        return null;
                    }
                }
                return valueMap.get(ids[ids.length - 1]);
            }
        }
    }

    /**
     * Returns a descriptive string representation of this process result, intended for debugging.
     * @return a string describing the contents of this result
     */
    public String describe() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.appendSize("size", this);
        tsb.append("values", this);
        return tsb.toString();
    }

}
