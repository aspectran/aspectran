/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.token;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.type.TokenType;

/**
 * <p>
 * Created: 2008. 03. 29 오전 1:55:03
 * </p>
 */
public class Tokenizer {

	private static final int MAX_TOKEN_NAME_LENGTH = 256;

	private static final int AT_STRING = 1;

	private static final int AT_SYMBOL = 2;

//	private static final int AT_PARAMETER_SYMBOL = 2;
//
//	private static final int AT_ATTRIBUTE_SYMBOL = 3;
//
//	private static final int AT_TOKEN_PROPERTY_SYMBOL = 4;
//
//	private static final int AT_REFERENCE_BEAN_SYMBOL = 5;

	private static final int AT_TOKEN_NAME = 3;

	private static final int AT_TOKEN_DEFVAL = 4;
	
	private static final char CR = '\r';

	private static final char LF = '\n';

	/**
	 * Tokenize.
	 * 
	 * @param pattern the pattern string
	 * @param trimStringToken the trim string token
	 * 
	 * @return the list< token>
	 */
	public static List<Token> tokenize(String pattern, boolean trimStringToken) {
		List<Token> tokens = new ArrayList<Token>();

		int patternLength = pattern.length();

		int status = AT_STRING;
		int tokenStartOffset = 0; // start position of token in the stringBuffer
		char symbol = Token.PARAMETER_SYMBOL; // PARAMETER_SYMBOL or ATTRIBUTE_SYMBOL
		char c;

		StringBuilder stringBuffer = new StringBuilder();
		StringBuilder tokenNameBuffer = new StringBuilder();
		StringBuilder defTextBuffer = new StringBuilder();

		for(int i = 0; i < patternLength; i++) {
			c = pattern.charAt(i);

			switch(status) {
			case AT_STRING:
				stringBuffer.append(c);

				if(Token.isTokenSymbol(c)) {
					symbol = c;
					status = AT_SYMBOL;
					// abc$ --> tokenStartOffset: 3
					tokenStartOffset = stringBuffer.length() - 1;
				}
				
				break;

			case AT_SYMBOL:
				stringBuffer.append(c);

				if(c == Token.START_BRACKET) {
					status = AT_TOKEN_NAME;
					break;
				}

				status = AT_STRING;
				break;

			case AT_TOKEN_NAME:
			case AT_TOKEN_DEFVAL:
				stringBuffer.append(c);

				if(status == AT_TOKEN_NAME) {
					if(c == Token.DEFAULT_VALUE_SEPARATOR) {
						status = AT_TOKEN_DEFVAL;
						break;
					}
				}

				if(c == Token.END_BRACKET) {
					if(tokenNameBuffer.length() > 0 || defTextBuffer.length() > 0) {
						// save previous non-token string
						if(tokenStartOffset > 0) {
							String defaultText = trimBuffer(stringBuffer, tokenStartOffset, trimStringToken);
							Token token = new Token(TokenType.TEXT, defaultText);
							tokens.add(token);
						}

						// save token name and default value
						Token token = makeToken(symbol, tokenNameBuffer, defTextBuffer);
						tokens.add(token);

						status = AT_STRING;
						stringBuffer.setLength(0);
						break;
					}

					status = AT_STRING;
					break;
				}

				if(status == AT_TOKEN_NAME) {
					if(tokenNameBuffer.length() > MAX_TOKEN_NAME_LENGTH) {
						status = AT_STRING;
						tokenNameBuffer.setLength(0);
					}

					tokenNameBuffer.append(c);
				} else
					defTextBuffer.append(c);

				break;
			}
		}

		if(stringBuffer.length() > 0) {
			String defaultText = trimBuffer(stringBuffer, stringBuffer.length(), trimStringToken);
			Token token = new Token(TokenType.TEXT, defaultText);
			tokens.add(token);
		}
		
		return tokens;
	}
	
	/**
	 * Make a token.
	 * 
	 * @param symbol the symbol
	 * @param tokenNameBuffer the token name buffer
	 * @param defTextBuffer the def value buffer
	 * 
	 * @return the token
	 */
	private static Token makeToken(char symbol, StringBuilder tokenNameBuffer, StringBuilder defTextBuffer) {
		TokenType type = null;
		String name = null;
		String defaultText = null;
		String getterName = null;

		if(tokenNameBuffer.length() > 0) {
			type = Token.tokenTypeOfSymbol(symbol);
			name = tokenNameBuffer.toString();
			
			if(symbol == Token.ATTRIBUTE_SYMBOL ||
					symbol == Token.REFERENCE_BEAN_SYMBOL) {
				int offset = name.indexOf(Token.BEAN_PROPERTY_DELIMITER);
				
				if(offset > 0) {
					String attrName = name.substring(0, offset);
					String propertyName = name.substring(offset + 1);
					
					if(propertyName.length() > 0) {
						name = attrName;
						getterName = propertyName;
					}
				}
			}
		
			tokenNameBuffer.setLength(0);
		} else {
			// when not exists tokenName then tokenType must be TEXT type
			type = TokenType.TEXT;
		}
		
		if(defTextBuffer.length() > 0) {
			defaultText = defTextBuffer.toString();
			defTextBuffer.setLength(0);
		}

		Token token = new Token(type, name);
		token.setDefaultText(defaultText);
		token.setGetterName(getterName);

		return token;
	}
	
