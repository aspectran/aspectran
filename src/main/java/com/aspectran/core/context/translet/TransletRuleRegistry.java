/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.translet;

import java.util.List;

import com.aspectran.core.activity.variable.ParameterMap;
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.Tokenizer;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class TransletRuleRegistry {

	private TransletRuleMap transletRuleMap;
	
	private TransletRuleMap restfulTransletRuleMap;
	
	public TransletRuleRegistry(TransletRuleMap transletRuleMap) {
		this.transletRuleMap = transletRuleMap;
		
		TransletRuleMap restfulTransletRuleMap = new TransletRuleMap();
		
		for(TransletRule transletRule : transletRuleMap) {
			if(transletRule.getRestVerb() != null) {
				applyRestful(transletRule);
				restfulTransletRuleMap.putTransletRule(transletRule);
			}
		}
		
		if(!restfulTransletRuleMap.isEmpty())
			this.restfulTransletRuleMap = restfulTransletRuleMap;
	}
	
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public TransletRuleMap getRestfulTransletRuleMap() {
		return restfulTransletRuleMap;
	}
	
	public boolean contains(String transletName) {
		return transletRuleMap.containsKey(transletName);
	}
	
	public boolean contains(String transletName, RequestMethodType restVerb) {
		String restfulTransletName = TransletRule.makeRestfulTransletName(transletName, restVerb);
		return restfulTransletRuleMap.containsKey(restfulTransletName);
	}
	
	public TransletRule getTransletRule(String transletName) {
		return transletRuleMap.get(transletName);
	}
	
	public TransletRule getTransletRule(String transletName, RequestMethodType restVerb, ParameterMap pathVariableMap) {
		String restfulTransletName = TransletRule.makeRestfulTransletName(transletName, restVerb);
		TransletRule transletRule = findRestfulTransletRule(restfulTransletName);
		
		if(transletRule != null) {
			preparsePathVariableMap(transletName, transletRule.getNameTokens(), pathVariableMap);
		}
		
		return transletRule;
	}

	private TransletRule findRestfulTransletRule(String restfulTransletName) {
		if(restfulTransletRuleMap != null) {
			for(TransletRule transletRule : restfulTransletRuleMap) {
				WildcardPattern namePattern = transletRule.getNamePattern();
				
				if(namePattern.matches(restfulTransletName))
					return transletRule;
			}
		}
		
		return null;
	}
	
	public void destroy() {
		if(transletRuleMap != null) {
			transletRuleMap.clear();
			transletRuleMap = null;
		}
		if(restfulTransletRuleMap != null) {
			restfulTransletRuleMap.clear();
			restfulTransletRuleMap = null;
		}
	}
	
	private static void applyRestful(TransletRule transletRule) {
		String name = transletRule.getName();
		RequestMethodType restVerb = transletRule.getRestVerb();
		
		List<Token> tokenList = Tokenizer.tokenize(name, false);
		Token[] nameTokens = tokenList.toArray(new Token[tokenList.size()]);
		
		StringBuilder sb = new StringBuilder(name.length());
		
		for(Token token : nameTokens) {
			if(token.getType() == TokenType.PARAMETER) {
				sb.append(WildcardPattern.STAR_CHAR);
			} else {
				sb.append(token.toString());
			}
		}
		
		String restfulNamePattern = TransletRule.makeRestfulTransletName(sb.toString(), restVerb);
		WildcardPattern namePattern = WildcardPattern.compile(restfulNamePattern, AspectranConstant.TRANSLET_NAME_SEPARATOR);
		
		transletRule.setNamePattern(namePattern);
		transletRule.setNameTokens(nameTokens);
	}

	private static boolean preparsePathVariableMap(String requestTransletRuleName, Token[] nameTokens, ParameterMap pathVariableMap) {
		/*
			/example/customers/123-567/approval
			/example/customers/
			${id1}
			-
			${id2}
			/approval
		*/
		int beginIndex = 0;
		int endIndex = 0;
		Token prevToken = null;
		Token lastToken = null;
		
		for(Token token : nameTokens) {
			if(token.getType() != TokenType.PARAMETER) {
				String term = token.toString();

				endIndex = requestTransletRuleName.indexOf(term, beginIndex);
				
				if(endIndex == -1)
					return false;
				
				if(endIndex > beginIndex) {
					String value = requestTransletRuleName.substring(beginIndex, endIndex);
					if(value.length() > 0) {
						pathVariableMap.put(prevToken.getName(), value);
					} else if(prevToken.getDefaultValue() != null) {
						// If the last token ends with a "/" can be given a default value.
						pathVariableMap.put(prevToken.getName(), prevToken.getDefaultValue());
					}
					
					beginIndex += value.length();
				}
				
				beginIndex += term.length();
			} else if(token.getType() == TokenType.PARAMETER) {
				lastToken = token;
			}
			
			prevToken = token;
		}
		
		if(lastToken != null && prevToken == lastToken) {
			String value = requestTransletRuleName.substring(beginIndex);
			if(value.length() > 0) {
				pathVariableMap.put(lastToken.getName(), value);
			} else if(lastToken.getDefaultValue() != null) {
				// If the last token ends with a "/" can be given a default value.
				pathVariableMap.put(lastToken.getName(), lastToken.getDefaultValue());
			}
		}
		
		return true;
	}

}
