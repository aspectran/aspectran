package com.aspectran.core.context.rule;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;

import static com.aspectran.core.context.rule.CaseRule.checkCaseNo;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseWhenRule implements ResponseRuleApplicable {

    private final int caseNo;

    private String expression;

    private Token[] tokens;

    private Response response;

    public CaseWhenRule(int caseNo) {
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
        this.expression = expression;
    }

    public Token[] getTokens() {
        return tokens;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public Response applyResponseRule(DispatchResponseRule dispatchResponseRule) {
        return null;
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        return null;
    }

    @Override
    public Response applyResponseRule(ForwardResponseRule forwardResponseRule) {
        return null;
    }

    @Override
    public Response applyResponseRule(RedirectResponseRule redirectResponseRule) {
        return null;
    }

}
