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
package com.aspectran.core.context.translet;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aspectran.core.activity.PathVariableMap;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.context.builder.AssistantLocal;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.translet.scan.TransletFileScanner;
import com.aspectran.core.util.FileScanner;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class TransletRuleRegistry.
 */
public class TransletRuleRegistry {
	
	private final Log log = LogFactory.getLog(TransletRuleRegistry.class);

	private final ApplicationAdapter applicationAdapter;
	
	private final TransletRuleMap transletRuleMap = new TransletRuleMap();
	
	private final Set<TransletRule> restfulTransletRuleSet = new HashSet<TransletRule>();
	
	private AssistantLocal assistantLocal;
	
	public TransletRuleRegistry(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}
	
	public AssistantLocal getAssistantLocal() {
		return assistantLocal;
	}

	public void setAssistantLocal(AssistantLocal assistantLocal) {
		this.assistantLocal = assistantLocal;
	}

	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public Set<TransletRule> getRestfulTransletRuleSet() {
		return restfulTransletRuleSet;
	}
	
	public boolean contains(String transletName) {
		return transletRuleMap.containsKey(transletName);
	}

	public TransletRule getTransletRule(String transletName) {
		return transletRuleMap.get(transletName);
	}
	
	public PathVariableMap getPathVariableMap(String transletName, RequestMethodType requestMethod) {
		if(restfulTransletRuleSet.isEmpty())
			return null;

		TransletRule transletRule = findRestfulTransletRule(transletName, requestMethod);
		PathVariableMap pathVariableMap = null;

		if(transletRule != null) {
			pathVariableMap = new PathVariableMap(transletRule);
			preparsePathVariableMap(transletName, transletRule.getNameTokens(), pathVariableMap);
		}

		return pathVariableMap;
	}

	private TransletRule findRestfulTransletRule(String transletName, RequestMethodType requestMethod) {
		for(TransletRule transletRule : restfulTransletRuleSet) {
			WildcardPattern namePattern = transletRule.getNamePattern();
			if(namePattern.matches(transletName)) {
				if(transletRule.getRequestMethods() == null ||
						requestMethod.containsTo(transletRule.getRequestMethods()))
					return transletRule;
			}
		}

		return null;
	}

	public Collection<TransletRule> getTransletRules() {
		return transletRuleMap.values();
	}

	public void clear() {
		transletRuleMap.clear();
		restfulTransletRuleSet.clear();
	}
	
	public void addTransletRule(final TransletRule transletRule) throws CloneNotSupportedException {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		if(defaultSettings != null) {
			transletRule.setTransletInterfaceClass(defaultSettings.getTransletInterfaceClass());
			transletRule.setTransletImplementClass(defaultSettings.getTransletImplementClass());
		}

		String scanPath = transletRule.getScanPath();

		if(scanPath != null) {
			TransletFileScanner scanner = new TransletFileScanner(applicationAdapter.getApplicationBasePath(), applicationAdapter.getClassLoader());
			if(transletRule.getFilterParameters() != null)
				scanner.setFilterParameters(transletRule.getFilterParameters());
			if(transletRule.getMaskPattern() != null)
				scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
			else
				scanner.setTransletNameMaskPattern(scanPath);

			final PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(transletRule.getName());

			scanner.scan(scanPath, new FileScanner.SaveHandler() {
				@Override
				public void save(String filePath, File scannedFile) {
					String scannedTransletName = filePath;
					TransletRule newTransletRule = TransletRule.replicate(transletRule, scannedTransletName);

					if(prefixSuffixPattern.isSplited()) {
						newTransletRule.setName(prefixSuffixPattern.join(scannedTransletName));
					} else {
						if(transletRule.getName() != null) {
							newTransletRule.setName(transletRule.getName() + scannedTransletName);
						}
					}

					parseTransletRule(newTransletRule);

				}
			});
		} else {
			parseTransletRule(transletRule);
		}
	}
	
