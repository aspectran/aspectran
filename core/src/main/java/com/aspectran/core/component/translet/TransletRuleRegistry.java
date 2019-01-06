/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Class TransletRuleRegistry.
 */
public class TransletRuleRegistry extends AbstractComponent {

    private static final Log log = LogFactory.getLog(TransletRuleRegistry.class);

    private final Map<String, TransletRule> transletRuleMap = new LinkedHashMap<>(256);

    private final Map<String, TransletRule> getTransletRuleMap = new HashMap<>();

    private final Map<String, TransletRule> postTransletRuleMap = new HashMap<>();

    private final Map<String, TransletRule> putTransletRuleMap = new HashMap<>();

    private final Map<String, TransletRule> patchTransletRuleMap = new HashMap<>();

    private final Map<String, TransletRule> deleteTransletRuleMap = new HashMap<>();

    private final Comparator<TransletRule> comparator = new WeightComparator();

    private final Set<TransletRule> wildGetTransletRuleSet = new TreeSet<>(comparator);

    private final Set<TransletRule> wildPostTransletRuleSet = new TreeSet<>(comparator);

    private final Set<TransletRule> wildPutTransletRuleSet = new TreeSet<>(comparator);

    private final Set<TransletRule> wildPatchTransletRuleSet = new TreeSet<>(comparator);

    private final Set<TransletRule> wildDeleteTransletRuleSet = new TreeSet<>(comparator);

    private final Set<TransletRule> etcTransletRuleSet = new TreeSet<>(comparator);

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

    public Collection<TransletRule> getTransletRules() {
        return transletRuleMap.values();
    }

    public TransletRule getTransletRule(String transletName) {
        return getTransletRule(transletName, MethodType.GET);
    }

    public TransletRule getTransletRule(String transletName, MethodType requestMethod) {
        if (requestMethod == null) {
            throw new IllegalArgumentException("requestMethod must not be null");
        }

        TransletRule transletRule;
        switch(requestMethod) {
            case GET:
                transletRule = transletRuleMap.get(transletName);
                if (transletRule == null) {
                    transletRule = getTransletRuleMap.get(transletName);
                }
                if (transletRule == null) {
                    transletRule = lookupWildTransletRule(wildGetTransletRuleSet, transletName);
                }
                break;
            case POST:
                transletRule = postTransletRuleMap.get(transletName);
                if (transletRule == null) {
                    transletRule = lookupWildTransletRule(wildPostTransletRuleSet, transletName);
                }
                break;
            case PUT:
                transletRule = putTransletRuleMap.get(transletName);
                if (transletRule == null) {
                    transletRule = lookupWildTransletRule(wildPutTransletRuleSet, transletName);
                }
                break;
            case PATCH:
                transletRule = patchTransletRuleMap.get(transletName);
                if (transletRule == null) {
                    transletRule = lookupWildTransletRule(wildPatchTransletRuleSet, transletName);
                }
                break;
            case DELETE:
                transletRule = deleteTransletRuleMap.get(transletName);
                if (transletRule == null) {
                    transletRule = lookupWildTransletRule(wildDeleteTransletRuleSet, transletName);
                }
                break;
            default:
                transletRule = lookupEtcTransletRule(transletName, requestMethod);
        }
        if (transletRule == null && requestMethod != MethodType.GET) {
            transletRule = transletRuleMap.get(transletName);
            if (transletRule == null) {
                transletRule = lookupWildTransletRule(wildGetTransletRuleSet, transletName);
            }
        }
        return transletRule;
    }

    private TransletRule lookupWildTransletRule(Set<TransletRule> transletRuleSet, String transletName) {
        if (!transletRuleSet.isEmpty()) {
            for (TransletRule transletRule : transletRuleSet) {
                WildcardPattern namePattern = transletRule.getNamePattern();
                if (namePattern != null) {
                    if (namePattern.matches(transletName)) {
                        return transletRule;
                    }
                } else {
                    if (transletName.equals(transletRule.getName())) {
                        return transletRule;
                    }
                }
            }
        }
        return null;
    }

