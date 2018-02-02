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

public class PrefixSuffixPattern {

    public static final char PREFIX_SUFFIX_PATTERN_SEPARATOR = '*';

    private String prefix;

    private String suffix;

    private boolean splitted;

    public PrefixSuffixPattern() {
    }

    public PrefixSuffixPattern(String input) {
        split(input);
    }

    public boolean split(String input) {
        int start = (input != null) ? input.indexOf(PREFIX_SUFFIX_PATTERN_SEPARATOR) : -1;
        if (start == -1) {
            prefix = null;
            suffix = null;
            splitted = false;
        } else {
            prefix = (start > 0 ? input.substring(0, start) : null);
            suffix = (start < input.length() - 1 ? input.substring(start + 1) : null);
            splitted = (prefix != null || suffix != null || (input.length() == 1 && input.charAt(0) == PREFIX_SUFFIX_PATTERN_SEPARATOR));
        }
        return splitted;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isSplitted() {
        return splitted;
    }

    public String join(String input) {
        return join(prefix, input, suffix);
    }

    public static String join(String prefix, String input, String suffix) {
        if (prefix != null && suffix != null) {
            return prefix + StringUtils.nullToEmpty(input) + suffix;
        } else if (prefix != null) {
            return prefix + StringUtils.nullToEmpty(input);
        } else if (suffix != null) {
            return StringUtils.nullToEmpty(input) + suffix;
        } else {
            return input;
        }
    }

    public static PrefixSuffixPattern parse(String input) {
        if (input != null && !input.isEmpty()) {
            PrefixSuffixPattern pattern = new PrefixSuffixPattern(input);
            if (pattern.isSplitted()) {
                return pattern;
            }
        }
        return null;
    }

}
