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
package com.aspectran.utils.wildcard;

import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Objects;

public class WildcardPattern {

    /** Escape character to treat the next character as a literal. */
    private static final char ESCAPE_CHAR = '\\';
    /** Space used internally when compacting the token array. */
    private static final char SPACE_CHAR = ' ';
    /** Wildcard for any sequence of characters within a segment. */
    public static final char STAR_CHAR = '*';
    /** Wildcard for exactly one character within a segment. */
    public static final char QUESTION_CHAR = '?';
    /** Wildcard for one or more characters within a segment. */
    public static final char PLUS_CHAR = '+';

    /** Token type: literal character. */
    static final int LITERAL_TYPE = 1;
    /** Token type: '*' (single-segment wildcard). */
    static final int STAR_TYPE = 2;
    /** Token type: '**' (cross-segment wildcard, respects separator). */
    static final int STAR_STAR_TYPE = 3;
    /** Token type: '?' (single character). */
    static final int QUESTION_TYPE = 4;
    /** Token type: '+' (one or more characters). */
    static final int PLUS_TYPE = 5;
    /** Internal marker: skip/removed during compaction. */
    static final int SKIP_TYPE = 8;
    /** Token type: separator character. */
    static final int SEPARATOR_TYPE = 9;
    /** End-of-tokens marker. */
    static final int EOT_TYPE = 0;

    /** Original pattern string as provided by the caller. */
    private final String patternString;

    /** Optional path separator used to distinguish segments; Character.MIN_VALUE means none. */
    private final char separator;

    /** Compacted token characters representing the pattern. */
    private final char[] tokens;

    /** Parallel array with token types for each entry in {@link #tokens}. */
    private final int[] types;

    /** Heuristic weight used to sort/select more specific patterns first. */
    private final float weight;

    /**
     * Create a pattern using the default behavior (no segment separator).
     * @param patternString the wildcard pattern text
     */
    public WildcardPattern(String patternString) {
        this(patternString, Character.MIN_VALUE);
    }

    /**
     * Create a pattern with an explicit segment separator. When provided, the
     * double-star token (**) may span across separators while single-star and
     * other tokens do not.
     * @param patternString the wildcard pattern text
     * @param separator the segment separator character, or {@link Character#MIN_VALUE} for none
     */
    public WildcardPattern(String patternString, char separator) {
        Assert.notNull(patternString, "patternString must not be null");
        this.patternString = patternString;
        this.separator = separator;
        this.tokens = patternString.toCharArray();
        this.types = new int[tokens.length];
        this.weight = parse();
    }

    /**
     * Tokenize and compact the pattern string into {@link #tokens} and {@link #types}
     * and compute a heuristic weight to rank pattern specificity.
     * @return the computed weight
     */
    private float parse() {
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
                // Separator character not escaped
                esc = false;
                types[i] = SEPARATOR_TYPE; // type 9: separator
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

        float weight = 0.0f;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == EOT_TYPE) {
                break;
            }
            weight += ((i + 1) * types[i] / 10f);
        }
        return weight;
    }

    /**
     * Return the configured segment separator or {@link Character#MIN_VALUE} if none.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Internal accessor for the compacted token characters.
     */
    protected char[] getTokens() {
        return tokens;
    }

    /**
     * Internal accessor for token types aligned to {@link #getTokens()}.
     */
    protected int[] getTypes() {
        return types;
    }

    /**
     * Return a weight indicating relative pattern specificity (higher is more specific).
     */
    public float getWeight() {
        return weight;
    }

    /**
     * If the pattern matches then returns true.
     * @param input the character sequence to be matched
     * @return true, if successful
     */
    public boolean matches(CharSequence input) {
        return WildcardMatcher.matches(this, input);
    }

    /**
     * Erase the characters that corresponds to the wildcard, and
     * returns collect only the remaining characters.
     * In other words, only it remains for the wildcard character.
     * @param input the character sequence to be masked
     * @return the masked string
     */
    public String mask(CharSequence input) {
        return WildcardMasker.mask(this, input);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WildcardPattern that)) {
            return false;
        }
        return (Objects.equals(that.toString(), patternString) &&
                Objects.equals(that.getSeparator(), getSeparator()));
    }

    @Override
    public int hashCode() {
        int result = this.patternString.hashCode();
        result = 31 * result + separator;
        return result;
    }

    @Override
    public String toString() {
        return patternString;
    }

    /**
     * Compile the given text into a {@link WildcardPattern} with no segment separator.
     * @param patternString the wildcard pattern text
     * @return a compiled pattern
     */
    @NonNull
    public static WildcardPattern compile(String patternString) {
        return new WildcardPattern(patternString);
    }

    /**
     * Compile the given text into a {@link WildcardPattern} with the supplied separator.
     * @param patternString the wildcard pattern text
     * @param separator the segment separator character
     * @return a compiled pattern
     */
    @NonNull
    public static WildcardPattern compile(String patternString, char separator) {
        return new WildcardPattern(patternString, separator);
    }

    /**
     * Quick check whether the supplied text contains any wildcard characters ('*', '?', '+').
     * @param patternString the text to inspect
     * @return {@code true} if wildcards are present; {@code false} otherwise
     */
    public static boolean hasWildcards(String patternString) {
        if (StringUtils.hasLength(patternString)) {
            char[] ca = patternString.toCharArray();
            for (char c : ca) {
                switch (c) {
                    case STAR_CHAR, QUESTION_CHAR, PLUS_CHAR -> {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
