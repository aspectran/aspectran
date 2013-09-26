package com.aspectran.core.util.wildcard;

public class WildcardMatcher {

	private WildcardPattern pattern;
	
	public WildcardMatcher(WildcardPattern pattern) {
		this.pattern = pattern;
	}
	
	public boolean matches(String str) {
		char[] tokens = pattern.getTokens();
		int[] types = pattern.getTypes();
		char[] separators = pattern.getSeparators();
		char[] ca = str.toCharArray();
		
		int tokensLength = tokens.length;
		int caLength = ca.length;

		int tokenIndex = 0;
		int caIndex = 0;
		
		System.out.println("tokens length: " + tokensLength);
		System.out.println("ca length: " + caLength);
		
		for(; tokenIndex < tokensLength && caIndex < caLength;) {
			if(types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
				if(tokens[tokenIndex++] != ca[caIndex++])
					return false;
			} else if(types[tokenIndex] == WildcardPattern.STAR_TYPE) {
				int start = ++tokenIndex;
				int end = start;
				boolean eof = false;
				for(; tokenIndex < tokensLength; tokenIndex++) {
					if(types[tokenIndex] == WildcardPattern.LITERAL_TYPE)
						end = tokenIndex + 1;
					else if(types[tokenIndex] == WildcardPattern.SKIP_TYPE) {
						eof = true;
						break;
					} else
						break;
				}
				if(end > start) {
					if(eof || end == tokensLength - 1) {
						int c = caLength - 1;
						for(; end > start && c >= caIndex; end--) {
							if(tokens[end] != ca[c--])
								return false;
						}
						if(end != start)
							return false;
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
					}
				}
			} else if(types[tokenIndex] == WildcardPattern.DOUBLE_STAR_TYPE) {
				if(separators != null) {
					int c = caLength - 1;
					if(separators.length == 1) {
						while(c > caIndex) {
							if(ca[c] == separators[0]) {
								caIndex = c + 1;
								break;
							}
							c--;
						}
					} else {
						while(c > caIndex) {
							int s = separators.length - 1;
							while(s >= 0) {
								if(ca[c--] == separators[s])
									s--;
								else
									s = separators.length - 1;
							}
							if(s == -1) {
								caIndex = c + separators.length + 1;
							}
						}
					}
				} else {
					caIndex = caLength; //complete
				}
				tokenIndex++;
			} else if(types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
				if(separators != null) {
					if(separators.length == 1) {
						if(ca[caIndex] != separators[0])
							caIndex++;
					} else {
						int s = separators.length - 1;
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
				if(separators != null) {
					if(separators.length == 1) {
						if(ca[caIndex] == separators[0])
							return false;
					} else {
						int s = separators.length - 1;
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
				if(separators.length == 1) {
					if(tokens[tokenIndex++] != ca[caIndex++])
						return false;
				} else {
					if(caIndex + separators.length > caLength)
						return false;
					for(int i = 0; i < separators.length; i++) {
						if(tokens[tokenIndex++] != ca[caIndex])
							return false;
						caIndex++;
					}
				}
			} else if(types[tokenIndex] == WildcardPattern.SKIP_TYPE) {
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
		String str = "/aaa\\*/**/bb*.txt";
		WildcardPattern pattern = WildcardPattern.compile(str, "/");
		
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
		boolean result = matcher.matches("/aaa\\*/mm/nn/bbZZ.txt");
		
		System.out.println("Result: " + result);
	}

}
