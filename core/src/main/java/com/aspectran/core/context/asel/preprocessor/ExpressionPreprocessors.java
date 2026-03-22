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

import java.util.ArrayList;
import java.util.List;

/**
 * A registry and executor for {@link ExpressionPreprocessor} implementations.
 *
 * <p>Created: 2026. 03. 22.</p>
 */
public class ExpressionPreprocessors {

    private final List<ExpressionPreprocessor> preprocessors = new ArrayList<>();

    public ExpressionPreprocessors() {
        // Register default preprocessors in order of precedence
        preprocessors.add(new TypeOperatorPreprocessor());
        preprocessors.add(new SafeNavigationPreprocessor());
        preprocessors.add(new CollectionOperatorPreprocessor());
        preprocessors.add(new ElvisOperatorPreprocessor());
        preprocessors.add(new MatchesOperatorPreprocessor());
    }


    public void addPreprocessor(ExpressionPreprocessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    public String process(String expression) {
        String processed = expression;
        for (ExpressionPreprocessor preprocessor : preprocessors) {
            processed = preprocessor.preprocess(processed);
        }
        return processed;
    }

}
