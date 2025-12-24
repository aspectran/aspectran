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

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Static utility methods for {@link String} or {@link CharSequence} instances.
 * <p>This class provides convenience methods for common string manipulations
 * such as checking for emptiness, trimming, splitting, and joining.</p>
 */
public class StringUtils {

    /** Constant for an empty {@link String}. */
    public static final String EMPTY = "";

    /** Constant for an empty {@link String} array. */
    private static final String[] EMPTY_STRING_ARRAY = {};

    /**
     * This class cannot be instantiated.
     */
    private StringUtils() {
    }

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
     * Checks that the given {@code CharSequence} is not {@code null} and has a length greater than 0.
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
     * @see #hasText(CharSequence)
     */
    public static boolean hasLength(CharSequence chars) {
        return (chars != null && !chars.isEmpty());
    }

    /**
     * Checks that the given {@code String} is not {@code null} and has a length greater than 0.
     * <p>Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.</p>
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * Checks whether the given {@code CharSequence} contains actual <em>text</em>.
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
     * @see #hasLength(CharSequence)
     */
    public static boolean hasText(CharSequence chars) {
        return (chars != null && !chars.isEmpty() && containsText(chars));
    }

    /**
     * Checks whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.</p>
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     *      length is greater than 0, and it does not contain whitespace only
     * @see #hasText(CharSequence)
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
     * Checks whether the given {@code CharSequence} contains any whitespace characters.
     * @param chars the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not empty and
     *      contains at least one whitespace character
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
     * Checks whether the given {@code String} contains any whitespace characters.
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not empty and
     *      contains at least one whitespace character
     * @see #containsWhitespace(CharSequence)
     */
    public static boolean containsWhitespace(String str) {
        return containsWhitespace((CharSequence)str);
    }

    /**
     * Trims leading and trailing whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}, or the original {@code String} if it has no length
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (!buf.isEmpty() && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        while (!buf.isEmpty() && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Trims <i>all</i> whitespace from the given {@code String}:
     * leading, trailing, and in between characters.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}, or the original {@code String} if it has no length
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
     * Trims leading whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}, or the original {@code String} if it has no length
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (!buf.isEmpty() && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

    /**
     * Trims trailing whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}, or the original {@code String} if it has no length
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (!buf.isEmpty() && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Trims all occurrences of the supplied leading character from the given {@code String}.
     * @param str the {@code String} to check
     * @param leadingChar the leading character to be trimmed
     * @return the trimmed {@code String}, or the original {@code String} if it has no length
     */
    public static String trimLeadingCharacter(String str, char leadingChar) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (!buf.isEmpty() && buf.charAt(0) == leadingChar) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

    /**
     * Trims all occurrences of the supplied trailing character from the given {@code String}.
     * @param str the {@code String} to check
     * @param trailingChar the trailing character to be trimmed
     * @return the trimmed {@code String}, or the original {@code String} if it has no length
     */
    public static String trimTrailingCharacter(String str, char trailingChar) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (!buf.isEmpty() && buf.charAt(buf.length() - 1) == trailingChar) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Tests if the given {@code String} starts with the specified prefix, ignoring case.
     * @param str the {@code String} to check
     * @param prefix the prefix to look for
     * @return {@code true} if the {@code String} starts with the prefix (case-insensitive)
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() &&
                str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    /**
     * Tests if the given {@code String} ends with the specified suffix, ignoring case.
     * @param str the {@code String} to check
     * @param suffix the suffix to look for
     * @return {@code true} if the {@code String} ends with the suffix (case-insensitive)
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return (str != null && suffix != null && str.length() >= suffix.length() &&
                str.regionMatches(true, str.length() - suffix.length(),
                        suffix, 0, suffix.length()));
    }

    /**
     * Tests if the given {@code String} starts with the specified prefix character.
     * @param str the {@code String} to check
     * @param prefix the prefix character to look for
     * @return true if the string starts with the specified prefix; otherwise false
     */
    public static boolean startsWith(String str, char prefix) {
        return (str != null && !str.isEmpty() && (str.charAt(0) == prefix));
    }

