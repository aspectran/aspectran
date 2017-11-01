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
import com.aspectran.core.util.xml.NodeletParser;

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
    public void reserve(Object beanIdOrClass, BeanReferenceInspectable inspectable, RuleAppender ruleAppender,
                        NodeletParser.LocationTracker locationTracker) {
        Set<RefererInfo> refererInfoSet = referenceMap.get(beanIdOrClass);
        if (refererInfoSet == null) {
            refererInfoSet = new LinkedHashSet<>();
            refererInfoSet.add(new RefererInfo(inspectable, ruleAppender, locationTracker));
            referenceMap.put(beanIdOrClass, refererInfoSet);
        } else {
            refererInfoSet.add(new RefererInfo(inspectable, ruleAppender, locationTracker));
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
                if (beanRules != null && beanRules.length == 1) {
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
                                "' on " + refererInfo.getRuleAppender().getQualifiedName() +
                                " " + refererInfo.getLocationTracker() +
                                " " + refererInfo.getBeanRefererType() + " " + refererInfo.getInspectable());
                    }
                }
            } else {
                for (RefererInfo refererInfo : refererInfoSet) {
                    if (refererInfo.getBeanRefererType() == BeanRefererType.BEAN_ACTION_RULE) {
                        BeanRuleAnalyzer.checkTransletActionParameter((BeanActionRule)refererInfo.getInspectable(),
                                beanRule, refererInfo.getRuleAppender());
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

    public Map<Object, Set<RefererInfo>> getReferenceMap() {
        return referenceMap;
    }

    private class RefererInfo {

        private final BeanReferenceInspectable inspectable;

        private final RuleAppender ruleAppender;

        private final NodeletParser.LocationTracker locationTracker;

        RefererInfo(BeanReferenceInspectable inspectable, RuleAppender ruleAppender,
                    NodeletParser.LocationTracker locationTracker) {
            this.inspectable = inspectable;
            this.ruleAppender = ruleAppender;
            this.locationTracker = locationTracker;
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

        public NodeletParser.LocationTracker getLocationTracker() {
            return locationTracker;
        }

    }

}
