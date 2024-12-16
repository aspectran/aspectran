/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.component.aspect;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.utils.cache.Cache;
import com.aspectran.utils.cache.ConcurrentReferenceCache;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.aspectran.utils.ConcurrentReferenceHashMap.ReferenceType;

/**
 * The Class AspectRuleRegistry.
 */
public class AspectRuleRegistry extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(AspectRuleRegistry.class);

    private static final RelevantAspectRuleHolder EMPTY_HOLDER = new RelevantAspectRuleHolder();

    private final Cache<PointcutPattern, RelevantAspectRuleHolder> softCache =
            new ConcurrentReferenceCache<>(ReferenceType.SOFT, this::createRelevantAspectRuleHolder);

    private final Cache<PointcutPattern, RelevantAspectRuleHolder> weakCache =
            new ConcurrentReferenceCache<>(ReferenceType.WEAK, this::createRelevantAspectRuleHolder);

    private final Map<String, AspectRule> aspectRuleMap = new LinkedHashMap<>();

    public AspectRuleRegistry() {
    }

    public Collection<AspectRule> getAspectRules() {
        return aspectRuleMap.values();
    }

    public AspectRule getAspectRule(String aspectId) {
        return aspectRuleMap.get(aspectId);
    }

    public boolean contains(String aspectId) {
        return aspectRuleMap.containsKey(aspectId);
    }

    public void addAspectRule(AspectRule aspectRule) throws IllegalRuleException {
        if (aspectRule == null) {
            throw new IllegalArgumentException("aspectRule must not be null");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("add AspectRule " + aspectRule);
        }
        AspectRule old = aspectRuleMap.put(aspectRule.getId(), aspectRule);
        if (old != null) {
            throw new IllegalRuleException("Duplicate AspectRule ID: " + aspectRule.getId());
        }
    }

    @Override
    protected void doInitialize() {
        // Nothing to do
    }

    @Override
    protected void doDestroy() {
        aspectRuleMap.clear();
    }

    public RelevantAspectRuleHolder getRelevantAspectRuleHolderFromSoftCache(PointcutPattern pointcutPattern) {
        return softCache.get(pointcutPattern);
    }

    public RelevantAspectRuleHolder getRelevantAspectRuleHolderFromWeakCache(PointcutPattern pointcutPattern) {
        return weakCache.get(pointcutPattern);
    }

    private RelevantAspectRuleHolder createRelevantAspectRuleHolder(PointcutPattern pointcutPattern) {
        AspectAdviceRulePostRegister postRegister = new AspectAdviceRulePostRegister();
        List<AspectRule> dynamicAspectRuleList = new ArrayList<>();
        for (AspectRule aspectRule : getAspectRules()) {
            if (aspectRule.isBeanRelevant()) {
                Pointcut pointcut = aspectRule.getPointcut();
                if (pointcut == null || pointcut.matches(pointcutPattern)) {
                    if (aspectRule.getJoinpointTargetType() == JoinpointTargetType.METHOD) {
                        postRegister.register(aspectRule);
                    } else {
                        dynamicAspectRuleList.add(aspectRule);
                    }
                }
            }
        }

        AspectAdviceRuleRegistry aarr = postRegister.getAspectAdviceRuleRegistry();
        if (!dynamicAspectRuleList.isEmpty() || aarr != null) {
            RelevantAspectRuleHolder holder = new RelevantAspectRuleHolder();
            holder.setAspectAdviceRuleRegistry(aarr);
            if (!dynamicAspectRuleList.isEmpty()) {
                holder.setDynamicAspectRuleList(dynamicAspectRuleList);
            }
            return holder;
        } else {
            return EMPTY_HOLDER;
        }
    }

}
