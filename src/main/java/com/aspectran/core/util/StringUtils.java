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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Static utility methods pertaining to {@code String} or {@code CharSequence} instances.
 * 
 * @author Juho Jeong
 */
public class StringUtils {

	/** The empty {@link String} */
	public static final String EMPTY = "";

	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	public static boolean hasLength(String str) {
		return hasLength((CharSequence)str);
	}

	public static boolean hasText(CharSequence str) {
		if(!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for(int i = 0; i < strLen; i++) {
			if(!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasText(String str) {
		return hasText((CharSequence)str);
	}

	public static boolean containsWhitespace(CharSequence str) {
		if(!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for(int i = 0; i < strLen; i++) {
			if(Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsWhitespace(String str) {
		return containsWhitespace((CharSequence)str);
	}

	public static String trimWhitespace(String str) {
		if(!hasLength(str)) {
			return str;
		}
		StringBuilder buf = new StringBuilder(str);
		while(buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
			buf.deleteCharAt(0);
		}
		while(buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	public static String trimAllWhitespace(String str) {
		if(!hasLength(str)) {
			return str;
		}
		StringBuilder buf = new StringBuilder(str);
		int index = 0;
		while(buf.length() > index) {
			if(Character.isWhitespace(buf.charAt(index))) {
				buf.deleteCharAt(index);
			} else {
				index++;
			}
		}
		return buf.toString();
	}

	public static String trimLeadingWhitespace(String str) {
		if(!hasLength(str)) {
			return str;
		}
		StringBuilder buf = new StringBuilder(str);
		while(buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
			buf.deleteCharAt(0);
		}
		return buf.toString();
	}

	public static String trimTrailingWhitespace(String str) {
		if(!hasLength(str)) {
			return str;
		}
		StringBuilder buf = new StringBuilder(str);
		while(buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if(!hasLength(str)) {
			return str;
		}
		StringBuilder buf = new StringBuilder(str);
		while(buf.length() > 0 && buf.charAt(0) == leadingCharacter) {
			buf.deleteCharAt(0);
		}
		return buf.toString();
	}

	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if(!hasLength(str)) {
			return str;
		}
		StringBuilder buf = new StringBuilder(str);
		while(buf.length() > 0 && buf.charAt(buf.length() - 1) == trailingCharacter) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if(str == null || prefix == null) {
			return false;
		}
		if(str.startsWith(prefix)) {
			return true;
		}
		if(str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}

	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if(str == null || suffix == null) {
			return false;
		}
		if(str.endsWith(suffix)) {
			return true;
		}
		if(str.length() < suffix.length()) {
			return false;
		}

		String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
		String lcSuffix = suffix.toLowerCase();
		return lcStr.equals(lcSuffix);
	}

	/**
	 * Returns {@code true} if the given string is null or is the empty string.
	 *
	 * @param string a string reference to check
	 * @return {@code true} if the string is null or is the empty string
	 */
	public static boolean isEmpty(String string) {
		return (string == null || string.length() == 0);
	}
	
	/**
	 * Returns the given string if it is non-null; the empty string otherwise.
	 *
	 * @param string the string to test and possibly return
	 * @return {@code string} itself if it is non-null; {@code ""} if it is null
	 */
	public static String nullToEmpty(String string) {
	  return (string == null) ? EMPTY : string;
	}


	/**
	 * Returns the given string if it is nonempty; {@code null} otherwise.
	 *
	 * @param string the string to test and possibly return
	 * @return {@code string} itself if it is nonempty; {@code null} if it is empty or null
	 */
	public static String emptyToNull(String string) {
		return (string == null || string.length() == 0) ? null : string;
	}

	/**
	 * 발견한 모든 검색 문자열을 치환 문자열로 교체한다.
	 * 
	 * @param string 대상 문자열
	 * @param search 발견 문자열
	 * @param replace 치환 문자열
	 * 
	 * @return String
	 */
	public static String replace(String string, String search, String replace) {
		if(string == null || search == null || replace == null)
			return string;

		StringBuilder sb = new StringBuilder();

		int searchLen = search.length();
		int stringLen = string.length();
		int index = 0;
		int oldIndex = 0;

		while((index = string.indexOf(search, oldIndex)) >= 0) {
			sb.append(string.substring(oldIndex, index));
			sb.append(replace);
			oldIndex = index + searchLen;
		}

		if(oldIndex < stringLen)
			sb.append(string.substring(oldIndex, stringLen));

		return sb.toString();
	}

	/**
	 * 발견한 모든 검색 문자열을 치환 문자열로 교체합니다.
	 * 발견 문장 배열과 치환 문자열 배열은 서로 쌍을 이루어야 합니다.
	 * 
	 * @param string 대상 문자열
	 * @param search 발견 문자열 배열
	 * @param replace 치환 문자열 배열
	 * 
	 * @return String
	 */
	public static String replace(String string, String[] search, String[] replace) {
		if(string == null || search == null || replace == null)
			return string;

		StringBuilder sb = new StringBuilder(string);

		int loop = (search.length <= replace.length) ? search.length : replace.length;
		int start = 0;
		int end;
		int searchLen;
		int replaceLen;

		for(int i = 0; i < loop; i++) {
			if(search[i] == null || replace[i] == null)
				continue;

			searchLen = search[i].length();
			replaceLen = replace[i].length();

			while(true) {
				if(sb.length() == 0)
					break;

				start = sb.indexOf(search[i], start + replaceLen);

				if(start == -1)
					break;

				end = start + searchLen;

				sb.replace(start, end, replace[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * 대상문자열(str)에서 구분문자열(delim)을 기준으로 문자열을 분리하여
	 * 각 분리된 문자열을 배열에 할당하여 반환한다.
	 * 
	 * @param string 분리 대상 문자열
	 * @param delim 구분 문자열
	 * 
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String string, String delim) {
		if(isEmpty(string))
			return new String[0];

		int cnt = search(string, delim);
		String[] item = new String[cnt + 1];

		if(cnt == 0) {
			item[0] = string;
			return item;
		}

		int idx = 0;
		int pos1 = 0;
		int pos2 = string.indexOf(delim);
		int delimLen = delim.length();

		while(pos2 >= 0) {
			item[idx++] = (pos1 > pos2 - 1) ? EMPTY : string.substring(pos1, pos2);

			pos1 = pos2 + delimLen;
			pos2 = string.indexOf(delim, pos1);
		}

		if(pos1 < string.length())
			item[idx] = string.substring(pos1);

		if(item[cnt] == null)
			item[cnt] = EMPTY;

		return item;
	}

	/**
	 * 대상 문자열(str)에서 구분 문자열(delim)을 기준으로 문자열을 분리하여
	 * 각 분리된 문자열을 배열에 할당하여 반환한다.
	 * size를 지정하면 ""문자열이  나머지 문자열 전체를 가지는 최대 size개 원소의 배열을 반환합니다.
	 * 
	 * @param string 분리 대상 문자열
	 * @param delim 구분 문자열
	 * @param size 결과 배열의 크기
	 * 
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String string, String delim, int size) {
		String[] arr1 = new String[size];
		String[] arr2 = split(string, delim);

		for(int i = 0; i < arr1.length; i++) {
			if(i < arr2.length)
				arr1[i] = arr2[i];
			else
				arr1[i] = EMPTY;
		}

		return arr1;
	}
	

	/**
	 * 대상문자열(str)에서 구분문자열(delim)을 기준으로 문자열을 분리하여
	 * 각 분리된 문자열을 배열에 할당하여 반환한다.
	 * 
	 * @param string 분리 대상 문자열
	 * @param delim 구분 문자열
	 * 
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String string, char delim) {
		if(isEmpty(string))
			return new String[0];

		int cnt = search(string, delim);
		String[] item = new String[cnt + 1];

		if(cnt == 0) {
			item[0] = string;
			return item;
		}

		int idx = 0;
		int pos1 = 0;
		int pos2 = string.indexOf(delim);

		while(pos2 >= 0) {
			item[idx++] = (pos1 > pos2 - 1) ? EMPTY : string.substring(pos1, pos2);

			pos1 = pos2 + 1;
			pos2 = string.indexOf(delim, pos1);
		}

		if(pos1 < string.length())
			item[idx] = string.substring(pos1);

		if(item[cnt] == null)
			item[cnt] = EMPTY;

		return item;
	}

	/**
	 * 대상 문자열(str)에서 구분 문자열(delim)을 기준으로 문자열을 분리하여
	 * 각 분리된 문자열을 배열에 할당하여 반환한다.
	 * size를 지정하면 ""문자열이  나머지 문자열 전체를 가지는 최대 size개 원소의 배열을 반환합니다.
	 * 
	 * @param string 분리 대상 문자열
	 * @param delim 구분 문자열
	 * @param size 결과 배열의 크기
	 * 
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String string, char delim, int size) {
		String[] arr1 = new String[size];
		String[] arr2 = split(string, delim);

		for(int i = 0; i < arr1.length; i++) {
			if(i < arr2.length)
				arr1[i] = arr2[i];
			else
				arr1[i] = EMPTY;
		}

		return arr1;
	}

	/**
	 * 대상문자열(str)에서 지정문자열(keyw)이 검색된 횟수를,
	 * 지정문자열이 없으면 0 을 반환한다.
	 * 
	 * @param string 대상문자열
	 * @param keyw 검색할 문자열
	 * 
	 * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
	 */
	public static int search(String string, String keyw) {
		int strLen = string.length();
		int keywLen = keyw.length();
		int pos = 0;
		int cnt = 0;

		if(keywLen == 0)
			return 0;

		while((pos = string.indexOf(keyw, pos)) != -1) {
			pos += keywLen;
			cnt++;

			if(pos >= strLen)
				break;
		}

		return cnt;
	}
	
	/**
	 * 대상문자열(str)에서 대소문자 구분없이 지정문자열(keyw)이 검색된 횟수를,
	 * 지정문자열이 없으면 0 을 반환한다.
	 * 
	 * @param string 대상문자열
	 * @param keyw 검색할 문자열
	 * 
	 * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
	 */
	public static int searchIgnoreCase(String string, String keyw) {
		return search(string.toLowerCase(), keyw.toLowerCase());
	}

	/**
	 * 대상문자열(str)에서 지정문자열(keyw)이 검색된 횟수를,
	 * 지정문자열이 없으면 0 을 반환한다.
	 * 
	 * @param chars 대상문자열
	 * @param c 검색할 문자열
	 * 
	 * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
	 */
	public static int search(CharSequence chars, char c) {
		int count = 0;
		for(int i = 0; i < chars.length(); i++) {
			if(chars.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 대상문자열(str)에서 지정문자열(keyw)이 검색된 횟수를,
	 * 지정문자열이 없으면 0 을 반환한다.
	 *
	 * @param chars 대상문자열
	 * @param c 검색할 문자열
	 * 
	 * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
	 */
	public static int searchIgnoreCase(CharSequence chars, char c) {
		int count = 0;
		char cl = Character.toLowerCase(c);
		for(int i = 0; i < chars.length(); i++) {
			if(Character.toLowerCase(chars.charAt(i)) == cl) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 주어진 <code>delimiters</code>에 의해 분리된 문자열 배열을 반환한다.
	 * 
	 * @param string the string
	 * @param delimiters the delimiters
	 * 
	 * @return the string[]
	 */
	public static String[] tokenize(String string, String delimiters) {
		return tokenize(string, delimiters, false);
	}

	/**
	 * 주어진 <code>delimiters</code>에 의해 분리된 문자열 배열을 반환한다.
	 * 
	 * @param string the string
	 * @param delimiters the delimiters
	 * @param trim the trim
	 * 
	 * @return the string[]
	 */
	public static String[] tokenize(String string, String delimiters, boolean trim) {
		if(string == null)
			return new String[0];

		StringTokenizer st = new StringTokenizer(string, delimiters);
		List<String> tokens = new ArrayList<String>();

		while(st.hasMoreTokens()) {
			tokens.add(trim ? st.nextToken().trim() : st.nextToken());
		}

		return tokens.toArray(new String[tokens.size()]);
	}

	public static Locale deduceLocale(String input) {
		if(input == null)
			return null;
		Locale locale = Locale.getDefault();
		if(input.length() > 0 && input.charAt(0) == '"')
			input = input.substring(1, input.length() -1);
		StringTokenizer st = new StringTokenizer(input, ",_ ");
		String lang = "";
		String country = "";
		if(st.hasMoreTokens()) {
			lang = st.nextToken();
		}
		if(st.hasMoreTokens()) {
			country = st.nextToken();
		}
		if(!st.hasMoreTokens()) {
			locale = new Locale(lang, country);
		}
		else {
			locale = new Locale(lang, country, st.nextToken());
		}
		return locale;
	}

}
