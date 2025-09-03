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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.asel.value.BooleanExpression;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.utils.StringUtils;

/**
 * Represents a single conditional case within a {@link ChooseRule}.
 * It contains a test expression and a set of actions or a response to execute
 * if the expression evaluates to true.
 *
 * <p>Created: 2019-01-06</p>
 *
 * @since 6.0.0
 */
public class ChooseWhenRule implements HasActionRules, HasResponseRules {

    private BooleanExpression booleanEvaluation;

    private ActionList actionList;

    private Response response;

    /**
     * Gets the boolean expression to be evaluated.
     * @return the boolean expression
     */
    public BooleanExpression getBooleanExpression() {
        return booleanEvaluation;
    }

    /**
     * Gets the raw expression string.
     * @return the expression string
     */
    public String getExpression() {
        if (booleanEvaluation != null) {
            return booleanEvaluation.getExpressionString();
        } else {
            return null;
        }
    }

    /**
     * Sets the boolean expression to be evaluated.
     * @param expression the expression string
     * @throws IllegalRuleException if the expression is invalid
     */
    public void setExpression(String expression) throws IllegalRuleException {
        String expressionToUse = (StringUtils.hasText(expression) ? expression.trim() : null);
        if (expressionToUse != null) {
            this.booleanEvaluation = new BooleanExpression(expressionToUse);
        } else {
            this.booleanEvaluation = null;
        }
    }

    /**
     * Gets the list of actions to be executed if the test is true.
     * @return the action list
     */
    public ActionList getActionList() {
        return actionList;
    }

    /**
     * Sets the list of actions.
     * @param actionList the action list
     */
    public void setActionList(ActionList actionList) {
        this.actionList = actionList;
    }

    /**
     * Gets the response to be generated if the test is true.
     * @return the response
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the response.
     * @param response the response
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public Executable putActionRule(HeaderActionRule headerActionRule) {
        return touchActionList().putActionRule(headerActionRule);
    }

    @Override
    public Executable putActionRule(EchoActionRule echoActionRule) {
        return touchActionList().putActionRule(echoActionRule);
    }

    @Override
    public Executable putActionRule(InvokeActionRule invokeActionRule) {
        return touchActionList().putActionRule(invokeActionRule);
    }

    @Override
    public Executable putActionRule(AnnotatedActionRule annotatedActionRule) {
        return touchActionList().putActionRule(annotatedActionRule);
    }

    @Override
    public Executable putActionRule(IncludeActionRule includeActionRule) {
        return touchActionList().putActionRule(includeActionRule);
    }

    @Override
    public Executable putActionRule(ChooseRule chooseRule) {
        return touchActionList().putActionRule(chooseRule);
    }

    @Override
    public void putActionRule(Executable action) {
        touchActionList().putActionRule(action);
    }

    /**
     * Returns the action list, creating it if it does not yet exist.
     * @return the action list
     */
    private ActionList touchActionList() {
        if (actionList == null) {
            actionList = new ActionList(false);
        }
        return actionList;
    }

    @Override
    public Response putResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.create(transformRule);
        this.response = response;
        return response;
    }

    @Override
    public Response putResponseRule(DispatchRule dispatchRule) {
        Response response = new DispatchResponse(dispatchRule);
        this.response = response;
        return response;
    }

    @Override
    public Response putResponseRule(ForwardRule forwardRule) {
        Response response = new ForwardResponse(forwardRule);
        this.response = response;
        return response;
    }

    @Override
    public Response putResponseRule(RedirectRule redirectRule) {
        Response response = new RedirectResponse(redirectRule);
        this.response = response;
        return response;
    }

}
