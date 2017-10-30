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
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

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

    private final Map<Object, Set<BeanReferenceInspectable>> relationMap;

    public BeanReferenceInspector() {
        relationMap = new LinkedHashMap<>();
    }

    /**
     * Reserves to bean reference inspection.
     *
     * @param beanIdOrClass the bean id or class
     * @param inspectable the object to be inspected
     */
    public void reserve(Object beanIdOrClass, BeanReferenceInspectable inspectable) {
        Set<BeanReferenceInspectable> ruleSet = relationMap.get(beanIdOrClass);
        if (ruleSet == null) {
            ruleSet = new LinkedHashSet<>();
            ruleSet.add(inspectable);
            relationMap.put(beanIdOrClass, ruleSet);
        } else {
            ruleSet.add(inspectable);
        }
    }

    /**
     * Inspect bean reference.
     *
     * @param beanRuleRegistry the bean rule registry
     * @throws BeanReferenceException the bean reference exception
     */
    public void inspect(BeanRuleRegistry beanRuleRegistry) throws BeanReferenceException {
        List<Object> unknownBeanIdList = new ArrayList<>();

        for (Map.Entry<Object, Set<BeanReferenceInspectable>> entry : relationMap.entrySet()) {
            Object beanIdOrClass = entry.getKey();
            Set<BeanReferenceInspectable> set = entry.getValue();

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
                boolean skip = false;
                for (BeanReferenceInspectable o : set) {
                    if (o instanceof Token) {
                        Token t = (Token)o;
                        if (t.getAlternativeValue() != null && t.getGetterName() != null) {
                            if (BeanUtils.hasReadableProperty((Class<?>)t.getAlternativeValue(), t.getGetterName())) {
                                skip = true;
                            }
                        }
                    }
                }
                if (!skip) {
                    unknownBeanIdList.add(beanIdOrClass);
                    for (BeanReferenceInspectable o : set) {
                        log.error("Cannot resolve reference to bean '" + beanIdOrClass.toString() +
                                "' on " + o.getBeanReferrerType() + " " + o);
                    }
                }
            } else {
                for (BeanReferenceInspectable o : set) {
                    if (o.getBeanReferrerType() == BeanReferrerType.BEAN_ACTION_RULE) {
                        BeanRuleAnalyzer.checkTransletActionParameter((BeanActionRule)o, beanRule);
                    }
                }
            }
        }

        if (!unknownBeanIdList.isEmpty()) {
            for (Object beanIdOrClass : unknownBeanIdList) {
                relationMap.remove(beanIdOrClass);
            }

            BeanReferenceException bre = new BeanReferenceException(unknownBeanIdList);
            bre.setBeanReferenceInspector(this);

            throw bre;
        }
    }

    public Map<Object, Set<BeanReferenceInspectable>> getRelationMap() {
        return relationMap;
    }

}
