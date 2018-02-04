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
package com.aspectran.core.util.wildcard;

/**
 * The Class WildcardPattern.
 * <p>
 * The following standard quantifiers are recognized:
 * <dl>
 * <dt>{@code *}</dt>  <dd>matches single character</dd>
 * <dt>{@code +}</dt>  <dd>matches one or more characters</dd>
 * <dt>{@code ?}</dt>  <dd>matches zero or more characters</dd>
 * <dt>{@code **}</dt> <dd>matches zero or more string delimited by separators</dd>
 * <dt>{@code \}</dt>  <dd>Wildcard characters can be escaped</dd>
 * </dl>
 * <p>
 * Examples
 * <dl>
 * <dt>/static/{@code *}</dt>  <dd>/static/a.jpg</dd>
 * <dt>/static{@code *}/{@code **}/b/{@code *}</dt>  <dd>matches one or more characters</dd>
 * <dt>/static{@code *}/{@code **}</dt>  <dd>/static/a/a.jpg</dd>
 * <dt>{@code **}/static/{@code **}</dt> <dd>a/b/static/a/b/c/a.jpg</dd>
 * <dt>/static-{@code ?}/a{@code ?}{@code ?}.jpg</dt>  <dd>/static-a/abc.jpg</dd>
 * </dl>
 */
public class WildcardPattern {

    private static final char ESCAPE_CHAR = '\\';
    private static final char SPACE_CHAR = ' ';
    public static final char STAR_CHAR = '*';
    private static final char QUESTION_CHAR = '?';
    private static final char PLUS_CHAR = '+';

    static final int LITERAL_TYPE = 1;
    static final int STAR_TYPE = 2;
    static final int STAR_STAR_TYPE = 3;
    static final int QUESTION_TYPE = 4;
    static final int PLUS_TYPE = 5;
    static final int SKIP_TYPE = 8;
    static final int SEPARATOR_TYPE = 9;
    static final int EOT_TYPE = 0;

    private char separator;

    private char[] tokens;

    private int[] types;

    private String patternString;

    public WildcardPattern(String patternString) {
        parse(patternString);
    }

    public WildcardPattern(String patternString, char separator) {
        this.separator = separator;

        parse(patternString);
    }

    private void parse(String patternString) {
        this.patternString = patternString;

        tokens = patternString.toCharArray();
        types = new int[tokens.length];

        boolean star = false;
        boolean esc = false;
        int ptype = SKIP_TYPE;
        int pindex = 0;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == STAR_CHAR) {
                if (esc) {
                    esc = false;
                } else {
                    if (star) {
                        types[i - 1] = SKIP_TYPE;
                        types[i] = STAR_STAR_TYPE; // type 2: double star
                        star = false;
                    } else {
                        types[i] = STAR_TYPE; // type 1: star
                        star = true;
                    }
                }
                if (ptype == QUESTION_TYPE && types[i] == STAR_TYPE) {
                    types[pindex] = SKIP_TYPE;
                }
            } else if (tokens[i] == QUESTION_CHAR) {
                if (esc) {
                    types[i - 1] = SKIP_TYPE;
                    esc = false;
                } else {
                    types[i] = QUESTION_TYPE; // type 3: question
                }
                if (ptype == STAR_TYPE && types[i] == QUESTION_TYPE) {
                    types[i] = SKIP_TYPE;
                }
            } else if (tokens[i] == PLUS_CHAR) {
                if (esc) {
                    types[i - 1] = SKIP_TYPE;
                    esc = false;
                } else {
                    types[i] = PLUS_TYPE; // type 4: plus
                }
                if (ptype == STAR_TYPE && types[i] == PLUS_CHAR) {
                    types[i] = SKIP_TYPE;
                }
            } else if (tokens[i] == separator) {
                // Separator character not escaped.
                esc = false;
                types[i] = SEPARATOR_TYPE; // type 9: separator
                /*
                if (esc) {
                    types[i - 1] = SKIP_TYPE;
                    esc = false;
                } else {
                    types[i] = SEPARATOR_TYPE; // type 9: separator
                }
                */
            } else if (tokens[i] == ESCAPE_CHAR) {
                types[i] = SKIP_TYPE;
                esc = true;
            } else {
                if (esc) {
                    types[i - 1] = LITERAL_TYPE;
                } else {
                    types[i] = LITERAL_TYPE;
                }
            }
            if (tokens[i] != STAR_CHAR && star) {
                star = false;
            }
            if (types[i] != SKIP_TYPE) {
                ptype = types[i];
                pindex = i;
            }
        }

        for (int i = 0, s = 0; i < tokens.length; i++) {
            if (types[i] == SKIP_TYPE) {
                s++;
                tokens[i] = SPACE_CHAR;
                types[i] = EOT_TYPE;
            } else if (s > 0) {
                tokens[i - s] = tokens[i];
                types[i - s] = types[i];
                tokens[i] = SPACE_CHAR;
                types[i] = EOT_TYPE;
            }
        }
    }

    public char getSeparator() {
        return separator;
    }

    protected char[] getTokens() {
        return tokens;
    }

    protected int[] getTypes() {
        return types;
    }

    /**
     * If the pattern matches then returns true.
     *
     * @param compareString the compare string
     * @return true, if successful
     */
    public boolean matches(String compareString) {
        return WildcardMatcher.matches(this, compareString);
    }

    /**
     * Erase the characters that corresponds to the wildcard, and
     * returns collect only the remaining characters.
     * In other words, only it remains for the wildcard character.
     *
     * @param nakedString the naked string
     * @return the masked string
     */
    public String mask(String nakedString) {
        return WildcardMasker.mask(this, nakedString);
    }

    @Override
    public String toString() {
        return patternString;
    }

    public static WildcardPattern compile(String patternString) {
        return new WildcardPattern(patternString);
    }

    public static WildcardPattern compile(String patternString, char separator) {
        return new WildcardPattern(patternString, separator);
    }

    public static boolean hasWildcards(String str) {
        char[] ca = str.toCharArray();
        for (char c : ca) {
            if (c == STAR_CHAR
                    || c == QUESTION_CHAR
                    || c == PLUS_CHAR) {
                return true;
            }
        }
        return false;
    }

}
