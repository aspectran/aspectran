package com.aspectran.utils.wildcard;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * <p>Created: 2025. 1. 5.</p>
 */
public class IncludeExcludeParameters extends AbstractParameters {

    private static final ParameterKey plus;
    private static final ParameterKey minus;

    private static final ParameterKey[] parameterKeys;

    static {
        plus = new ParameterKey("+", ValueType.STRING, true, true);
        minus = new ParameterKey("-", ValueType.STRING, true, true);

        parameterKeys = new ParameterKey[]{
            plus,
            minus
        };
    }

    public IncludeExcludeParameters() {
        super(parameterKeys);
    }

    public IncludeExcludeParameters(String apon) throws AponParseException {
        this();
        readFrom(apon);
    }

    public String[] getIncludePatterns() {
        return getStringArray(plus);
    }

    public IncludeExcludeParameters addIncludePattern(String pattern) {
        putValue(plus, pattern);
        return this;
    }

    public String[] getExcludePatterns() {
        return getStringArray(minus);
    }

    public IncludeExcludeParameters addExcludePattern(String pattern) {
        putValue(minus, pattern);
        return this;
    }

    public boolean hasPatterns() {
        return (hasValue(plus) || hasValue(minus));
    }

}
