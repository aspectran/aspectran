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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Action that calls a method of an annotated advice bean defined
 * by annotations declaring an aspect.
 *
 * <p>Created: 2019. 07. 18</p>
 */
public class AnnotatedAdviceAction extends AnnotatedAction {

    private final AdviceRule adviceRule;

    /**
     * Instantiates a new AdviceAction.
     * @param adviceRule the advice rule
     * @param annotatedActionRule the annotated method action rule
     */
    public AnnotatedAdviceAction(AdviceRule adviceRule, AnnotatedActionRule annotatedActionRule) {
        super(annotatedActionRule);
        this.adviceRule = adviceRule;
    }

    /**
     * Gets the advice rule.
     * @return the advice rule
     */
    public AdviceRule getAdviceRule() {
        return adviceRule;
    }

    @Override
    protected Object resolveBean(@NonNull Activity activity) throws Exception {
        Object bean = activity.getAdviceBean(adviceRule.getAspectId());
        if (bean == null) {
            throw new ActionExecutionException("No advice bean found for " + adviceRule);
        }
        return bean;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.INVOKE_ANNOTATED_ADVICE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", adviceRule.getAdviceType());
        tsb.append("action", super.toString());
        if (adviceRule.getAspectRule().getOrder() != Integer.MAX_VALUE) {
            tsb.append("order", adviceRule.getAspectRule().getOrder());
        }
        return tsb.toString();
    }

}
