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


public class WildcardMatcherTest {

	private WildcardPatternTest pattern;
	
	private CharSequence input;
	
	private int[] separatorFlags;
	
	private int separatorCount;
	
	private int separatorIndex;
	
	public WildcardMatcherTest(WildcardPatternTest pattern) {
		this.pattern = pattern;
	}
	
	public boolean matches(CharSequence input) {
		separatorCount = 0;
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
	
	public WildcardMatcherTest first() {
		separatorIndex = 0;
		return this;
	}

	public WildcardMatcherTest last() {
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
		
		int tokensLength = tokens.length;
		int inputLength = input.length();

		int sepaCount = 0;

		int tokenIndex = 0;
		int inputIndex = 0;
		
		//System.out.println("tokens length: " + tokensLength);
		//System.out.println("input length: " + caLength);
		
		while(tokenIndex < tokensLength && inputIndex < inputLength) {
			System.out.println("token index=" + tokenIndex + ", token=" + tokens[tokenIndex] + ", type=" + types[tokenIndex]);
			System.out.println("  input index=" + inputIndex + ", char=" + input.charAt(inputIndex));
			
			if(types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
				if(tokens[tokenIndex++] != input.charAt(inputIndex++)) {
					return false;
				}
			} else if(types[tokenIndex] == WildcardPattern.STAR_TYPE) {
				int t1 = tokenIndex + 1;
				int t2 = t1;
				for(; t2 < tokensLength; t2++) {
					if(types[t2] == WildcardPattern.EOT_TYPE || types[t2] != WildcardPattern.LITERAL_TYPE)
						break;
				}
				
				System.out.println("*t1=" + t1 + ", t2=" + t2);
				
				if(t1 == t2) {
					inputIndex++;
					tokenIndex++;
				} else {
					int t = t1;
					do {
						if(tokens[t] != input.charAt(inputIndex++))
							t = t1;
						else
							t++;
					} while(t < t2 && inputIndex < inputLength);
					
					System.out.println("start*t=" + t + ", t2=" + t2 + ", inputIndex=" + inputIndex);
					
					if(t < t2)
						return false;
					
					tokenIndex = t2;
				}
			} else if(types[tokenIndex] == WildcardPattern.STAR_STAR_TYPE) {
				// a.b.**.*d.**.f
				System.out.println("---------------star star start");
				if(separator > 0) {
					//"com.**.*scope.**.*Xml*"
					//"com.**.**.?scope*.**.*Xml*"
					//"com//j/j/*scope.**.*Xml*"
					int t1 = -1;
					int t2 = -1;
					for(int n = tokenIndex + 1; n < tokensLength; n++) {
						System.out.println("---------------types[" + n + "]: " + types[n]);
						if(t1 == -1) {
							if(types[n] == WildcardPattern.LITERAL_TYPE) {
								t1 = n;
							} else if(types[n] == WildcardPattern.SEPARATOR_TYPE) {
								//t1 = n;
								System.out.println("---------------*t1: " + t1);
							}
						} else {
							System.out.println("---------------+t1: " + t1);
							if(types[n] == WildcardPattern.SEPARATOR_TYPE) {
								t2 = n - 1;
								break;
							} else if(types[n] != WildcardPattern.LITERAL_TYPE) {
								System.out.println("----no literal type-----------+n: " + n + ", tokens[n]: " + tokens[n]);
								t2 = n - 1;
								break;
							}
						}
					}
					System.out.println("---------------@t1: " + t1 + " @t2: " + t2);
					System.out.println("---------------@tokenIndex: " + tokenIndex);
					if(t1 > -1 && t2 > -1) {
						// a.**.d.?
						// 2 + 3 = 5
						//if(t1 > tokenIndex + 3 && types[t1 - 1] == WildcardPattern.SEPARATOR_TYPE) {
						//	t1--;
						//	System.out.println("!#t1: " + t1 + ", type: " + types[t1] + ", token: " + tokens[t1]);
						//}
						int c1 = inputIndex;
						int c2 = c1;
						int t = t1;
						while(t <= t2 && c2 < inputLength) {
							if(input.charAt(c2++) != tokens[t]) {
								t = t1;
							} else {
								System.out.println("---------------uu@t: " + t + " char: " + tokens[t] + ", t2: " + t2 + ", c2: " + (c2-1) + ", c2-char: " + input.charAt(c2-1));
								t++;
							}
						}
						System.out.println("---------------@t: " + t + " @t2: " + t2 + " @c2: " + c2);
						if(t <= t2) {
							System.out.println("!return#c2: " + c2 + ", inputLength: " + inputLength);
							System.out.println("!return#c2: " + c2 + ", input: " + (c2 >= inputLength ? "" : input.charAt(c2)));
							System.out.println("!return#t2: " + t2 + ", type: " + types[t2] + ", token: " + tokens[t2]);
							return false;
						}
						inputIndex = c2;
						tokenIndex = t2 + 1;
						
						System.out.println("!!! c1: " + c1 + ", c1-char: " + input.charAt(c1) + ", c2: " + c2 + ", c2-char: " + input.charAt(c2));
						
						/*
						c1--;
						System.out.println("#c1: " + c1 + ", char: " + input.charAt(c1));
						System.out.println("#t2: " + t2 + ", type: " + types[t2] + ", token: " + tokens[t2]);
						System.out.println("#t1: " + t1 + ", type: " + types[t1] + ", token: " + tokens[t1]);
						int caIndex2 = inputIndex;
						if(types[t1] == WildcardPattern.SEPARATOR_TYPE) {
							inputIndex = c1 + 1;
							tokenIndex = t2 + 1;
							System.out.println("##c1: " + c1 + ", char: " + input.charAt(c1));
							System.out.println("##tokenIndex: " + tokenIndex + ", type: " + types[tokenIndex] + ", token: " + tokens[tokenIndex]);
						} else {
							if(types[t2] == WildcardPattern.SEPARATOR_TYPE)
								c1--;
							for(; c1 >= inputIndex; c1--) {
								System.out.println("for###c1: " + c1 + ", inputIndex: " + inputIndex + ", input.charAt(c1): " + input.charAt(c1));
								if(input.charAt(c1) == separator) {
									inputIndex = c1 + 1;
									break;
								}
							}
							System.out.println("###c1: " + c1 + ", inputIndex: " + inputIndex + ", input.charAt(c1): " + input.charAt(c1));
							if(c1 < inputIndex) {
								System.out.println("###inputIndex: " + inputIndex + ", char: " + input.charAt(inputIndex));
								System.out.println("###tokenIndex: " + tokenIndex + ", type: " + types[tokenIndex] + ", token: " + tokens[tokenIndex]);
							}
							tokenIndex = t2;
							System.out.println("######inputIndex: " + inputIndex + ", caIndex2: " + caIndex2 + ", tokenIndex: " + tokenIndex);
							if(inputIndex == caIndex2 && types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE)
								tokenIndex++;
							System.out.println("######inputIndex: " + inputIndex + ", caIndex2: " + caIndex2 + ", tokenIndex: " + tokenIndex);
						}
						System.out.println("pass#caIndex: " + inputIndex + ", char: " + input.charAt(inputIndex));
						System.out.println("pass#tokenIndex: " + tokenIndex + ", type: " + types[tokenIndex] + ", token: " + tokens[tokenIndex]);
*/
						if(separatorFlags != null && c1 < inputIndex) {
							for(int k = inputIndex - 1; k >= c1; k--) {
								if(input.charAt(k) == separator) {
									separatorFlags[k] = ++sepaCount;
									System.out.println("!!! k: " + k + ", separatorFlags[k]: " + separatorFlags[k]);
								}
							}
						}
					} else {
						tokenIndex++;

						int sc1 = 0;
						for(int n = tokenIndex; n < tokensLength; n++) {
							System.out.println("!!! n: " + n + ", tokens[n]: " + tokens[n]);
							if(types[n] == WildcardPattern.SEPARATOR_TYPE) {
								sc1++;
							}
						}
						System.out.println("!!! sc1: " + sc1);
						if(sc1 > 0) {
							int c1 = inputIndex;
							int c2 = inputLength;
							int sc2 = 0;
							while(c2 > c1) {
								c2--;
								if(input.charAt(c2) == separator)
									sc2++;
								if(sc1 == sc2)
									break;
							}
							System.out.println("!!! sc2: " + sc2);
							if(sc1 == sc2) {
								inputIndex = c2;
								System.out.println("!!! inputIndex: " + inputIndex);
								if(separatorFlags != null) {
									while(c1 < c2) {
										if(input.charAt(c1) == separator) {
											separatorFlags[c1] = ++sepaCount;
										}
										System.out.println("!!! c1: " + c1 + ", separatorFlags[c1]: " + separatorFlags[c1]);
										c1++;
									}
								}
							}
						}
						
						System.out.println("--------------- #end tokens[" + tokenIndex + "]: " + tokens[tokenIndex]);
					}
				} else {
					inputIndex = inputLength; //complete
					tokenIndex++;
					System.out.println("---------------star star complete inputIndex: " + inputIndex);
				}
				System.out.println("---------------star star end inputIndex: " + inputIndex);
			} else if(types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
				if(tokenIndex > tokensLength - 1 ||
						types[tokenIndex + 1] != WildcardPattern.LITERAL_TYPE ||
						tokens[tokenIndex + 1] != input.charAt(inputIndex)) {
					if(separator > 0) {
						if(input.charAt(inputIndex) != separator)
							inputIndex++;
					} else {
						inputIndex++;
					}
				}
				tokenIndex++;
			} else if(types[tokenIndex] == WildcardPattern.PLUS_TYPE) {
				if(separator > 0) {
					if(input.charAt(inputIndex) == separator)
						return false;
				}
				inputIndex++;
				tokenIndex++;
			} else if(types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE) {
				//System.out.println("**빠짐 tokens[tokenIndex]: " + tokens[tokenIndex] + ", input.charAt(inputIndex): " + input.charAt(inputIndex));
				if(tokens[tokenIndex++] != input.charAt(inputIndex++)) {
					return false;
				}
				if(separatorFlags != null)
					separatorFlags[inputIndex - 1] = ++sepaCount;
			} else if(types[tokenIndex] == WildcardPattern.EOT_TYPE) {
				break;
			} else {
				tokenIndex++;
			}
		}
		
		System.out.println("tokenIndex: " + tokenIndex + ", tokensLength: " + tokensLength);
		
		if(tokenIndex < tokensLength) {
			for(int i = tokenIndex; i < tokensLength; i++) {
				System.out.println("types[" + i + "]: " + types[i]);
				if(types[i] == WildcardPattern.LITERAL_TYPE ||
						types[i] == WildcardPattern.PLUS_TYPE ||
						types[i] == WildcardPattern.SEPARATOR_TYPE) {
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
		String str = "?c?om/**/x?/*scope/**/*XmlBean/**/*";
		String str2 = "com/x/scope/x1]xscope/111111111XmlBean/000/*";
		WildcardPatternTest pattern = WildcardPatternTest.compile(str, '/');
		
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
		
		System.out.println("result: " + result);
		System.out.println("separatorCount: " + matcher.getSeparatorCount());
		
		System.out.println("pattern: " + str);
		while(matcher.hasNext()) {
			System.out.println(" -" + matcher.next());
		}
	}

}
