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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.type.ActionType;

/**
 * Represents a command that performs a specific, predefined operation within the execution
 * of a translet. This is the base interface for all "Actions" in Aspectran.
 *
 * <p>An Action is a fundamental building block of a translet's content. Each implementation
 * of this interface carries out a distinct, concrete task. The range of tasks is diverse,
 * including:
 * <ul>
 *   <li>Invoking methods on user-defined beans (e.g., {@link InvokeAction})</li>
 *   <li>Manipulating the HTTP response (e.g., {@link HeaderAction})</li>
 *   <li>Controlling the execution flow (e.g., {@link ChooseAction}, {@link IncludeAction})</li>
 *   <li>Rendering data back to the client (e.g., {@link EchoAction})</li>
 * </ul>
 * All actions are executed by the {@link Activity} engine and operate on the data
 * contained within the {@link com.aspectran.core.activity.Translet} context.</p>
 */
public interface Executable {

    /**
     * Executes the action and returns the result.
     * @param activity the {@link Activity} object on which the action will be executed.
     * @return the result of executing the action.
     * @throws Exception an exception that may occur during the execution of the action
     */
    Object execute(Activity activity) throws Exception;

    /**
     * Returns the unique identifier of the action.
     * @return the action ID, or {@code null} if not specified
     */
    default String getActionId() {
        return null;
    }

    /**
     * Indicates whether to expose the result of this action.
     * @return {@code true} if the action is hidden; {@code false} otherwise.
     */
    default boolean isHidden() {
        return false;
    }

    /**
     * Returns the type of the action.
     * @return the {@link ActionType} of this action, or {@code null} if not specified
     */
    default ActionType getActionType() {
        return null;
    }

}
