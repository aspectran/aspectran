package com.aspectran.core.util.wildcard;

public class WildcardMatcher {

	private WildcardPattern pattern;
	
	public WildcardMatcher(WildcardPattern pattern) {
		this.pattern = pattern;
	}
	
	public boolean matches(String str) {
		char[] tokens = pattern.getTokens();
		int[] types = pattern.getTypes();
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
				for(; tokenIndex < tokensLength; tokenIndex++) {
					if(types[tokenIndex] == WildcardPattern.SKIP_TYPE || types[tokenIndex] == WildcardPattern.LITERAL_TYPE)
						end = tokenIndex;
					else
						break;
				}
				if(end > start) {
					if(end == tokensLength - 1) {
						int m = caLength - 1;
						int n = end;
						for(; n >= start && m >= caIndex; n--) {
							if(types[n] != WildcardPattern.SKIP_TYPE) {
								if(tokens[n] != ca[m--])
									return false;
							}
						}
						if(n != start)
							return false;
					} else {
						int n = start;
						for(; n <= end && caIndex < caLength; caIndex++) {
							if(types[n] != WildcardPattern.SKIP_TYPE) {
								if(tokens[n] != ca[caIndex]) {
									n = start;
									continue;
								}
							}
							n++;
						}
						if(n <= end)
							return false;
					}
				}
			} else if(types[tokenIndex] == WildcardPattern.DOUBLE_STAR_TYPE) {
				int n = tokensLength - 1;
				while(n > tokenIndex) {
					if(types[n] == WildcardPattern.DOUBLE_STAR_TYPE) {
						break;
					}
					n--;
				}
				if(n == tokenIndex) {
					tokenIndex = tokensLength;
				} else {
					tokenIndex = n + 1;
				}
			} else if(types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
				
				
				//if(types[tokenIndex] != ca[caIndex++])
			} else if(types[tokenIndex] == WildcardPattern.PLUS_TYPE) {
				//if(tokens[tokenIndex] != ca[caIndex++])
				//	return false;
			} else if(types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE) {
				char[] separators = pattern.getSeparators();
				if(separators.length == 1) {
					if(tokens[tokenIndex++] != ca[caIndex++])
						return false;
				} else {
					for(int i = 0; i < separators.length; i++) {
						if(tokens[tokenIndex++] != ca[caIndex++])
							return false;
					}
				}
			} else if(types[tokenIndex] == WildcardPattern.SKIP_TYPE) {
				break;
			} else {
				tokenIndex++;
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
