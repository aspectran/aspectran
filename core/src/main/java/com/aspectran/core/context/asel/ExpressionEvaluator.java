package com.aspectran.core.context.asel;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.token.Token;
import ognl.OgnlContext;

import java.util.Set;

/**
 * <p>Created: 2024-11-27</p>
 */
public interface ExpressionEvaluator {

    String getExpressionString();

    String getSubstitutedExpression();

    Object getParsedExpression();

    Token[] getTokens();

    Set<String> getTokenVarNames();

    Object evaluate(Activity activity, OgnlContext ognlContext);

    Object evaluate(Activity activity, OgnlContext ognlContext, Object root);

    Object evaluate(Activity activity, OgnlContext ognlContext, Object root, Class<?> resultType);

}
