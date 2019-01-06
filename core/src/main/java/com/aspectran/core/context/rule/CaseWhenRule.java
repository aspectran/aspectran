package com.aspectran.core.context.rule;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.expr.token.Token;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseWhenRule {

    private final int caseWhenNo;

    private String expression;

    private Token[] tokens;

    private Response response;

    public CaseWhenRule(int caseWhenNo) {
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

}