    /**
     * Tests if the given {@code String} ends with the specified suffix character.
     * @param str the {@code String} to check
     * @param suffix the suffix character to look for
     * @return true if the string ends with the specified suffix; otherwise false
     */
    public static boolean endsWith(String str, char suffix) {
        return (str != null && !str.isEmpty() && (str.charAt(str.length() - 1) == suffix));
    }

    /**
     * Replaces all occurrences of a substring within a string with another string.
     * @param str {@code String} to examine (may be {@code null})
     * @param search {@code String} to replace (may be {@code null})
     * @param replacement {@code String} to insert (may be {@code null})
     * @return the modified {@code String}, or the original if any input is {@code null}
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
     * Replaces multiple substrings in a string with corresponding replacements.
     * This method is more efficient than calling {@link #replace(String, String, String)} multiple times.
     * @param str {@code String} to examine (may be {@code null})
     * @param searchList array of {@code String}s to replace
     * @param replacementList array of {@code String}s to insert
     * @return the modified {@code String}, or the original if any input is {@code null}
     */
    public static String replace(String str, String[] searchList, String[] replacementList) {
        if (str == null || searchList == null || replacementList == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int loop = Math.min(searchList.length, replacementList.length);
        for (int i = 0; i < loop; i++) {
            String search = searchList[i];
            String replacement = replacementList[i];
            if (search == null || replacement == null) {
                continue;
            }
            int searchLen = search.length();
            int replaceLen = replacement.length();
            int start = 0;
            while (start < sb.length()) {
                int index = sb.indexOf(search, start);
                if (index == -1) {
                    break;
                }
                sb.replace(index, index + searchLen, replacement);
                start = index + replaceLen;
            }
        }
        return sb.toString();
    }

    /**
     * Replaces the last occurrence of a substring within a string with another string.
     * @param str {@code String} to examine
     * @param searchStr {@code String} to replace
     * @param replacement {@code String} to insert
     * @return a {@code String} with the replacement
     */
    @NonNull
    public static String replaceLast(@NonNull String str, @NonNull String searchStr, @NonNull String replacement) {
        int index = str.lastIndexOf(searchStr);
        if (index > -1) {
            return str.substring(0, index) + replacement + str.substring(index + searchStr.length());
        } else {
            return str;
        }
    }

    /**
     * Returns a {@code String} consisting of a specified character repeated a given number of times.
     * @param ch character to repeat
     * @param repeat number of times to repeat the character; a non-positive value returns an empty string
     * @return a {@code String} with the repeated character
     */
    public static String repeat(char ch, final int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        if (repeat == 1) {
            return String.valueOf(ch);
        }
        char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    /**
     * Divides a {@code String} into a two-element array at the first occurrence of the delimiter.
     * The delimiter is not included in the result.
     * @param str the string to divide (may be {@code null})
     * @param delim the delimiter to split on (may be {@code null})
     * @return a two-element array with the part before the delimiter at index 0 and the part
     *      after at index 1. If the delimiter is not found, the original string is at index 0.
     */
    public static String[] divide(String str, String delim) {
        if (str == null) {
            return new String[] {null, null};
        }
        if (str.isEmpty()) {
            return new String[] {EMPTY, null};
        }
        if (isEmpty(delim)) {
            return new String[] {str, null};
        }
        int idx = str.indexOf(delim);
        if (idx < 0) {
            return new String[] {str, null};
        }
        String str1 = str.substring(0, idx);
        String str2 = str.substring(idx + delim.length());
        return new String[] {str1, str2};
    }

    /**
     * Splits the provided text into an array, using the given delimiter.
     * @param str the string to be separated (may be {@code null})
     * @param delim the delimiter (may be {@code null})
     * @return an array of the splitted strings, never {@code null}
     */
    public static String[] split(String str, String delim) {
        if (isEmpty(str)) {
            return EMPTY_STRING_ARRAY;
        }
        int cnt = search(str, delim);
        String[] arr = new String[cnt + 1];
        if (cnt == 0) {
            arr[0] = str;
            return arr;
        }
        int idx = 0;
        int idx1 = 0;
        int idx2 = str.indexOf(delim);
        int delimLen = delim.length();
        while (idx2 >= 0) {
            arr[idx++] = (idx1 > idx2 - 1 ? EMPTY : str.substring(idx1, idx2));
            idx1 = idx2 + delimLen;
            idx2 = str.indexOf(delim, idx1);
        }
        if (idx1 < str.length()) {
            arr[idx] = str.substring(idx1);
        }
        if (arr[cnt] == null) {
            arr[cnt] = EMPTY;
        }
        return arr;
    }

    /**
     * Splits the provided text into an array of a specified size, using the given delimiter.
     * If the split results in fewer elements than the size, the remaining elements are filled with empty strings.
     * @param str the string to be separated
     * @param delim the delimiter
     * @param size the desired size of the array
     * @return an array of the splitted strings, with a fixed size
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
     * Splits the provided text into an array, using the given delimiter character.
     * @param str the string to be separated
     * @param delim the delimiter character
     * @return an array of the splitted strings, never {@code null}
     */
    public static String[] split(String str, char delim) {
        if (isEmpty(str)) {
            return EMPTY_STRING_ARRAY;
        }
        int cnt = search(str, delim);
        String[] arr = new String[cnt + 1];
        if (cnt == 0) {
            arr[0] = str;
            return arr;
        }
        int idx = 0;
        int idx1 = 0;
        int idx2 = str.indexOf(delim);
        while (idx2 >= 0) {
            arr[idx++] = (idx1 > idx2 - 1 ? EMPTY : str.substring(idx1, idx2));
            idx1 = idx2 + 1;
            idx2 = str.indexOf(delim, idx1);
        }
        if (idx1 < str.length()) {
            arr[idx] = str.substring(idx1);
        }
        if (arr[cnt] == null) {
            arr[cnt] = EMPTY;
        }
        return arr;
    }

    /**
     * Splits the provided text into an array of a specified size, using the given delimiter character.
     * If the split results in fewer elements than the size, the remaining elements are filled with empty strings.
     * @param str the string to be separated
     * @param delim the delimiter character
     * @param size the desired size of the array
     * @return an array of the splitted strings, with a fixed size
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
     * Converts a comma-delimited list (e.g., a row from a CSV file) into an
     * array of strings. Tokens are trimmed.
     * @param str the input {@code String}
     * @return an array of strings, or an empty array if the input is empty
     */
    public static String[] splitWithComma(String str) {
        return tokenize(str, ",", true);
    }

    /**
     * Tokenizes the given {@code String} into a {@code String} array via a {@link StringTokenizer}.
     * @param str the {@code String} to tokenize
     * @param delimiters the delimiter characters
     * @return an array of the tokens, never {@code null}
     */
    public static String[] tokenize(String str, String delimiters) {
        return tokenize(str, delimiters, false);
    }

    /**
     * Tokenizes the given {@code String} into a {@code String} array via a {@link StringTokenizer}.
     * @param str the String to tokenize
     * @param delimiters the delimiter characters
     * @param trim whether to trim the tokens
     * @return an array of the tokens, never {@code null}
     */
    public static String[] tokenize(String str, String delimiters, boolean trim) {
        if (isEmpty(str) || isEmpty(delimiters)) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer st = new StringTokenizer((trim ? str.trim() : str), delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            tokens.add(trim ? token.trim() : token);
        }
        return tokens.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Converts an array into a delimited {@code String} (e.g. CSV).
     * @param arr the array to convert
     * @param delim the delimiter to use (e.g., ",")
     * @return the delimited {@code String}
     */
    public static String join(Object[] arr, String delim) {
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
     * Converts a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * @param collection the collection to convert
     * @param delim the delimiter to use (e.g., ",")
     * @return the delimited {@code String}
     */
    public static String join(Collection<?> collection, String delim) {
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
     * Converts an array into a {@code String} separated by the system-dependent line separator.
     * @param arr the array to join
     * @return the joined {@code String}
     */
    public static String joinWithLines(Object[] arr) {
        return join(arr, System.lineSeparator());
    }

    /**
     * Converts a {@code Collection} into a {@code String} separated by the system-dependent line separator.
     * @param collection the collection to join
     * @return the joined {@code String}
     */
    public static String joinWithLines(Collection<?> collection) {
        return join(collection, System.lineSeparator());
    }

    /**
     * Converts a {@code String} array into a comma-separated {@code String}.
     * @param arr the array to display
     * @return the comma-separated {@code String}
     */
    public static String joinWithCommas(String[] arr) {
        return join(arr, ", ");
    }

    /**
     * Converts a {@code Collection} into a comma-separated {@code String}.
     * @param collection the collection to convert
     * @return the comma-separated {@code String}
     */
    public static String joinWithCommas(Collection<?> collection) {
        return join(collection, ", ");
    }

    /**
     * Copies the given {@link Collection} of {@code String}s into a {@code String} array.
     * @param collection the {@code Collection} to copy (may be {@code null})
     * @return the resulting {@code String} array, or an empty array if the collection is {@code null} or empty
     */
    public static String[] toStringArray(Collection<String> collection) {
        return (collection != null && !collection.isEmpty() ?
                collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
    }

    /**
     * Copies the given {@link Enumeration} of {@code String}s into a {@code String} array.
     * @param enumeration the {@code Enumeration} to copy (may be {@code null})
     * @return the resulting {@code String} array, or an empty array if the enumeration is {@code null} or empty
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return (enumeration != null ? toStringArray(Collections.list(enumeration)) : EMPTY_STRING_ARRAY);
    }

    /**
     * Counts how many times the substring appears in the larger string.
     * @param str the string to search in (may be {@code null})
     * @param searchStr the substring to search for (may be {@code null})
     * @return the number of times the substring appears, 0 if either string is empty
     */
    public static int search(String str, String searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return 0;
        }
        int len1 = str.length();
        int len2 = searchStr.length();
        int idx = 0;
        int cnt = 0;
        while ((idx = str.indexOf(searchStr, idx)) != -1) {
            idx += len2;
            cnt++;
            if (idx >= len1) {
                break;
            }
        }
        return cnt;
    }

    /**
     * Counts how many times the case-insensitive substring appears in the larger string.
     * @param str the string to search in (may be {@code null})
     * @param searchStr the substring to search for (may be {@code null})
     * @return the number of times the substring appears, 0 if either string is empty
     */
    public static int searchIgnoreCase(String str, String searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return 0;
        }
        return search(str.toLowerCase(), searchStr.toLowerCase());
    }

    /**
     * Counts how many times the character appears in the larger string.
     * @param chars the character sequence to search in (may be {@code null})
     * @param searchChar the character to search for
     * @return the number of times the character appears, 0 if the sequence is empty
     */
    public static int search(CharSequence chars, char searchChar) {
        if (chars == null || chars.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < chars.length(); i++) {
            if (chars.charAt(i) == searchChar) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many times the case-insensitive character appears in the larger string.
     * @param chars the character sequence to search in (may be {@code null})
     * @param searchChar the character to search for
     * @return the number of times the character appears, 0 if the sequence is empty
     */
    public static int searchIgnoreCase(CharSequence chars, char searchChar) {
        if (chars == null || chars.isEmpty()) {
            return 0;
        }
        int count = 0;
        char cl = Character.toLowerCase(searchChar);
        for (int i = 0; i < chars.length(); i++) {
            if (Character.toLowerCase(chars.charAt(i)) == cl) {
                count++;
            }
        }
        return count;
    }

}
