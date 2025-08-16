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
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Utility to apply a wildcard pattern as a mask over an input sequence.
 * <p>
 * Given a compiled {@link WildcardPattern} and an input, this class erases the
 * characters that are consumed by wildcard tokens and collects only the
 * remaining literal characters in their original order. If the input does not
 * match the pattern, {@code null} is returned.
 * </p>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>The behavior is separator-aware: when a separator is configured on the
 *   pattern, single-segment wildcards ("*", "?", "+") will not cross the
 *   separator while the double-star token ("**") may span across it.</li>
 *   <li>This class does not allocate intermediate Strings while masking; it
 *   writes into a character buffer and returns a new String when matching
 *   finishes successfully.</li>
 * </ul>
 */
public class WildcardMasker {

    /**
     * Apply the supplied {@link WildcardPattern} to the given input and return a
     * masked string that retains only the characters matched by literal tokens.
     * Characters consumed by wildcard tokens are omitted.
     * <p>If the input does not conform to the pattern, this method returns
     * {@code null}.</p>
     * @param pattern the precompiled wildcard pattern (must not be {@code null})
     * @param input the input character sequence to be masked (must not be {@code null})
     * @return the masked string if the input matches; {@code null} otherwise
     */
    @Nullable
    public static String mask(WildcardPattern pattern, CharSequence input) {
        Assert.notNull(pattern, "pattern must not be null");
        Assert.notNull(input, "input must not be null");
        char[] tokens = pattern.getTokens();
        int[] types = pattern.getTypes();
        char separator = pattern.getSeparator();

        int tlen = tokens.length;
        int clen = input.length();

        char[] masks = new char[clen];
        char c;

        int tidx = 0;
        int cidx = 0;

        int trng1;
        int trng2;
        int ttemp;

        int crng1;
        int crng2;
        int ctmp;

        int scnt1;
        int scnt2;

        while (tidx < tlen && cidx < clen) {
            if (types[tidx] == WildcardPattern.LITERAL_TYPE) {
                if (tokens[tidx++] != input.charAt(cidx++)) {
                    return null;
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
                            c = input.charAt(cidx);
                            if (c == separator) {
                                break;
                            }
                            masks[cidx] = c;
                        }
                        tidx++;
                    } else {
                        // *suffix
                        ttemp = trng1;
                        do {
                            c = input.charAt(cidx);
                            if (c == separator) {
                                return null;
                            }
                            if (tokens[ttemp] != c) {
                                ttemp = trng1;
                                masks[cidx] = c;
                            } else {
                                ttemp++;
                            }
                            cidx++;
                        } while (ttemp < trng2 && cidx < clen);
                        if (ttemp < trng2) {
                            return null;
                        }
                        tidx = trng2;
                    }
                } else {
                    for (; cidx < clen; cidx++) {
                        c = input.charAt(cidx);
                        if (c == separator) {
                            break;
                        }
                        masks[cidx] = c;
                    }
                    tidx++;
                }
            } else if (types[tidx] == WildcardPattern.STAR_STAR_TYPE) {
                if (separator != Character.MIN_VALUE) {
                    trng1 = -1;
                    trng2 = -1;
                    for (ttemp = tidx + 1; ttemp < tlen; ttemp++) {
                        if (trng1 == -1) {
                            if (types[ttemp] == WildcardPattern.LITERAL_TYPE) {
                                trng1 = ttemp;
                            }
                        } else {
                            if (types[ttemp] != WildcardPattern.LITERAL_TYPE) {
                                trng2 = ttemp - 1;
                                break;
                            }
                        }
                    }
                    if (trng1 > -1 && trng2 > -1) {
                        crng2 = cidx;
                        ttemp = trng1;
                        while (ttemp <= trng2 && crng2 < clen) {
                            c = input.charAt(crng2);
                            if (c != tokens[ttemp]) {
                                ttemp = trng1;
                                masks[crng2] = c;
                            } else {
                                ttemp++;
                            }
                            crng2++;
                        }
                        if (ttemp <= trng2) {
                            tidx = trng2;
                            if (cidx > 0) {
                                cidx--;
                                masks[cidx] = 0; //erase
                            }
                        } else {
                            cidx = crng2;
                            tidx = trng2 + 1;
                        }
                    } else {
                        tidx++;
                        scnt1 = 0;
                        for (ttemp = tidx; ttemp < tlen; ttemp++) {
                            if (types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
                                scnt1++;
                            }
                        }
                        if (scnt1 > 0) {
                            crng1 = cidx;
                            crng2 = clen;
                            scnt2 = 0;
                            while (crng2 > 0 && crng1 <= crng2--) {
                                if (input.charAt(crng2) == separator) {
                                    scnt2++;
                                }
                                if (scnt1 == scnt2) {
                                    break;
                                }
                            }
                            if (scnt1 == scnt2) {
                                cidx = crng2;
                                for (ctmp = crng1; ctmp < crng2; ctmp++) {
                                    masks[ctmp] = input.charAt(ctmp);
                                }
                            }
                        } else {
                            for (; cidx < clen; cidx++) {
                                masks[cidx] = input.charAt(cidx);
                            }
                        }
                    }
                } else {
                    for (ctmp = cidx; ctmp < clen; ctmp++) {
                        masks[ctmp] = input.charAt(ctmp);
                    }
                    cidx = clen; //complete
                    tidx++;
                }
            } else if (types[tidx] == WildcardPattern.QUESTION_TYPE) {
                if (tidx > tlen - 1
                        || types[tidx + 1] != WildcardPattern.LITERAL_TYPE
                        || tokens[tidx + 1] != input.charAt(cidx)) {
                    if (separator != Character.MIN_VALUE) {
                        if (input.charAt(cidx) != separator) {
                            masks[cidx] = input.charAt(cidx);
                            cidx++;
                        }
                    } else {
                        masks[cidx] = input.charAt(cidx);
                        cidx++;
                    }
                }
                tidx++;
            } else if (types[tidx] == WildcardPattern.PLUS_TYPE) {
                if (separator != Character.MIN_VALUE) {
                    if (input.charAt(cidx) == separator) {
                        return null;
                    }
                }
                masks[cidx] = input.charAt(cidx);
                cidx++;
                tidx++;
            } else if (types[tidx] == WildcardPattern.SEPARATOR_TYPE) {
                if (tokens[tidx] != input.charAt(cidx)) {
                    return null;
                }
                if (tidx > 0 && cidx > 0 && masks[cidx - 1] > 0
                        && (types[tidx - 1] == WildcardPattern.STAR_STAR_TYPE
                            || types[tidx - 1] == WildcardPattern.STAR_TYPE)) {
                    masks[cidx] = input.charAt(cidx);
                }
                tidx++;
                cidx++;
            } else if (types[tidx] == WildcardPattern.EOT_TYPE) {
                break;
            } else {
                tidx++;
            }
        }

        if (cidx < clen) {
            if (cidx == 0 && tlen > 0 && types[0] == WildcardPattern.STAR_STAR_TYPE) {
                for (int end = 0; end < clen; end++) {
                    if (input.charAt(end) != separator) {
                        if (end > 0) {
                            return input.subSequence(end, clen).toString();
                        }
                        break;
                    }
                }
                return input.toString();
            }
            return null;
        }

        if (tidx < tlen) {
            for (ttemp = tidx; ttemp < tlen; ttemp++) {
                if (types[ttemp] == WildcardPattern.LITERAL_TYPE
                        || types[ttemp] == WildcardPattern.PLUS_TYPE
                        || types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
                    return null;
                }
            }
        }

        StringBuilder sb = new StringBuilder(masks.length);
        for (char mask : masks) {
            if (mask > 0) {
                sb.append(mask);
            }
        }
        if (types[0] == WildcardPattern.STAR_STAR_TYPE || types[0] == WildcardPattern.STAR_TYPE) {
            for (int end = 0; end < sb.length(); end++) {
                if (sb.charAt(end) != separator) {
                    if (end > 0) {
                        sb.delete(0, end);
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

}
