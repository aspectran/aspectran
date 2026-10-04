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
package com.aspectran.core.context.rule.util;

import com.aspectran.core.context.rule.type.TextStyleType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponFormat;
import org.jspecify.annotations.Nullable;

/**
 * Contains methods to transform a given text to a specific style.
 *
 * <p>Created: 2017. 3. 22.</p>
 */
public class TextStyler {

    private TextStyler() {
    }

    /**
     * Styles the given text based on the specified style alias.
     * @param text the text to style
     * @param style the style alias (e.g., "apon", "compact", "compressed")
     * @return the styled text
     * @throws IllegalArgumentException if no text style type is found for the given alias
     */
    public static String styling(@Nullable String text, @Nullable String style) {
        TextStyleType textStyleType = TextStyleType.resolve(style);
        if (style != null && textStyleType == null) {
            throw new IllegalArgumentException("No text style type for '" + style + "'");
        }
        return styling(text, textStyleType);
    }

    /**
     * Styles the given text based on the specified {@code TextStyleType}.
     * @param text the text to style
     * @param textStyleType the text style type
     * @return the styled text
     */
    public static String styling(@Nullable String text, @Nullable TextStyleType textStyleType) {
        if (text == null || text.isEmpty() || textStyleType == null) {
            return text;
        }
        return switch (textStyleType) {
            case APON -> stripAponStyle(text);
            case COMPACT -> compact(text);
            case COMPRESSED -> compress(text);
        };
    }

    /**
     * Strips APON style formatting from the given text.
     * @param text the text to strip APON style from
     * @return the text with APON style stripped
     */
    public static String stripAponStyle(@Nullable String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        int lineCount = 0;
        int len = text.length();
        int i = 0;
        boolean hasAponLines = false;

        while (i < len) {
            while (i < len && (text.charAt(i) == ' ' || text.charAt(i) == '\t')) {
                i++;
            }
            if (i < len && text.charAt(i) == AponFormat.TEXT_LINE_START) {
                hasAponLines = true;
                int contentStart = i + 1;
                while (i < len && text.charAt(i) != '\n' && text.charAt(i) != '\r') {
                    i++;
                }
                if (lineCount > 0) {
                    sb.append(AponFormat.SYSTEM_NEW_LINE);
                }
                sb.append(text, contentStart, i);
                lineCount++;
            } else {
                while (i < len && text.charAt(i) != '\n' && text.charAt(i) != '\r') {
                    i++;
                }
            }
            if (i < len && text.charAt(i) == '\r') {
                i++;
            }
            if (i < len && text.charAt(i) == '\n') {
                i++;
            }
        }

        if (hasAponLines) {
            return sb.toString();
        } else {
            return text.strip();
        }
    }

    /**
     * Compacts the given text by removing leading/trailing whitespace from
     * each line and replacing multiple newlines with a single one.
     * @param text the text to compact
     * @return the compacted text
     */
    public static String compact(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        int len = text.length();
        int i = 0;
        while (i < len) {
            int lineStart = i;
            while (i < len && text.charAt(i) != '\n' && text.charAt(i) != '\r') {
                i++;
            }
            int lineEnd = i;
            int trimStart = lineStart;
            while (trimStart < lineEnd && Character.isWhitespace(text.charAt(trimStart))) {
                trimStart++;
            }
            int trimEnd = lineEnd;
            while (trimEnd > trimStart && Character.isWhitespace(text.charAt(trimEnd - 1))) {
                trimEnd--;
            }
            if (trimStart < trimEnd) {
                if (!sb.isEmpty()) {
                    sb.append(System.lineSeparator());
                }
                sb.append(text, trimStart, trimEnd);
            }
            if (i < len && text.charAt(i) == '\r') {
                i++;
            }
            if (i < len && text.charAt(i) == '\n') {
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Compresses the given text into a single line by removing all newlines
     * and trimming leading/trailing whitespace from each line.
     * @param text the text to compress
     * @return the compressed text
     */
    public static String compress(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        int len = text.length();
        int i = 0;
        while (i < len) {
            int lineStart = i;
            while (i < len && text.charAt(i) != '\n' && text.charAt(i) != '\r') {
                i++;
            }
            int lineEnd = i;
            int trimStart = lineStart;
            while (trimStart < lineEnd && Character.isWhitespace(text.charAt(trimStart))) {
                trimStart++;
            }
            int trimEnd = lineEnd;
            while (trimEnd > trimStart && Character.isWhitespace(text.charAt(trimEnd - 1))) {
                trimEnd--;
            }
            if (trimStart < trimEnd) {
                sb.append(text, trimStart, trimEnd);
            }
            if (i < len && text.charAt(i) == '\r') {
                i++;
            }
            if (i < len && text.charAt(i) == '\n') {
                i++;
            }
        }
        return sb.toString();
    }

}
