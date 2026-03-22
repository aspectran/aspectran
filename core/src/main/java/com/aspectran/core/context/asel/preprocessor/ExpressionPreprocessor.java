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

/**
 * Interface for pre-processing an expression before it is parsed by the OGNL engine.
 * <p>Implementations can modify the expression string to support custom syntax
 * or optimize the expression structure.</p>
 *
 * <p>Created: 2026. 03. 22.</p>
 */
public interface ExpressionPreprocessor {

    /**
     * Processes the given expression string.
     * @param expression the expression string to process
     * @return the processed expression string
     */
    String preprocess(String expression);

}
