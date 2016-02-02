/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.util;

public class PrefixSuffixPattern {

	public static final char PREFIX_SUFFIX_PATTERN_SEPARATOR = '*';
	
	private String prefix;
	
	private String suffix;
	
	private boolean splited;
	
	public PrefixSuffixPattern() {
	}
	
	public PrefixSuffixPattern(String input) {
		split(input);
	}
	
	public boolean split(String input) {
		int startIndex = input.indexOf(PREFIX_SUFFIX_PATTERN_SEPARATOR);
		if(startIndex == -1) {
			prefix = null;
			suffix = null;
			splited = false;
			return false;
		}
		
		prefix = (startIndex > 0) ? input.substring(0, startIndex) : null;
		suffix = (startIndex < input.length() - 1) ? input.substring(startIndex + 1) : null;
		splited = (prefix != null || suffix != null || (input.length() == 1 && input.charAt(0) == PREFIX_SUFFIX_PATTERN_SEPARATOR));
		
		return splited;
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
	
	public boolean isSplited() {
		return splited;
	}

	public String join(String input) {
		return join(prefix, input, suffix);
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
