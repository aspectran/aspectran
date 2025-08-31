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
 * Advices are actions taken for a particular join point.
 * In terms of programming, they are methods that gets executed
 * when a certain join point with matching pointcut is reached
 * in the application.
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

    public String getAspectId() {
        return aspectId;
    }

    public AspectRule getAspectRule() {
        return aspectRule;
    }

    public String getAdviceBeanId() {
        return adviceBeanId;
    }

    public Class<?> getAdviceBeanClass() {
        return adviceBeanClass;
    }

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
            throw new IllegalStateException("Cannot resolve advice bean for " + invokeActionRule + " in " + this);
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
        throw new UnsupportedOperationException();
    }

    public Executable getAdviceAction() {
        return adviceAction;
    }

    public void setAdviceAction(Executable adviceAction) {
        this.adviceAction = adviceAction;
    }

    public ActionType getActionType() {
        return (adviceAction != null ? adviceAction.getActionType() : null);
    }

    public ExceptionRule getExceptionRule() {
        return exceptionRule;
    }

    public ExceptionThrownRule getExceptionThrownRule() {
        return exceptionThrownRule;
    }

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

    @NonNull
    public static AdviceRule newInstance(AspectRule aspectRule, AdviceType adviceType) {
        return new AdviceRule(aspectRule, adviceType);
    }

}
