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

import com.aspectran.core.util.StringUtils;

/**
 * The Class WildcardMatcherTest.
 */
public class WildcardMatcherTest {

	private WildcardPatternTest pattern;
	
	private CharSequence input;
	
	private int[] separatorFlags;
	
	private int separatorCount = -1;
	
	private int separatorIndex;
	
	public WildcardMatcherTest(WildcardPatternTest pattern) {
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
		
		
//		for(int i : separatorFlags) {
//			System.out.println("separatorFlag: " + i);
//			
//		}
		
		
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
	
	public WildcardMatcherTest first() {
		separatorIndex = 0;
		return this;
	}

	public WildcardMatcherTest last() {
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
		//System.out.println("group: " + group);
		
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
			
			//System.out.println("#1 start: " + start + ", offset: " + offset);
			
			if(start > 0 && offset == -1) {
				offset = separatorFlags.length;
				//System.out.println("#2 start: " + start + ", offset: " + offset);
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
	
	public WildcardPatternTest getWildcardPattern() {
		return pattern;
	}
	
	public static boolean matches(WildcardPatternTest pattern, CharSequence input) {
		return matches(pattern, input, null);
	}
	
	/**
	 * @param pattern
	 * @param input
	 * @param separatorFlags
	 * @return
	 */
	private static boolean matches(WildcardPatternTest pattern, CharSequence input, int[] separatorFlags) {
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
		
		//System.out.println("tokens length: " + tokensLength);
		//System.out.println("input length: " + caLength);
		
		while(tindex < tlength && cindex < clength) {
			System.out.println("token index=" + tindex + ", token=" + tokens[tindex] + ", type=" + types[tindex]);
			System.out.println("  input index=" + cindex + ", char=" + input.charAt(cindex));
			
			if(types[tindex] == WildcardPattern.LITERAL_TYPE) {
				if(tokens[tindex++] != input.charAt(cindex++)) {
					return false;
				}
			} else if(types[tindex] == WildcardPattern.STAR_TYPE) {
				trange1 = tindex + 1;
				if(trange1 < tlength) {
					trange2 = trange1;
					//System.out.println("*trange1=" + trange1 + " tokens[trange1]: " + tokens[trange1] + ", trange2=" + trange2 + " tokens[trange2]: " + tokens[trange2]);
					for(; trange2 < tlength; trange2++) {
						if(types[trange2] == WildcardPattern.EOT_TYPE || types[trange2] != WildcardPattern.LITERAL_TYPE)
							break;
					}
					
					//System.out.println("*trange1=" + trange1 + " tokens[trange1]: " + tokens[trange1] + ", trange2=" + trange2 + " tokens[trange2]: " + tokens[trange2]);
					
					if(trange1 == trange2) {
						// prefix*
						for(; cindex < clength; cindex++) {
							System.out.println("----star0 cindex=" + cindex + ", char=" + input.charAt(cindex));
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
						
						System.out.println("----star1 ttemp=" + ttemp + ", t2=" + trange2 + ", cindex=" + cindex);
						
						if(ttemp < trange2)
							return false;
						
						tindex = trange2;
					}
				} else {
					for(; cindex < clength; cindex++) {
						System.out.println("----star2 cindex=" + cindex + ", char=" + input.charAt(cindex));
						if(input.charAt(cindex) == separator)
							break;
					}
					tindex++;
				}
			} else if(types[tindex] == WildcardPattern.STAR_STAR_TYPE) {
				// a.b.**.*d.**.f
				System.out.println("---------------star star start");
				if(separator > 0) {
					//"com.**.*scope.**.*Xml*"
					//"com.**.**.?scope*.**.*Xml*"
					//"com//j/j/*scope.**.*Xml*"
					trange1 = -1;
					trange2 = -1;
					for(ttemp = tindex + 1; ttemp < tlength; ttemp++) {
						System.out.println("---------------types[" + ttemp + "]: " + types[ttemp]);
						if(trange1 == -1) {
							if(types[ttemp] == WildcardPattern.LITERAL_TYPE) {
								trange1 = ttemp;
							}
						} else {
							System.out.println("---------------+trange1: " + trange1);
							if(types[ttemp] != WildcardPattern.LITERAL_TYPE) {
								System.out.println("----no literal type-----------+ttemp: " + ttemp + ", tokens[ttemp]: " + tokens[ttemp]);
								trange2 = ttemp - 1;
								break;
							}
						}
					}
					System.out.println("---------------@t1: " + trange1 + " @t2: " + trange2);
					System.out.println("---------------@tindex: " + tindex);
					if(trange1 > -1 && trange2 > -1) {
						// a.**.d.?
						// 2 + 3 = 5
						//if(t1 > tindex + 3 && types[t1 - 1] == WildcardPattern.SEPARATOR_TYPE) {
						//	t1--;
						//	System.out.println("!#t1: " + t1 + ", type: " + types[t1] + ", token: " + tokens[t1]);
						//}
						crange1 = cindex;
						crange2 = cindex;
						ttemp = trange1;
						while(ttemp <= trange2 && crange2 < clength) {
							if(input.charAt(crange2++) != tokens[ttemp]) {
								ttemp = trange1;
							} else {
								System.out.println("---------------uu@t: " + ttemp + " char: " + tokens[ttemp] + ", t2: " + trange2 + ", c2: " + (crange2-1) + ", c2-char: " + input.charAt(crange2-1));
								ttemp++;
							}
						}
						System.out.println("---------------@t: " + ttemp + " @t2: " + trange2 + " @c2: " + crange2);
						if(ttemp <= trange2) {
							System.out.println("!return# trange1: " + trange1 + ", type: " + types[trange1] + ", token: " + tokens[trange1]);
							System.out.println("!return# trange2: " + trange2 + ", type: " + types[trange2] + ", token: " + tokens[trange2]);
							System.out.println("!return# crange1: " + crange1 + ", char: " + input.charAt(crange1));
							System.out.println("!return# crange2: " + crange2 + ", char: " + (crange2 >= clength ? "over" : input.charAt(crange2)));
							tindex = trange2;
							if(cindex > 0)
								cindex--;
						} else {
							//System.out.println("!!! c1: " + crange1 + ", c1-char: " + input.charAt(crange1) + ", c2: " + crange2 + ", c2-char: " + input.charAt(crange2));
							if(separatorFlags != null && crange1 < crange2) {
								for(ctemp = crange1; ctemp < crange2; ctemp++) {
									System.out.println("!!!separator ctemp: " + ctemp + ", char: " + input.charAt(ctemp));
									if(input.charAt(ctemp) == separator) {
										separatorFlags[ctemp] = ++sepaCount;
										System.out.println("!!!separator ctemp: " + ctemp + ", separatorFlags[ctemp]: " + separatorFlags[ctemp]);
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
							System.out.println("!!! ttemp: " + ttemp + ", tokens[ttemp]: " + tokens[ttemp]);
							if(types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
								scnt1++;
							}
						}
						System.out.println("!!! scnt1: " + scnt1);
						if(scnt1 > 0) {
							crange1 = cindex;
							crange2 = clength;
							scnt2 = 0;
							while(crange2 > 0 && crange1 <= crange2--) {
								System.out.println("!!! scnt2: " + scnt2 + ", crange2: " + crange2);
								if(input.charAt(crange2) == separator)
									scnt2++;
								if(scnt1 == scnt2)
									break;
							}
							System.out.println("!!! scnt2: " + scnt2);
							if(scnt1 == scnt2) {
								cindex = crange2;
								System.out.println("!!! cindex: " + cindex);
								if(separatorFlags != null) {
									while(crange1 < crange2) {
										if(input.charAt(crange1) == separator) {
											separatorFlags[crange1] = ++sepaCount;
										}
										System.out.println("!!! c1: " + crange1 + ", separatorFlags[c1]: " + separatorFlags[crange1]);
										crange1++;
									}
								}
							}
						}
						
						System.out.println("--------------- #end tokens[" + tindex + "]: " + tokens[tindex]);
					}
				} else {
					cindex = clength; //complete
					tindex++;
					System.out.println("---------------star star complete cindex: " + cindex);
				}
				System.out.println("!# cindex: " + cindex + ", type: " + types[tindex]);
				System.out.println("!# tindex: " + tindex + ", type: " + types[tindex] + " cindex: " + cindex + ", char: " + (cindex >= clength ? "over" : input.charAt(cindex)));
				System.out.println("---------------star star end");
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
				//System.out.println("**빠짐 tokens[tindex]: " + tokens[tindex] + ", input.charAt(cindex): " + input.charAt(cindex));
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
		
		System.out.println("---------------------------------------------------");
		System.out.println("tindex: " + tindex + ", tlength: " + tlength);
		System.out.println("cindex: " + cindex + ", clength: " + clength);
		System.out.println("---------------------------------------------------");

		if(cindex < clength) {
			return false;
		}

		if(tindex < tlength) {
			for(ttemp = tindex; ttemp < tlength; ttemp++) {
				System.out.println("types[" + ttemp + "]: " + types[ttemp]);
				if(types[ttemp] == WildcardPattern.LITERAL_TYPE ||
						types[ttemp] == WildcardPattern.PLUS_TYPE ||
						types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static void main(String argv[]) {
		//String str = "/aaa\\*/**/bb*.txt**";
		//String str = "**.Sample*Test*Bean";
		//String str = "?c?om.**.x?.*scope.**.*XmlBean*";
		//String str2 = str;
		//String str = "?c?om.**.x?.*scope.**.*XmlBean*.**.*Action?";
		//String str2 = "com.x.scope.main.x1xscope.1234XmlBean5678.p1.p2.p3.endAction";
		String str = ".com.aspectran.**.service.**.*Action?";
		String str2 = ".com.aspectran.a.service.c.dAction1";
		//String str = "*A";
		//String str2 = "common/MyTransletA";
		WildcardPatternTest pattern = WildcardPatternTest.compile(str, '.');
		
		int i = 0;
		for(char c : pattern.getTokens()) {
			System.out.print(i);
			System.out.print(": ");
			System.out.print(c);
			System.out.print(", ");
			System.out.println(pattern.getTypes()[i]);
			i++;
		}
		
		WildcardMatcherTest matcher = new WildcardMatcherTest(pattern);
		//boolean result = matcher.matches("/aaa*/mm/nn/bbZZ.txt");
		//boolean result = matcher.matches("com.aspectran.test.SampleTestBean");
		boolean result = matcher.matches(str2);
		
		System.out.println("---------------------------------------------------");
		System.out.println("result: " + result);
		System.out.println("separatorCount: " + matcher.getSeparatorCount());
		
		System.out.println("pattern: " + str);
		while(matcher.hasNext()) {
			System.out.println(" -" + matcher.next());
		}
	}

}
