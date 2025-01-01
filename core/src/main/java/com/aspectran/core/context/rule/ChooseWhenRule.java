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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.asel.value.BooleanExpression;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.utils.StringUtils;

/**
 * <p>Created: 2019-01-06</p>
 *
 * @since 6.0.0
 */
public class ChooseWhenRule implements ActionRuleApplicable, ResponseRuleApplicable {

    private BooleanExpression booleanEvaluation;

    private ActionList actionList;

    private Response response;

    public BooleanExpression getBooleanExpression() {
        return booleanEvaluation;
    }

    public String getExpression() {
        if (booleanEvaluation != null) {
            return booleanEvaluation.getExpressionString();
        } else {
            return null;
        }
    }

    public void setExpression(String expression) throws IllegalRuleException {
        String expressionToUse = (StringUtils.hasText(expression) ? expression.trim() : null);
        if (expressionToUse != null) {
            this.booleanEvaluation = new BooleanExpression(expressionToUse);
        } else {
            this.booleanEvaluation = null;
        }
    }

    public ActionList getActionList() {
        return actionList;
    }

    public void setActionList(ActionList actionList) {
        this.actionList = actionList;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public Executable applyActionRule(HeaderActionRule headerActionRule) {
        return touchActionList().applyActionRule(headerActionRule);
    }

    @Override
    public Executable applyActionRule(EchoActionRule echoActionRule) {
        return touchActionList().applyActionRule(echoActionRule);
    }

    @Override
    public Executable applyActionRule(InvokeActionRule invokeActionRule) {
        return touchActionList().applyActionRule(invokeActionRule);
    }

    @Override
    public Executable applyActionRule(AnnotatedActionRule annotatedActionRule) {
        return touchActionList().applyActionRule(annotatedActionRule);
    }

    @Override
    public Executable applyActionRule(IncludeActionRule includeActionRule) {
        return touchActionList().applyActionRule(includeActionRule);
    }

    @Override
    public Executable applyActionRule(ChooseRule chooseRule) {
        return touchActionList().applyActionRule(chooseRule);
    }

    @Override
    public void applyActionRule(Executable action) {
        touchActionList().applyActionRule(action);
    }

    /**
     * Returns the action list.
     * If not yet instantiated then create a new one.
     * @return the action list
     */
    private ActionList touchActionList() {
        if (actionList == null) {
            actionList = new ActionList(false);
        }
        return actionList;
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.create(transformRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(DispatchRule dispatchRule) {
        Response response = new DispatchResponse(dispatchRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(ForwardRule forwardRule) {
        Response response = new ForwardResponse(forwardRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(RedirectRule redirectRule) {
        Response response = new RedirectResponse(redirectRule);
        this.response = response;
        return response;
    }

}
