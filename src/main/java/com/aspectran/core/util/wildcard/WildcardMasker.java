/**
 * Copyright 2008-2017 Juho Jeong
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
 * The Class WildcardMasker.
 */
public class WildcardMasker {

	/**
	 * Erase the characters that corresponds to the wildcard, and
	 * returns collect only the remaining characters.
	 * In other words, only it remains for the wildcard character.
	 *
	 * @param pattern the pattern
	 * @param input the input
	 * @return the string
	 */
	public static String mask(WildcardPattern pattern, CharSequence input) {
		char[] tokens = pattern.getTokens();
		int[] types = pattern.getTypes();
		char separator = pattern.getSeparator();

		int tlength = tokens.length;
		int clength = input.length();

		char[] masks = new char[clength];
		char c;

		int tindex = 0;
		int cindex = 0;
		
		int trange1;
		int trange2;
		int ttemp;
		
		int crange1;
		int crange2;
		int ctemp;
		
		int scnt1;
		int scnt2;
		
		while (tindex < tlength && cindex < clength) {
			if (types[tindex] == WildcardPattern.LITERAL_TYPE) {
				if (tokens[tindex++] != input.charAt(cindex++)) {
					return null;
				}
			} else if (types[tindex] == WildcardPattern.STAR_TYPE) {
				trange1 = tindex + 1;
				if (trange1 < tlength) {
					trange2 = trange1;
					for (; trange2 < tlength; trange2++) {
						if (types[trange2] == WildcardPattern.EOT_TYPE
								|| types[trange2] != WildcardPattern.LITERAL_TYPE) {
							break;
						}
					}
					if (trange1 == trange2) {
						// prefix*
						for (; cindex < clength; cindex++) {
							c = input.charAt(cindex);
							if (c == separator) {
								break;
							}
							masks[cindex] = c;
						}
						tindex++;
					} else {
						// *suffix
						ttemp = trange1;
						do {
							c = input.charAt(cindex);
							if (c == separator) {
								return null;
							}
							if (tokens[ttemp] != c) {
								ttemp = trange1;
								masks[cindex] = c;
							} else {
								ttemp++;
							}
							cindex++;
						} while (ttemp < trange2 && cindex < clength);
						if (ttemp < trange2) {
							return null;
						}
						tindex = trange2;
					}
				} else {
					for (; cindex < clength; cindex++) {
						c = input.charAt(cindex);
						if (c == separator) {
							break;
						}
						masks[cindex] = c;
					}
					tindex++;
				}
			} else if (types[tindex] == WildcardPattern.STAR_STAR_TYPE) {
				if (separator > 0) {
					trange1 = -1;
					trange2 = -1;
					for (ttemp = tindex + 1; ttemp < tlength; ttemp++) {
						if (trange1 == -1) {
							if (types[ttemp] == WildcardPattern.LITERAL_TYPE) {
								trange1 = ttemp;
							}
						} else {
							if (types[ttemp] != WildcardPattern.LITERAL_TYPE) {
								trange2 = ttemp - 1;
								break;
							}
						}
					}
					if (trange1 > -1 && trange2 > -1) {
						crange2 = cindex;
						ttemp = trange1;
						while (ttemp <= trange2 && crange2 < clength) {
							c = input.charAt(crange2);
							if (c != tokens[ttemp]) {
								ttemp = trange1;
								masks[crange2] = c;
							} else {
								ttemp++;
							}
							crange2++;
						}
						if (ttemp <= trange2) {
							tindex = trange2;
							if (cindex > 0) {
								cindex--;
								masks[cindex] = 0; //erase
							}
						} else {
							cindex = crange2;
							tindex = trange2 + 1;
						}
					} else {
						tindex++;
						scnt1 = 0;
						for (ttemp = tindex; ttemp < tlength; ttemp++) {
							if (types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
								scnt1++;
							}
						}
						if (scnt1 > 0) {
							crange1 = cindex;
							crange2 = clength;
							scnt2 = 0;
							while (crange2 > 0 && crange1 <= crange2--) {
								if (input.charAt(crange2) == separator) {
									scnt2++;
								}
								if (scnt1 == scnt2) {
									break;
								}
							}
							if (scnt1 == scnt2) {
								cindex = crange2;
								for (ctemp = crange1; ctemp < crange2; ctemp++) {
									masks[ctemp] = input.charAt(ctemp);
								}
							}
						} else {
							for (; cindex < clength; cindex++) {
								masks[cindex] = input.charAt(cindex);
							}
						}
					}
				} else {
					for (ctemp = cindex; ctemp < clength; ctemp++) {
						masks[ctemp] = input.charAt(ctemp);
					}
					cindex = clength; //complete
					tindex++;
				}
			} else if (types[tindex] == WildcardPattern.QUESTION_TYPE) {
				if (tindex > tlength - 1
						|| types[tindex + 1] != WildcardPattern.LITERAL_TYPE
						|| tokens[tindex + 1] != input.charAt(cindex)) {
					if (separator > 0) {
						if (input.charAt(cindex) != separator) {
							masks[cindex] = input.charAt(cindex);
							cindex++;
						}
					} else {
						masks[cindex] = input.charAt(cindex);
						cindex++;
					}
				}
				tindex++;
			} else if (types[tindex] == WildcardPattern.PLUS_TYPE) {
				if (separator > 0) {
					if (input.charAt(cindex) == separator) {
						return null;
					}
				}
				masks[cindex] = input.charAt(cindex);
				cindex++;
				tindex++;
			} else if (types[tindex] == WildcardPattern.SEPARATOR_TYPE) {
				if (tokens[tindex] != input.charAt(cindex)) {
					return null;
				}
				if (tindex > 0 && cindex > 0 && masks[cindex - 1] > 0
						&& (types[tindex - 1] == WildcardPattern.STAR_STAR_TYPE
							|| types[tindex - 1] == WildcardPattern.STAR_TYPE)) {
					masks[cindex] = input.charAt(cindex);
				}
				tindex++;
				cindex++;
			} else if (types[tindex] == WildcardPattern.EOT_TYPE) {
				break;
			} else {
				tindex++;
			}
		}
		
		if (cindex < clength) {
			if(cindex == 0 && tlength > 0 && types[0] == WildcardPattern.STAR_STAR_TYPE) {
				for (int end = 0; end < clength; end++) {
					if(input.charAt(end) != separator) {
						if(end > 0) {
							return input.subSequence(end, clength).toString();
						}
						break;
					}
				}
				return input.toString();
			}
			return null;
		}

		if (tindex < tlength) {
			for (ttemp = tindex; ttemp < tlength; ttemp++) {
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

		if(types[0] == WildcardPattern.STAR_STAR_TYPE || types[0] == WildcardPattern.STAR_TYPE) {
			for (int end = 0; end < sb.length(); end++) {
				if(sb.charAt(end) != separator) {
					if(end > 0) {
						sb.delete(0, end);
					}
					break;
				}
			}
		}

		return sb.toString();
	}

}
