package com.aspectran.core.util.wildcard;


public class CopyOfWildcardMatcher {

	private WildcardPattern pattern;
	
	private char[] charArray;
	
	private int[] separatorFlags;
	
	private int separatorCount;
	
	private int separatorIndex;
	
	public CopyOfWildcardMatcher(WildcardPattern pattern) {
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
	
	public CopyOfWildcardMatcher first() {
		separatorIndex = 0;
		return this;
	}

	public CopyOfWildcardMatcher last() {
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
		
		boolean separator = false;
		boolean breaking = false;
		
		while(tokenIndex < tokensLength && caIndex < caLength) {
			System.out.println("token index=" + tokenIndex + ", token=" + tokens[tokenIndex] + ", type=" + types[tokenIndex]);
			System.out.println("token index=" + tokenIndex + ", ca index=" + caIndex + ", char=" + ca[caIndex]);
			
			if(sepaLength > 0 && ca[caIndex] == separators[0]) {
				separator = true;
				for(int s = 0; s < sepaLength; s++) {
					if(ca[caIndex + s] != separators[s]) {
						separator = false;
						break;
					}
				}
			}
			
			if(separator) {
				if(types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE) {
					caIndex += sepaLength;
					tokenIndex++;
				} else if(types[tokenIndex] == WildcardPattern.STAR_TYPE) {
					caIndex += sepaLength;
				} else if(types[tokenIndex] == WildcardPattern.STAR_STAR_TYPE) {
					caIndex += sepaLength;
				} else if(types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
					tokenIndex++;
				} else {
					return false;
				}
				separator = false;
			} else {
				if(types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
					if(tokens[tokenIndex] != ca[caIndex])
						return false;
					caIndex++;
					tokenIndex++;
				} else if(types[tokenIndex] == WildcardPattern.STAR_TYPE) {
					for(int n = tokenIndex + 1; n < tokensLength; n++) {
						if(types[tokenIndex] != types[n]) {
							tokenIndex = n - 1;
							break;
						}
					}
					caIndex++;
				} else if(types[tokenIndex] == WildcardPattern.STAR_STAR_TYPE) {
					for(int n = tokenIndex + 1; n < tokensLength; n++) {
						if(types[tokenIndex] != types[n]) {
							tokenIndex = n - 1;
							break;
						}
					}
					caIndex++;
				} else {
					caIndex++;
					tokenIndex++;
				}					
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
		//String str = "com.**.scope.**.*Xml*";
		String str = "com.*.scope.*.*Xml*";
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
		
		CopyOfWildcardMatcher matcher = new CopyOfWildcardMatcher(pattern);
		//boolean result = matcher.matches("/aaa*/mm/nn/bbZZ.txt");
		//boolean result = matcher.matches("com.aspectran.test.SampleTestBean");
		//boolean result = matcher.matches("com.ab.cd.scope.**.*Xml*");
		boolean result = matcher.matches("com.ab.cd.scope.**.*Xml*");
		
		System.out.println("result: " + result);
		System.out.println("separatorCount: " + matcher.getSeparatorCount());
		
		while(matcher.hasNext()) {
			System.out.println(" -" + matcher.next());
		}
	}

}
