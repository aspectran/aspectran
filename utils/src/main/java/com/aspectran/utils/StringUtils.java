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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Static utility methods pertaining to {@code String} or {@code CharSequence} instances.
 */
public class StringUtils {

    /** Constant for an empty {@link String}. */
    public static final String EMPTY = "";

    /** Constant for an empty {@link String} array. */
    private static final String[] EMPTY_STRING_ARRAY = {};

    /**
     * Returns {@code true} if the given string is null or is the empty string.
     * @param str a string reference to check
     * @return {@code true} if the string is null or is the empty string
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }

    /**
     * Returns the given string if it is non-null; the empty string otherwise.
     * @param str the string to test and possibly return
     * @return {@code string} itself if it is non-null; {@code ""} if it is null
     */
    public static String nullToEmpty(String str) {
        return (str != null ? str : EMPTY);
    }

    /**
     * Returns the given string if it is nonempty; {@code null} otherwise.
     * @param str the string to test and possibly return
     * @return {@code string} itself if it is nonempty; {@code null} if it is empty or null
     */
    public static String emptyToNull(String str) {
        return (str == null || str.isEmpty() ? null : str);
    }

    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.</p>
     * <pre>
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     * @param chars the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see #hasLength(String)
     * @see #hasText(CharSequence)
     */
    public static boolean hasLength(CharSequence chars) {
        return (chars != null && chars.length() > 0);
    }

