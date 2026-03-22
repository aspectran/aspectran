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
package com.aspectran.core.context.asel.preprocessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry and executor for {@link ExpressionPreprocessor} implementations.
 * <p>This class manages a sequence of preprocessors that transform AsEL expressions
 * before they are parsed by the underlying engine. It provides a global instance
 * that can be extended by users to add custom expression transformation logic.</p>
 *
 * <p>By registering custom preprocessors, users can introduce new operators,
 * syntax shortcuts, or domain-specific optimizations to AsEL.</p>
 *
 * <pre>{@code
 * // Example: Registering a custom preprocessor
 * ExpressionPreprocessors.getInstance().addPreprocessor(myCustomPreprocessor);
 * }</pre>
 *
 * <p>Created: 2026. 03. 22.</p>
 */
public class ExpressionPreprocessors {

    private static final ExpressionPreprocessors instance = new ExpressionPreprocessors();

    private final List<ExpressionPreprocessor> preprocessors = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new ExpressionPreprocessors and registers default preprocessors.
     * <p>The default preprocessors include support for Safe Navigation (?.),
     * Elvis operator (?:), and SpEL-style collection operators.</p>
     */
    public ExpressionPreprocessors() {
        // Register the unified AselExpressionPreprocessor
        preprocessors.add(new AselExpressionPreprocessor());
    }

    /**
     * Returns the global shared instance of the ExpressionPreprocessors registry.
     * <p>Use this instance to register custom preprocessors that should apply
     * globally across the application.</p>
     * @return the global ExpressionPreprocessors instance
     */
    public static ExpressionPreprocessors getInstance() {
        return instance;
    }

    /**
     * Adds a custom expression preprocessor to the registry.
     * <p>Preprocessors are executed in the order they are added.</p>
     * @param preprocessor the preprocessor to add
     */
    public void addPreprocessor(ExpressionPreprocessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    /**
     * Executes all registered preprocessors on the given expression.
     * @param expression the raw expression string
     * @return the fully processed expression string
     */
    public String process(String expression) {
        String processed = expression;
        for (ExpressionPreprocessor preprocessor : preprocessors) {
            processed = preprocessor.preprocess(processed);
        }
        return processed;
    }

}
