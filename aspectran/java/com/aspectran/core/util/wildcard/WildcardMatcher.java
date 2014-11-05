package com.aspectran.core.util.wildcard;


public class WildcardMatcher {

	private WildcardPattern pattern;
	
	private char[] charArray;
	
	private int[] separatorFlags;
	
	private int separatorCount;
	
	private int separatorIndex;
	
	public WildcardMatcher(WildcardPattern pattern) {
		this.pattern = pattern;
	}
	
	public boolean matches(String str) {
		separatorCount = 0;
		separatorIndex = 0;

		if(str == null) {
			charArray = null;
			separatorFlags = null;
			return false;
		}
		
		charArray = str.toCharArray();
		separatorFlags = new int[charArray.length];
		
		boolean result = matches(pattern, charArray, separatorFlags);
		
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
	
	public WildcardMatcher first() {
		separatorIndex = 0;
		return this;
	}

	public WildcardMatcher last() {
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
		
		if(separatorCount == 0)
			return String.copyValueOf(charArray);
		
		if(group < 0 || group > separatorCount)
			throw new IndexOutOfBoundsException();
		
		int offset = 0;
		int count = -1;
		
		if(group == 0) {
			for(int i = 0; i < separatorFlags.length; i++) {
				if(separatorFlags[i] == 1) {
					count = i;
					break;
				}
			}

			if(count == -1)
				count = separatorFlags.length;
		} else {
			for(int i = 0; i < separatorFlags.length; i++) {
				if(separatorFlags[i] == group) {
					offset = i + 1;
				} else if(offset > 0 && separatorFlags[i] == group + 1) {
					count = i - offset;
					break;
				}
			}
			
			if(offset > 0 && count == -1)
				count = separatorFlags.length - offset;
		}
		
		if(count == -1)
			return null;
		else if(count == 0)
			return "";
		else
			return String.copyValueOf(charArray, offset, count);
	}
	
	public int getSeparatorCount() {
		return separatorCount;
	}
	
	public WildcardPattern getWildcardPattern() {
		return pattern;
	}
	
	public static boolean matches(WildcardPattern pattern, String str) {
		return matches(pattern, str.toCharArray(), null);
	}
	
	public static boolean matches(WildcardPattern pattern, char[] ca) {
		return matches(pattern, ca, null);
	}
	
	/**
	 * @param pattern
	 * @param ca
	 * @param separatorFlags
	 * @return
	 */
	private static boolean matches(WildcardPattern pattern, char[] ca, int[] separatorFlags) {
		char[] tokens = pattern.getTokens();
		int[] types = pattern.getTypes();
		char[] separators = pattern.getSeparators();
		
		int tokensLength = tokens.length;
		int caLength = ca.length;

		int sepaLength = separators == null ? -1 : separators.length;
		int sepaCount = 0;

		int tokenIndex = 0;
		int caIndex = 0;
		
		//System.out.println("tokens length: " + tokensLength);
		//System.out.println("ca length: " + caLength);
		
		while(tokenIndex < tokensLength && caIndex < caLength) {
			//System.out.println("token index=" + tokenIndex + ", token=" + tokens[tokenIndex] + ", type=" + types[tokenIndex]);
			//System.out.println("  ca index=" + caIndex + ", char=" + ca[caIndex]);
			
			if(types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
				if(tokens[tokenIndex++] != ca[caIndex++])
					return false;
			} else if(types[tokenIndex] == WildcardPattern.STAR_TYPE) {
				int t1 = tokenIndex + 1;
				int t2 = t1;
				for(; t2 < tokensLength; t2++) {
					if(types[t2] == WildcardPattern.EOT_TYPE || types[t2] != WildcardPattern.LITERAL_TYPE)
						break;
				}
				
				//System.out.println("*t1=" + t1 + ", t2=" + t2);
				
				if(t1 == t2) {
					caIndex++;
					tokenIndex++;
				} else {
					int t = t1;
					do {
						if(tokens[t] != ca[caIndex++])
							t = t1;
						else
							t++;
					} while(t < t2 && caIndex < caLength);
					//System.out.println("*t=" + t + ", t2=" + t2 + ", caIndex=" + caIndex);
					if(t < t2)
						return false;
					tokenIndex = t2;;
					/*
					if(eot || t2 == tokensLength - 1) {
						int c = caLength - 1;
						while(t2 > t1 && c >= caIndex) {
							//System.out.println(tokens[end - 1] + " : " + ca[c]);
							if(tokens[--t2] != ca[c--])
								return false;
						}
						if(t2 != t1)
							return false;
						else
							return true;
					} else {
						int t = t1;
						for(; t < t2 && caIndex < caLength; caIndex++) {
							if(tokens[t] != ca[caIndex])
								t = t1;
							else
								t++;
						}
						if(t < t2)
							return false;
						//tokenIndex = end;
					}
					*/
				}
			} else if(types[tokenIndex] == WildcardPattern.STAR_STAR_TYPE) {
				if(sepaLength > 0) {
					//"com.**.*scope.**.*Xml*"
					//"com.**.**.?scope*.**.*Xml*"
					//"com//j/j/*scope.**.*Xml*"
					int t1 = -1;
					int t2 = -1;
					for(int n = tokenIndex + 1; n < tokensLength; n++) {
						if(t1 == -1) {
							if(types[n] == WildcardPattern.LITERAL_TYPE) {
								t1 = n;
							}
						} else {
							if(types[n] == WildcardPattern.SEPARATOR_TYPE) {
								t2 = n + sepaLength - 1;
								break;
							} else if(types[n] != WildcardPattern.LITERAL_TYPE) {
								t2 = n - 1;
								break;
							}
						}
					}
					if(t1 > -1 && t2 > -1) {
						if(t1 > tokenIndex + 3 && types[t1 - 1] == WildcardPattern.SEPARATOR_TYPE) {
							t1 -= sepaLength;
							//System.out.println("!#t1: " + t1 + ", type: " + types[t1] + ", token: " + tokens[t1]);
						}
						int c1 = caIndex;
						int t = t1;
						while(t <= t2 && c1 < caLength) {
							if(ca[c1] != tokens[t]) {
								t = t1;
							} else {
								t++;
							}
							c1++;
						}
						if(t <= t2) {
							//System.out.println("!return#c1: " + c1 + ", caLength: " + caLength);
							//System.out.println("!return#c1: " + c1 + ", ca: " + (c1 >= caLength ? "" : ca[c1]));
							//System.out.println("!return#t2: " + t2 + ", type: " + types[t2] + ", token: " + tokens[t2]);
							return false;
						}
						c1--;
						//System.out.println("#c1: " + c1 + ", ca: " + ca[c1]);
						//System.out.println("#t2: " + t2 + ", type: " + types[t2] + ", token: " + tokens[t2]);
						int caIndex2 = caIndex;
						if(types[t1] == WildcardPattern.SEPARATOR_TYPE) {
							caIndex = c1 + 1;
							tokenIndex = t2 + 1;
							//System.out.println("##c1: " + c1 + ", ca: " + ca[c1]);
							//System.out.println("##tokenIndex: " + tokenIndex + ", type: " + types[tokenIndex] + ", token: " + tokens[tokenIndex]);
						} else {
							if(sepaLength == 1) {
								if(types[t2] == WildcardPattern.SEPARATOR_TYPE)
									c1--;
								for(; c1 >= caIndex; c1--) {
									if(ca[c1] == separators[0]) {
										caIndex = c1;
										break;
									}
								}
								//if(c1 < caIndex) {
									//System.out.println("###caIndex: " + caIndex + ", caIndex: " + ca[caIndex]);
									//System.out.println("###tokenIndex: " + tokenIndex + ", type: " + types[tokenIndex] + ", token: " + tokens[tokenIndex]);
								//}
							} else {
								//System.out.println("####t1: " + t1 + ", t2: " + t2 + ", c1: " + c1);
								while(types[t2] != WildcardPattern.SEPARATOR_TYPE && t2 > t1) {
									t2--;
									c1--;
								}
								int s = sepaLength - 1;
								while(s >= 0 && c1 < caLength) {
									if(ca[c1] == separators[s])
										s--;
									else
										s = sepaLength - 1;
									c1--;
								}
								if(s == -1) {
									caIndex = c1 + 1;
								}
								//System.out.println("####t1: " + t1 + ", t2: " + t2 + ", c1: " + c1);
							}
							tokenIndex++;
							if(caIndex == caIndex2 && types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE)
								tokenIndex++;
							//System.out.println("######caIndex: " + caIndex + ", caIndex2: " + caIndex2 + ", tokenIndex: " + tokenIndex);
						}
						//System.out.println("pass#caIndex: " + caIndex + ", caIndex: " + ca[caIndex]);
						//System.out.println("pass#tokenIndex: " + tokenIndex + ", type: " + types[tokenIndex] + ", token: " + tokens[tokenIndex]);

						if(separatorFlags != null && caIndex2 < caIndex) {
							if(sepaLength == 1) {
								for(int k = caIndex - 1; k >= caIndex2; k--) {
									if(ca[k] == separators[0])
										separatorFlags[k] = ++sepaCount;
								}
							} else {
								int k = caIndex - 1;
								while(k >= caIndex2) {
									int s = sepaLength - 1;
									while(s >= 0) {
										if(ca[k] == separators[s])
											s--;
										else
											s = sepaLength - 1;
										k--;
									}
									if(s == -1) {
										++sepaCount;
										for(s = 0; s < sepaLength; s++) {
											separatorFlags[k + s] = sepaCount;
										}
									}
								}
							}
						}
					} else {
						tokenIndex++;
					}
				} else {
					caIndex = caLength; //complete
					tokenIndex++;
				}
				/*
				if(sepaLength > 0) {
					int c = caLength - 1;
					if(sepaLength == 1) {
						while(c > caIndex) {
							if(ca[c] == separators[0])
								break;
							c--;
						}
						if(c > caIndex) {
							if(separatorFlags != null) {
								for(int k = c - 1; k > caIndex; k--) {
									if(ca[k] == separators[0])
										separatorFlags[k] = ++sepaCount;
								}
							}
							caIndex = c;
						}
					} else {
						while(c > caIndex) {
							int s = sepaLength - 1;
							while(s >= 0) {
								if(ca[c] == separators[s])
									s--;
								else
									s = sepaLength - 1;
								c--;
							}
							if(s == -1) {
								c++;
								break;
							}
						}
						if(c > caIndex) {
							if(separatorFlags != null) {
								separatorFlags[c] = ++sepaCount;
								int k = c - 1;
								while(k > caIndex) {
									int s = sepaLength - 1;
									while(s >= 0) {
										if(ca[k] == separators[s])
											s--;
										else
											s = sepaLength - 1;
										k--;
									}
									if(s == -1) {
										++sepaCount;
										for(s = 1; s <= sepaLength; s++) {
											separatorFlags[s] = sepaCount;
										}
									}
								}
							}
							caIndex = c;
						}
					}
				} else {
					caIndex = caLength; //complete
				}
				tokenIndex++;
				*/
			} else if(types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
				if(tokenIndex > tokensLength - 1 ||
						types[tokenIndex + 1] != WildcardPattern.LITERAL_TYPE ||
						tokens[tokenIndex + 1] != ca[caIndex]) {
					if(sepaLength > 0) {
						if(sepaLength == 1) {
							if(ca[caIndex] != separators[0])
								caIndex++;
						} else {
							int s = sepaLength - 1;
							if(caIndex + s < caLength) {
								for(; s >= 0; s--) {
									if(ca[caIndex + s] != separators[s])
										break;
								}
							}
							if(s != -1)
								caIndex++;
						}
					} else {
						caIndex++;
					}
				}
				tokenIndex++;
			} else if(types[tokenIndex] == WildcardPattern.PLUS_TYPE) {
				if(sepaLength > 0) {
					if(sepaLength == 1) {
						if(ca[caIndex] == separators[0])
							return false;
					} else {
						if(caIndex + sepaLength - 1 < caLength) {
							int s = sepaLength - 1;
							for(; s >= 0; s--) {
								if(ca[caIndex + s] != separators[s])
									break;
							}
							if(s == -1)
								return false;
						}
					}
				}
				caIndex++;
				tokenIndex++;
			} else if(types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE) {
				if(sepaLength == 1) {
					if(tokens[tokenIndex++] != ca[caIndex++])
						return false;
					if(separatorFlags != null)
						separatorFlags[caIndex - 1] = ++sepaCount;
				} else {
					if(caIndex + sepaLength > caLength)
						return false;
					for(int s = 0; s < sepaLength; s++) {
						if(tokens[tokenIndex++] != ca[caIndex++])
							return false;
					}
					if(separatorFlags != null) {
						++sepaCount;
						for(int s = sepaLength - 1; s >= 0; s--) {
							separatorFlags[caIndex - s - 1] = sepaCount;
						}
					}
				}
			} else if(types[tokenIndex] == WildcardPattern.EOT_TYPE) {
				break;
			} else {
				tokenIndex++;
			}
		}
		
		if(tokenIndex < tokensLength) {
			for(int i = tokenIndex; i < tokensLength; i++) {
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
		String str = "?c?om][**][x?][*scope][**][*XmlBean*";
		WildcardPattern pattern = WildcardPattern.compile(str, "][");
		
		int i = 0;
		for(char c : pattern.getTokens()) {
			System.out.print(i);
			System.out.print(": ");
			System.out.print(c);
			System.out.print(", ");
			System.out.println(pattern.getTypes()[i]);
			i++;
		}
		
		WildcardMatcher matcher = new WildcardMatcher(pattern);
		//boolean result = matcher.matches("/aaa*/mm/nn/bbZZ.txt");
		//boolean result = matcher.matches("com.aspectran.test.SampleTestBean");
		boolean result = matcher.matches("com][x][scope][b1][b2][*XmlBean000");
		
		System.out.println("result: " + result);
		System.out.println("separatorCount: " + matcher.getSeparatorCount());
		
		System.out.println("pattern: " + str);
		while(matcher.hasNext()) {
			System.out.println(" -" + matcher.next());
		}
	}

}
