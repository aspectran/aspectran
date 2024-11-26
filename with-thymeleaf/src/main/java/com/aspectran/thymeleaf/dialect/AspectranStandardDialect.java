package com.aspectran.thymeleaf.dialect;

import com.aspectran.thymeleaf.expression.ASELVariableExpressionEvaluator;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;

public class AspectranStandardDialect extends StandardDialect {

    public static final String NAME = "AspectranStandard";

    public static final String PREFIX = "th";

    public static final int PROCESSOR_PRECEDENCE = 1000;

    public static final ASELVariableExpressionEvaluator EVALUATOR = new ASELVariableExpressionEvaluator(true);

    public AspectranStandardDialect() {
        super(NAME, PREFIX, PROCESSOR_PRECEDENCE);
    }

    @Override
    public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
        return EVALUATOR;
    }

    @Override
    public void setVariableExpressionEvaluator(IStandardVariableExpressionEvaluator variableExpressionEvaluator) {
        throw new UnsupportedOperationException(
                "Variable Expression Evaluator cannot be modified in AspectranStandardDialect");
    }

}
