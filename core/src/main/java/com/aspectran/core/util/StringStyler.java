/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.apon.AponFormat;

/**
 * Contains methods to transform a given string to a specific style.
 *
 * <p>Created: 2017. 3. 22.</p>
 */
public class StringStyler {

    public static String toAponStyle(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        StringBuilder sb = new StringBuilder(content.length());
        int start = 0;
        int line = 0;
        for (int end = 0; end < content.length(); end++) {
            char c = content.charAt(end);
            if (start == 0 && c == AponFormat.TEXT_LINE_START) {
                if (line > 0) {
                    sb.append(AponFormat.NEW_LINE_CHAR);
                }
                start = end + 1;
                line++;
            } else if (start > 0) {
                if (c == '\n' || c == '\r') {
                    if (end > start) {
                        sb.append(content, start, end);
                    }
                    start = 0;
                }
            }
        }
        if (start > 0 && start < content.length()) {
            sb.append(content, start, content.length());
        }
        return sb.toString();
    }

    public static String toCompactStyle(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        content = content.trim();
        StringBuilder sb = new StringBuilder(content.length());
        int start = 0;
        for (int end = 0; end < content.length(); end++) {
            char c = content.charAt(end);
            if (c == '\n' || c == '\r') {
                if (start > -1) {
                    sb.append(content.substring(start, end).trim());
                    sb.append(ActivityContext.LINE_SEPARATOR);
                    start = -1;
                }
            } else if (start == -1) {
                start = end;
            }
        }
        if (start > -1) {
            sb.append(content.substring(start).trim());
        }
        return sb.toString();
    }

    public static String toCompressedStyle(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        content = content.trim();
        StringBuilder sb = new StringBuilder(content.length());
        int start = 0;
        for (int end = 0; end < content.length(); end++) {
            char c = content.charAt(end);
            if (c == '\n' || c == '\r') {
                if (start > -1) {
                    sb.append(content.substring(start, end).trim());
                    start = -1;
                }
            } else if (start == -1) {
                start = end;
            }
        }
        if (start > -1) {
            sb.append(content.substring(start).trim());
        }
        return sb.toString();
    }

}
