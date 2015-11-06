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

/**
 * The Class WildcardMatcherTest.
 */
public class WildcardMaskerTest {

	/**
	 * Erase the characters that corresponds to the wildcard, and returns collect only the remaining characters.
	 * In other words, only it remains for the wildcard character.
	 *
	 * @param pattern the pattern
	 * @param input the input
	 * @return the string
	 */
	public static String mask(WildcardPatternTest pattern, CharSequence input) {
		char[] tokens = pattern.getTokens();
		int[] types = pattern.getTypes();
		char separator = pattern.getSeparator();

		int tlength = tokens.length;
		int clength = input.length();

		char[] masks = new char[clength];
		char c;

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
					return null;
				}
				//masks[cindex] = input.charAt(cindex);
			} else if(types[tindex] == WildcardPattern.STAR_TYPE) {
				trange1 = tindex + 1;
				if(trange1 < tlength) {
					trange2 = trange1;
					System.out.println("*trange1=" + trange1 + " tokens[trange1]: " + tokens[trange1] + ", trange2=" + trange2 + " tokens[trange2]: " + tokens[trange2]);
					for(; trange2 < tlength; trange2++) {
						System.out.println("star-1 trange2: " + trange2 + ", types[trange2]:" + types[trange2]);
						if(types[trange2] == WildcardPattern.EOT_TYPE || types[trange2] != WildcardPattern.LITERAL_TYPE) {
							System.out.println("star-1 break trange2: " + trange2 + ", types[trange2]:" + types[trange2]);
							break;
						}
					}
					
					//System.out.println("*trange1=" + trange1 + " tokens[trange1]: " + tokens[trange1] + ", trange2=" + trange2 + " tokens[trange2]: " + tokens[trange2]);
					
					if(trange1 == trange2) {
						// prefix*
						for(; cindex < clength; cindex++) {
							System.out.println("----star0 cindex=" + cindex + ", char=" + input.charAt(cindex));
							c = input.charAt(cindex);
							if(c == separator)
								break;
							masks[cindex] = c;
							System.out.println("mask cindex: " + cindex + ", char: " + c);
						}
						tindex++;
					} else {
						// *suffix
						ttemp = trange1;
						System.out.println("----star1 ttemp=" + ttemp + ", trange2=" + trange2 + ", cindex=" + cindex);
						do {
							c = input.charAt(cindex);
							if(c == separator)
								return null;
							if(tokens[ttemp] != c) {
								ttemp = trange1;
								masks[cindex] = c;
								System.out.println("mask cindex: " + cindex + ", char: " + c);
							} else {
								ttemp++;
							}
							cindex++;
						} while(ttemp < trange2 && cindex < clength);
						
						System.out.println("----star1 ttemp=" + ttemp + ", t2=" + trange2 + ", cindex=" + cindex);
						
						if(ttemp < trange2)
							return null;
						
						tindex = trange2;
					}
				} else {
					for(; cindex < clength; cindex++) {
						System.out.println("----star2 cindex=" + cindex + ", char=" + input.charAt(cindex));
						c = input.charAt(cindex);
						if(c == separator)
							break;
						masks[cindex] = c;
						System.out.println("mask cindex: " + cindex + ", char: " + c + ", separator: " + separator);
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
							c = input.charAt(crange2);
							if(c != tokens[ttemp]) {
								ttemp = trange1;
								masks[crange2] = c;
							} else {
								System.out.println("---------------uu@t: " + ttemp + " char: " + tokens[ttemp] + ", t2: " + trange2 + ", c2: " + (crange2) + ", c2-char: " + input.charAt(crange2-1));
								ttemp++;
							}
							crange2++;
						}
						System.out.println("---------------@t: " + ttemp + " @t2: " + trange2 + " @c2: " + crange2);
						if(ttemp <= trange2) {
							System.out.println("!return# trange1: " + trange1 + ", type: " + types[trange1] + ", token: " + tokens[trange1]);
							System.out.println("!return# trange2: " + trange2 + ", type: " + types[trange2] + ", token: " + tokens[trange2]);
							System.out.println("!return# crange1: " + crange1 + ", char: " + input.charAt(crange1));
							System.out.println("!return# crange2: " + crange2 + ", char: " + (crange2 >= clength ? "over" : input.charAt(crange2)));
							tindex = trange2;
							if(cindex > 0) {
								cindex--;
								System.out.println("mask cindex: " + cindex + ", erase char: " + input.charAt(cindex));
								masks[cindex] = 0; //erase
							}
						} else {
							//System.out.println("!!! c1: " + crange1 + ", c1-char: " + input.charAt(crange1) + ", c2: " + crange2 + ", c2-char: " + input.charAt(crange2));
							cindex = crange2;
							tindex = trange2 + 1;
//							for(ctemp = crange1; ctemp < crange2; ctemp++) {
//								masks[ctemp] = input.charAt(ctemp);
//								System.out.println("-mask ctemp: " + ctemp + ", char: " + input.charAt(ctemp));
//							}
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
								for(ctemp = crange1; ctemp < crange2; ctemp++) {
									masks[ctemp] = input.charAt(ctemp);
									System.out.println("mask ctemp: " + ctemp + ", char: " + input.charAt(ctemp));
								}
								System.out.println("!!! cindex: " + cindex);
							}
						}
						
						System.out.println("--------------- #end tokens[" + tindex + "]: " + tokens[tindex]);
					}
				} else {
					for(ctemp = cindex; ctemp < clength; ctemp++) {
						masks[ctemp] = input.charAt(ctemp);
						System.out.println("mask ctemp: " + ctemp + ", char: " + input.charAt(ctemp));
					}
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
						if(input.charAt(cindex) != separator) {
							masks[cindex] = input.charAt(cindex);
							cindex++;
						}
					} else {
						masks[cindex] = input.charAt(cindex);
						cindex++;
					}
				}
				tindex++;
			} else if(types[tindex] == WildcardPattern.PLUS_TYPE) {
				if(separator > 0) {
					if(input.charAt(cindex) == separator)
						return null;
				}
				masks[cindex] = input.charAt(cindex);
				cindex++;
				tindex++;
			} else if(types[tindex] == WildcardPattern.SEPARATOR_TYPE) {
				//System.out.println("**빠짐 tokens[tindex]: " + tokens[tindex] + ", input.charAt(cindex): " + input.charAt(cindex));
				if(tokens[tindex] != input.charAt(cindex)) {
					return null;
				}
				if(tindex > 0) System.out.println("separator on types[tindex - 1]:" + types[tindex - 1]);
				if(tindex > 0 && (types[tindex - 1] == WildcardPattern.STAR_STAR_TYPE || types[tindex - 1] == WildcardPattern.STAR_TYPE)) {
					System.out.println("separator on types[tindex - 1] == WildcardPattern.STAR_STAR_TYPE");
					masks[cindex] = input.charAt(cindex);
				}
				tindex++;
				cindex++;
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
			return null;
		}

		if(tindex < tlength) {
			for(ttemp = tindex; ttemp < tlength; ttemp++) {
				System.out.println("types[" + ttemp + "]: " + types[ttemp]);
				if(types[ttemp] == WildcardPattern.LITERAL_TYPE ||
						types[ttemp] == WildcardPattern.PLUS_TYPE ||
						types[ttemp] == WildcardPattern.SEPARATOR_TYPE) {
					return null;
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < masks.length; i++) {
			if(masks[i] > 0)
				sb.append(masks[i]);
		}
		
		return sb.toString();
	}
	
	public static void main(String argv[]) {
		//String str = "/com/aspectran/example/HelloWorldAction.act";
		//String str2 = "**/*.act";
		String str = "example.helloworld.abc.b.HelloWorldAction";
		String str2 = "example.**.abc.**.*";
		WildcardPatternTest pattern = WildcardPatternTest.compile(str2, '.');
		
		int i = 0;
		for(char c : pattern.getTokens()) {
			System.out.print(i);
			System.out.print(": ");
			System.out.print(c);
			System.out.print(", ");
			System.out.println(pattern.getTypes()[i]);
			i++;
		}
		
		System.out.println(WildcardMaskerTest.mask(pattern, str));
		
	}

}
