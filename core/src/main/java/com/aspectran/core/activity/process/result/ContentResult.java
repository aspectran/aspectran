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
        add(actionResult);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("values", this);
        return tsb.toString();
    }

}
