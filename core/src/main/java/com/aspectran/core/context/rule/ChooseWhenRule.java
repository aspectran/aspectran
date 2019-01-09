package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.context.expr.BooleanExpression;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.util.StringUtils;

import java.util.Collection;

import static com.aspectran.core.context.rule.ChooseRule.checkCaseNo;

/**
 * <p>Created: 2019-01-06</p>
 */
public class ChooseWhenRule implements ActionRuleApplicable, ResponseRuleApplicable {

    private final int caseNo;

    private String expression;

    private Token[] tokens;

    private ActionList actionList;

    private Response response;

    public ChooseWhenRule(int caseNo) {
        checkCaseNo(caseNo);
        this.caseNo = caseNo;
    }

    public int getCaseNo() {
        return caseNo;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = (StringUtils.hasLength(expression) ? expression : null);
        this.tokens = BooleanExpression.parseTokens(expression);
    }

    public Token[] getTokens() {
        return tokens;
    }

    public ActionList getActionList() {
        return actionList;
    }

    public void setActionList(ActionList actionList) {
        this.actionList = actionList;
    }

    public void join(ActionRuleApplicable applicable) {
        if (actionList != null && !actionList.isEmpty()) {
            for (Executable action : actionList) {
                action.setLastInCaseWhen(false);
            }
            actionList.get(actionList.size() - 1).setLastInCaseWhen(true);
            applicable.applyActionRule(actionList);
            actionList = null;
        }
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public Executable applyActionRule(BeanMethodActionRule beanMethodActionRule) {
        Executable action = touchActionList().applyActionRule(beanMethodActionRule);
        action.setCaseNo(caseNo);
        return action;
    }

    @Override
    public Executable applyActionRule(ConfigBeanMethodActionRule configBeanMethodActionRule) {
        Executable action = touchActionList().applyActionRule(configBeanMethodActionRule);
        action.setCaseNo(caseNo);
        return action;
    }

    @Override
    public Executable applyActionRule(IncludeActionRule includeActionRule) {
        Executable action = touchActionList().applyActionRule(includeActionRule);
        action.setCaseNo(caseNo);
        return action;
    }

    @Override
    public Executable applyActionRule(EchoActionRule echoActionRule) {
        Executable action = touchActionList().applyActionRule(echoActionRule);
        action.setCaseNo(caseNo);
        return action;
    }

    @Override
    public Executable applyActionRule(HeaderActionRule headerActionRule) {
        Executable action = touchActionList().applyActionRule(headerActionRule);
        action.setCaseNo(caseNo);
        return action;
    }

    @Override
    public void applyActionRule(Executable action) {
        touchActionList().applyActionRule(action);
    }

    @Override
    public void applyActionRule(Collection<Executable> actionList) {
        touchActionList().addAll(actionList);
    }

    /**
     * Returns the action list.
     * If not yet instantiated then create a new one.
     *
     * @return the action list
     */
    private ActionList touchActionList() {
        if (actionList == null) {
            actionList = new ActionList();
        }
        return actionList;
    }

    @Override
    public Response applyResponseRule(DispatchRule dispatchRule) {
        Response response = new DispatchResponse(dispatchRule);
        this.response = response;
        return response;
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        Response response = TransformResponseFactory.createTransformResponse(transformRule);
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
