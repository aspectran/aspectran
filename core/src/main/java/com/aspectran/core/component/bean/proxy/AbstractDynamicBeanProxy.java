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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The Class AbstractDynamicBeanProxy.
 */
public abstract class AbstractDynamicBeanProxy {

    protected final Log log = LogFactory.getLog(getClass());

    private static final RelevantAspectRuleHolder EMPTY_HOLDER = new RelevantAspectRuleHolder();

    private static final Map<String, RelevantAspectRuleHolder> cache = new WeakHashMap<>();

    private final AspectRuleRegistry aspectRuleRegistry;

    public AbstractDynamicBeanProxy(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    protected AspectAdviceRuleRegistry retrieveAspectAdviceRuleRegistry(Activity activity,
            String transletName, String beanId, String className, String methodName) {
        RelevantAspectRuleHolder holder = getRelevantAspectRuleHolder(transletName, beanId, className, methodName);
        if (holder.getDynamicAspectRuleList() != null) {
            for (AspectRule aspectRule : holder.getDynamicAspectRuleList()) {
                // register dynamically
                activity.registerAspectRule(aspectRule);
            }
        }
        return holder.getAspectAdviceRuleRegistry();
    }

    private RelevantAspectRuleHolder getRelevantAspectRuleHolder(
            String transletName, String beanId, String className, String methodName) {
        String pattern = PointcutPatternRule.combinePattern(transletName, beanId, className, methodName);

        // Check the cache first
        RelevantAspectRuleHolder holder = cache.get(pattern);
        if (holder == null) {
            synchronized (cache) {
                holder = cache.get(pattern);
                if (holder == null) {
                    holder = createRelevantAspectRuleHolder(transletName, beanId, className, methodName);
                    cache.put(pattern, holder);

                    if (log.isDebugEnabled()) {
                        log.debug("cache relevantAspectRuleHolder [" + pattern + "] " + holder);
                    }
                }
            }
        }
        return holder;
    }

    private RelevantAspectRuleHolder createRelevantAspectRuleHolder(
            String transletName, String beanId, String className, String methodName) {
        AspectAdviceRulePostRegister postRegister = new AspectAdviceRulePostRegister();
        List<AspectRule> dynamicAspectRuleList = new ArrayList<>();
        for (AspectRule aspectRule : aspectRuleRegistry.getAspectRules()) {
            if (aspectRule.isBeanRelevant()) {
                Pointcut pointcut = aspectRule.getPointcut();
                if (pointcut == null || pointcut.matches(transletName, beanId, className, methodName)) {
                    if (aspectRule.getJoinpointTargetType() == JoinpointTargetType.METHOD) {
                        postRegister.register(aspectRule);
                    } else if (aspectRule.getJoinpointTargetType() == JoinpointTargetType.TRANSLET) {
                        dynamicAspectRuleList.add(aspectRule);
                    }
                }
            }
        }

        AspectAdviceRuleRegistry registry = postRegister.getAspectAdviceRuleRegistry();
        if (!dynamicAspectRuleList.isEmpty() || (registry != null && registry.getAspectRuleCount() > 0)) {
            RelevantAspectRuleHolder holder = new RelevantAspectRuleHolder();
            if (!dynamicAspectRuleList.isEmpty()) {
                holder.setDynamicAspectRuleList(dynamicAspectRuleList);
            } else {
                holder.setAspectAdviceRuleRegistry(registry);
            }
            return holder;
        } else {
            return EMPTY_HOLDER;
        }
    }

    protected boolean isSameBean(BeanRule beanRule, AspectAdviceRule aspectAdviceRule) {
        if (beanRule.getId() != null) {
            return (beanRule.getId().equals(aspectAdviceRule.getAdviceBeanId()));
        }
        if (beanRule.getBeanClass() != null) {
            return (beanRule.getBeanClass() == aspectAdviceRule.getAdviceBeanClass());
        }
        return false;
    }

}