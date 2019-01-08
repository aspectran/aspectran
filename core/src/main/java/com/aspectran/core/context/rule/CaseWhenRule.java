package com.aspectran.core.context.rule;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseWhenRule implements ResponseRuleApplicable {

    private final int caseWhenNo;

    private String expression;

    private Token[] tokens;

    private Response response;

    public CaseWhenRule(int caseWhenNo) {
        if (caseWhenNo <= 0 || caseWhenNo > 999) {
            throw new IllegalArgumentException("caseWhenNo must be > 0 and <= 999");
        }
        this.caseWhenNo = caseWhenNo;
    }

    public int getCaseWhenNo() {
        return caseWhenNo;
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