	/**
	 * Returns a copy of the string, with leading and trailing whitespace omitted.
	 * <pre>
	 * "   \r\n   aaa  \r\n  bbb  "   ==&gt;   "\naaa  \n  bbb"
	 * "  aaa    \r\n   bbb   \r\n  "   ==&gt;   "aaa\nbbb\n"
	 * </pre>
	 * 
	 * @param sb the sb
	 * @param end the end
	 * @param trim the trim
	 * 
	 * @return the string
	 */
	private static String trimBuffer(StringBuilder sb, int end, boolean trim) {
		if(!trim)
			return sb.substring(0, end);
		
		int start = 0;
		boolean leadingLF = false;
		boolean tailingLF = false;
		char c;
		
		// leading whitespace
		for(int i = 0; i < end; i++) {
			c = sb.charAt(i);
			
			if(c == LF || c == CR) {
				leadingLF = true;
			} else if(!Character.isWhitespace(c)) {
				start = i;
				break;
			}
		}

		if(leadingLF && start == 0)
			return new Character(LF).toString();
		
		// tailing whitespace
		for(int i = end - 1; i > start; i--) {
			c = sb.charAt(i);
			
			if(c == LF || c == CR) {
				tailingLF = true;
			} else if(!Character.isWhitespace(c)) {
				end = i + 1;
				break;
			}
		}

		// restore a new line character which is leading whitespace
		if(leadingLF)
			sb.setCharAt(--start, LF);
		
		// restore a new line character which is tailing whitespace
		if(tailingLF)
			sb.setCharAt(end++, LF);
		
		return sb.substring(start, end);
	}
	
	/**
	 * Optimize tokens.
	 * 
	 * @param tokens the tokens
	 * 
	 * @return the token[]
	 */
	public static Token[] optimizeTokens(Token[] tokens) {
		if(tokens == null)
			return tokens;
		
		String firstDefaultText = null;
		String lastDefaultText = null;
		
		if(tokens.length == 1) {
			if(tokens[0].getType() == TokenType.TEXT)
				firstDefaultText = tokens[0].getDefaultText();
		} else if(tokens.length > 1) {
			if(tokens[0].getType() == TokenType.TEXT)
				firstDefaultText = tokens[0].getDefaultText();

			if(tokens[tokens.length - 1].getType() == TokenType.TEXT)
				lastDefaultText = tokens[tokens.length - 1].getDefaultText();
		}

		if(firstDefaultText != null) {
			String text = trimLeadingWhitespace(firstDefaultText);
			
			if(firstDefaultText != text)
				tokens[0] = new Token(TokenType.TEXT, text);
		}
		
		if(lastDefaultText != null) {
			String text = trimTailingWhitespace(lastDefaultText);

			if(lastDefaultText != text)
				tokens[tokens.length - 1] = new Token(TokenType.TEXT, text);
		}
		
		return tokens;
	}
	
	/**
	 * Trim leading whitespace.
	 * 
	 * @param string the string
	 * 
	 * @return the string
	 */
	private static String trimLeadingWhitespace(String string) {
		if(string.length() == 0)
			return string;
		
		int start = 0;
		char c;

		for(int i = 0; i < string.length(); i++) {
			c = string.charAt(i);
			
			if(!Character.isWhitespace(c)) {
				start = i;
				break;
			}
		}
		
		if(start == 0)
			return string;
		
		return string.substring(start);
	}
	
	/**
	 * Trim tailing whitespace.
	 * 
	 * @param string the string
	 * 
	 * @return the string
	 */
	private static String trimTailingWhitespace(String string) {
		if(string.length() == 0)
			return string;

		int end = 0;
		char c;
		
		for(int i = string.length() - 1; i >= 0; i--) {
			c = string.charAt(i);
			
			if(!Character.isWhitespace(c)) {
				end = i;
				break;
			}
		}
		
		if(end == 0)
			return string;
		
		return string.substring(0, end + 1);
	}
}
