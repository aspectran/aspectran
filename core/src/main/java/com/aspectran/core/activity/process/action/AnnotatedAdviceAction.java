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
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * {@code AnnotatedAdviceAction} that invokes a method for Annotated Aspect Advice.
 *
 * <p>Created: 2019. 07. 18</p>
 */
public class AnnotatedAdviceAction extends AnnotatedAction {

    private final AspectAdviceRule aspectAdviceRule;

    /**
     * Instantiates a new AdviceAction.
     * @param aspectAdviceRule the aspect advice rule
     * @param annotatedActionRule the annotated method action rule
     */
    public AnnotatedAdviceAction(AspectAdviceRule aspectAdviceRule, AnnotatedActionRule annotatedActionRule) {
        super(annotatedActionRule);
        this.aspectAdviceRule = aspectAdviceRule;
    }

    /**
     * Gets the aspect advice rule.
     * @return the aspect advice rule
     */
    public AspectAdviceRule getAspectAdviceRule() {
        return aspectAdviceRule;
    }

    @Override
    protected Object resolveBean(@NonNull Activity activity) throws Exception {
        Object bean = activity.getAspectAdviceBean(aspectAdviceRule.getAspectId());
        if (bean == null) {
            throw new ActionExecutionException("No advice bean found for " + aspectAdviceRule);
        }
        return bean;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", aspectAdviceRule.getAspectAdviceType());
        tsb.append("action", super.toString());
        if (aspectAdviceRule.getAspectRule().getOrder() != Integer.MAX_VALUE) {
            tsb.append("order", aspectAdviceRule.getAspectRule().getOrder());
        }
        return tsb.toString();
    }

}
