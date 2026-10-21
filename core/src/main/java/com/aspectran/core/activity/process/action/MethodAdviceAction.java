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
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A specialized action for executing an advice method that is identified by annotations.
 *
 * <p>This class is used internally by the framework to implement Aspectran's AOP features
 * for annotated advice beans, handling the invocation of methods marked with advice
 * annotations like {@code @Before}, {@code @After}, etc. It extends {@link MethodAction}
 * and overrides bean resolution to fetch the advice bean from the AOP context.</p>
 *
 * <p>Created: 2019. 07. 18</p>
 */
public class MethodAdviceAction extends MethodAction {

    private final AdviceRule adviceRule;

    /**
     * Instantiates a new MethodAdviceAction.
     * @param adviceRule the advice rule that this action executes
     * @param methodActionRule the method action rule for the method call
     */
    public MethodAdviceAction(AdviceRule adviceRule, MethodActionRule methodActionRule) {
        super(methodActionRule);
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
     * If the advice method is static, no bean is required and this method will return {@code null}.
     * @param activity the current activity
     * @return the resolved advice bean instance
     * @throws ActionExecutionException if the advice bean cannot be found
     */
    @Override
    protected Object resolveBean(@NonNull Activity activity) throws ActionExecutionException {
        // A static advice method does not require a bean instance.
        Method method = getMethodActionRule().getMethod();
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        Object bean = activity.getAdviceBean(adviceRule.getAspectId());
        if (bean == null) {
            throw new ActionExecutionException("No advice bean found for " + adviceRule);
        }
        return bean;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ADVICE_METHOD;
    }

    @Override
    public void toString(@NonNull ToStringBuilder tsb) {
        if (adviceRule == null) {
            super.toString(tsb);
        } else {
            if (getActionId() != null) {
                tsb.append("id", getActionId());
            }
            tsb.append("method", getMethodActionRule().getMethod());
        }
    }

    @Override
    public String toString() {
        if (adviceRule == null) {
            return super.toString();
        }
        return adviceRule.toString(this);
    }

}
