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

import com.aspectran.core.util.StringUtils;

/**
 * Checks whether a string matches a given wildcard pattern.
 */
public class WildcardMatcher {

    private WildcardPattern pattern;

    private CharSequence input;

    private int[] separatorFlags;

    private int separatorCount = -1;

    private int separatorIndex;

    public WildcardMatcher(WildcardPattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Checks whether a string matches a given wildcard pattern.
     *
     * @param input the input string
     * @return {@code true} if string matches the pattern, otherwise {@code false}
     */
    public boolean matches(CharSequence input) {
        separatorCount = -1;
        separatorIndex = 0;

        if (input == null) {
            this.input = null;
            separatorFlags = null;
            return false;
        }

        this.input = input;
        separatorFlags = new int[input.length()];

        boolean result = matches(pattern, input, separatorFlags);

        if (result) {
            for (int i = separatorFlags.length - 1; i >= 0; i--) {
                if (separatorFlags[i] > 0) {
                    separatorCount = separatorFlags[i];
                    break;
                }
            }
        }

        return result;
    }

    public int separate(CharSequence input) {
        separatorCount = -1;
        separatorIndex = 0;

        if (input == null) {
            this.input = null;
            separatorFlags = null;
            return 0;
        }

        this.input = input;
        int len = input.length();
        char separator = pattern.getSeparator();
        separatorFlags = new int[len];

        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == separator) {
                separatorFlags[i] = ++separatorCount;
            }
        }

