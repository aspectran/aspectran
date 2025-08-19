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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;

/**
 * Defines a contract for objects that can accept and apply various action rules.
 *
 * <p>This interface acts as a factory or builder for creating {@link Executable} action
 * instances from their corresponding rule definitions. It is a core part of how Aspectran's
 * configuration parser translates rule definitions (e.g., from XML) into executable
 * objects. The primary implementor is typically
 * {@link com.aspectran.core.activity.process.ActionList}.</p>
 *
 * @since 2011. 2. 21.
 */
public interface ActionRuleApplicable {

    /**
     * Applies a header action rule, creating and adding a
     * {@link com.aspectran.core.activity.process.action.HeaderAction} to the current context.
     * @param headerActionRule the header action rule to apply
     * @return the created {@link Executable} action instance
     */
    Executable applyActionRule(HeaderActionRule headerActionRule);

    /**
     * Applies an echo action rule, creating and adding an
     * {@link com.aspectran.core.activity.process.action.EchoAction} to the current context.
     * @param echoActionRule the echo action rule to apply
     * @return the created {@link Executable} action instance
     */
    Executable applyActionRule(EchoActionRule echoActionRule);

    /**
     * Applies an invoke action rule, creating and adding an
     * {@link com.aspectran.core.activity.process.action.InvokeAction} to the current context.
     * @param invokeActionRule the invoke action rule to apply
     * @return the created {@link Executable} action instance
     */
    Executable applyActionRule(InvokeActionRule invokeActionRule);

    /**
     * Applies an annotated action rule, creating and adding an
     * {@link com.aspectran.core.activity.process.action.AnnotatedAction} to the current context.
     * @param annotatedActionRule the annotated action rule to apply
     * @return the created {@link Executable} action instance
     */
    Executable applyActionRule(AnnotatedActionRule annotatedActionRule);

    /**
     * Applies an include action rule, creating and adding an
     * {@link com.aspectran.core.activity.process.action.IncludeAction} to the current context.
     * @param includeActionRule the include action rule to apply
     * @return the created {@link Executable} action instance
     */
    Executable applyActionRule(IncludeActionRule includeActionRule);

    /**
     * Applies a choose rule, creating and adding a
     * {@link com.aspectran.core.activity.process.action.ChooseAction} to the current context.
     * @param chooseRule the choose rule to apply
     * @return the created {@link Executable} action instance
     */
    Executable applyActionRule(ChooseRule chooseRule);

    /**
     * Applies a pre-existing executable action instance.
     * @param action an executable action instance to apply
     */
    void applyActionRule(Executable action);

}