    private TransletRule lookupEtcTransletRule(String transletName, MethodType requestMethod) {
        if (!etcTransletRuleSet.isEmpty()) {
            for (TransletRule transletRule : etcTransletRuleSet) {
                if (requestMethod.containsTo(transletRule.getAllowedMethods())) {
                    WildcardPattern namePattern = transletRule.getNamePattern();
                    if (namePattern != null) {
                        if (namePattern.matches(transletName)) {
                            return transletRule;
                        }
                    } else {
                        if (transletName.equals(transletRule.getName())) {
                            return transletRule;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean contains(String transletName) {
        return contains(transletName, MethodType.GET);
    }

    public boolean contains(String transletName, MethodType requestMethod) {
        return (getTransletRule(transletName, requestMethod) != null);
    }

    public void addTransletRule(final TransletRule transletRule) {
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

        MethodType[] allowedMethods = transletRule.getAllowedMethods();
        if (hasPathVariables(transletName)) {
            savePathVariables(transletRule);
            if (allowedMethods != null) {
                String restfulTransletName = assembleTransletName(transletName, allowedMethods);
                transletRuleMap.put(restfulTransletName, transletRule);
                for (MethodType methodType : allowedMethods) {
                    switch (methodType) {
                        case GET:
                            wildGetTransletRuleSet.add(transletRule);
                            break;
                        case POST:
                            wildPostTransletRuleSet.add(transletRule);
                            break;
                        case PUT:
                            wildPutTransletRuleSet.add(transletRule);
                            break;
                        case PATCH:
                            wildPatchTransletRuleSet.add(transletRule);
                            break;
                        case DELETE:
                            wildDeleteTransletRuleSet.add(transletRule);
                            break;
                        default:
                            etcTransletRuleSet.add(transletRule);
                    }
                }
            } else {
                transletRuleMap.put(transletName, transletRule);
                wildGetTransletRuleSet.add(transletRule);
            }
        } else {
            if (allowedMethods != null) {
                String restfulTransletName = assembleTransletName(transletName, allowedMethods);
                transletRuleMap.put(restfulTransletName, transletRule);
                for (MethodType methodType : allowedMethods) {
                    switch(methodType) {
                        case GET:
                            getTransletRuleMap.put(transletName, transletRule);
                            break;
                        case POST:
                            postTransletRuleMap.put(transletName, transletRule);
                            break;
                        case PUT:
                            putTransletRuleMap.put(transletName, transletRule);
                            break;
                        case PATCH:
                            patchTransletRuleMap.put(transletName, transletRule);
                            break;
                        case DELETE:
                            deleteTransletRuleMap.put(transletName, transletRule);
                            break;
                        default:
                            etcTransletRuleSet.add(transletRule);
                    }
                }
            } else {
                transletRuleMap.put(transletName, transletRule);
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("add TransletRule " + transletRule);
        }
    }

    private void savePathVariables(TransletRule transletRule) {
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

        String wildTransletName = sb.toString();
        if (WildcardPattern.hasWildcards(wildTransletName)) {
            WildcardPattern namePattern = WildcardPattern.compile(wildTransletName,
                    ActivityContext.NAME_SEPARATOR_CHAR);
            transletRule.setNamePattern(namePattern);
            transletRule.setNameTokens(nameTokens);
        }
    }

    private boolean hasPathVariables(String transletName) {
        return ((transletName.contains("${") || transletName.contains("@{")) && transletName.contains("}"));
    }

    private String assembleTransletName(String transletName, MethodType[] allowedMethods) {
        if (allowedMethods != null) {
            if (allowedMethods.length > 1) {
                int len = transletName.length() + (allowedMethods.length * 8);
                StringBuilder sb = new StringBuilder(len);
                for (MethodType type : allowedMethods) {
                    sb.append(type).append(" ");
                }
                sb.append(transletName);
                return sb.toString();
            } else if (allowedMethods.length == 1) {
                return assembleRestfulTransletName(transletName, allowedMethods[0]);
            }
        }
        return assembleRestfulTransletName(transletName, MethodType.GET);
    }

    private String assembleRestfulTransletName(String transletName, MethodType requestMethod) {
        return (requestMethod + " " + transletName);
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
        if (StringUtils.startsWith(transletName, ActivityContext.NAME_SEPARATOR_CHAR)) {
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
        getTransletRuleMap.clear();
        postTransletRuleMap.clear();
        putTransletRuleMap.clear();
        patchTransletRuleMap.clear();
        deleteTransletRuleMap.clear();
        wildGetTransletRuleSet.clear();
        wildPostTransletRuleSet.clear();
        wildPutTransletRuleSet.clear();
        wildPatchTransletRuleSet.clear();
        wildDeleteTransletRuleSet.clear();
        etcTransletRuleSet.clear();
    }

    class WeightComparator implements Comparator<TransletRule> {

        @Override
        public int compare(TransletRule transletRule1, TransletRule transletRule2) {
            if (transletRule1.getNamePattern() != null && transletRule2.getNamePattern() != null) {
                float weight1 = transletRule1.getNamePattern().getWeight();
                float weight2 = transletRule2.getNamePattern().getWeight();
                int cmp = Float.compare(weight2, weight1);
                if (cmp == 0) {
                    cmp = transletRule1.getNamePattern().toString().compareTo(transletRule2.getNamePattern().toString());
                }
                return cmp;
            } else if (transletRule1.getNamePattern() != null) {
                return transletRule2.getName().compareTo(transletRule1.getNamePattern().toString());
            } else if (transletRule2.getNamePattern() != null) {
                return transletRule1.getName().compareTo(transletRule2.getNamePattern().toString());
            } else {
                return transletRule2.getName().compareTo(transletRule1.getName());
            }
        }

    }

}
