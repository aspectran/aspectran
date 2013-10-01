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
	
	public boolean hasWildcards(String str) {
		char[] ca = str.toCharArray();
		
		for(int i = 0; i < ca.length; i++) {
			if(ca[i] == WildcardPattern.STAR_CHAR ||
					ca[i] == WildcardPattern.QUESTION_CHAR ||
					ca[i] == WildcardPattern.PLUS_CHAR)
				return true;
		}
		
		return false;
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
		
		for(; tokenIndex < tokensLength && caIndex < caLength;) {
			//System.out.println("token index=" + tokenIndex + ", token=" + tokens[tokenIndex] + ", type=" + types[tokenIndex]);
			//System.out.println("token index=" + tokenIndex + ", ca index=" + caIndex + ", char=" + ca[caIndex]);
			
			if(types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
				if(tokens[tokenIndex++] != ca[caIndex++])
					return false;
			} else if(types[tokenIndex] == WildcardPattern.STAR_TYPE) {
				int start = ++tokenIndex;
				int end = start;
				boolean eot = false;
				for(; end < tokensLength; end++) {
					if(types[end] == WildcardPattern.EOT_TYPE) {
						eot = true;
						break;
					} else if(types[end] != WildcardPattern.LITERAL_TYPE)
						break;
				}
				//System.out.println("start=" + start + ", end=" + end + ", eot=" + eot);
				if(end > start) {
					if(eot || end == tokensLength - 1) {
						int c = caLength - 1;
						while(end > start && c >= caIndex) {
							//System.out.println(tokens[end - 1] + " : " + ca[c]);
							if(tokens[--end] != ca[c--])
								return false;
						}
						if(end != start)
							return false;
						else
							return true;
					} else {
						int t = start;
						for(; t < end && caIndex < caLength; caIndex++) {
							if(tokens[t] != ca[caIndex])
								t = start;
							else
								t++;
						}
						if(t < end)
							return false;
						tokenIndex = end;
					}
				}
			} else if(types[tokenIndex] == WildcardPattern.DOUBLE_STAR_TYPE) {
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
			} else if(types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
				if(sepaLength > 0) {
					if(sepaLength == 1) {
						if(ca[caIndex] != separators[0])
							caIndex++;
					} else {
						int s = sepaLength - 1;
						for(; s >= 0; s--) {
							if(caIndex + s >= caLength)
								break;
							if(ca[caIndex + s] != separators[s])
								break;
						}
						if(s != -1)
							caIndex++;
					}
				} else {
					caIndex++;
				}
				tokenIndex++;
			} else if(types[tokenIndex] == WildcardPattern.PLUS_TYPE) {
				if(sepaLength > 0) {
					if(sepaLength == 1) {
						if(ca[caIndex] == separators[0])
							return false;
					} else {
						int s = sepaLength - 1;
						for(; s >= 0; s--) {
							if(caIndex + s >= caLength)
								break;
							if(ca[caIndex + s] != separators[s])
								break;
						}
						if(s == -1)
							return false;
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
					for(int i = 0; i < sepaLength; i++) {
						if(tokens[tokenIndex++] != ca[caIndex])
							return false;
						caIndex++;
					}
					if(separatorFlags != null)
						separatorFlags[caIndex - sepaLength] = ++sepaCount;
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
		String str = "com.aspectran.test.**.Sample*Test*Bean";
		WildcardPattern pattern = WildcardPattern.compile(str, ".");
		
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
		boolean result = matcher.matches("com.aspectran.test.**.Sample*Test*Bean");
		
		System.out.println("result: " + result);
		System.out.println("separatorCount: " + matcher.getSeparatorCount());
		
		while(matcher.hasNext()) {
			System.out.println(" -" + matcher.next());
		}
	}

}
