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
package com.aspectran.freemarker.directive;

/**
 * A utility class that provides advanced string trimming capabilities.
 * <p>Unlike a standard trim, this class can be configured to remove and/or add
 * specific prefixes and suffixes from a string. If no custom rules are applied,
 * it defaults to standard whitespace trimming.</p>
 *
 * @since 2016. 1. 29.
 */
public class Trimmer {

    private String prefix;

    private String suffix;

    private String[] deprefixes;

    private String[] desuffixes;

    private boolean caseSensitive;

    /**
     * Gets the prefix to be added to the string.
     * @return the prefix string
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix to be added to the string.
     * @param prefix the prefix string
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the suffix to be added to the string.
     * @return the suffix string
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix to be added to the string.
     * @param suffix the suffix string
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Gets the array of prefixes to be removed from the string.
     * @return the array of prefixes to remove
     */
    public String[] getDeprefixes() {
        return deprefixes;
    }

    /**
     * Sets the array of prefixes to be removed from the string.
     * @param deprefixes the array of prefixes to remove
     */
    public void setDeprefixes(String[] deprefixes) {
        this.deprefixes = deprefixes;
    }

    /**
     * Gets the array of suffixes to be removed from the string.
     * @return the array of suffixes to remove
     */
    public String[] getDesuffixes() {
        return desuffixes;
    }

    /**
     * Sets the array of suffixes to be removed from the string.
     * @param desuffixes the array of suffixes to remove
     */
    public void setDesuffixes(String[] desuffixes) {
        this.desuffixes = desuffixes;
    }

    /**
     * Returns whether the prefix/suffix removal is case-sensitive.
     * @return true if case-sensitive, false otherwise
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Sets whether the prefix/suffix removal should be case-sensitive.
     * @param caseSensitive true for case-sensitive matching, false otherwise
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Applies the configured trimming rules to the given string.
     * The order of operations is: (1) remove deprefixes, (2) remove desuffixes,
     * (3) add prefix, (4) add suffix. If no rules are applied, performs a standard trim.
     * @param str the string to trim
     * @return the trimmed string
     */
    public String trim(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }

        StringBuilder builder = new StringBuilder(str);
        boolean deprefixed = deprefixing(builder);
        boolean desuffixed = desuffixing(builder);
        boolean prefixed = prefixing(builder);
        boolean sufixed = suffixing(builder);
        if (deprefixed || desuffixed || prefixed || sufixed) {
            return builder.toString();
        } else {
            return str.trim();
        }
    }

    /**
     * Removes any configured deprefixes from the start of the string builder.
     * @param builder the string builder to modify
     * @return true if any deprefix was removed, false otherwise
     */
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

    /**
     * Removes any configured desuffixes from the end of the string builder.
     * @param builder the string builder to modify
     * @return true if any desuffix was removed, false otherwise
     */
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

    /**
     * Adds the configured prefix to the start of the string builder.
     * @param builder the string builder to modify
     * @return true if a prefix was added, false otherwise
     */
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

    /**
     * Adds the configured suffix to the end of the string builder.
     * @param builder the string builder to modify
     * @return true if a suffix was added, false otherwise
     */
    private boolean suffixing(StringBuilder builder) {
        if (suffix == null) {
            return false;
        }
        builder.append(suffix);
        return true;
    }

    /**
     * Deletes a substring from the builder if it matches at the specified start position.
     * @param str the substring to match and delete
     * @param builder the string builder to modify
     * @param start the starting index for the comparison
     * @return true if the substring was found and deleted, false otherwise
     */
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
