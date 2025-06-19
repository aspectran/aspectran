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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

public class PrefixSuffixPattern {

    public static final char PREFIX_SUFFIX_SEPARATOR = '*';

    private String prefix;

    private String suffix;

    public PrefixSuffixPattern() {
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean separate(String expression) {
        int start = (expression != null ? expression.indexOf(PREFIX_SUFFIX_SEPARATOR) : -1);
        if (start == -1) {
            prefix = null;
            suffix = null;
            return false;
        } else {
            prefix = (start > 0 ? expression.substring(0, start) : null);
            suffix = (start < expression.length() - 1 ? expression.substring(start + 1) : null);
            return (prefix != null || suffix != null || expression.charAt(0) == PREFIX_SUFFIX_SEPARATOR);
        }
    }

    public String enclose(String infix) {
        return join(prefix, infix, suffix);
    }

    @NonNull
    public static String join(String prefix, String infix, String suffix) {
        Assert.notNull(infix, "infix must not be null");
        if (prefix != null && suffix != null) {
            return prefix + infix + suffix;
        } else if (prefix != null) {
            return prefix + infix;
        } else if (suffix != null) {
            return infix + suffix;
        } else {
            return infix;
        }
    }

    @Nullable
    public static PrefixSuffixPattern of(String expression) {
        if (StringUtils.hasLength(expression)) {
            PrefixSuffixPattern pattern = new PrefixSuffixPattern();
            if (pattern.separate(expression)) {
                return pattern;
            }
        }
        return null;
    }

}
