/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

/**
 * Contains methods to transform a given text to a specific style.
 *
 * <p>Created: 2017. 3. 22.</p>
 */
public class TextStyler {

    public static String styling(String text, String style) {
        TextStyleType textStyleType = TextStyleType.resolve(style);
        if (style != null && textStyleType == null) {
            throw new IllegalArgumentException("No text style type for '" + style + "'");
        }
        return styling(text, textStyleType);
    }

    public static String styling(String text, TextStyleType textStyleType) {
        if (textStyleType == TextStyleType.APON) {
            return TextStyler.stripAponStyle(text);
        } else if (textStyleType == TextStyleType.COMPACT) {
            return TextStyler.compact(text);
        } else if (textStyleType == TextStyleType.COMPRESSED) {
            return TextStyler.compress(text);
        } else {
            return text;
        }
    }

    public static String stripAponStyle(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        int start = 0;
        int line = 0;
        for (int end = 0; end < text.length(); end++) {
            char c = text.charAt(end);
            if (start == 0 && c == AponFormat.TEXT_LINE_START) {
                if (line > 0) {
                    sb.append(AponFormat.SYSTEM_NEW_LINE);
                }
                start = end + 1;
                line++;
            } else if (start > 0) {
                if (c == '\n' || c == '\r') {
                    if (end > start) {
                        sb.append(text, start, end);
                    }
                    start = 0;
                }
            }
        }
        if (start > 0 && start < text.length()) {
            sb.append(text, start, text.length());
        }
        if (!sb.isEmpty()) {
            return sb.toString();
        }
        for (start = 0; start < text.length(); start++) {
            if (text.charAt(start) != ' ') {
                break;
            }
        }
        int end = text.length();
        while (end > 0) {
            if (text.charAt(end - 1) != ' ') {
                break;
            }
            end--;
        }
        return text.substring(start, end);
    }

    public static String compact(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        text = text.trim();
        StringBuilder sb = new StringBuilder(text.length());
        int start = 0;
        for (int end = 0; end < text.length(); end++) {
            char c = text.charAt(end);
            if (c == '\n' || c == '\r') {
                if (start > -1) {
                    sb.append(text.substring(start, end).trim());
                    sb.append(System.lineSeparator());
                    start = -1;
                }
            } else if (start == -1) {
                start = end;
            }
        }
        if (start > -1) {
            sb.append(text.substring(start).trim());
        }
        return sb.toString();
    }

    public static String compress(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        text = text.trim();
        StringBuilder sb = new StringBuilder(text.length());
        int start = 0;
        for (int end = 0; end < text.length(); end++) {
            char c = text.charAt(end);
            if (c == '\n' || c == '\r') {
                if (start > -1) {
                    sb.append(text.substring(start, end).trim());
                    start = -1;
                }
            } else if (start == -1) {
                start = end;
            }
        }
        if (start > -1) {
            sb.append(text.substring(start).trim());
        }
        return sb.toString();
    }

}
