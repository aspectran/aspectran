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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.component.bean.BeanRuleAnalyzer;
import com.aspectran.core.component.bean.BeanRuleException;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.nodelet.NodeTracker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The Class BeanReferenceInspector.
 */
public class BeanReferenceInspector {

    private final Log log = LogFactory.getLog(BeanReferenceInspector.class);

    private final Map<RefererKey, Set<RefererInfo>> refererInfoMap = new LinkedHashMap<>();

    /**
     * Reserves to bean reference inspection.
     *
     * @param beanId the bean id
     * @param beanClass the bean class
     * @param inspectable the object to be inspected
     * @param ruleAppender the rule appender
     */
    public void reserve(String beanId, Class<?> beanClass, BeanReferenceInspectable inspectable, RuleAppender ruleAppender) {
        RefererKey key = new RefererKey(beanId, beanClass);
        Set<RefererInfo> refererInfoSet = refererInfoMap.get(key);
        if (refererInfoSet == null) {
            refererInfoSet = new LinkedHashSet<>();
            refererInfoSet.add(new RefererInfo(inspectable, ruleAppender));
            refererInfoMap.put(key, refererInfoSet);
        } else {
            refererInfoSet.add(new RefererInfo(inspectable, ruleAppender));
        }
    }

    /**
     * Inspect bean reference.
     *
     * @param beanRuleRegistry the bean rule registry
     * @throws BeanReferenceException the bean reference exception
     */
    public void inspect(BeanRuleRegistry beanRuleRegistry) throws BeanReferenceException {
        List<Object> brokenReferences = new ArrayList<>();

        for (Map.Entry<RefererKey, Set<RefererInfo>> entry : refererInfoMap.entrySet()) {
            RefererKey refererKey = entry.getKey();
            String beanId = refererKey.getBeanId();
            Class<?> beanClass = refererKey.getBeanClass();
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
                    beanRule = beanRuleRegistry.getConfigBeanRule(beanClass);
                }
            } else if (beanId != null) {
                beanRule = beanRuleRegistry.getBeanRule(beanId);
            }

            if (beanRule == null) {
                if (beanRules != null && beanRules.length > 1) {
                    String referer = refererKey.getBeanClass().getName();
                    brokenReferences.add(referer);
                    log.error("No unique bean of type [" + referer + "] is defined: " +
                            "expected single matching bean but found " + beanRules.length + ": [" +
                            NoUniqueBeanException.getBeanDescriptions(beanRules) + "]");
                } else {
                    for (RefererInfo refererInfo : refererInfoSet) {
                        if (!isStaticMethodReference(refererInfo)) {
                            String referer = refererKey.toString();
                            brokenReferences.add(referer);
                            log.error("Cannot resolve reference to bean " + referer +
                                    " on " + refererInfo);
                        }
                    }
                }
            } else {
                for (RefererInfo refererInfo : refererInfoSet) {
                    if (refererInfo.getBeanRefererType() == BeanRefererType.BEAN_ACTION_RULE) {
                        checkTransletActionParameter((BeanActionRule)refererInfo.getInspectable(),
                                beanRule, refererInfo);
                    }
                }
            }
        }

        if (!brokenReferences.isEmpty()) {
            throw new BeanReferenceException(brokenReferences);
        }
    }

    private boolean isStaticMethodReference(RefererInfo refererInfo) {
        if (refererInfo.getBeanRefererType() == BeanRefererType.TOKEN) {
            Token t = (Token)refererInfo.getInspectable();
            if (t.getAlternativeValue() != null && t.getGetterName() != null) {
                if (BeanUtils.hasReadableProperty((Class<?>)t.getAlternativeValue(), t.getGetterName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkTransletActionParameter(BeanActionRule beanActionRule, BeanRule beanRule, RefererInfo refererInfo) {
        if (beanActionRule.getArgumentItemRuleMap() == null) {
            Class<?> beanClass = beanRule.getTargetBeanClass();
            String methodName = beanActionRule.getMethodName();

            Method m1 = MethodUtils.getAccessibleMethod(beanClass, methodName, BeanRuleAnalyzer.TRANSLET_ACTION_PARAMETER_TYPES);
            if (m1 != null) {
                beanActionRule.setMethod(m1);
                beanActionRule.setRequiresTranslet(true);
            } else {
                Method m2 = MethodUtils.getAccessibleMethod(beanClass, methodName);
                if (m2 == null) {
                    throw new BeanRuleException("No such action method " + methodName + "() on bean " + beanClass +
                            " in " + refererInfo, beanRule);
                }
                beanActionRule.setMethod(m2);
                beanActionRule.setRequiresTranslet(false);
            }
        }
    }

    private class RefererKey {

        private final String beanId;

        private final Class<?> beanClass;

        private volatile int hashCode;

        RefererKey(String beanId, Class<?> beanClass) {
            this.beanId = beanId;
            this.beanClass = beanClass;
        }

        String getBeanId() {
            return beanId;
        }

        Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            RefererKey key = (RefererKey)obj;
            return (Objects.equals(beanId, key.beanId) &&
                    Objects.equals(beanClass, key.beanClass));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = hashCode;
            if (result == 0) {
                if (beanId != null) {
                    result = prime * result + beanId.hashCode();
                }
                if (beanClass != null) {
                    result = prime * result + beanClass.hashCode();
                }
                hashCode = result;
            }
            return result;
        }

        @Override
        public String toString() {
            if (beanId != null && beanClass != null) {
                ToStringBuilder tsb = new ToStringBuilder();
                tsb.append("class", beanClass);
                tsb.append("qualifier", beanId);
                return tsb.toString();
            } else if (beanId != null) {
                return beanId;
            } else {
                return beanClass.toString();
            }
        }

    }

    private class RefererInfo {

        private final BeanReferenceInspectable inspectable;

        private final RuleAppender ruleAppender;

        private final NodeTracker nodeTracker;

        RefererInfo(BeanReferenceInspectable inspectable, RuleAppender ruleAppender) {
            this.inspectable = inspectable;
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

        BeanReferenceInspectable getInspectable() {
            return inspectable;
        }

        RuleAppender getRuleAppender() {
            return ruleAppender;
        }

        BeanRefererType getBeanRefererType() {
            return inspectable.getBeanRefererType();
        }

        NodeTracker getNodeTracker() {
            return nodeTracker;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (ruleAppender != null) {
                sb.append(ruleAppender.getQualifiedName());
                if (nodeTracker != null) {
                    sb.append(" ");
                    sb.append(nodeTracker.toString());
                }
                sb.append(" ");
            }
            sb.append(inspectable.getBeanRefererType());
            sb.append(" ");
            sb.append(inspectable);
            return sb.toString();
        }

    }

}
