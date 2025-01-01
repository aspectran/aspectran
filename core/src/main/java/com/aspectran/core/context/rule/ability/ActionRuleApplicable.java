/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * The Interface ActionRuleApplicable.
 *
 * @since 2011. 2. 21.
 */
public interface ActionRuleApplicable {

    /**
     * Adds the header action rule.
     * @param headerActionRule the header action rule
     * @return an instance of the executable action
     */
    Executable applyActionRule(HeaderActionRule headerActionRule);

    /**
     * Adds the echo action rule.
     * @param echoActionRule the echo action rule
     * @return an instance of the executable action
     */
    Executable applyActionRule(EchoActionRule echoActionRule);

    /**
     * Adds the invoke action rule.
     * @param invokeActionRule the invoke action rule
     * @return an instance of the executable action
     */
    Executable applyActionRule(InvokeActionRule invokeActionRule);

    /**
     * Adds the annotated method action rule.
     * @param annotatedActionRule the annotated method action rule
     * @return an instance of the executable action
     */
    Executable applyActionRule(AnnotatedActionRule annotatedActionRule);

    /**
     * Adds the include action rule.
     * @param includeActionRule the include action rule
     * @return an instance of the executable action
     */
    Executable applyActionRule(IncludeActionRule includeActionRule);

    /**
     * Adds a list of action instances that can be executed.
     * @param chooseRule the choose rule
     * @return an instance of the executable action
     */
    Executable applyActionRule(ChooseRule chooseRule);

    /**
     * Adds an executable action instance.
     * @param action an executable action instance
     */
    void applyActionRule(Executable action);

}
