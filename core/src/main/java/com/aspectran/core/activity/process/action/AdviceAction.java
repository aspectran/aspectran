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
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * A specialized {@link InvokeAction} for executing a method on an advice bean as part of an
 * AOP aspect.
 *
 * <p>This action is used internally by the framework to run advice logic (e.g., @Before, @After)
 * defined in an {@link com.aspectran.core.context.rule.AspectRule}.
 * It overrides the bean resolution logic. If the action specifies an explicit bean to invoke,
 * it uses the standard bean resolution. Otherwise, it resolves the bean from the current
 * AOP advice context.</p>
 *
 * <p>Created: 2019. 07. 18</p>
 */
public class AdviceAction extends InvokeAction {

    private final AdviceRule adviceRule;

    /**
     * Instantiates a new AdviceAction.
     * @param adviceRule the advice rule that this action executes
     * @param invokeActionRule the invoke action rule for the method call
     */
    public AdviceAction(AdviceRule adviceRule, InvokeActionRule invokeActionRule) {
        super(invokeActionRule);
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
     * Resolves the target bean instance. If an explicit bean ID or class is defined in the
     * underlying invoke rule, this method delegates to the superclass's resolution logic.
     * Otherwise, it retrieves the appropriate advice bean from the current activity's
     * advice context.
     * @param activity the current activity
     * @return the resolved advice bean instance
     * @throws ActionExecutionException if a required bean instance cannot be resolved
     */
    @Override
    protected Object resolveBean(@NonNull Activity activity) throws ActionExecutionException {
        if (getInvokeActionRule().getBeanId() != null || getInvokeActionRule().getBeanClass() != null) {
            return super.resolveBean(activity);
        }
        Object bean = activity.getAdviceBean(adviceRule.getAspectId());
        if (bean == null) {
            throw new ActionExecutionException("No advice bean found for " + adviceRule);
        }
        return bean;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", adviceRule.getAdviceType());
        if (getInvokeActionRule().getBeanId() != null || getInvokeActionRule().getBeanClass() != null) {
            tsb.append("action", super.toString());
        } else {
            if (getInvokeActionRule().getMethod() != null) {
                tsb.append("method", getInvokeActionRule().getMethod());
            } else {
                Object beanIdentifier = adviceRule.getAdviceBeanId();
                if (beanIdentifier == null) {
                    beanIdentifier = adviceRule.getAdviceBeanClass();
                }
                tsb.append("bean", beanIdentifier);
                tsb.append("method", getInvokeActionRule().getMethodName());
            }
        }
        if (adviceRule.getAspectRule().getOrder() != Integer.MAX_VALUE) {
            tsb.append("order", adviceRule.getAspectRule().getOrder());
        }
        return tsb.toString();
    }

}
