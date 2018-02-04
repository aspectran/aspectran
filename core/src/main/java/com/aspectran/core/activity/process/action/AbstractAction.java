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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.process.ActionList;

/**
 * The Class AbstractAction.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public abstract class AbstractAction implements Executable {

    protected final ActionList parent;

    /**
     * Instantiates a new AbstractAction.
     *
     * @param parent the parent
     */
    public AbstractAction(ActionList parent) {
        this.parent = parent;
    }

    @Override
    public ActionList getParent() {
        return parent;
    }

    @Override
    public abstract String getActionId();

}
