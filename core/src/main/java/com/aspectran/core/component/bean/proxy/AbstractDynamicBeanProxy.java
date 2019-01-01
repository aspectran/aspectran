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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.util.ConcurrentReferenceHashMap;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class AbstractDynamicBeanProxy.
 */
public abstract class AbstractDynamicBeanProxy {

    private static final Log log = LogFactory.getLog(AbstractDynamicBeanProxy.class);

    private static final RelevantAspectRuleHolder EMPTY_HOLDER = new RelevantAspectRuleHolder();

    private static final Map<String, RelevantAspectRuleHolder> cache = new ConcurrentReferenceHashMap<>(256);

    private final AspectRuleRegistry aspectRuleRegistry;

    public AbstractDynamicBeanProxy(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    protected boolean isAvoidAdvice(Method method) {
        return (Object.class == method.getDeclaringClass() ||
                method.getDeclaringClass().isAnnotationPresent(AvoidAdvice.class) ||
                method.isAnnotationPresent(AvoidAdvice.class));
    }

    protected AspectAdviceRuleRegistry retrieveAspectAdviceRuleRegistry(Activity activity,
            String transletName, String beanId, String className, String methodName) {
        RelevantAspectRuleHolder holder = getRelevantAspectRuleHolder(transletName, beanId, className, methodName);
        AspectAdviceRuleRegistry aarr = holder.getAspectAdviceRuleRegistry();
        if (aarr != null && aarr.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule sar : aarr.getSettingsAdviceRuleList()) {
                activity.registerSettingsAdviceRule(sar);
            }
        }
        if (holder.getDynamicAspectRuleList() != null) {
            for (AspectRule aspectRule : holder.getDynamicAspectRuleList()) {
                // register dynamically
                activity.registerAspectRule(aspectRule);
            }
        }
        return aarr;
    }

    private RelevantAspectRuleHolder getRelevantAspectRuleHolder(
            String transletName, String beanId, String className, String methodName) {
        String pattern = PointcutPatternRule.combinePattern(transletName, beanId, className, methodName);
        RelevantAspectRuleHolder holder = cache.get(pattern);
        if (holder == null) {
            holder = createRelevantAspectRuleHolder(transletName, beanId, className, methodName);
            RelevantAspectRuleHolder existing = cache.putIfAbsent(pattern, holder);
            if (existing != null) {
                holder = existing;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Caching RelevantAspectRuleHolder [" + pattern + "] " + holder);
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
        if (!dynamicAspectRuleList.isEmpty() || registry != null) {
            RelevantAspectRuleHolder holder = new RelevantAspectRuleHolder();
            holder.setAspectAdviceRuleRegistry(registry);
            if (!dynamicAspectRuleList.isEmpty()) {
                holder.setDynamicAspectRuleList(dynamicAspectRuleList);
            }
            return holder;
        } else {
            return EMPTY_HOLDER;
        }
    }

    protected boolean isSameBean(BeanRule beanRule, AspectAdviceRule aspectAdviceRule) {
        if (beanRule.getId() != null && beanRule.getId().equals(aspectAdviceRule.getAdviceBeanId())) {
            return true;
        }
        if (beanRule.getBeanClass() != null && aspectAdviceRule.getAdviceBeanClass() != null) {
            return (beanRule.getBeanClass() == aspectAdviceRule.getAdviceBeanClass());
        }
        return false;
    }

}