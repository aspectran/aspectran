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
package com.aspectran.core.support.freemarker.directive;

/**
 * The Class Trimmer.
 *
 * <p>Created: 2016. 1. 29.</p>
 */
public class Trimmer {

    private String prefix;

    private String suffix;

    private String[] deprefixes;

    private String[] desuffixes;

    private boolean caseSensitive;

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

    public String[] getDeprefixes() {
        return deprefixes;
    }

    public void setDeprefixes(String[] deprefixes) {
        this.deprefixes = deprefixes;
    }

    public String[] getDesuffixes() {
        return desuffixes;
    }

    public void setDesuffixes(String[] desuffixes) {
        this.desuffixes = desuffixes;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String trim(String str) {
        if (str == null) {
            return null;
        }

        if (str.length() == 0) {
            return str;
        }

        StringBuilder builder = new StringBuilder(str);

        boolean deprefixed = deprefixing(builder);
        boolean desuffixed = desuffixing(builder);
        boolean prefixed = prefixing(builder);
        boolean sufixed = suffixing(builder);

        if (deprefixed || desuffixed || prefixed || sufixed) {
            return builder.toString();
        }

        return str.trim();
    }

    private boolean deprefixing(StringBuilder builder) {
        boolean applied = false;

        if (deprefixes != null && deprefixes.length > 0) {
            int start = 0;
            for (; start < builder.length(); start++) {
                if (!Character.isWhitespace(builder.charAt(start))) {
                    break;
                }
            }

            for (String deprefix : deprefixes) {
                if (delete(deprefix, builder, start)) {
                    applied = true;
                }
            }
        }

        return applied;
    }

    private boolean desuffixing(StringBuilder builder) {
        boolean applied = false;

        if (desuffixes != null && desuffixes.length > 0) {
            int len = builder.length();
            for (; len > 0; len--) {
                if (!Character.isWhitespace(builder.charAt(len - 1))) {
                    break;
                }
            }

            for (String desuffix : desuffixes) {
                int start = len - desuffix.length();
                if (delete(desuffix, builder, start)) {
                    applied = true;
                }
            }
        }

        return applied;
    }

    private boolean prefixing(StringBuilder builder) {
        if (prefix == null) {
            return false;
        }

        int start = 0;
        for (; start < builder.length(); start++) {
            if (!Character.isWhitespace(builder.charAt(start))) {
                break;
            }
        }

        builder.insert(start, prefix);

        return true;
    }

    private boolean suffixing(StringBuilder builder) {
        if (suffix == null) {
            return false;
        }

        builder.append(suffix);

        return true;
    }

    private boolean delete(String str, StringBuilder builder, int start) {
        if (str == null || str.length() > builder.length()) {
            return false;
        }

        int end = start + str.length();

        for (int i = start, j = 0; i < end; i++, j++) {
            char c1 = caseSensitive ? builder.charAt(i) : Character.toLowerCase(builder.charAt(i));
            char c2 = caseSensitive ? str.charAt(j) : Character.toLowerCase(str.charAt(j));
            if (c1 != c2) {
                return false;
            }
        }

        builder.delete(start, end);

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{prefix=").append(prefix);
        sb.append(", suffix=").append(suffix);
        sb.append(", deprefixes=");
        if (deprefixes != null) {
            sb.append("[");
            for (int i = 0; i < deprefixes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(deprefixes[i]);
            }
            sb.append("]");
        } else {
            sb.append("null");
        }
        sb.append(", desuffixes=");
        if (desuffixes != null) {
            sb.append("[");
            for (int i = 0; i < desuffixes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(desuffixes[i]);
            }
            sb.append("]");
        } else {
            sb.append("null");
        }
        sb.append(", caseSensitive=").append(caseSensitive);
        sb.append("}");
        return sb.toString();
    }

}
