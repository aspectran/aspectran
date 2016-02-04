/**
 * Copyright 2008-2016 Juho Jeong
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
 * The Class WildcardMatcher.
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
	
	public boolean matches(CharSequence input) {
		separatorCount = -1;
		separatorIndex = 0;

		if(input == null) {
			this.input = null;
			separatorFlags = null;
			return false;
		}
		
		this.input = input;
		separatorFlags = new int[input.length()];
		
		boolean result = matches(pattern, input, separatorFlags);
		
		if(result) {
			for(int i = separatorFlags.length - 1; i >= 0; i--) {
				if(separatorFlags[i] > 0) {
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

		if(input == null) {
			this.input = null;
			separatorFlags = null;
			return 0;
		}
		
		this.input = input;
		int len = input.length();
		char separator = pattern.getSeparator();
		separatorFlags = new int[len];
		
		for(int i = 0; i < len; i++) {
			if(input.charAt(i) == separator) {
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
		if(separatorCount > -1)
			separatorIndex = separatorCount;
		return this;
	}
	
	public boolean hasNext() {
		return separatorIndex <= separatorCount;
	}

	public boolean hasPrev() {
		return separatorIndex >= 0;
	}
	
	public String next() {
		if(separatorIndex > separatorCount)
			return null;

		return find(separatorIndex++);
	}
	
	public String prev() {
		if(separatorIndex < 0)
			return null;
		
		return find(separatorIndex--);
	}

	public String find() {
		return find(separatorIndex);
	}
	
	public String find(int group) {
		if(separatorCount == 0) {
			if(input == null)
				return null;
			
			return input.toString();
		}
		
		if(group < 0 || group > separatorCount)
			throw new IndexOutOfBoundsException();
		
		int start = 0;
		int offset = -1;
		
		if(group == 0) {
			for(int i = 0; i < separatorFlags.length; i++) {
				if(separatorFlags[i] == 1) {
					offset = i;
					break;
				}
			}

			if(offset == -1)
				offset = separatorFlags.length;
		} else {
			for(int i = 0; i < separatorFlags.length; i++) {
				if(separatorFlags[i] == group) {
					start = i + 1;
				} else if(start > 0 && separatorFlags[i] == group + 1) {
					offset = i;
					break;
				}
			}
			
			if(start > 0 && offset == -1) {
				offset = separatorFlags.length;
			}
		}
		
		if(offset == -1)
			return null;
		else if(offset == 0)
			return StringUtils.EMPTY;
		else
			return input.subSequence(start, offset).toString();
	}
	
	public int getSeparatorCount() {
		return separatorCount;
	}
	
	public WildcardPattern getWildcardPattern() {
		return pattern;
	}
	
	public static boolean matches(WildcardPattern pattern, CharSequence input) {
		return matches(pattern, input, null);
	}
	
	private static boolean matches(WildcardPattern pattern, CharSequence input, int[] separatorFlags) {
		char[] tokens = pattern.getTokens();
		int[] types = pattern.getTypes();
		char separator = pattern.getSeparator();
		
		int tlength = tokens.length;
		int clength = input.length();

		int sepaCount = 0;

		int tindex = 0;
		int cindex = 0;
		
		int trange1 = 0;
		int trange2 = 0;
		int ttemp = 0;
		
		int crange1 = 0;
		int crange2 = 0;
		int ctemp = 0;
		
		int scnt1 = 0;
		int scnt2 = 0;
		
		while(tindex < tlength && cindex < clength) {
			if(types[tindex] == WildcardPattern.LITERAL_TYPE) {
				if(tokens[tindex++] != input.charAt(cindex++)) {
					return false;
				}
			} else if(types[tindex] == WildcardPattern.STAR_TYPE) {
				trange1 = tindex + 1;
				if(trange1 < tlength) {
					trange2 = trange1;
					for(; trange2 < tlength; trange2++) {
						if(types[trange2] == WildcardPattern.EOT_TYPE || types[trange2] != WildcardPattern.LITERAL_TYPE)
							break;
					}
					if(trange1 == trange2) {
						// prefix*
						for(; cindex < clength; cindex++) {
							if(input.charAt(cindex) == separator)
								break;
						}
						tindex++;
					} else {
						// *suffix
						ttemp = trange1;
						do {
							if(input.charAt(cindex) == separator)
								return false;
							if(tokens[ttemp] != input.charAt(cindex++))
								ttemp = trange1;
							else
								ttemp++;
						} while(ttemp < trange2 && cindex < clength);
						if(ttemp < trange2)
							return false;
						tindex = trange2;
					}
				} else {
					for(; cindex < clength; cindex++) {
						if(input.charAt(cindex) == separator)
							break;
					}
					tindex++;
				}
			} else if(types[tindex] == WildcardPattern.STAR_STAR_TYPE) {
				if(separator > 0) {
					trange1 = -1;
					trange2 = -1;
					for(ttemp = tindex + 1; ttemp < tlength; ttemp++) {
						if(trange1 == -1) {
							if(types[ttemp] == WildcardPattern.LITERAL_TYPE) {
								trange1 = ttemp;
							}
						} else {
							if(types[ttemp] != WildcardPattern.LITERAL_TYPE) {
								trange2 = ttemp - 1;
								break;
							}
						}
					}
					if(trange1 > -1 && trange2 > -1) {
						crange1 = cindex;
						crange2 = cindex;
						ttemp = trange1;
						while(ttemp <= trange2 && crange2 < clength) {
							if(input.charAt(crange2++) != tokens[ttemp]) {
								ttemp = trange1;
							} else {
								ttemp++;
							}
						}
						if(ttemp <= trange2) {
							tindex = trange2;
							if(cindex > 0)
								cindex--;
						} else {
							if(separatorFlags != null && crange1 < crange2) {
								for(ctemp = crange1; ctemp < crange2; ctemp++) {
									if(input.charAt(ctemp) == separator) {
										separatorFlags[ctemp] = ++sepaCount;
									}
								}
							}
							cindex = crange2;
							tindex = trange2 + 1;
						}
					} else {
						tindex++;

						scnt1 = 0;
						for(ttemp = tindex; ttemp < tlength; ttemp++) {
							if(types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
								scnt1++;
							}
						}
						if(scnt1 > 0) {
							crange1 = cindex;
							crange2 = clength;
							scnt2 = 0;
							while(crange2 > 0 && crange1 <= crange2--) {
								if(input.charAt(crange2) == separator)
									scnt2++;
								if(scnt1 == scnt2)
									break;
							}
							if(scnt1 == scnt2) {
								cindex = crange2;
								if(separatorFlags != null) {
									while(crange1 < crange2) {
										if(input.charAt(crange1) == separator) {
											separatorFlags[crange1] = ++sepaCount;
										}
										crange1++;
									}
								}
							}
						}
					}
				} else {
					cindex = clength; //complete
					tindex++;
				}
			} else if(types[tindex] == WildcardPattern.QUESTION_TYPE) {
				if(tindex > tlength - 1 ||
						types[tindex + 1] != WildcardPattern.LITERAL_TYPE ||
						tokens[tindex + 1] != input.charAt(cindex)) {
					if(separator > 0) {
						if(input.charAt(cindex) != separator)
							cindex++;
					} else {
						cindex++;
					}
				}
				tindex++;
			} else if(types[tindex] == WildcardPattern.PLUS_TYPE) {
				if(separator > 0) {
					if(input.charAt(cindex) == separator)
						return false;
				}
				cindex++;
				tindex++;
			} else if(types[tindex] == WildcardPattern.SEPARATOR_TYPE) {
				if(tokens[tindex++] != input.charAt(cindex++)) {
					return false;
				}
				if(separatorFlags != null)
					separatorFlags[cindex - 1] = ++sepaCount;
			} else if(types[tindex] == WildcardPattern.EOT_TYPE) {
				break;
			} else {
				tindex++;
			}
		}
		
		if(cindex < clength) {
			return false;
		}

		if(tindex < tlength) {
			for(ttemp = tindex; ttemp < tlength; ttemp++) {
				if(types[ttemp] == WildcardPattern.LITERAL_TYPE ||
						types[ttemp] == WildcardPattern.PLUS_TYPE ||
						types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
					return false;
				}
			}
		}
		
		return true;
	}

}
