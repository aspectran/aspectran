/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.activity.aspect;

import com.aspectran.core.activity.SessionScopeActivity;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.AspectAdviceRule;

import java.util.List;

/**
 * The Class SessionScopeAdvisor.
 */
public class SessionScopeAdvisor {

    private final SessionScopeActivity activity;

    private final List<AspectAdviceRule> beforeAdviceRuleList;

    private final List<AspectAdviceRule> afterAdviceRuleList;

    SessionScopeAdvisor(SessionScopeActivity activity, AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
        this.activity = activity;
        this.beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
        this.afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();
    }

    public SessionScopeActivity getSessionScopeActivity() {
        return activity;
    }

    public void executeBeforeAdvice() {
        if (beforeAdviceRuleList != null) {
            activity.executeAdvice(beforeAdviceRuleList, true);
        }
    }

    public void executeAfterAdvice() {
        if (afterAdviceRuleList != null) {
            activity.executeAdvice(afterAdviceRuleList, false);
        }
    }

}
