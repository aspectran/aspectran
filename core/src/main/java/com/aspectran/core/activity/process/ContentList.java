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
 * The set of Content is called Contents or ContentList.
 *
 * <p>Created: 2008. 03. 22 PM 5:47:57</p>
 */
public class ContentList extends ArrayList<ActionList> implements Replicable<ContentList> {

    @Serial
    private static final long serialVersionUID = 2567969961069441527L;

    private final boolean explicit;

    @Nullable
    private String name;

    public ContentList(boolean explicit) {
        super(3);
        this.explicit = explicit;
    }

    private ContentList(ContentList contentList) {
        super(contentList);
        this.explicit = contentList.isExplicit();
        this.name = contentList.getName();
    }

    public boolean isExplicit() {
        return explicit;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public ActionList getActionList(String name) {
        for (ActionList actionList : this) {
            if (Objects.equals(name, actionList.getName())) {
                return actionList;
            }
        }
        return null;
    }

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

    @NonNull
    public static ContentList newInstance(String name) {
        ContentList contentList = new ContentList(true);
        contentList.setName(name);
        return contentList;
    }

}
