/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.util.ToStringBuilder;

/**
 * {@code AdviceAction} that invokes a method for Aspect Advice.
 *
 * <p>Created: 2019. 07. 18</p>
 */
public class AdviceAction extends InvokeAction {

    private final AspectAdviceRule aspectAdviceRule;

    /**
     * Instantiates a new AdviceAction.
     *
     * @param invokeActionRule the invoke action rule
     * @param aspectAdviceRule the aspect advice rule
     */
    public AdviceAction(InvokeActionRule invokeActionRule, AspectAdviceRule aspectAdviceRule) {
        super(invokeActionRule);
        this.aspectAdviceRule = aspectAdviceRule;
    }

    /**
     * Gets the aspect advice rule.
     *
     * @return the aspect advice rule
     */
    public AspectAdviceRule getAspectAdviceRule() {
        return aspectAdviceRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        if (getInvokeActionRule().getBeanId() != null) {
            return super.execute(activity);
        }  else {
            Object bean = activity.getAspectAdviceBean(aspectAdviceRule.getAspectId());
            if (bean == null) {
                throw new ActionExecutionException("No advice bean found for " + aspectAdviceRule);
            }
            return execute(activity, bean);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", aspectAdviceRule.getAspectAdviceType());
        tsb.append("bean", aspectAdviceRule.getAdviceBeanId());
        tsb.append("bean", aspectAdviceRule.getAdviceBeanClass());
        if (getInvokeActionRule().getMethod() != null) {
            tsb.append("method", getInvokeActionRule().getMethod());
        } else {
            tsb.append("method", getInvokeActionRule().getMethodName());
        }
        tsb.append("order", aspectAdviceRule.getAspectRule().getOrder());
        return tsb.toString();
    }

}
