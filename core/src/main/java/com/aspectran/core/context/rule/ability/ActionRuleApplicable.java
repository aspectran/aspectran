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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeadingActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.MethodActionRule;

/**
 * The Interface ActionRuleApplicable.
 *
 * @since 2011. 2. 21.
 */
public interface ActionRuleApplicable {

    /**
     * Adds the bean action.
     *
     * @param beanActionRule the bean action rule
     */
    void applyActionRule(BeanActionRule beanActionRule);

    /**
     * Adds the method action.
     *
     * @param methodActionRule the bean action rule
     */
    void applyActionRule(MethodActionRule methodActionRule);

    /**
     * Adds the process-call action.
     *
     * @param includeActionRule the process call action rule
     */
    void applyActionRule(IncludeActionRule includeActionRule);

    /**
     * Adds the echo action.
     *
     * @param echoActionRule the echo action rule
     */
    void applyActionRule(EchoActionRule echoActionRule);

    /**
     * Adds the heading action.
     *
     * @param headingActionRule the heading action rule
     */
    void applyActionRule(HeadingActionRule headingActionRule);

}
