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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.action.AdviceAction;
import com.aspectran.core.activity.process.action.AnnotatedAdviceAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.HeaderAction;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AdviceType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Defines a rule for an advice that is executed at a specific join point.
 * It holds the logic (as an action) to be executed and the context of the aspect it belongs to.
 *
 * <p>Created: 2008. 04. 01 PM 11:19:28</p>
 */
public class AdviceRule implements HasActionRules {

    private final AspectRule aspectRule;

    private final String aspectId;

    private final String adviceBeanId;

    private final Class<?> adviceBeanClass;

    private final AdviceType adviceType;

    private Executable adviceAction;

    private ExceptionRule exceptionRule;

    private ExceptionThrownRule exceptionThrownRule;

    /**
     * Instantiates a new AdviceRule.
     * @param aspectRule the aspect rule that this advice belongs to
     * @param adviceType the type of this advice (e.g., before, after)
     */
    public AdviceRule(AspectRule aspectRule, AdviceType adviceType) {
        if (aspectRule == null) {
            throw new IllegalArgumentException("aspectRule must not be null");
        }
        if (adviceType == null) {
            throw new IllegalArgumentException("adviceType must not be null");
        }
        this.aspectRule = aspectRule;
        this.aspectId = aspectRule.getId();
        this.adviceBeanId = aspectRule.getAdviceBeanId();
        this.adviceBeanClass = aspectRule.getAdviceBeanClass();
        this.adviceType = adviceType;
    }

    /**
     * Gets the ID of the aspect that this advice belongs to.
     * @return the aspect ID
     */
    public String getAspectId() {
        return aspectId;
    }

    /**
     * Gets the aspect rule that this advice belongs to.
     * @return the parent aspect rule
     */
    public AspectRule getAspectRule() {
        return aspectRule;
    }

    /**
     * Gets the bean ID of the advice bean.
     * @return the advice bean ID
     */
    public String getAdviceBeanId() {
        return adviceBeanId;
    }

    /**
     * Gets the class of the advice bean.
     * @return the advice bean class
     */
    public Class<?> getAdviceBeanClass() {
        return adviceBeanClass;
    }

    /**
     * Gets the type of this advice (e.g., before, after, around).
     * @return the advice type
     */
    public AdviceType getAdviceType() {
        return adviceType;
    }

    @Override
    public Executable putActionRule(HeaderActionRule headerActionRule) {
        adviceAction = new HeaderAction(headerActionRule);
        return adviceAction;
    }

    @Override
    public Executable putActionRule(EchoActionRule echoActionRule) {
        adviceAction = new EchoAction(echoActionRule);
        return adviceAction;
    }

    @Override
    public Executable putActionRule(InvokeActionRule invokeActionRule) {
        if (adviceBeanId == null && adviceBeanClass == null &&
                invokeActionRule.getBeanId() == null && invokeActionRule.getBeanClass() == null) {
            throw new IllegalStateException("No bean specified for an invoke action. An invoke action " +
                    "within an advice rule requires a target bean to be defined either on the invoke rule " +
                    "itself or on the parent aspect rule. Rule: " + invokeActionRule);
        }
        adviceAction = new AdviceAction(this, invokeActionRule);
        return adviceAction;
    }

    @Override
    public Executable putActionRule(AnnotatedActionRule annotatedActionRule) {
        throw new UnsupportedOperationException("No support applying AnnotatedActionRule to AdviceRule");
    }

    @Override
    public Executable putActionRule(IncludeActionRule includeActionRule) {
        throw new UnsupportedOperationException("No support applying IncludeActionRule to AdviceRule");
    }

    @Override
    public Executable putActionRule(ChooseRule chooseRule) {
        throw new UnsupportedOperationException("No support applying ChooseRule to AdviceRule");
    }

    @Override
    public void putActionRule(Executable action) {
        throw new UnsupportedOperationException(
                "Adding a pre-constructed Executable action is not supported in AdviceRule. " +
                "Define actions using their specific rule types (e.g., InvokeActionRule).");
    }

    /**
     * Gets the executable action that implements the advice logic.
     * @return the executable advice action
     */
    public Executable getAdviceAction() {
        return adviceAction;
    }

    /**
     * Sets the executable action for the advice.
     * @param adviceAction the executable advice action
     */
    public void setAdviceAction(Executable adviceAction) {
        this.adviceAction = adviceAction;
    }

    /**
     * Gets the type of the advice action.
     * @return the action type
     */
    public ActionType getActionType() {
        return (adviceAction != null ? adviceAction.getActionType() : null);
    }

    /**
     * Gets the exception handling rule for this advice.
     * @return the exception rule
     */
    public ExceptionRule getExceptionRule() {
        return exceptionRule;
    }

    /**
     * Gets the rule for a specific exception type that may be thrown.
     * @return the exception thrown rule
     */
    public ExceptionThrownRule getExceptionThrownRule() {
        return exceptionThrownRule;
    }

    /**
     * Sets the rule for a specific exception type, wrapping it in an {@link ExceptionRule}.
     * @param exceptionThrownRule the exception thrown rule
     */
    public void setExceptionThrownRule(ExceptionThrownRule exceptionThrownRule) {
        ExceptionRule exceptionRule = new ExceptionRule();
        exceptionRule.putExceptionThrownRule(exceptionThrownRule);

        this.exceptionRule = exceptionRule;
        this.exceptionThrownRule = exceptionThrownRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", adviceType);
        tsb.append("bean", adviceBeanId);
        if (adviceBeanId == null) {
            tsb.append("bean", adviceBeanClass);
        }
        tsb.append("action", toString(adviceAction, null));
        return tsb.toString();
    }

    public static String toString(Executable adviceAction, @Nullable AdviceRule adviceRule) {
        if (adviceAction instanceof AdviceAction || adviceAction instanceof AnnotatedAdviceAction) {
            return adviceAction.toString();
        }
        ToStringBuilder tsb = new ToStringBuilder();
        if (adviceRule != null) {
            tsb.append("type", adviceRule.getAdviceType());
        }
        if (adviceAction != null) {
            if (adviceAction.getActionType() != null) {
                tsb.append("action", adviceAction.toString());
            } else {
                tsb.append("instance", adviceAction.toString());
            }
        }
        if (adviceRule != null && adviceRule.getAspectRule() != null) {
            int order = adviceRule.getAspectRule().getOrder();
            if (order != Integer.MAX_VALUE) {
                tsb.append("order", order);
            }
            tsb.append("isolated", adviceRule.getAspectRule().getIsolated());
        }
        return tsb.toString();
    }

    /**
     * Creates a new instance of AdviceRule.
     * @param aspectRule the aspect rule that this advice belongs to
     * @param adviceType the type of this advice (e.g., before, after)
     * @return a new AdviceRule instance
     */
    @NonNull
    public static AdviceRule newInstance(AspectRule aspectRule, AdviceType adviceType) {
        return new AdviceRule(aspectRule, adviceType);
    }

}
