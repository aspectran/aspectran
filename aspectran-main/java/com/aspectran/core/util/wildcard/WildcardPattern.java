package com.aspectran.core.util.wildcard;

public class WildcardPattern {

	private static final char ESCAPE = '\\';
	
	private static final char STAR = '*';
	
	private static final char QUESTION = '?';
	
	private static final char PLUS = '+';
	
	private char[] separators;
	
	private char[] tokens;

	private int[] types;
	
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
		int sepa = 0;
		int sepaEsc = 0;
		boolean esc = false;
		
		for(int i = 0; i < tokens.length; i++) {
			if(tokens[i] == STAR) {
				if(esc) {
					types[i - 1] = -1;
					esc = false;
				} else {
					if(star) {
						types[i - 1] = 2; // type 2: double star
						types[i] = 2; // type 2: double star
						star = false;
					} else {
						types[i] = 1; // type 1: star
						star = true;
					}
				}
			} else if(tokens[i] == QUESTION) {
				if(esc) {
					types[i - 1] = -1;
					esc = false;
				} else {
					types[i] = 3; // type 3: question
				}
			} else if(tokens[i] == PLUS) {
				if(esc) {
					types[i - 1] = -1;
					esc = false;
				} else {
					types[i] = 4; // type 4: question
				}
			} else if(separators != null) {
				if(tokens[i] == separators[sepa]) {
					if(tokens[i] == ESCAPE) {
						if(esc) {
							sepa++;
							sepaEsc++;
							types[i - 1] = -1;
						} else {
							esc = true;
						}
					} else
						sepa++;
				}
				if(sepa == separators.length) {
					if(sepa == 1) {
						types[i] = 9; // type 9: separator
					} else {          
						for(int j = i - sepa + sepaEsc + 1; j <= i; j++) {
							if(types[j] != -1)
								types[j] = 9; // type 9: separator
						}
					}
					sepa = 0;
					sepaEsc = 0;
				}
			} else if(tokens[i] == ESCAPE) {
				esc = true;
				System.out.println("[" + i + " " + esc + "]");
			}
			
			if(tokens[i] != STAR && star)
				star = false;
			
			if(sepa > 0 && tokens[i] != separators[sepa - 1]) {
				sepa = 0;
				sepaEsc = 0;
			}
		}
	}
	
	protected char[] getTokens() {
		return tokens;
	}
	
	protected int[] getTypes() {
		return types;
	}
	
	public static WildcardPattern compile(String patternString, String separator) {
		return new WildcardPattern(patternString, separator);
	}
	
	public static void main(String argv[]) {
		String str = "/aaa\\*/**/bb*.txt";
		WildcardPattern w = WildcardPattern.compile(str, "\\");
		
		int i = 0;
		for(char c : w.getTokens()) {
			System.out.print(i);
			System.out.print(": ");
			System.out.print(c);
			System.out.print(", ");
			System.out.println(w.getTypes()[i]);
			i++;
		}
	}

}
