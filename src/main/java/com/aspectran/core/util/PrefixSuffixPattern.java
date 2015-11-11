/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
