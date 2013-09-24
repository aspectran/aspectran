package com.aspectran.core.util.wildcard;

public class WildcardPattern {

	private static final String QUESTION = "?";

	private static final String STAR = "*";
	
	private static final String DOUBLE_STAR = "**";
	
	private String separator;
	
	private String[] tokens;

	private int[] types;

	public WildcardPattern(String patternString) {
		this(patternString, null);
	}

	public WildcardPattern(String patternString, String separator) {
		this.separator = separator;
	}
	
	private void parse(String patternString) {
		char[] ch = patternString.toCharArray();
	}
	
	public static WildcardPattern compile(String patternString) {
		return new WildcardPattern(patternString);
	}

}
