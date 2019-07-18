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
        this.aspectAdviceRule = (invokeActionRule.getBeanId() == null ? aspectAdviceRule : null);
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
        if (aspectAdviceRule != null) {
            Object bean = activity.getAspectAdviceBean(aspectAdviceRule.getAspectId());
            if (bean == null) {
                throw new ActionExecutionException("No such bean; Invalid AspectAdviceRule " + aspectAdviceRule);
            }
            return execute(activity, bean);
        } else {
            return super.execute(activity);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionType", getActionType());
        tsb.append("invokeActionRule", getInvokeActionRule());
        if (aspectAdviceRule != null) {
            tsb.append("aspectAdviceRule", aspectAdviceRule.toString(true));
        }
        return tsb.toString();
    }

}
