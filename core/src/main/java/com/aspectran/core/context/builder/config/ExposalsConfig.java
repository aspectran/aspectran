package com.aspectran.core.context.builder.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class ExposalsConfig extends AbstractParameters {

    public static final ParameterDefinition plus;
    public static final ParameterDefinition minus;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        plus = new ParameterDefinition("+", ParameterValueType.STRING, true, true);
        minus = new ParameterDefinition("-", ParameterValueType.STRING, true, true);

        parameterDefinitions = new ParameterDefinition[] {
                plus,
                minus
        };
    }

    public ExposalsConfig() {
        super(parameterDefinitions);
    }

    public ExposalsConfig(String text) {
        super(parameterDefinitions, text);
    }

    public void addIncludePattern(String pattern) {
        putValue(plus, pattern);
    }

    public void addExecludePattern(String pattern) {
        putValue(minus, pattern);
    }
    
}
