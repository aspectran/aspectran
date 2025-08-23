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
package com.aspectran.utils.wildcard;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * APON-backed parameter bean for include/exclude wildcard patterns.
 * <p>
 * The keys are represented by "+" (include) and "-" (exclude). Multiple values
 * can be provided for each key. This class is typically used together with
 * {@link IncludeExcludeWildcardPatterns} to evaluate inputs against
 * include/exclude rules.
 * </p>
 *
 * <p>Example APON text:</p>
 * <pre>
 * +: com/example/**
 * -: com/example/internal/**
 * </pre>
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

    /**
     * Create an empty parameter bean with include/exclude keys registered.
     */
    protected IncludeExcludeParameters(ParameterKey[] parameterKeys) {
        super(parameterKeys, IncludeExcludeParameters.parameterKeys);
    }

    /**
     * Create an empty parameter bean with include/exclude keys registered.
     */
    public IncludeExcludeParameters() {
        super(parameterKeys);
    }

    /**
     * Create and initialize from APON text.
     * @param apon APON text containing + and - entries
     * @throws AponParseException if the APON text cannot be parsed
     */
    public IncludeExcludeParameters(String apon) throws AponParseException {
        this();
        readFrom(apon);
    }

    /**
     * Return the include patterns (values of the "+" key) or {@code null} if none.
     */
    public String[] getIncludePatterns() {
        return getStringArray(plus);
    }

    /**
     * Add an include pattern.
     * @param pattern the pattern string to include
     * @return this instance for method chaining
     */
    public IncludeExcludeParameters addIncludePattern(String pattern) {
        putValue(plus, pattern);
        return this;
    }

    /**
     * Return the exclude patterns (values of the "-" key) or {@code null} if none.
     */
    public String[] getExcludePatterns() {
        return getStringArray(minus);
    }

    /**
     * Add an exclude pattern.
     * @param pattern the pattern string to exclude
     * @return this instance for method chaining
     */
    public IncludeExcludeParameters addExcludePattern(String pattern) {
        putValue(minus, pattern);
        return this;
    }

    /**
     * Whether any include or exclude patterns have been set.
     */
    public boolean hasPatterns() {
        return (hasValue(plus) || hasValue(minus));
    }

}
