/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.component.bean.BeanRuleAnalyzer;
import com.aspectran.core.component.bean.BeanRuleException;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.nodelet.NodeTracker;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The Class BeanReferenceInspector.
 */
public class BeanReferenceInspector {

    private static final Logger logger = LoggerFactory.getLogger(BeanReferenceInspector.class);

    private final Map<RefererKey, Set<RefererInfo>> refererInfoMap = new LinkedHashMap<>(256);

    /**
     * Reserves to bean reference inspection.
     * @param beanId the bean id
     * @param beanClass the bean class
     * @param referenceable the object to be inspected
     * @param ruleAppender the rule appender
     */
    public void reserve(String beanId, Class<?> beanClass, BeanReferenceable referenceable, RuleAppender ruleAppender) {
        RefererKey key = new RefererKey(beanClass, beanId);
        Set<RefererInfo> refererInfoSet = refererInfoMap.get(key);
        if (refererInfoSet == null) {
            refererInfoSet = new LinkedHashSet<>();
            refererInfoSet.add(new RefererInfo(referenceable, ruleAppender));
            refererInfoMap.put(key, refererInfoSet);
        } else {
            refererInfoSet.add(new RefererInfo(referenceable, ruleAppender));
        }
    }

    /**
     * Inspect bean reference.
     * @param beanRuleRegistry the bean rule registry
     * @throws BeanReferenceException the bean reference exception
     * @throws BeanRuleException if an illegal bean rule is found
     */
    public void inspect(BeanRuleRegistry beanRuleRegistry) throws BeanReferenceException, BeanRuleException {
        Map<RefererInfo, RefererKey> brokenReferences = new LinkedHashMap<>();

        for (Map.Entry<RefererKey, Set<RefererInfo>> entry : refererInfoMap.entrySet()) {
            RefererKey refererKey = entry.getKey();
            String beanId = refererKey.getQualifier();
            Class<?> beanClass = refererKey.getType();
            Set<RefererInfo> refererInfoSet = entry.getValue();

            BeanRule beanRule = null;
            BeanRule[] beanRules = null;

            if (beanClass != null) {
                beanRules = beanRuleRegistry.getBeanRules(beanClass);
                if (beanRules != null) {
                    if (beanRules.length == 1) {
                        if (beanId != null) {
                            if (beanId.equals(beanRules[0].getId())) {
                                beanRule = beanRules[0];
                            }
                        } else {
                            beanRule = beanRules[0];
                        }
                    } else {
                        if (beanId != null) {
                            for (BeanRule br : beanRules) {
                                if (beanId.equals(br.getId())) {
                                    beanRule = br;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (beanRule == null && beanRules == null) {
                    beanRule = beanRuleRegistry.getBeanRuleForConfig(beanClass);
                }
            } else if (beanId != null) {
                beanRule = beanRuleRegistry.getBeanRule(beanId);
            }

            if (beanRule == null) {
                if (beanRules != null && beanRules.length > 1) {
                    for (RefererInfo refererInfo : refererInfoSet) {
                        if (beanId != null) {
                            logger.error("Cannot resolve reference to bean " + refererKey +
                                    "; Referer: " + refererInfo);
                        } else {
                            logger.error("No unique bean of type [" + beanClass + "] is defined: " +
                                    "expected single matching bean but found " + beanRules.length + ": [" +
                                    NoUniqueBeanException.getBeanDescriptions(beanRules) + "]; Referer: " + refererInfo);
                        }
                        brokenReferences.put(refererInfo, refererKey);
                    }
                } else {
                    for (RefererInfo refererInfo : refererInfoSet) {
                        if (!isValidStaticReference(refererInfo)) {
                            logger.error("Cannot resolve reference: " + refererInfo);
                            brokenReferences.put(refererInfo, null);
                        }
                    }
                }
            } else {
                for (RefererInfo refererInfo : refererInfoSet) {
                    if (refererInfo.getBeanRefererType() == BeanRefererType.BEAN_METHOD_ACTION_RULE) {
                        checkTransletActionParameter((InvokeActionRule)refererInfo.getReferenceable(),
                                beanRule, refererInfo);
                    }
                }
            }
        }

        if (!brokenReferences.isEmpty()) {
            throw new BeanReferenceException(brokenReferences);
        }
    }

    private boolean isValidStaticReference(@NonNull RefererInfo refererInfo) {
        if (refererInfo.getBeanRefererType() == BeanRefererType.TOKEN) {
            Token token = (Token)refererInfo.getReferenceable();
            if (token.getAlternativeValue() != null && token.getGetterName() != null) {
                Class<?> beanClass = (Class<?>)token.getAlternativeValue();
                if (beanClass.isEnum()) {
                    Object[] enums = beanClass.getEnumConstants();
                    if (enums != null) {
                        for (Object en : enums) {
                            if (token.getGetterName().equals(en.toString())) {
                                return true;
                            }
                        }
                    }
                }
                return BeanUtils.hasReadableProperty((Class<?>)token.getAlternativeValue(), token.getGetterName());
            }
        }
        return false;
    }

    private void checkTransletActionParameter(@NonNull InvokeActionRule invokeActionRule, BeanRule beanRule, RefererInfo refererInfo)
            throws BeanRuleException {
        if (invokeActionRule.getArgumentItemRuleMap() == null) {
            Class<?> beanClass = beanRule.getTargetBeanClass();
            String methodName = invokeActionRule.getMethodName();
            Method m1 = MethodUtils.getAccessibleMethod(beanClass, methodName, BeanRuleAnalyzer.TRANSLET_ACTION_PARAMETER_TYPES);
            if (m1 != null) {
                invokeActionRule.setMethod(m1);
                invokeActionRule.setRequiresTranslet(true);
            } else {
                Method m2 = MethodUtils.getAccessibleMethod(beanClass, methodName);
                if (m2 == null) {
                    throw new BeanRuleException("No such bean method " + methodName + "() on bean " + beanClass +
                            " in " + refererInfo, beanRule);
                }
                invokeActionRule.setMethod(m2);
                invokeActionRule.setRequiresTranslet(false);
            }
        }
    }

    static class RefererKey {

        private final Class<?> type;

        private final String qualifier;

        private volatile int hashCode;

        RefererKey(Class<?> type, String qualifier) {
            this.type = type;
            this.qualifier = qualifier;
        }

        Class<?> getType() {
            return type;
        }

        String getQualifier() {
            return qualifier;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof RefererKey key)) {
                return false;
            }
            return (Objects.equals(type, key.type) &&
                    Objects.equals(qualifier, key.qualifier));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = hashCode;
            if (result == 0) {
                result = 11;
                if (type != null) {
                    result = prime * result + type.hashCode();
                }
                if (qualifier != null) {
                    result = prime * result + qualifier.hashCode();
                }
                hashCode = result;
            }
            return result;
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder();
            tsb.append("type", type);
            tsb.append("qualifier", qualifier);
            return tsb.toString();
        }

    }

    static class RefererInfo {

        private final BeanReferenceable referenceable;

        private final RuleAppender ruleAppender;

        private final NodeTracker nodeTracker;

        RefererInfo(@NonNull BeanReferenceable referenceable, @Nullable RuleAppender ruleAppender) {
            this.referenceable = referenceable;
            this.ruleAppender = ruleAppender;

            if (ruleAppender != null) {
                NodeTracker nodeTracker = ruleAppender.getNodeTracker();
                if (nodeTracker != null) {
                    this.nodeTracker = nodeTracker.getClonedNodeTracker();
                } else {
                    this.nodeTracker = null;
                }
            } else {
                this.nodeTracker = null;
            }
        }

        BeanReferenceable getReferenceable() {
            return referenceable;
        }

        RuleAppender getRuleAppender() {
            return ruleAppender;
        }

        BeanRefererType getBeanRefererType() {
            return referenceable.getBeanRefererType();
        }

        NodeTracker getNodeTracker() {
            return nodeTracker;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(referenceable.getBeanRefererType());
            sb.append(" ").append(referenceable);
            if (ruleAppender != null) {
                sb.append(" in ").append(ruleAppender.getQualifiedName());
                if (nodeTracker != null) {
                    sb.append(" ").append(nodeTracker);
                }
            }
            return sb.toString();
        }

    }

}
