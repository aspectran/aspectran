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
import java.util.TimeZone;

/**
 * Static utility methods pertaining to {@code String} or {@code CharSequence} instances.
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

	/**
	 * Trim leading and trailing whitespace from the given {@code String}.
	 * 
	 * @param str the {@code String} to check
	 * @return the trimmed {@code String}
	 * @see java.lang.Character#isWhitespace
	 */
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

	/**
	 * Trim <i>all</i> whitespace from the given {@code String}:
	 * leading, trailing, and in between characters.
	 * 
	 * @param str the {@code String} to check
	 * @return the trimmed {@code String}
	 * @see java.lang.Character#isWhitespace
	 */
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

	/**
	 * Trim leading whitespace from the given {@code String}.
	 * 
	 * @param str the {@code String} to check
	 * @return the trimmed {@code String}
	 * @see java.lang.Character#isWhitespace
	 */
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

	/**
	 * Trim trailing whitespace from the given {@code String}.
	 * 
	 * @param str the {@code String} to check
	 * @return the trimmed {@code String}
	 * @see java.lang.Character#isWhitespace
	 */
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

	/**
	 * Trim all occurrences of the supplied leading character from the given {@code String}.
	 * 
	 * @param str the {@code String} to check
	 * @param leadingCharacter the leading character to be trimmed
	 * @return the trimmed {@code String}
	 */
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

	/**
	 * Trim all occurrences of the supplied trailing character from the given {@code String}.
	 * 
	 * @param str the {@code String} to check
	 * @param trailingCharacter the trailing character to be trimmed
	 * @return the trimmed {@code String}
	 */
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

	/**
	 * Test if the given {@code String} starts with the specified prefix,
	 * ignoring upper/lower case.
	 * 
	 * @param str the {@code String} to check
	 * @param prefix the prefix to look for
	 * @return {@code true} if the {@code String} starts with the prefix, case insensitive, or both {@code null}
	 * @see java.lang.String#startsWith
	 */
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

	/**
	 * Test if the given {@code String} ends with the specified suffix,
	 * ignoring upper/lower case.
	 * 
	 * @param str the {@code String} to check
	 * @param suffix the suffix to look for
	 * @return {@code true} if the {@code String} ends with the suffix, case insensitive, or both {@code null}
	 * @see java.lang.String#endsWith
	 */
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
	 * @param str a string reference to check
	 * @return {@code true} if the string is null or is the empty string
	 */
	public static boolean isEmpty(String str) {
		return (str == null || str.length() == 0);
	}
	
	/**
	 * Returns the given string if it is non-null; the empty string otherwise.
	 *
	 * @param str the string to test and possibly return
	 * @return {@code string} itself if it is non-null; {@code ""} if it is null
	 */
	public static String nullToEmpty(String str) {
	  return (str == null) ? EMPTY : str;
	}

	/**
	 * Returns the given string if it is nonempty; {@code null} otherwise.
	 *
	 * @param str the string to test and possibly return
	 * @return {@code string} itself if it is nonempty; {@code null} if it is empty or null
	 */
	public static String emptyToNull(String str) {
		return (str == null || str.length() == 0) ? null : str;
	}

	public static boolean startsWith(String str, char prefix) {
		if(str == null || str.isEmpty())
			return false;
		return (str.charAt(0) == prefix);
	}
	
	public static boolean endsWith(String str, char suffix) {
		if(str == null || str.isEmpty())
			return false;
		return (str.charAt(str.length() - 1) == suffix);
	}

	/**
	 * 발견한 모든 검색 문자열을 치환 문자열로 교체한다.
	 * 
	 * @param str 대상 문자열
	 * @param search 발견 문자열
	 * @param replace 치환 문자열
	 * @return String
	 */
	public static String replace(String str, String search, String replace) {
		if(str == null || search == null || replace == null)
			return str;

		StringBuilder sb = new StringBuilder();

		int searchLen = search.length();
		int stringLen = str.length();
		int index = 0;
		int oldIndex = 0;

		while((index = str.indexOf(search, oldIndex)) >= 0) {
			sb.append(str.substring(oldIndex, index));
			sb.append(replace);
			oldIndex = index + searchLen;
		}

		if(oldIndex < stringLen)
			sb.append(str.substring(oldIndex, stringLen));

		return sb.toString();
	}

	/**
	 * 발견한 모든 검색 문자열을 치환 문자열로 교체합니다.
	 * 발견 문장 배열과 치환 문자열 배열은 서로 쌍을 이루어야 합니다.
	 * 
	 * @param str 대상 문자열
	 * @param search 발견 문자열 배열
	 * @param replace 치환 문자열 배열
	 * @return String
	 */
	public static String replace(String str, String[] search, String[] replace) {
		if(str == null || search == null || replace == null)
			return str;

		StringBuilder sb = new StringBuilder(str);

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
	 * @param str 분리 대상 문자열
	 * @param delim 구분 문자열
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String str, String delim) {
		if(isEmpty(str))
			return new String[0];

		int cnt = search(str, delim);
		String[] item = new String[cnt + 1];

		if(cnt == 0) {
			item[0] = str;
			return item;
		}

		int idx = 0;
		int pos1 = 0;
		int pos2 = str.indexOf(delim);
		int delimLen = delim.length();

		while(pos2 >= 0) {
			item[idx++] = (pos1 > pos2 - 1) ? EMPTY : str.substring(pos1, pos2);

			pos1 = pos2 + delimLen;
			pos2 = str.indexOf(delim, pos1);
		}

		if(pos1 < str.length())
			item[idx] = str.substring(pos1);

		if(item[cnt] == null)
			item[cnt] = EMPTY;

		return item;
	}

	/**
	 * 대상 문자열(str)에서 구분 문자열(delim)을 기준으로 문자열을 분리하여
	 * 각 분리된 문자열을 배열에 할당하여 반환한다.
	 * size를 지정하면 ""문자열이  나머지 문자열 전체를 가지는 최대 size개 원소의 배열을 반환합니다.
	 * 
	 * @param str 분리 대상 문자열
	 * @param delim 구분 문자열
	 * @param size 결과 배열의 크기
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String str, String delim, int size) {
		String[] arr1 = new String[size];
		String[] arr2 = split(str, delim);

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
	 * @param str 분리 대상 문자열
	 * @param delim 구분 문자열
	 * 
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String str, char delim) {
		if(isEmpty(str))
			return new String[0];

		int cnt = search(str, delim);
		String[] item = new String[cnt + 1];

		if(cnt == 0) {
			item[0] = str;
			return item;
		}

		int idx = 0;
		int pos1 = 0;
		int pos2 = str.indexOf(delim);

		while(pos2 >= 0) {
			item[idx++] = (pos1 > pos2 - 1) ? EMPTY : str.substring(pos1, pos2);

			pos1 = pos2 + 1;
			pos2 = str.indexOf(delim, pos1);
		}

		if(pos1 < str.length())
			item[idx] = str.substring(pos1);

		if(item[cnt] == null)
			item[cnt] = EMPTY;

		return item;
	}

	/**
	 * 대상 문자열(str)에서 구분 문자열(delim)을 기준으로 문자열을 분리하여
	 * 각 분리된 문자열을 배열에 할당하여 반환한다.
	 * size를 지정하면 ""문자열이  나머지 문자열 전체를 가지는 최대 size개 원소의 배열을 반환합니다.
	 * 
	 * @param str 분리 대상 문자열
	 * @param delim 구분 문자열
	 * @param size 결과 배열의 크기
	 * @return 분리된 문자열을 순서대로 배열에 격납하여 반환한다.
	 */
	public static String[] split(String str, char delim, int size) {
		String[] arr1 = new String[size];
		String[] arr2 = split(str, delim);

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
	 * @param str 대상문자열
	 * @param keyw 검색할 문자열
	 * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
	 */
	public static int search(String str, String keyw) {
		int strLen = str.length();
		int keywLen = keyw.length();
		int pos = 0;
		int cnt = 0;

		if(keywLen == 0)
			return 0;

		while((pos = str.indexOf(keyw, pos)) != -1) {
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
	 * @param str 대상문자열
	 * @param keyw 검색할 문자열
	 * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
	 */
	public static int searchIgnoreCase(String str, String keyw) {
		return search(str.toLowerCase(), keyw.toLowerCase());
	}

	/**
	 * 대상문자열(str)에서 지정문자열(keyw)이 검색된 횟수를,
	 * 지정문자열이 없으면 0 을 반환한다.
	 * 
	 * @param chars 대상문자열
	 * @param c 검색할 문자열
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
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * 
	 * @param str the String to tokenize
	 * @param delimiters the delimiter characters
	 * @return an array of the tokens
	 */
	public static String[] tokenize(String str, String delimiters) {
		return tokenize(str, delimiters, false);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * 
	 * @param str the String to tokenize
	 * @param delimiters the delimiter characters
	 * @param trim trim the tokens via String's trim
	 * @return an array of the tokens
	 */
	public static String[] tokenize(String str, String delimiters, boolean trim) {
		if(str == null)
			return new String[0];

		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();

		while(st.hasMoreTokens()) {
			tokens.add(trim ? st.nextToken().trim() : st.nextToken());
		}

		return tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
	 * <p>Useful for {@code toString()} implementations.
	 * 
	 * @param arr the array to display
	 * @param delim the delimiter to use (typically a ",")
	 * @return the delimited {@code String}
	 */
	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if(arr == null || arr.length == 0) {
			return EMPTY;
		}
		if(arr.length == 1) {
			return (arr[0] == null) ? EMPTY : arr[0].toString();
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.length; i++) {
			if(i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}
	
	/**
	 * Convert a comma delimited list (e.g., a row from a CSV file) into an
	 * array of strings.
	 * 
	 * @param str the input {@code String}
	 * @return an array of strings, or the empty array in case of empty input
	 */
	public static String[] splitCommaDelimitedString(String str) {
		return tokenize(str, ",", true);
	}	
	
	/**
	 * Convert a {@code String} array into a comma delimited {@code String}
	 * (i.e., CSV).
	 * 
	 * @param arr the array to display
	 * @return the delimited {@code String}
	 */
	public static String joinCommaDelimitedList(String[] arr) {
		return arrayToDelimitedString(arr, ", ");
	}	
	
	/**
	 * Parse the given {@code localeString} value into a {@link Locale}.
	 * <p>This is the inverse operation of {@link Locale#toString Locale's toString}.
	 *
	 * @param localeString the locale {@code String}, following {@code Locale's}
	 * {@code toString()} format ("en", "en_UK", etc);
	 * also accepts spaces as separators, as an alternative to underscores
	 * @return a corresponding {@code Locale} instance
	 * @throws IllegalArgumentException in case of an invalid locale specification
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenize(localeString, "_ ", false);
		String language = (parts.length > 0 ? parts[0] : EMPTY);
		String country = (parts.length > 1 ? parts[1] : EMPTY);
		validateLocalePart(language);
		validateLocalePart(country);
		String variant = EMPTY;
		if(parts.length > 2) {
			// There is definitely a variant, and it is everything after the country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the variant.
			variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
			if(variant.startsWith("_")) {
				variant = trimLeadingCharacter(variant, '_');
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	private static void validateLocalePart(String localePart) {
		for(int i = 0; i < localePart.length(); i++) {
			char ch = localePart.charAt(i);
			if(ch != '_' && ch != ' ' && !Character.isLetterOrDigit(ch)) {
				throw new IllegalArgumentException(
						"Locale part \"" + localePart + "\" contains invalid characters");
			}
		}
	}

	/**
	 * Determine the RFC 3066 compliant language tag,
	 * as used for the HTTP "Accept-Language" header.
	 * @param locale the Locale to transform to a language tag
	 * @return the RFC 3066 compliant language tag as {@code String}
	 */
	public static String toLanguageTag(Locale locale) {
		return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : EMPTY);
	}

	/**
	 * Parse the given {@code timeZoneString} value into a {@link TimeZone}.
	 * @param timeZoneString the time zone {@code String}, following {@link TimeZone#getTimeZone(String)}
	 * but throwing {@link IllegalArgumentException} in case of an invalid time zone specification
	 * @return a corresponding {@link TimeZone} instance
	 * @throws IllegalArgumentException in case of an invalid time zone specification
	 */
	public static TimeZone parseTimeZoneString(String timeZoneString) {
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
		if("GMT".equals(timeZone.getID()) && !timeZoneString.startsWith("GMT")) {
			// We don't want that GMT fallback...
			throw new IllegalArgumentException("Invalid time zone specification '" + timeZoneString + "'");
		}
		return timeZone;
	}

}