    /**
     * Check that the given {@code String} is neither {@code null} nor of length 0.
     * <p>Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.</p>
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasLength(CharSequence)
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.</p>
     * <pre>
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     * @param chars the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     *      its length is greater than 0, and it does not contain whitespace only
     * @see #hasLength(String)
     * @see #hasText(CharSequence)
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence chars) {
        return (chars != null && chars.length() > 0 && containsText(chars));
    }

    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.</p>
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     *      length is greater than 0, and it does not contain whitespace only
     * @see #hasText(CharSequence)
     * @see #hasLength(String)
     * @see Character#isWhitespace
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(@NonNull CharSequence chars) {
        int strLen = chars.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(chars.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code CharSequence} contains any whitespace characters.
     * @param chars the {@code CharSequence} to check (maybe {@code null})
     * @return {@code true} if the {@code CharSequence} is not empty and
     *      contains at least 1 whitespace character
     * @see Character#isWhitespace
     */
    public static boolean containsWhitespace(CharSequence chars) {
        if (!hasLength(chars)) {
            return false;
        }
        int strLen = chars.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(chars.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code String} contains any whitespace characters.
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not empty and
     *      contains at least 1 whitespace character
     * @see #containsWhitespace(CharSequence)
     */
    public static boolean containsWhitespace(String str) {
        return containsWhitespace((CharSequence)str);
    }

    /**
     * Trim leading and trailing whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Trim <i>all</i> whitespace from the given {@code String}:
     * leading, trailing, and in between characters.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        int index = 0;
        while (buf.length() > index) {
            if (Character.isWhitespace(buf.charAt(index))) {
                buf.deleteCharAt(index);
            } else {
                index++;
            }
        }
        return buf.toString();
    }

    /**
     * Trim leading whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

    /**
     * Trim trailing whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Trim all occurrences of the supplied leading character from the given {@code String}.
     * @param str the {@code String} to check
     * @param leadingChar the leading character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimLeadingCharacter(String str, char leadingChar) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && buf.charAt(0) == leadingChar) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

    /**
     * Trim all occurrences of the supplied trailing character from the given {@code String}.
     * @param str the {@code String} to check
     * @param trailingChar the trailing character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimTrailingCharacter(String str, char trailingChar) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && buf.charAt(buf.length() - 1) == trailingChar) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Test if the given {@code String} starts with the specified prefix,
     * ignoring upper/lower case.
     * @param str the {@code String} to check
     * @param prefix the prefix to look for
     * @return {@code true} if the {@code String} starts with the prefix,
     *      case-insensitive, or both {@code null}
     * @see java.lang.String#startsWith
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() &&
                str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    /**
     * Test if the given {@code String} ends with the specified suffix,
     * ignoring upper/lower case.
     * @param str the {@code String} to check
     * @param suffix the suffix to look for
     * @return {@code true} if the {@code String} ends with the suffix,
     *      case-insensitive, or both {@code null}
     * @see java.lang.String#endsWith
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return (str != null && suffix != null && str.length() >= suffix.length() &&
                str.regionMatches(true, str.length() - suffix.length(),
                        suffix, 0, suffix.length()));
    }

    /**
     * Test if the given {@code String} starts with the specified prefix character.
     * @param str the {@code String} to check
     * @param prefix the prefix character to look for
     * @return true if the string starts with the specified prefix; otherwise false
     * @see java.lang.String#startsWith
     */
    public static boolean startsWith(String str, char prefix) {
        return (str != null && !str.isEmpty() && (str.charAt(0) == prefix));
    }

    /**
     * Test if the given {@code String} ends with the specified prefix character.
     * @param str the {@code String} to check
     * @param suffix the prefix character to look for
     * @return true if the string ends with the specified suffix; otherwise false
     * @see java.lang.String#endsWith
     */
    public static boolean endsWith(String str, char suffix) {
        return (str != null && !str.isEmpty() && (str.charAt(str.length() - 1) == suffix));
    }

    /**
     * Replace all occurrences of a substring within a string with another string.
     * @param str {@code String} to examine
     * @param search {@code String} to replace
     * @param replacement {@code String} to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String str, String search, String replacement) {
        if (str == null || search == null || replacement == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        int searchLen = search.length();
        int stringLen = str.length();
        int oldIndex = 0;
        int index;
        while ((index = str.indexOf(search, oldIndex)) >= 0) {
            sb.append(str, oldIndex, index);
            sb.append(replacement);
            oldIndex = index + searchLen;
        }
        if (oldIndex < stringLen) {
            sb.append(str, oldIndex, stringLen);
        }
        return sb.toString();
    }

    /**
     * Replace all occurrences of a substring within a string with another string.
     * @param str {@code String} to examine
     * @param searchList {@code String} array to replace
     * @param replacementList {@code String} array to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String str, String[] searchList, String[] replacementList) {
        if (str == null || searchList == null || replacementList == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int loop = Math.min(searchList.length, replacementList.length);
        int start = 0;
        int end;
        int searchLen;
        int replaceLen;
        for (int i = 0; i < loop; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            searchLen = searchList[i].length();
            replaceLen = replacementList[i].length();
            while (true) {
                if (sb.length() == 0) {
                    break;
                }
                start = sb.indexOf(searchList[i], start + replaceLen);
                if (start == -1) {
                    break;
                }
                end = start + searchLen;
                sb.replace(start, end, replacementList[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Replace last occurrence of a string.
     * @param str {@code String} to examine
     * @param searchStr {@code String} to replace
     * @param replacement {@code String} to insert
     * @return a {@code String} with the replacements
     */
    @NonNull
    public static String replaceLast(@NonNull String str, @NonNull String searchStr, @NonNull String replacement) {
        int pos = str.lastIndexOf(searchStr);
        if (pos > -1) {
            return str.substring(0, pos) + replacement +
                    str.substring(pos + searchStr.length());
        } else {
            return str;
        }
    }

    /**
     * Returns an array of strings separated by the delimiter string.
     * @param str the string to be separated
     * @param delim the delimiter
     * @return an array, containing the splitted strings
     */
    public static String[] split(String str, String delim) {
        if (isEmpty(str)) {
            return EMPTY_STRING_ARRAY;
        }
        int cnt = search(str, delim);
        String[] item = new String[cnt + 1];
        if (cnt == 0) {
            item[0] = str;
            return item;
        }
        int idx = 0;
        int pos1 = 0;
        int pos2 = str.indexOf(delim);
        int delimLen = delim.length();
        while (pos2 >= 0) {
            item[idx++] = (pos1 > pos2 - 1) ? EMPTY : str.substring(pos1, pos2);
            pos1 = pos2 + delimLen;
            pos2 = str.indexOf(delim, pos1);
        }
        if (pos1 < str.length()) {
            item[idx] = str.substring(pos1);
        }
        if (item[cnt] == null) {
            item[cnt] = EMPTY;
        }
        return item;
    }

    /**
     * Returns an array of strings separated by the delimiter string.
     * @param str the string to be separated
     * @param delim the delimiter
     * @param size the size of the array
     * @return an array, containing the splitted strings
     */
    @NonNull
    public static String[] split(String str, String delim, int size) {
        String[] arr1 = new String[size];
        String[] arr2 = split(str, delim);
        for (int i = 0; i < arr1.length; i++) {
            if (i < arr2.length) {
                arr1[i] = arr2[i];
            } else {
                arr1[i] = EMPTY;
            }
        }
        return arr1;
    }

    /**
     * Returns an array of strings separated by the delimiter string.
     * @param str the string to be separated
     * @param delim the delimiter
     * @return an array, containing the splitted strings
     */
    public static String[] split(String str, char delim) {
        if (isEmpty(str)) {
            return EMPTY_STRING_ARRAY;
        }
        int cnt = search(str, delim);
        String[] item = new String[cnt + 1];
        if (cnt == 0) {
            item[0] = str;
            return item;
        }
        int idx = 0;
        int pos1 = 0;
        int pos2 = str.indexOf(delim);
        while (pos2 >= 0) {
            item[idx++] = (pos1 > pos2 - 1) ? EMPTY : str.substring(pos1, pos2);
            pos1 = pos2 + 1;
            pos2 = str.indexOf(delim, pos1);
        }
        if (pos1 < str.length()) {
            item[idx] = str.substring(pos1);
        }
        if (item[cnt] == null) {
            item[cnt] = EMPTY;
        }
        return item;
    }

    /**
     * Returns an array of strings separated by the delimiter string.
     * @param str the string to be separated
     * @param delim the delimiter
     * @param size the size of the array
     * @return an array, containing the splitted strings
     */
    @NonNull
    public static String[] split(String str, char delim, int size) {
        String[] arr1 = new String[size];
        String[] arr2 = split(str, delim);
        for (int i = 0; i < arr1.length; i++) {
            if (i < arr2.length) {
                arr1[i] = arr2[i];
            } else {
                arr1[i] = EMPTY;
            }
        }
        return arr1;
    }

    /**
     * Returns the number of times the specified string was found
     * in the target string, or 0 if there is no specified string.
     * @param str the target string
     * @param searchStr the string to find
     * @return the number of times the specified string was found
     */
    public static int search(@NonNull String str, @NonNull String searchStr) {
        int strLen = str.length();
        int keywLen = searchStr.length();
        int pos = 0;
        int cnt = 0;
        if (keywLen == 0) {
            return 0;
        }
        while ((pos = str.indexOf(searchStr, pos)) != -1) {
            pos += keywLen;
            cnt++;
            if (pos >= strLen) {
                break;
            }
        }
        return cnt;
    }

    /**
     * Returns the number of times the specified string was found
     * in the target string, or 0 if there is no specified string.
     * When searching for the specified string, it is not case-sensitive.
     * @param str the target string
     * @param searchStr the string to find
     * @return the number of times the specified string was found
     */
    public static int searchIgnoreCase(@NonNull String str, @NonNull String searchStr) {
        return search(str.toLowerCase(), searchStr.toLowerCase());
    }

    /**
     * Returns the number of times the specified character was found
     * in the target string, or 0 if there is no specified character.
     * @param chars the target string
     * @param c the character to find
     * @return the number of times the specified character was found
     */
    public static int search(@NonNull CharSequence chars, char c) {
        int count = 0;
        for (int i = 0; i < chars.length(); i++) {
            if (chars.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of times the specified character was found
     * in the target string, or 0 if there is no specified character.
     * When searching for the specified character, it is not case-sensitive.
     * @param chars the target string
     * @param searchChar the character to find
     * @return the number of times the specified character was found
     */
    public static int searchIgnoreCase(@NonNull CharSequence chars, char searchChar) {
        int count = 0;
        char cl = Character.toLowerCase(searchChar);
        for (int i = 0; i < chars.length(); i++) {
            if (Character.toLowerCase(chars.charAt(i)) == cl) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tokenize the given {@code String} into a String array via a StringTokenizer.
     * @param str the {@code String} to tokenize
     * @param delimiters the delimiter characters
     * @return an array of the tokens
     */
    public static String[] tokenize(String str, String delimiters) {
        return tokenize(str, delimiters, false);
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a {@code StringTokenizer}.
     * @param str the String to tokenize
     * @param delimiters the delimiter characters
     * @param trim trim the tokens via String's trim
     * @return an array of the tokens
     */
    public static String[] tokenize(String str, String delimiters, boolean trim) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            tokens.add(trim ? token.trim() : token);
        }
        return tokens.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.</p>
     * @param arr the array to display
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String toDelimitedString(Object[] arr, String delim) {
        if (arr == null || arr.length == 0) {
            return EMPTY;
        }
        if (arr.length == 1) {
            return (arr[0] == null ? EMPTY : arr[0].toString());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(delim);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.</p>
     * @param collection the collection
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String toDelimitedString(Collection<?> collection, String delim) {
        if (collection == null || collection.isEmpty()) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object o : collection) {
            if (!first) {
                sb.append(delim);
            }
            sb.append(o);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Convert a {@code String} array into a delimited {@code String}
     * by a system-dependent line separator.
     * @param arr the array to display
     * @return the delimited {@code String}
     */
    public static String toLineDelimitedString(Object[] arr) {
        return toDelimitedString(arr, System.lineSeparator());
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String}
     * by a system-dependent line separator.
     * @param collection the collection
     * @return the delimited {@code String}
     */
    public static String toLineDelimitedString(Collection<?> collection) {
        return toDelimitedString(collection, System.lineSeparator());
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into an
     * array of strings.
     * @param str the input {@code String}
     * @return an array of strings, or the empty array in case of empty input
     */
    public static String[] splitCommaDelimitedString(String str) {
        return tokenize(str, ",", true);
    }

    /**
     * Convert a {@code String} array into a comma delimited {@code String}
     * (i.e., CSV).
     * @param arr the array to display
     * @return the delimited {@code String}
     */
    public static String joinCommaDelimitedList(String[] arr) {
        return toDelimitedString(arr, ", ");
    }

    /**
     * Convert a {@code Collection} into a comma delimited {@code String}
     * (i.e., CSV).
     * @param collection the collection
     * @return the delimited {@code String}
     */
    public static String joinCommaDelimitedList(Collection<?> collection) {
        return toDelimitedString(collection, ", ");
    }

    /**
     * Copy the given {@link Collection} into a {@code String} array.
     * <p>The {@code Collection} must contain {@code String} elements only.
     * @param collection the {@code Collection} to copy
     * (potentially {@code null} or empty)
     * @return the resulting {@code String} array
     */
    public static String[] toStringArray(Collection<String> collection) {
        return (collection != null && !collection.isEmpty() ?
            collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
    }

    /**
     * Copy the given {@link Enumeration} into a {@code String} array.
     * <p>The {@code Enumeration} must contain {@code String} elements only.
     * @param enumeration the {@code Enumeration} to copy
     * (potentially {@code null} or empty)
     * @return the resulting {@code String} array
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return (enumeration != null ? toStringArray(Collections.list(enumeration)) : EMPTY_STRING_ARRAY);
    }

    /**
     * Returns padding using the specified delimiter repeated to a given length.
     * @param ch character to repeat
     * @param repeat number of times to repeat char, negative treated as zero
     * @return String with repeated character
     */
    public static String repeat(char ch, final int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    /**
     * Convert byte size into human friendly format.
     * @param bytes the number of bytes
     * @return a human friendly byte size (includes units)
     */
    public static String convertToHumanFriendlyByteSize(long bytes) {
        if (bytes < 1024 && bytes > -1024) {
            return bytes + " B";
        }
        String minus = null;
        if (bytes < 0) {
            minus = "-";
            bytes = -bytes;
        }
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        double d = (double)bytes / (1L << (z * 10));
        String format = (d % 1.0 == 0 ? "%s%.0f %sB" : "%s%.1f %sB");
        return String.format(format, nullToEmpty(minus), d, " KMGTPE".charAt(z));
    }

    /**
     * Convert byte size into machine friendly format.
     * @param bytes the human friendly byte size (includes units)
     * @return a number of bytes
     * @throws NumberFormatException if failed parse given size
     */
    @SuppressWarnings("fallthrough")
    public static long convertToMachineFriendlyByteSize(@NonNull String bytes) {
        double d;
        try {
            d = Double.parseDouble(bytes.replaceAll("[GMK]?B?$", EMPTY));
        } catch (NumberFormatException e)  {
            String msg = "Size must be specified as bytes (B), " +
                    "kilobytes (KB), megabytes (MB), gigabytes (GB). " +
                    "E.g. 1024, 1KB, 10M, 10MB, 100G, 100GB";
            throw new NumberFormatException(msg + " " + e.getMessage());
        }
        long l = Math.round(d * 1024 * 1024 * 1024L);
        int index = Math.max(0, bytes.length() - (bytes.endsWith("B") ? 2 : 1));
        switch (bytes.charAt(index)) {
            default:  l /= 1024;
            case 'K': l /= 1024;
            case 'M': l /= 1024;
            case 'G': return l;
        }
    }

}
