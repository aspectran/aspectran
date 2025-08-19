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
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * A specialized action for executing an advice method that is identified by annotations.
 *
 * <p>This class is used internally by the framework to implement Aspectran's AOP features
 * for annotated advice beans, handling the invocation of methods marked with advice
 * annotations like {@code @Before}, {@code @After}, etc.</p>
 *
 * <p>Created: 2019. 07. 18</p>
 */
public class AnnotatedAdviceAction extends AnnotatedAction {

    private final AdviceRule adviceRule;

    /**
     * Instantiates a new AnnotatedAdviceAction.
     * @param adviceRule the advice rule that this action executes
     * @param annotatedActionRule the annotated action rule for the method call
     */
    public AnnotatedAdviceAction(AdviceRule adviceRule, AnnotatedActionRule annotatedActionRule) {
        super(annotatedActionRule);
        this.adviceRule = adviceRule;
    }

    /**
     * Returns the advice rule associated with this action.
     * @return the advice rule
     */
    public AdviceRule getAdviceRule() {
        return adviceRule;
    }

    /**
     * Resolves the advice bean instance from the AOP context.
     * @param activity the current activity
     * @return the resolved advice bean instance
     * @throws Exception if the advice bean cannot be found
     */
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
