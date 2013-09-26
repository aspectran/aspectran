package com.aspectran.core.util.wildcard;

public class WildcardPattern {

	protected static final char ESCAPE_CHAR = '\\';
	
	protected static final char SPACE_CHAR = ' ';
	
	protected static final char STAR_CHAR = '*';
	
	protected static final char QUESTION_CHAR = '?';
	
	protected static final char PLUS_CHAR = '+';
	
	protected static int EOT_TYPE = -2;
	
	protected static int SKIP_TYPE = -1;
	
	protected static int LITERAL_TYPE = 0;
	
	protected static int STAR_TYPE = 1;
	
	protected static int DOUBLE_STAR_TYPE = 2;
	
	protected static int QUESTION_TYPE = 3;
	
	protected static int PLUS_TYPE = 4;
	
	protected static int SEPARATOR_TYPE = 9;
	
	private char[] separators;
	
	private char[] tokens;

	private int[] types;
	
	private int separatorCount;
	
	private int[] separatorPositions;
	
	public WildcardPattern(String patternString) {
		this(patternString, null);
	}

	public WildcardPattern(String patternString, String separator) {
		if(separator != null && separator.length() > 0)
			this.separators = separator.toCharArray();
		else
			this.separators = null;
		
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
						types[i] = DOUBLE_STAR_TYPE; // type 2: double star
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

		if(separators != null) {
			int sepa = 0;
			int skip = 0;

			for(int i = 0; i < tokens.length; i++) {
				if(types[i] > SKIP_TYPE) {
					if(tokens[i] == separators[sepa]) {
						sepa++;
					}
					if(sepa == separators.length) {
						if(sepa == 1) {
							types[i] = SEPARATOR_TYPE; // type 9: separator
						} else {          
							for(int j = i - sepa + skip + 1; j <= i; j++) {
								if(types[j] != SKIP_TYPE)
									types[j] = SEPARATOR_TYPE; // type 9: separator
							}
						}
						sepa = 0;
						separatorCount++;
					}
				}

				if(sepa > 0) {
					if(types[i] == SKIP_TYPE)
						skip++;
					else if(tokens[i] != separators[sepa - 1])
						sepa = 0;
				}
			}
			
			if(separatorCount > 0)
				separatorPositions = new int[separatorCount];
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

	protected char[] getSeparators() {
		return separators;
	}

	protected char[] getTokens() {
		return tokens;
	}
	
	protected int[] getTypes() {
		return types;
	}
	
	public int getSeparatorCount() {
		return separatorCount;
	}

	protected void setSeparatorPosition(int separatorIndex, int caPosition) {
		separatorPositions[separatorIndex] = caPosition;
	}
	
	public boolean matches(String str) {
		return WildcardMatcher.matches(this, str);
	}
	
	public static WildcardPattern compile(String patternString, String separator) {
		return new WildcardPattern(patternString, separator);
	}
	
	public static void main(String argv[]) {
		String str = "\\aaa\\*\\**\\bb*.txt**";
		WildcardPattern pattern = WildcardPattern.compile(str, "\\");
		
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
		boolean result = pattern.matches("\\aaa\\*\\mm\\nn/bbZZ.txt");
		
		System.out.println("Result: " + result);
	}

}
