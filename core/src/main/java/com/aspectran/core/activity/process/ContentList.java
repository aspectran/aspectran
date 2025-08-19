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
package com.aspectran.core.activity.process;

import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A container for a structured collection of {@link ActionList} objects, representing a
 * major section of a translet's executable content (e.g., a {@code <contents>}
 * or {@code <response>} block).
 *
 * <p>This class allows for the hierarchical organization of the process flow, where
 * different groups of actions can be defined and executed as part of a larger
 * transaction. This structure is key to how Aspectran models request processing
 * in a way that can be represented like an XML document.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:47:57</p>
 */
public class ContentList extends ArrayList<ActionList> implements Replicable<ContentList> {

    @Serial
    private static final long serialVersionUID = 2567969961069441527L;

    private final boolean explicit;

    @Nullable
    private String name;

    /**
     * Instantiates a new ContentList.
     * @param explicit whether this content list was explicitly defined
     */
    public ContentList(boolean explicit) {
        super(3);
        this.explicit = explicit;
    }

    private ContentList(ContentList contentList) {
        super(contentList);
        this.explicit = contentList.isExplicit();
        this.name = contentList.getName();
    }

    /**
     * Returns whether this content list was explicitly defined in the configuration.
     * @return true if the content list was explicit, false otherwise
     */
    public boolean isExplicit() {
        return explicit;
    }

    /**
     * Returns the name of this content list.
     * @return the name of the content list
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this content list.
     * @param name the name of the content list
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Retrieves an {@link ActionList} by its name.
     * @param name the name of the action list to find
     * @return the corresponding {@link ActionList}, or {@code null} if not found
     */
    public ActionList getActionList(String name) {
        for (ActionList actionList : this) {
            if (Objects.equals(name, actionList.getName())) {
                return actionList;
            }
        }
        return null;
    }

    /**
     * Adds an {@link ActionList} to this content list.
     * @param actionList the action list to add
     */
    public void addActionList(ActionList actionList) {
        if (actionList != null) {
            add(actionList);
        }
    }

    @Override
    public ContentList replicate() {
        return new ContentList(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("contents", this);
        return tsb.toString();
    }

    /**
     * A factory method to create a new instance of ContentList.
     * @param name the name of the content list
     * @return the new ContentList instance
     */
    @NonNull
    public static ContentList newInstance(String name) {
        ContentList contentList = new ContentList(true);
        contentList.setName(name);
        return contentList;
    }

}
