/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.util.wildcard;

public class WildcardPatternTest {

	protected static final char ESCAPE_CHAR = '\\';
	
	protected static final char SPACE_CHAR = ' ';
	
	protected static final char STAR_CHAR = '*';
	
	protected static final char QUESTION_CHAR = '?';
	
	protected static final char PLUS_CHAR = '+';
	
	protected static final int EOT_TYPE = -2;

	protected static final int SKIP_TYPE = -1;

	protected static final int LITERAL_TYPE = 0;

	protected static final int STAR_TYPE = 1;

	protected static final int STAR_STAR_TYPE = 2;

	protected static final int QUESTION_TYPE = 3;

	protected static final int PLUS_TYPE = 4;

	protected static final int SEPARATOR_TYPE = 9;
	
	private char[] separator;
	
	private char[] tokens;

	private int[] types;
	
	public WildcardPatternTest(String patternString) {
		parse(patternString);
	}
	
	public WildcardPatternTest(String patternString, char separator) {
		this.separator = new char[] { separator };
		
		parse(patternString);
	}

	public WildcardPatternTest(String patternString, String separator) {
		if(separator != null && separator.length() > 0)
			this.separator = separator.toCharArray();
		
		parse(patternString);
	}

	private void parse(String patternString) {
		tokens = patternString.toCharArray();
		types = new int[tokens.length];

		boolean star = false;
		boolean esc = false;
		int ptype = SKIP_TYPE;
		int pindex = 0;

		for(int i = 0; i < tokens.length; i++) {
			if(tokens[i] == STAR_CHAR) {
				if(esc) {
					esc = false;
				} else {
					if(star) {
						types[i - 1] = SKIP_TYPE;
						types[i] = STAR_STAR_TYPE; // type 2: double star
						star = false;
					} else {
						types[i] = STAR_TYPE; // type 1: star
						star = true;
					}
				}
				if(ptype == QUESTION_TYPE && types[i] == STAR_TYPE) {
					types[pindex] = SKIP_TYPE;
				}
			} else if(tokens[i] == QUESTION_CHAR) {
				if(esc) {
					types[i - 1] = SKIP_TYPE;
					esc = false;
				} else {
					types[i] = QUESTION_TYPE; // type 3: question
				}
				if(ptype == STAR_TYPE && types[i] == QUESTION_TYPE) {
					types[i] = SKIP_TYPE;
				}
			} else if(tokens[i] == PLUS_CHAR) {
				if(esc) {
					types[i - 1] = SKIP_TYPE;
					esc = false;
				} else {
					types[i] = PLUS_TYPE; // type 4: plus
				}
				if(ptype == STAR_TYPE && types[i] == PLUS_CHAR) {
					types[i] = SKIP_TYPE;
				}
			} else if(tokens[i] == ESCAPE_CHAR) {
				types[i] = SKIP_TYPE;
				esc = true;
			} else {
				if(esc)
					types[i - 1] = LITERAL_TYPE;
			}

			if(tokens[i] != STAR_CHAR && star)
				star = false;
			
			if(types[i] != SKIP_TYPE) {
				ptype = types[i];
				pindex = i;
			}
		}

		if(separator != null) {
			int sepa = 0;
			int skip = 0;

			for(int i = 0; i < tokens.length; i++) {
				if(types[i] > SKIP_TYPE) {
					if(tokens[i] == separator[sepa]) {
						sepa++;
					}
					if(sepa == separator.length) {
						if(sepa == 1) {
							types[i] = SEPARATOR_TYPE; // type 9: separator
						} else {          
							for(int j = i - sepa + skip + 1; j <= i; j++) {
								if(types[j] != SKIP_TYPE)
									types[j] = SEPARATOR_TYPE; // type 9: separator
							}
						}
						sepa = 0;
					}
				}

				if(sepa > 0) {
					if(types[i] == SKIP_TYPE)
						skip++;
					else if(tokens[i] != separator[sepa - 1])
						sepa = 0;
				}
			}
		}
		
		for(int i = 0, j = 0; i < tokens.length; i++) {
			if(types[i] == SKIP_TYPE) {
				j++; 
				tokens[i] = SPACE_CHAR;
				types[i] = EOT_TYPE;
			} else if(j > 0) {
				tokens[i - j] = tokens[i];
				types[i - j] = types[i];
				tokens[i] = SPACE_CHAR;
				types[i] = EOT_TYPE;
			}
		}
	}

	public char[] getSeparator() {
		return separator;
	}

	protected char[] getTokens() {
		return tokens;
	}
	
	protected int[] getTypes() {
		return types;
	}
	
	public boolean matches(String str) {
		return WildcardMatcherTest.matches(this, str);
	}
	
	public static WildcardPatternTest compile(String patternString) {
		return new WildcardPatternTest(patternString);
	}
	
	public static WildcardPatternTest compile(String patternString, char separator) {
		return new WildcardPatternTest(patternString, separator);
	}
	
	public static WildcardPatternTest compile(String patternString, String separator) {
		return new WildcardPatternTest(patternString, separator);
	}
	
	public static boolean hasWildcards(String str) {
		char[] ca = str.toCharArray();
		
		for(int i = 0; i < ca.length; i++) {
			if(ca[i] == STAR_CHAR ||
					ca[i] == QUESTION_CHAR ||
					ca[i] == PLUS_CHAR)
				return true;
		}
		
		return false;
	}
	
	public static void main(String argv[]) {
		//String str = "\\aaa\\*\\**\\bb*.txt**";
		//String str = "**/bb*";
		String str = "com.**.scope.**.*Xml*";
		WildcardPatternTest pattern = WildcardPatternTest.compile(str, ".");
		
		int i = 0;
		for(char c : pattern.getTokens()) {
			System.out.print(i);
			System.out.print(": ");
			System.out.print(c);
			System.out.print(", ");
			System.out.println(pattern.getTypes()[i]);
			i++;
		}
		
		//WildcardMatcher matcher = new WildcardMatcher(pattern);
		boolean result = pattern.matches(str);
		
		System.out.println("Result: " + result);
	}

}