        return separatorCount;
    }

    public WildcardMatcher first() {
        separatorIndex = 0;
        return this;
    }

    public WildcardMatcher last() {
        if (separatorCount > -1) {
            separatorIndex = separatorCount;
        }
        return this;
    }

    public boolean hasNext() {
        return separatorIndex <= separatorCount;
    }

    public boolean hasPrevious() {
        return separatorIndex >= 0;
    }

    public String next() {
        if (separatorIndex > separatorCount) {
            return null;
        }
        return find(separatorIndex++);
    }

    public String previous() {
        if (separatorIndex < 0) {
            return null;
        }
        return find(separatorIndex--);
    }

    public String find() {
        return find(separatorIndex);
    }

    public String find(int group) {
        if (separatorCount == 0) {
            if (input == null) {
                return null;
            }
            return input.toString();
        }

        if (group < 0 || group > separatorCount) {
            throw new IndexOutOfBoundsException();
        }

        int start = 0;
        int offset = -1;

        if (group == 0) {
            for (int i = 0; i < separatorFlags.length; i++) {
                if (separatorFlags[i] == 1) {
                    offset = i;
                    break;
                }
            }

            if (offset == -1) {
                offset = separatorFlags.length;
            }
        } else {
            for (int i = 0; i < separatorFlags.length; i++) {
                if (separatorFlags[i] == group) {
                    start = i + 1;
                } else if (start > 0 && separatorFlags[i] == group + 1) {
                    offset = i;
                    break;
                }
            }

            if (start > 0 && offset == -1) {
                offset = separatorFlags.length;
            }
        }

        if (offset == -1) {
            return null;
        } else if (offset == 0) {
            return StringUtils.EMPTY;
        } else {
            return input.subSequence(start, offset).toString();
        }
    }

    public int getSeparatorCount() {
        return separatorCount;
    }

    public WildcardPattern getWildcardPattern() {
        return pattern;
    }

    /**
     * Checks whether a string matches a given wildcard pattern.
     *
     * @param pattern the pattern to match
     * @param input the input string
     * @return {@code true} if string matches the pattern, otherwise {@code false}
     */
    public static boolean matches(WildcardPattern pattern, CharSequence input) {
        return matches(pattern, input, null);
    }

    private static boolean matches(WildcardPattern pattern, CharSequence input, int[] separatorFlags) {
        char[] tokens = pattern.getTokens();
        int[] types = pattern.getTypes();
        char sepa = pattern.getSeparator();

        int tlen = tokens.length;
        int clen = input.length();

        int sepaCnt = 0;

        int tidx = 0;
        int cidx = 0;

        int trng1;
        int trng2;
        int ttmp;

        int crng1;
        int crng2;
        int ctmp;

        int scnt1;
        int scnt2;

        while (tidx < tlen && cidx < clen) {
            if (types[tidx] == WildcardPattern.LITERAL_TYPE) {
                if (tokens[tidx++] != input.charAt(cidx++)) {
                    return false;
                }
            } else if (types[tidx] == WildcardPattern.STAR_TYPE) {
                trng1 = tidx + 1;
                if (trng1 < tlen) {
                    trng2 = trng1;
                    for (; trng2 < tlen; trng2++) {
                        if (types[trng2] == WildcardPattern.EOT_TYPE
                                || types[trng2] != WildcardPattern.LITERAL_TYPE) {
                            break;
                        }
                    }
                    if (trng1 == trng2) {
                        // prefix*
                        for (; cidx < clen; cidx++) {
                            if (input.charAt(cidx) == sepa) {
                                break;
                            }
                        }
                        tidx++;
                    } else {
                        // *suffix
                        ttmp = trng1;
                        do {
                            if (input.charAt(cidx) == sepa) {
                                return false;
                            }
                            if (tokens[ttmp] != input.charAt(cidx++)) {
                                ttmp = trng1;
                            } else {
                                ttmp++;
                            }
                        } while (ttmp < trng2 && cidx < clen);
                        if (ttmp < trng2) {
                            return false;
                        }
                        tidx = trng2;
                    }
                } else {
                    for (; cidx < clen; cidx++) {
                        if (input.charAt(cidx) == sepa) {
                            break;
                        }
                    }
                    tidx++;
                }
            } else if (types[tidx] == WildcardPattern.STAR_STAR_TYPE) {
                if (sepa > 0) {
                    trng1 = -1;
                    trng2 = -1;
                    for (ttmp = tidx + 1; ttmp < tlen; ttmp++) {
                        if (trng1 == -1) {
                            if (types[ttmp] == WildcardPattern.LITERAL_TYPE) {
                                trng1 = ttmp;
                            }
                        } else {
                            if (types[ttmp] != WildcardPattern.LITERAL_TYPE) {
                                trng2 = ttmp - 1;
                                break;
                            }
                        }
                    }
                    if (trng1 > -1 && trng2 > -1) {
                        crng1 = cidx;
                        crng2 = cidx;
                        ttmp = trng1;
                        while (ttmp <= trng2 && crng2 < clen) {
                            if (input.charAt(crng2++) != tokens[ttmp]) {
                                ttmp = trng1;
                            } else {
                                ttmp++;
                            }
                        }
                        if (ttmp <= trng2) {
                            tidx = trng2;
                            if (cidx > 0) {
                                cidx--;
                            }
                        } else {
                            if (separatorFlags != null && crng1 < crng2) {
                                for (ctmp = crng1; ctmp < crng2; ctmp++) {
                                    if (input.charAt(ctmp) == sepa) {
                                        separatorFlags[ctmp] = ++sepaCnt;
                                    }
                                }
                            }
                            cidx = crng2;
                            tidx = trng2 + 1;
                        }
                    } else {
                        tidx++;
                        scnt1 = 0;
                        for (ttmp = tidx; ttmp < tlen; ttmp++) {
                            if (types[ttmp] == WildcardPattern.SEPARATOR_TYPE) {
                                scnt1++;
                            }
                        }
                        if (scnt1 > 0) {
                            crng1 = cidx;
                            crng2 = clen;
                            scnt2 = 0;
                            while (crng2 > 0 && crng1 <= crng2--) {
                                if (input.charAt(crng2) == sepa) {
                                    scnt2++;
                                }
                                if (scnt1 == scnt2) {
                                    break;
                                }
                            }
                            if (scnt1 == scnt2) {
                                cidx = crng2;
                                if (separatorFlags != null) {
                                    while (crng1 < crng2) {
                                        if (input.charAt(crng1) == sepa) {
                                            separatorFlags[crng1] = ++sepaCnt;
                                        }
                                        crng1++;
                                    }
                                }
                            }
                        } else {
                            cidx = clen; //complete
                        }
                    }
                } else {
                    cidx = clen; //complete
                    tidx++;
                }
            } else if (types[tidx] == WildcardPattern.QUESTION_TYPE) {
                if (tidx > tlen - 1
                        || types[tidx + 1] != WildcardPattern.LITERAL_TYPE
                        || tokens[tidx + 1] != input.charAt(cidx)) {
                    if (sepa > 0) {
                        if (input.charAt(cidx) != sepa) {
                            cidx++;
                        }
                    } else {
                        cidx++;
                    }
                }
                tidx++;
            } else if (types[tidx] == WildcardPattern.PLUS_TYPE) {
                if (sepa > 0) {
                    if (input.charAt(cidx) == sepa) {
                        return false;
                    }
                }
                cidx++;
                tidx++;
            } else if (types[tidx] == WildcardPattern.SEPARATOR_TYPE) {
                if (tokens[tidx++] != input.charAt(cidx++)) {
                    return false;
                }
                if (separatorFlags != null) {
                    separatorFlags[cidx - 1] = ++sepaCnt;
                }
            } else if (types[tidx] == WildcardPattern.EOT_TYPE) {
                break;
            } else {
                tidx++;
            }
        }

        if (cidx < clen) {
            return false;
        }

        if (tidx < tlen) {
            for (ttmp = tidx; ttmp < tlen; ttmp++) {
                if (types[ttmp] == WildcardPattern.LITERAL_TYPE
                        || types[ttmp] == WildcardPattern.PLUS_TYPE
                        || types[ttmp] == WildcardPattern.SEPARATOR_TYPE) {
                    return false;
                }
            }
        }

        return true;
    }

}
