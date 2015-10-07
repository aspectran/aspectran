package com.aspectran.core.util;

public class PrefixSuffixPattern {

	public static final char PREFIX_SUFFIX_PATTERN_SEPARATOR = '*';
	
	private String prefix;
	
	private String suffix;
	
	public PrefixSuffixPattern() {
	}
	
	public boolean split(String input) {
		prefix = null;
		suffix = null;

		int startIndex = input.indexOf(PREFIX_SUFFIX_PATTERN_SEPARATOR);
		if(startIndex == -1) {
			return false;
		}
		
		if(startIndex > 0)
			prefix = input.substring(0, startIndex);
		
		if(startIndex < input.length() - 1)
			suffix = input.substring(startIndex + 1);
		
		return (prefix != null || suffix != null || (input.length() == 1 && input.charAt(0) == PREFIX_SUFFIX_PATTERN_SEPARATOR));
	}
	
	public String join(String input) {
		return join(prefix, input, suffix);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public static String join(String prefix, String input, String suffix) {
		if(prefix != null && suffix != null) {
			return prefix + input + suffix;
		} else if(prefix != null) {
			return prefix + input;
		} else if(suffix != null) {
			return input + suffix;
		} else {
			return input;
		}
	}
	
}