	private void parseTransletRule(TransletRule transletRule) {
		if(transletRule.getRequestRule() == null) {
			RequestRule requestRule = new RequestRule();
			transletRule.setRequestRule(requestRule);
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		
		if(responseRuleList == null || responseRuleList.isEmpty()) {
			putTransletRule(transletRule);
		} else if(responseRuleList.size() == 1) {
			transletRule.setResponseRule(responseRuleList.get(0));
			putTransletRule(transletRule);
		} else {
			ResponseRule defaultResponseRule = null;
			
			for(ResponseRule responseRule : responseRuleList) {
				String responseName = responseRule.getName();
				
				if(responseName == null || responseName.isEmpty()) {
					if(defaultResponseRule != null) {
						log.warn("ignore duplicated default response rule " + defaultResponseRule + " of transletRule " + transletRule);
					}
					defaultResponseRule = responseRule;
				} else {
					TransletRule subTransletRule = TransletRule.replicate(transletRule, responseRule);
					putTransletRule(subTransletRule);
				}
			}
			
			if(defaultResponseRule != null) {
				transletRule.setResponseRule(defaultResponseRule);
				putTransletRule(transletRule);
			}
		}
	}

	private void putTransletRule(TransletRule transletRule) {
		String transletName = applyTransletNamePattern(transletRule.getName());

		transletRule.determineResponseRule();
		transletRule.setName(transletName);

		if(transletRule.getRequestMethods() != null) {
			String key = TransletRule.makeRestfulTransletName(transletName, transletRule.getRequestMethods());
			transletRuleMap.put(key, transletRule);
			putRestfulTransletRule(transletRule);
		} else {
			transletRuleMap.putTransletRule(transletRule);
		}

		if(log.isTraceEnabled())
			log.trace("add TransletRule " + transletRule);

	}

	private void putRestfulTransletRule(TransletRule transletRule) {
		String transletName = transletRule.getName();
		List<Token> tokenList = Tokenizer.tokenize(transletName, false);
		Token[] nameTokens = tokenList.toArray(new Token[tokenList.size()]);

		StringBuilder sb = new StringBuilder(transletName.length());
		for(Token token : nameTokens) {
			if(token.getType() == TokenType.PARAMETER || token.getType() == TokenType.ATTRIBUTE) {
				sb.append(WildcardPattern.STAR_CHAR);
			} else {
				sb.append(token.stringify());
			}
		}

		String wildTransletName = sb.toString();
		WildcardPattern namePattern = WildcardPattern.compile(wildTransletName, AspectranConstants.TRANSLET_NAME_SEPARATOR);

		transletRule.setNamePattern(namePattern);
		transletRule.setNameTokens(nameTokens);

		restfulTransletRuleSet.add(transletRule);
	}

	/**
	 * Returns the trnaslet name of the prefix and suffix are combined.
	 * 
	 * @param transletName the translet name
	 * @return the string
	 */
	public String applyTransletNamePattern(String transletName) {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		
		if(defaultSettings == null)
			return transletName;

		if(transletName != null && transletName.length() > 0 && transletName.charAt(0) == AspectranConstants.TRANSLET_NAME_SEPARATOR)
			return transletName;

		if(defaultSettings.getTransletNamePrefix() == null && 
				defaultSettings.getTransletNameSuffix() == null)
			return transletName;
		
		StringBuilder sb = new StringBuilder();
		
		if(defaultSettings.getTransletNamePrefix() != null)
			sb.append(defaultSettings.getTransletNamePrefix());

		sb.append(transletName);
		
		if(defaultSettings.getTransletNameSuffix() != null)
			sb.append(defaultSettings.getTransletNameSuffix());
		
		return sb.toString();
	}

	private boolean preparsePathVariableMap(String requestTransletRuleName, Token[] nameTokens, PathVariableMap pathVariableMap) {
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
			TokenType type = token.getType();

			if(type == TokenType.PARAMETER || type == TokenType.ATTRIBUTE) {
				lastToken = token;
			} else {
				String term = token.stringify();

				endIndex = requestTransletRuleName.indexOf(term, beginIndex);

				if(endIndex == -1)
					return false;

				if(endIndex > beginIndex) {
					String value = requestTransletRuleName.substring(beginIndex, endIndex);
					if(value.length() > 0) {
						pathVariableMap.put(prevToken, value);
					} else if(prevToken.getValue() != null) {
						// If the last token ends with a "/" can be given a default value.
						pathVariableMap.put(prevToken, prevToken.getValue());
					}

					beginIndex += value.length();
				}

				beginIndex += term.length();
			}
			
			prevToken = token;
		}
		
		if(lastToken != null && prevToken == lastToken) {
			String value = requestTransletRuleName.substring(beginIndex);
			if(value.length() > 0) {
				pathVariableMap.put(lastToken, value);
			} else if(lastToken.getValue() != null) {
				// If the last token ends with a "/" can be given a default value.
				pathVariableMap.put(lastToken, lastToken.getValue());
			}
		}
		
		return true;
	}

}
