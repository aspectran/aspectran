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
package com.aspectran.core.context.expr.token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.context.rule.type.TokenType;

public class TokenParser {

	public static Token[] parse(String value) {
		return parse(value, false);
	}
	
	public static Token[] parse(String value, boolean trimStringToken) {
		if(value == null)
			return null;
		
		List<Token> tokenList = Tokenizer.tokenize(value, trimStringToken);

		Token[] tokens = null;

		if(tokenList.size() > 0) {
			tokens = tokenList.toArray(new Token[tokenList.size()]);
			
			if(!trimStringToken) {
				/**
				 * If you did not trim the string type token,
				 * first token string is left trim
				 * last token string is right trim
				 */
				tokens = Tokenizer.optimize(tokens);
			}
		} else {
			tokens = null;
		}
		
		return tokens;
	}
	
	public static List<Token[]> parseAsList(String value) {
		if(value == null)
			return null;
		
		List<Token> tokenList = Tokenizer.tokenize(value, true);
		
		List<Token[]> tokensList = null;
		
		for(int i = tokenList.size() - 1; i >= 0; i--) {
			Token t = tokenList.get(i);

			if(t.getType() == TokenType.TEXT && t.getDefaultValue() != null) {
				// remove empty token
				if(t.getDefaultValue().trim().length() == 0)
					tokenList.remove(i);
			}
		}
		
		if(tokenList.size() > 0) {
			tokensList = new ArrayList<Token[]>();
			
			for(int i = 0; i < tokensList.size(); i++) {
				Token[] ts = new Token[1];
				ts[0] = tokenList.get(i);
				tokensList.add(ts);
			}
		}
		
		return tokensList;
	}
	
	public static Map<String, Token[]> parseAsMap(String value) {
		if(value == null)
			return null;
		
		List<Token> tokenList = Tokenizer.tokenize(value, true);
		
		Map<String, Token[]> tokensMap = null;
		
		for(int i = tokenList.size() - 1; i >= 0; i--) {
			Token t = tokenList.get(i);

			if(t.getType() == TokenType.TEXT ||
					t.getName() == null || t.getName().length() == 0 ||
					t.getDefaultValue() == null || t.getDefaultValue().length() == 0) {
				tokenList.remove(i);
			}
		}

		if(tokenList.size() > 0) {
			tokensMap = new LinkedHashMap<String, Token[]>();
			
			for(Token t : tokenList) {
				Token[] ts = new Token[1];
				ts[0] = t;
				tokensMap.put(t.getName(), ts);
			}
		}
		
		return tokensMap;
	}

}
