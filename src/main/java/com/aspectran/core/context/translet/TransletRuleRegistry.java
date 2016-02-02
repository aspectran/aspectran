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
import java.util.List;
import java.util.Map;

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
import com.aspectran.core.context.translet.scan.TransletClassScanner;
import com.aspectran.core.context.translet.scan.TransletFileScanner;
import com.aspectran.core.context.variable.ParameterMap;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.ResourceUtils;
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
	
	private final TransletRuleMap restfulTransletRuleMap = new TransletRuleMap();
	
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

	public Collection<TransletRule> getTransletRules() {
		return transletRuleMap.values();
	}

	public void clear() {
		transletRuleMap.clear();
		restfulTransletRuleMap.clear();
	}
	
	public void addTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		if(defaultSettings != null) {
			transletRule.setTransletInterfaceClass(defaultSettings.getTransletInterfaceClass());
			transletRule.setTransletImplementClass(defaultSettings.getTransletImplementClass());
		}

		String scanPath = transletRule.getScanPath();

		if(scanPath != null) {
			if(scanPath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				TransletClassScanner scanner = new TransletClassScanner(applicationAdapter.getClassLoader());
				if(transletRule.getFilterParameters() != null)
					scanner.setFilterParameters(transletRule.getFilterParameters());

				Map<String, Class<?>> transletClassMap = scanner.scanClasses(scanPath);

				if(transletClassMap != null && !transletClassMap.isEmpty()) {
					for(Map.Entry<String, Class<?>> entry : transletClassMap.entrySet()) {
						String className = entry.getKey();
						Class<?> transletClass = entry.getValue();

						String transletName = null;

						//TODO

						TransletRule newTransletRule = TransletRule.newDerivedTransletRule(transletRule, transletName);
						putTransletRule(newTransletRule);
					}
				}
			} else {
				TransletFileScanner scanner = new TransletFileScanner(applicationAdapter.getApplicationBasePath(), applicationAdapter.getClassLoader());
				if(transletRule.getFilterParameters() != null)
					scanner.setFilterParameters(transletRule.getFilterParameters());
				if(transletRule.getMaskPattern() != null)
					scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
				else
					scanner.setTransletNameMaskPattern(scanPath);

				Map<String, File> templateFileMap = scanner.scanFiles(scanPath);

				if(templateFileMap != null && !templateFileMap.isEmpty()) {
					PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(transletRule.getName());

					for(Map.Entry<String, File> entry : templateFileMap.entrySet()) {
						String scannedTransletName = entry.getKey();
						TransletRule newTransletRule = TransletRule.newDerivedTransletRule(transletRule, scannedTransletName);

						if(prefixSuffixPattern.isSplited()) {
							newTransletRule.setName(prefixSuffixPattern.join(scannedTransletName));
						} else {
							if(transletRule.getName() != null) {
								newTransletRule.setName(transletRule.getName() + scannedTransletName);
							}
						}

						putTransletRule(newTransletRule);
					}
				}
			}
		} else {
			putTransletRule(transletRule);
		}
	}
	
	private void putTransletRule(TransletRule transletRule) throws CloneNotSupportedException {
		if(transletRule.getRequestRule() == null) {
			RequestRule requestRule = new RequestRule();
			transletRule.setRequestRule(requestRule);
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		
		if(responseRuleList == null || responseRuleList.isEmpty()) {
			transletRule.determineResponseRule();
			transletRule.setName(applyTransletNamePattern(transletRule.getName()));
			transletRuleMap.putTransletRule(transletRule);
			
			if(transletRule.getRestVerb() != null) {
				applyRestful(transletRule);
			}
			
			if(log.isTraceEnabled())
				log.trace("add TransletRule " + transletRule);
		} else if(responseRuleList.size() == 1) {
			transletRule.setResponseRule(responseRuleList.get(0));
			transletRule.determineResponseRule();
			transletRule.setName(applyTransletNamePattern(transletRule.getName()));
			transletRuleMap.putTransletRule(transletRule);

			if(transletRule.getRestVerb() != null) {
				applyRestful(transletRule);
			}
			
			if(log.isTraceEnabled())
				log.trace("add TransletRule " + transletRule);
		} else {
			ResponseRule defaultResponseRule = null;
			
			for(ResponseRule responseRule : responseRuleList) {
				String responseName = responseRule.getName();
				
				if(responseName == null || responseName.length() == 0) {
					if(defaultResponseRule != null) {
						log.warn("ignore duplicated default response rule " + defaultResponseRule + " of transletRule " + transletRule);
					}
					defaultResponseRule = responseRule;
				} else {
					TransletRule subTransletRule = TransletRule.newSubTransletRule(transletRule, responseRule);
					subTransletRule.determineResponseRule();
					subTransletRule.setName(applyTransletNamePattern(subTransletRule.getName()));
					transletRuleMap.putTransletRule(subTransletRule);
					
					if(transletRule.getRestVerb() != null) {
						applyRestful(transletRule);
					}
					
					if(log.isTraceEnabled())
						log.trace("add sub TransletRule " + subTransletRule);
				}
			}
			
			if(defaultResponseRule != null) {
				transletRule.setResponseRule(defaultResponseRule);
				transletRule.determineResponseRule();
				transletRule.setName(applyTransletNamePattern(transletRule.getName()));
				transletRuleMap.putTransletRule(transletRule);
				
				if(transletRule.getRestVerb() != null) {
					applyRestful(transletRule);
				}
				
				if(log.isTraceEnabled())
					log.trace("add TransletRule " + transletRule);
			}
		}
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
	
	private void applyRestful(TransletRule transletRule) {
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
		WildcardPattern namePattern = WildcardPattern.compile(restfulNamePattern, AspectranConstants.TRANSLET_NAME_SEPARATOR);
		
		transletRule.setNamePattern(namePattern);
		transletRule.setNameTokens(nameTokens);
		
		restfulTransletRuleMap.putTransletRule(transletRule);
	}

	private boolean preparsePathVariableMap(String requestTransletRuleName, Token[] nameTokens, ParameterMap pathVariableMap) {
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
