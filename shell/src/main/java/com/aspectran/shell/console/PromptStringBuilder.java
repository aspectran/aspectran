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
package com.aspectran.shell.console;

/**
 * A fluent builder for creating styled prompt strings for the console.
 * <p>Implementations of this interface allow for the construction of complex
 * prompts with different text styles in a chained manner.</p>
 *
 * <p>Created: 2017. 11. 10.</p>
 */
public interface PromptStringBuilder {

    /**
     * Appends a string to the prompt.
     * @param str the string to append
     * @return this builder instance
     */
    PromptStringBuilder append(String str);

    /**
     * Sets the style for subsequent text.
     * @param styles the style names to apply
     * @return this builder instance
     */
    PromptStringBuilder setStyle(String... styles);

    /**
     * Resets the current style and applies new styles.
     * @param styles the new style names to apply
     * @return this builder instance
     */
    PromptStringBuilder resetStyle(String... styles);

    /**
     * Resets all styles to the default.
     * @return this builder instance
     */
    PromptStringBuilder resetStyle();

    /**
     * Applies the secondary style.
     * @return this builder instance
     */
    PromptStringBuilder secondaryStyle();

    /**
     * Applies the success style.
     * @return this builder instance
     */
    PromptStringBuilder successStyle();

    /**
     * Applies the danger style.
     * @return this builder instance
     */
    PromptStringBuilder dangerStyle();

    /**
     * Applies the warning style.
     * @return this builder instance
     */
    PromptStringBuilder warningStyle();

    /**
     * Applies the info style.
     * @return this builder instance
     */
    PromptStringBuilder infoStyle();

    /**
     * Sets a default value to be used if the user provides empty input for this prompt.
     * @param defaultValue the default value
     * @return this builder instance
     */
    PromptStringBuilder defaultValue(String defaultValue);

    /**
     * Gets the default value for this prompt.
     * @return the default value, or {@code null} if not set
     */
    String getDefaultValue();

    /**
     * Clears the content of the builder.
     */
    void clear();

}
