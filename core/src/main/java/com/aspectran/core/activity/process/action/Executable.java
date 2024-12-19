/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.type.ActionType;

/**
 * The Interface Executable.
 *
 * <p>Created: 2008. 03. 23 AM 10:38:29</p>
 */
public interface Executable {

    /**
     * Execute this action.
     * @param activity the activity
     * @return the result of action execution
     * @throws Exception if the action fails to execute
     */
    Object execute(Activity activity) throws Exception;

    /**
     * Gets the action id.
     * @return the action id
     */
    default String getActionId() {
        return null;
    }

    /**
     * Returns whether this action is hidden.
     * @return true, if is hidden action
     */
    default boolean isHidden() {
        return false;
    }

    /**
     * Gets the Action Type.
     * @return the Action Type
     */
    default ActionType getActionType() {
        return null;
    }

}
