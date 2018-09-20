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
package com.aspectran.shell.command.option;

import java.util.Arrays;

/**
 * Contains useful helper methods for classes within this package.
 */
public final class OptionUtils {

    /**
     * Validates whether {@code opt} is a permissible Option
     * shortOpt.  The rules that specify if the {@code opt}
     * is valid are:
     *
     * <ul>
     *  <li>a single character {@code opt} that is either
     *  ' '(special case), '?', '@' or a letter</li>
     *  <li>a multi character {@code opt} that only contains
     *  letters.</li>
     * </ul>
     * <p>
     * In case {@code opt} is {@code null} no further validation is performed.
     * </p>
     *
     * @param opt the option string to validate, may be null
     * @throws IllegalArgumentException if the Option is not valid
     */
    static void validateOption(String opt) throws IllegalArgumentException {
        // if opt is NULL do not check further
        if (opt == null) {
            return;
        }

        // handle the single character opt
        if (opt.length() == 1) {
            char ch = opt.charAt(0);
            if (!isValidOpt(ch)) {
                throw new IllegalArgumentException("Illegal option name '" + ch + "'");
            }
        }
        // handle the multi character opt
        else {
            for (char ch : opt.toCharArray()) {
                if (!isValidChar(ch)) {
                    throw new IllegalArgumentException("The option '" + opt + "' contains an illegal "
                            + "character : '" + ch + "'");
                }
            }
        }
    }

    /**
     * Returns whether the specified character is a valid Option.
     *
     * @param c the option to validate
     * @return true if {@code c} is a letter, '?' or '@', otherwise false
     */
    private static boolean isValidOpt(char c) {
        return (isValidChar(c) || c == '?' || c == '@');
    }

    /**
     * Returns whether the specified character is a valid character.
     *
     * @param c the character to validate
     * @return true if {@code c} is a letter
     */
    private static boolean isValidChar(final char c) {
        return Character.isJavaIdentifierPart(c);
    }

    /**
     * Remove the hyphens from the beginning of <code>str</code> and
     * return the new String.
     *
     * @param str the string from which the hyphens should be removed
     * @return the new String
     */
    static String stripLeadingHyphens(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("--")) {
            return str.substring(2);
        } else if (str.startsWith("-")) {
            return str.substring(1);
        }
        return str;
    }

    /**
     * Remove the leading and trailing quotes from <code>str</code>.
     * E.g. if str is '"one two"', then 'one two' is returned.
     *
     * @param str the string from which the leading and trailing quotes
     *      should be removed
     * @return the string without the leading and trailing quotes
     */
    static String stripLeadingAndTrailingQuotes(String str) {
        int length = str.length();
        if (length > 1 && str.startsWith("\"") && str.endsWith("\"") &&
                str.substring(1, length - 1).indexOf('"') == -1) {
            str = str.substring(1, length - 1);
        }
        return str;
    }

    /**
     * Return a String of padding of length {@code len}.
     *
     * @param len the length of the String of padding to create
     * @return the String of padding
     */
    public static String createPadding(int len) {
        char[] padding = new char[len];
        Arrays.fill(padding, ' ');
        return new String(padding);
    }

    /**
     * Remove the trailing whitespace from the specified String.
     *
     * @param s the String to remove the trailing padding from
     * @return the String of without the trailing padding
     */
    public static String rtrim(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        int pos = s.length();
        while (pos > 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }
        return s.substring(0, pos);
    }

    /**
     * Finds the next text wrap position after {@code startPos} for the
     * text in {@code text} with the column width {@code width}.
     * The wrap point is the last position before startPos+width having a
     * whitespace character (space, \n, \r). If there is no whitespace character
     * before startPos+width, it will return startPos+width.
     *
     * @param text the text being searched for the wrap position
     * @param width width of the wrapped text
     * @param startPos position from which to start the lookup whitespace character
     * @return position on which the text must be wrapped or -1 if the wrap
     *      position is at the end of the text
     */
    public static int findWrapPos(String text, int width, int startPos) {
        // the line ends before the max wrap pos or a new line char found
        int pos = text.indexOf('\n', startPos);
        if (pos != -1 && pos <= width) {
            return pos + 1;
        }

        pos = text.indexOf('\t', startPos);
        if (pos != -1 && pos <= width) {
            return pos + 1;
        }

        if (startPos + width >= text.length()) {
            return -1;
        }

        // look for the last whitespace character before startPos+width
        for (pos = startPos + width; pos >= startPos; --pos) {
            char c = text.charAt(pos);
            if (c == ' ' || c == '\n' || c == '\r') {
                break;
            }
        }

        // if we found it - just return
        if (pos > startPos) {
            return pos;
        }

        // if we didn't find one, simply chop at startPos+width
        pos = startPos + width;

        return (pos == text.length() ? -1 : pos);
    }

}
