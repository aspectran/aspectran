/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.core.component.translet.scan.TransletScanFilter;
import com.aspectran.core.component.translet.scan.TransletScanner;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.Tokenizer;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.rule.util.Namespace;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.PrefixSuffixPattern;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.aspectran.core.context.ActivityContext.NAME_SEPARATOR_CHAR;

/**
 * The Class TransletRuleRegistry.
 */
public class TransletRuleRegistry extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(TransletRuleRegistry.class);

    private final Map<String, TransletRule> transletRuleMap = new LinkedHashMap<>();

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

    public TransletRuleRegistry(String basePath, ClassLoader classLoader) {
        this.basePath = basePath;
        this.classLoader = classLoader;
    }

    public void setAssistantLocal(AssistantLocal assistantLocal) {
        this.assistantLocal = assistantLocal;
    }

    public Collection<TransletRule> getTransletRules() {
        return transletRuleMap.values();
    }

    public TransletRule getTransletRule(String requestName) {
        return getTransletRule(requestName, MethodType.GET);
    }

    public TransletRule getTransletRule(String requestName, MethodType requestMethod) {
        if (requestName == null) {
            throw new IllegalArgumentException("requestName must not be null");
        }
        if (requestMethod == null) {
            throw new IllegalArgumentException("requestMethod must not be null");
        }

        TransletRule transletRule;
        switch (requestMethod) {
            case GET:
                transletRule = getTransletRuleMap.get(requestName);
                if (transletRule == null) {
                    transletRule = retrieveWildTransletRule(wildGetTransletRuleSet, requestName);
                }
                break;
            case POST:
                transletRule = postTransletRuleMap.get(requestName);
                if (transletRule == null) {
                    transletRule = retrieveWildTransletRule(wildPostTransletRuleSet, requestName);
                }
                break;
            case PUT:
                transletRule = putTransletRuleMap.get(requestName);
                if (transletRule == null) {
                    transletRule = retrieveWildTransletRule(wildPutTransletRuleSet, requestName);
                }
                break;
            case PATCH:
                transletRule = patchTransletRuleMap.get(requestName);
                if (transletRule == null) {
                    transletRule = retrieveWildTransletRule(wildPatchTransletRuleSet, requestName);
                }
                break;
            case DELETE:
                transletRule = deleteTransletRuleMap.get(requestName);
                if (transletRule == null) {
                    transletRule = retrieveWildTransletRule(wildDeleteTransletRuleSet, requestName);
                }
                break;
            default:
                transletRule = retrieveEtcTransletRule(requestName, requestMethod);
        }
        if (transletRule == null && requestMethod != MethodType.GET) {
            transletRule = transletRuleMap.get(requestName);
            if (transletRule == null) {
                transletRule = retrieveWildTransletRule(wildGetTransletRuleSet, requestName);
            }
        }
        return transletRule;
    }

    @Nullable
    private TransletRule retrieveWildTransletRule(@NonNull Set<TransletRule> transletRuleSet, String requestName) {
        if (!transletRuleSet.isEmpty()) {
            for (TransletRule transletRule : transletRuleSet) {
                WildcardPattern namePattern = transletRule.getNamePattern();
                if (namePattern != null) {
                    if (namePattern.matches(requestName)) {
                        return transletRule;
                    }
                } else {
                    if (requestName.equals(transletRule.getName())) {
                        return transletRule;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private TransletRule retrieveEtcTransletRule(String requestName, MethodType requestMethod) {
        if (!etcTransletRuleSet.isEmpty()) {
            for (TransletRule transletRule : etcTransletRuleSet) {
                if (requestMethod.containsTo(transletRule.getAllowedMethods())) {
                    WildcardPattern namePattern = transletRule.getNamePattern();
                    if (namePattern != null) {
                        if (namePattern.matches(requestName)) {
                            return transletRule;
                        }
                    } else {
                        if (requestName.equals(transletRule.getName())) {
                            return transletRule;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean contains(String requestName) {
        return contains(requestName, MethodType.GET);
    }

    public boolean contains(String requestName, MethodType requestMethod) {
        return (getTransletRule(requestName, requestMethod) != null);
    }

    public void addTransletRule(TransletRule transletRule) throws IllegalRuleException {
        if (transletRule == null) {
            throw new IllegalArgumentException("transletRule must not be null");
        }
        String scanPath = transletRule.getScanPath();
        if (scanPath != null) {
            TransletScanner scanner = createTransletScanner(transletRule);
            PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.of(transletRule.getName());
            scanner.scan(scanPath, (filePath, scannedFile) -> {
                TransletRule newTransletRule = TransletRule.replicate(transletRule, filePath);
                if (prefixSuffixPattern != null) {
                    newTransletRule.setName(prefixSuffixPattern.enclose(filePath));
                } else if (transletRule.getName() != null) {
                    newTransletRule.setName(transletRule.getName() + filePath);
                }
                dissectTransletRule(newTransletRule);
            });
        } else {
            dissectTransletRule(transletRule);
        }
    }

    @NonNull
    private TransletScanner createTransletScanner(@NonNull TransletRule transletRule) throws IllegalRuleException {
        TransletScanner scanner = new TransletScanner(basePath);
        if (transletRule.getFilterParameters() != null) {
            FilterParameters filterParameters = transletRule.getFilterParameters();
            String transletScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
            if (transletScanFilterClassName != null) {
                TransletScanFilter transletScanFilter;
                try {
                    Class<?> filterClass = classLoader.loadClass(transletScanFilterClassName);
                    transletScanFilter = (TransletScanFilter)ClassUtils.createInstance(filterClass);
                } catch (Exception e) {
                    throw new IllegalRuleException("Failed to instantiate TransletScanFilter [" +
                            transletScanFilterClassName + "]", e);
                }
                scanner.setTransletScanFilter(transletScanFilter);
            }
            String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
            if (excludePatterns != null) {
                scanner.setExcludePatterns(excludePatterns);
            }
        }
        if (transletRule.getMaskPattern() != null) {
            scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
        } else {
            scanner.setTransletNameMaskPattern(transletRule.getScanPath());
        }
        return scanner;
    }

    private void dissectTransletRule(@NonNull TransletRule transletRule) {
        if (transletRule.getRequestRule() == null) {
            RequestRule requestRule = new RequestRule(false);
            transletRule.setRequestRule(requestRule);
        }

        List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
        if (responseRuleList == null || responseRuleList.isEmpty()) {
            saveTransletRule(transletRule);
        } else if (responseRuleList.size() == 1) {
            transletRule.setResponseRule(responseRuleList.get(0));
            saveTransletRule(transletRule);
        } else {
            // Grouped translets
            ResponseRule defaultResponseRule = null;
            for (ResponseRule responseRule : responseRuleList) {
                String responseName = responseRule.getName();
                if (responseName == null || responseName.isEmpty()) {
                    if (defaultResponseRule != null) {
                        logger.warn("Ignore duplicated default response rule {} of transletRule {}",
                                defaultResponseRule, transletRule);
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

    private void saveTransletRule(@NonNull TransletRule transletRule) {
        transletRule.determineResponseRule();

        String transletName = Namespace.applyTransletNamePattern(
            assistantLocal.getDefaultSettings(), transletRule.getName());
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
            if (WildcardPattern.hasWildcards(transletRule.getName())) {
                WildcardPattern namePattern = WildcardPattern.compile(transletRule.getName(), NAME_SEPARATOR_CHAR);
                transletRule.setNamePattern(namePattern);
            }
            if (allowedMethods != null) {
                String restfulTransletName = assembleTransletName(transletName, allowedMethods);
                transletRuleMap.put(restfulTransletName, transletRule);
                for (MethodType methodType : allowedMethods) {
                    switch (methodType) {
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
                getTransletRuleMap.put(transletName, transletRule);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("add TransletRule {}", transletRule);
        }
    }

    private void savePathVariables(@NonNull TransletRule transletRule) {
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
            WildcardPattern namePattern = WildcardPattern.compile(wildTransletName, NAME_SEPARATOR_CHAR);
            transletRule.setNamePattern(namePattern);
            transletRule.setNameTokens(nameTokens);
        }
    }

    private boolean hasPathVariables(@NonNull String transletName) {
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

    @NonNull
    private String assembleRestfulTransletName(String transletName, MethodType requestMethod) {
        return (requestMethod + " " + transletName);
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

    static class WeightComparator implements Comparator<TransletRule> {

        @Override
        public int compare(@NonNull TransletRule tr1, TransletRule tr2) {
            if (tr1.getNamePattern() != null && tr2.getNamePattern() != null) {
                float weight1 = tr1.getNamePattern().getWeight();
                float weight2 = tr2.getNamePattern().getWeight();
                int cmp = Float.compare(weight2, weight1);
                if (cmp == 0) {
                    cmp = tr1.getNamePattern().toString().compareTo(tr2.getNamePattern().toString());
                }
                return cmp;
            } else if (tr1.getNamePattern() != null) {
                return tr2.getName().compareTo(tr1.getNamePattern().toString());
            } else if (tr2.getNamePattern() != null) {
                return tr1.getName().compareTo(tr2.getNamePattern().toString());
            } else {
                return tr2.getName().compareTo(tr1.getName());
            }
        }

    }

}
