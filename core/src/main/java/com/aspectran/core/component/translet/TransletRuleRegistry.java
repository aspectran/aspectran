/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.component.translet;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.translet.scan.TransletFileScanner;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class TransletRuleRegistry.
 */
public class TransletRuleRegistry extends AbstractComponent {

    private final Log log = LogFactory.getLog(TransletRuleRegistry.class);

    private final Map<String, TransletRule> transletRuleMap = new LinkedHashMap<>();

    private final Set<TransletRule> restfulTransletRuleSet = new HashSet<>();

    private final String basePath;

    private final ClassLoader classLoader;

    private AssistantLocal assistantLocal;

    public TransletRuleRegistry(Environment environment) {
        this.basePath = environment.getBasePath();
        this.classLoader = environment.getClassLoader();
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

    public TransletRule getRestfulTransletRule(String requestTransletName, MethodType requestMethod) {
        if (!restfulTransletRuleSet.isEmpty()) {
            for (TransletRule transletRule : restfulTransletRuleSet) {
                WildcardPattern namePattern = transletRule.getNamePattern();
                if (namePattern != null) {
                    if (namePattern.matches(requestTransletName)) {
                        if (transletRule.getAllowedMethods() == null
                                || requestMethod.containsTo(transletRule.getAllowedMethods())) {
                            return transletRule;
                        }
                    }
                } else {
                    if (requestTransletName.equals(transletRule.getName())) {
                        if (transletRule.getAllowedMethods() == null
                                || requestMethod.containsTo(transletRule.getAllowedMethods())) {
                            return transletRule;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Collection<TransletRule> getTransletRules() {
        return transletRuleMap.values();
    }

    public void addTransletRule(final TransletRule transletRule) {
        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        if (defaultSettings != null) {
            transletRule.setTransletInterfaceClass(defaultSettings.getTransletInterfaceClass());
            transletRule.setTransletImplementationClass(defaultSettings.getTransletImplementationClass());
        }

        String scanPath = transletRule.getScanPath();
        if (scanPath != null) {
            TransletFileScanner scanner = new TransletFileScanner(basePath, classLoader);
            if (transletRule.getFilterParameters() != null) {
                scanner.setFilterParameters(transletRule.getFilterParameters());
            }
            if (transletRule.getMaskPattern() != null) {
                scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
            } else {
                scanner.setTransletNameMaskPattern(scanPath);
            }
            PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(transletRule.getName());
            scanner.scan(scanPath, (filePath, scannedFile) -> {
                TransletRule newTransletRule = TransletRule.replicate(transletRule, filePath);
                if (prefixSuffixPattern.isSplitted()) {
                    newTransletRule.setName(prefixSuffixPattern.join(filePath));
                } else {
                    if (transletRule.getName() != null) {
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
        if (transletRule.getRequestRule() == null) {
            RequestRule requestRule = new RequestRule();
            transletRule.setRequestRule(requestRule);
        }

        List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
        if (responseRuleList == null || responseRuleList.isEmpty()) {
            saveTransletRule(transletRule);
        } else if (responseRuleList.size() == 1) {
            transletRule.setResponseRule(responseRuleList.get(0));
            saveTransletRule(transletRule);
        } else {
            ResponseRule defaultResponseRule = null;
            for (ResponseRule responseRule : responseRuleList) {
                String responseName = responseRule.getName();
                if (responseName == null || responseName.isEmpty()) {
                    if (defaultResponseRule != null) {
                        log.warn("Ignore duplicated default response rule " + defaultResponseRule +
                                " of transletRule " + transletRule);
                    }
                    defaultResponseRule = responseRule;
                } else {
                    TransletRule subTransletRule = transletRule.replicate();
                    subTransletRule.setResponseRule(responseRule);
                    saveTransletRule(subTransletRule);
                }
            }
            if (defaultResponseRule != null) {
                transletRule.setResponseRule(defaultResponseRule);
                saveTransletRule(transletRule);
            }
        }
    }

    private void saveTransletRule(TransletRule transletRule) {
        transletRule.determineResponseRule();

        String transletName = applyTransletNamePattern(transletRule.getName());
        transletRule.setName(transletName);

        if (transletRule.getAllowedMethods() != null || TransletRule.isRestfulTransletName(transletName)) {
            String key = TransletRule.makeRestfulTransletName(transletName, transletRule.getAllowedMethods());
            transletRuleMap.put(key, transletRule);
            saveRestfulTransletRule(transletRule);
        } else {
            transletRuleMap.put(transletName, transletRule);
        }

        if (log.isTraceEnabled()) {
            log.trace("add TransletRule " + transletRule);
        }
    }

    private void saveRestfulTransletRule(TransletRule transletRule) {
        final String transletName = transletRule.getName();
        List<Token> tokenList = Tokenizer.tokenize(transletName, false);
        Token[] nameTokens = tokenList.toArray(new Token[0]);
        StringBuilder sb = new StringBuilder(transletName.length());

        for (Token token : nameTokens) {
            if (token.getType() == TokenType.PARAMETER || token.getType() == TokenType.ATTRIBUTE) {
                sb.append(WildcardPattern.STAR_CHAR);
            } else {
                String tokenString = token.stringify();
                sb.append(tokenString);
            }
        }

        String newTransletName = sb.toString();
        if (WildcardPattern.hasWildcards(newTransletName)) {
            WildcardPattern namePattern = WildcardPattern.compile(newTransletName, ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
            transletRule.setNamePattern(namePattern);
            transletRule.setNameTokens(nameTokens);
        }

        restfulTransletRuleSet.add(transletRule);
    }

    /**
     * Returns the translet name of the prefix and suffix are combined.
     *
     * @param transletName the translet name
     * @return the new translet name
     */
    public String applyTransletNamePattern(String transletName) {
        return applyTransletNamePattern(transletName, false);
    }

    /**
     * Returns the translet name of the prefix and suffix are combined.
     *
     * @param transletName the translet name
     * @param absolutely whether to allow absolutely name for translet
     * @return the new translet name
     */
    public String applyTransletNamePattern(String transletName, boolean absolutely) {
        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        if (defaultSettings == null) {
            return transletName;
        }
        if (StringUtils.startsWith(transletName, ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR)) {
            if (absolutely) {
                return transletName;
            }
            transletName = transletName.substring(1);
        }
        if (defaultSettings.getTransletNamePrefix() == null
                && defaultSettings.getTransletNameSuffix() == null) {
            return transletName;
        }

        StringBuilder sb = new StringBuilder();
        if (defaultSettings.getTransletNamePrefix() != null) {
            sb.append(defaultSettings.getTransletNamePrefix());
        }
        if (transletName != null) {
            sb.append(transletName);
        }
        if (defaultSettings.getTransletNameSuffix() != null) {
            sb.append(defaultSettings.getTransletNameSuffix());
        }
        return sb.toString();
    }

    @Override
    protected void doInitialize() {
        // Nothing to do
    }

    @Override
    protected void doDestroy() {
        transletRuleMap.clear();
        restfulTransletRuleSet.clear();
    }

}
