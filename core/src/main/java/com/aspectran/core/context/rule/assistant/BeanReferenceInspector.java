/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.nodelet.NodeTracker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class BeanReferenceInspector.
 */
public class BeanReferenceInspector {

    private final Log log = LogFactory.getLog(BeanReferenceInspector.class);

    private final Map<Object, Set<RefererInfo>> referenceMap;

    public BeanReferenceInspector() {
        referenceMap = new LinkedHashMap<>();
    }

    /**
     * Reserves to bean reference inspection.
     *
     * @param beanIdOrClass the bean id or class
     * @param inspectable the object to be inspected
     * @param ruleAppender the rule appender
     */
    public void reserve(Object beanIdOrClass, BeanReferenceInspectable inspectable, RuleAppender ruleAppender) {
        Set<RefererInfo> refererInfoSet = referenceMap.get(beanIdOrClass);
        if (refererInfoSet == null) {
            refererInfoSet = new LinkedHashSet<>();
            refererInfoSet.add(new RefererInfo(inspectable, ruleAppender));
            referenceMap.put(beanIdOrClass, refererInfoSet);
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

        for (Map.Entry<Object, Set<RefererInfo>> entry : referenceMap.entrySet()) {
            Object beanIdOrClass = entry.getKey();
            Set<RefererInfo> refererInfoSet = entry.getValue();

            BeanRule beanRule;
            if (beanIdOrClass instanceof Class<?>) {
                BeanRule[] beanRules = beanRuleRegistry.getBeanRules((Class<?>)beanIdOrClass);
                if (beanRules != null && beanRules.length > 0) {
                    beanRule = beanRules[0];
                } else {
                    beanRule = null;
                }
                if (beanRule == null) {
                    beanRule = beanRuleRegistry.getConfigBeanRule((Class<?>)beanIdOrClass);
                }
            } else {
                beanRule = beanRuleRegistry.getBeanRule(beanIdOrClass);
            }

            if (beanRule == null) {
                boolean excepted = false;
                for (RefererInfo refererInfo : refererInfoSet) {
                    if (refererInfo.getBeanRefererType() == BeanRefererType.TOKEN) {
                        Token t = (Token)refererInfo.getInspectable();
                        if (t.getAlternativeValue() != null && t.getGetterName() != null) {
                            if (BeanUtils.hasReadableProperty((Class<?>)t.getAlternativeValue(), t.getGetterName())) {
                                excepted = true;
                                break;
                            }
                        }
                    }
                }
                if (!excepted) {
                    brokenReferences.add(beanIdOrClass);
                    for (RefererInfo refererInfo : refererInfoSet) {
                        log.error("Cannot resolve reference to bean '" + beanIdOrClass.toString() +
                                "' on " + refererInfo);
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
            for (Object beanIdOrClass : brokenReferences) {
                referenceMap.remove(beanIdOrClass);
            }

            BeanReferenceException bre = new BeanReferenceException(brokenReferences);
            bre.setBeanReferenceInspector(this);

            throw bre;
        }
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
                            " in " + refererInfo
                            , beanRule);
                }
                beanActionRule.setMethod(m2);
                beanActionRule.setRequiresTranslet(false);
            }
        }
    }

    public Map<Object, Set<RefererInfo>> getReferenceMap() {
        return referenceMap;
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

        public NodeTracker getNodeTracker() {
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
