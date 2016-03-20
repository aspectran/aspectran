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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.PathVariableMap;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.AssistantLocal;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.translet.scan.TransletFileScanner;
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
	
	private final Map<String, TransletRule> transletRuleMap = new LinkedHashMap<String, TransletRule>();
	
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

	public Map<String, TransletRule> getTransletRuleMap() {
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
	
	public PathVariableMap getPathVariableMap(TransletRule transletRule, String requestTransletName) {
		Token[] nameTokens = transletRule.getNameTokens();
		if(nameTokens == null || nameTokens.length == 1 && nameTokens[0].getType() == TokenType.TEXT)
			return null;

		return PathVariableMap.newInstance(nameTokens, requestTransletName);
	}

	public TransletRule getRestfulTransletRule(String requestTransletName, RequestMethodType requestMethod) {
		if(restfulTransletRuleSet.isEmpty())
			return null;

		for(TransletRule transletRule : restfulTransletRuleSet) {
			WildcardPattern namePattern = transletRule.getNamePattern();
			if(namePattern != null) {
				if(namePattern.matches(requestTransletName)) {
					if(transletRule.getRequestMethods() == null || requestMethod.containsTo(transletRule.getRequestMethods()))
						return transletRule;
				}
			} else {
				if(requestTransletName.equals(transletRule.getName())) {
					if(transletRule.getRequestMethods() == null || requestMethod.containsTo(transletRule.getRequestMethods()))
						return transletRule;
				}
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
	
	public void addTransletRule(final TransletRule transletRule) {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();

		if(defaultSettings != null) {
			transletRule.setTransletInterfaceClass(defaultSettings.getTransletInterfaceClass());
			transletRule.setTransletImplementationClass(defaultSettings.getTransletImplementationClass());
		}

		String scanPath = transletRule.getScanPath();

		if(scanPath != null) {
			TransletFileScanner scanner = new TransletFileScanner(applicationAdapter.getApplicationBasePath(), applicationAdapter.getClassLoader());

			if(transletRule.getFilterParameters() != null) {
				scanner.setFilterParameters(transletRule.getFilterParameters());
			}
			if(transletRule.getMaskPattern() != null) {
				scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
			} else {
				scanner.setTransletNameMaskPattern(scanPath);
			}

			PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(transletRule.getName());

			scanner.scan(scanPath, (filePath, scannedFile) -> {
                TransletRule newTransletRule = TransletRule.replicate(transletRule, filePath);

                if(prefixSuffixPattern.isSplited()) {
                    newTransletRule.setName(prefixSuffixPattern.join(filePath));
                } else {
                    if(transletRule.getName() != null) {
                        newTransletRule.setName(transletRule.getName() + filePath);
                    }
                }

                dissectTransletRule(newTransletRule);
            });
		} else {
			dissectTransletRule(transletRule);
		}
	}
	
	private void dissectTransletRule(TransletRule transletRule) {
		if(transletRule.getRequestRule() == null) {
			RequestRule requestRule = new RequestRule();
			transletRule.setRequestRule(requestRule);
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		
		if(responseRuleList == null || responseRuleList.isEmpty()) {
			saveTransletRule(transletRule);
		} else if(responseRuleList.size() == 1) {
			transletRule.setResponseRule(responseRuleList.get(0));
			saveTransletRule(transletRule);
		} else {
			ResponseRule defaultResponseRule = null;
			
			for(ResponseRule responseRule : responseRuleList) {
				String responseName = responseRule.getName();
				
				if(responseName == null || responseName.isEmpty()) {
					if(defaultResponseRule != null) {
						log.warn("Ignore duplicated default response rule " + defaultResponseRule + " of transletRule " + transletRule);
					}
					defaultResponseRule = responseRule;
				} else {
					TransletRule subTransletRule = transletRule.replicate();
					subTransletRule.setResponseRule(responseRule);
					saveTransletRule(subTransletRule);
				}
			}
			
			if(defaultResponseRule != null) {
				transletRule.setResponseRule(defaultResponseRule);
				saveTransletRule(transletRule);
			}
		}
	}

	private void saveTransletRule(TransletRule transletRule) {
		transletRule.determineResponseRule();

		String transletName = applyTransletNamePattern(transletRule.getName());
		transletRule.setName(transletName);

		if(transletRule.getRequestMethods() != null) {
			String key = TransletRule.makeRestfulTransletName(transletName, transletRule.getRequestMethods());
			transletRuleMap.put(key, transletRule);
			saveRestfulTransletRule(transletRule);
		} else {
			transletRuleMap.put(transletRule.getName(), transletRule);
		}

		if(log.isTraceEnabled())
			log.trace("add TransletRule " + transletRule);
	}

	private void saveRestfulTransletRule(TransletRule transletRule) {
		String transletName = transletRule.getName();
		List<Token> tokenList = Tokenizer.tokenize(transletName, false);
		Token[] nameTokens = tokenList.toArray(new Token[tokenList.size()]);

		StringBuilder wildsTransletName = new StringBuilder(transletName.length());
		boolean wilds = false;

		for(Token token : nameTokens) {
			if(token.getType() == TokenType.PARAMETER || token.getType() == TokenType.ATTRIBUTE) {
				wildsTransletName.append(WildcardPattern.STAR_CHAR);
				wilds = true;
			} else {
				String tokenString = token.stringify();
				wildsTransletName.append(tokenString);
			}
		}

		if(wilds) {
			WildcardPattern namePattern = WildcardPattern.compile(wildsTransletName.toString(), ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);

			transletRule.setNamePattern(namePattern);
			transletRule.setNameTokens(nameTokens);
		}

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

		if(transletName != null && transletName.length() > 0 && transletName.charAt(0) == ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR)
			return transletName;

		if(defaultSettings.getTransletNamePrefix() == null && 
				defaultSettings.getTransletNameSuffix() == null)
			return transletName;
		
		StringBuilder sb = new StringBuilder();
		
		if(defaultSettings.getTransletNamePrefix() != null)
			sb.append(defaultSettings.getTransletNamePrefix());

		if(transletName != null)
			sb.append(transletName);
		
		if(defaultSettings.getTransletNameSuffix() != null)
			sb.append(defaultSettings.getTransletNameSuffix());
		
		return sb.toString();
	}

}
