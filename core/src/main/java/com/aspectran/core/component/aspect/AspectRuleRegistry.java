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
package com.aspectran.core.component.aspect;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.cache.Cache;
import com.aspectran.utils.cache.ConcurrentReferenceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.aspectran.utils.ConcurrentReferenceHashMap.ReferenceType;

/**
 * A central registry for all {@link AspectRule}s in the application context.
 *
 * <p>This class manages the lifecycle of aspect rules and provides a caching
 * mechanism to quickly find aspects that are relevant to a specific join point
 * (represented by a {@link PointcutPattern}). This caching is a key performance
 * optimization. It uses both soft and weak reference caches to manage memory
 * effectively.
 * </p>
 */
public class AspectRuleRegistry extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(AspectRuleRegistry.class);

    private static final RelevantAspectRuleHolder EMPTY_HOLDER = new RelevantAspectRuleHolder();

    /** Cache for relevant aspect rules with soft references, allowing garbage collection under memory pressure. */
    private final Cache<PointcutPattern, RelevantAspectRuleHolder> softCache =
            new ConcurrentReferenceCache<>(ReferenceType.SOFT, this::createRelevantAspectRuleHolder);

    /** Cache for relevant aspect rules with weak references, allowing for more eager garbage collection. */
    private final Cache<PointcutPattern, RelevantAspectRuleHolder> weakCache =
            new ConcurrentReferenceCache<>(ReferenceType.WEAK, this::createRelevantAspectRuleHolder);

    private final Map<String, AspectRule> aspectRuleMap = new ConcurrentHashMap<>();

    private final List<AspectRule> aspectRules = new CopyOnWriteArrayList<>();

    /** Keeps track of aspect rules added dynamically after initialization. */
    private final List<String> newAspectRules = new CopyOnWriteArrayList<>();

    /**
     * Returns all registered aspect rules.
     * @return a collection of {@link AspectRule}s
     */
    public Collection<AspectRule> getAspectRules() {
        return aspectRules;
    }

    /**
     * Returns the aspect rule for the given aspect ID.
     * @param aspectId the ID of the aspect
     * @return the {@link AspectRule}, or {@code null} if not found
     */
    public AspectRule getAspectRule(String aspectId) {
        return aspectRuleMap.get(aspectId);
    }

    /**
     * Checks if an aspect rule with the given ID is registered.
     * @param aspectId the ID of the aspect
     * @return true if the aspect rule exists, false otherwise
     */
    public boolean contains(String aspectId) {
        return aspectRuleMap.containsKey(aspectId);
    }

    /**
     * Adds a new aspect rule to the registry.
     * If the registry is already initialized, the new rule is marked as dynamic,
     * and the caches are cleared to ensure they are rebuilt with the new rule.
     * @param aspectRule the aspect rule to add
     * @throws IllegalRuleException if an aspect rule with the same ID already exists
     */
    public void addAspectRule(@NonNull AspectRule aspectRule) throws IllegalRuleException {
        Assert.notNull(aspectRule, "aspectRule must not be null");
        Assert.notNull(aspectRule.getId(), "Aspect ID must not be null");

        if (logger.isTraceEnabled()) {
            logger.trace("add AspectRule {}", aspectRule);
        }

        determineBeanRelevant(aspectRule);

        AspectRule existing = aspectRuleMap.putIfAbsent(aspectRule.getId(), aspectRule);
        if (existing == null) {
            aspectRules.add(aspectRule);
            if (isInitialized()) {
                newAspectRules.add(aspectRule.getId());
                softCache.clear();
                weakCache.clear();
            }
        } else {
            throw new IllegalRuleException("Duplicate AspectRule ID: " + aspectRule.getId());
        }
    }

    /**
     * Removes an aspect rule from the registry.
     * @param aspectId the ID of the aspect to remove
     */
    public void removeAspectRule(String aspectId) {
        AspectRule existing = aspectRuleMap.remove(aspectId);
        if (existing != null) {
            if (aspectRules.remove(existing)) {
                if (isInitialized()) {
                    if (newAspectRules.remove(aspectId)) {
                        softCache.clear();
                        weakCache.clear();
                    }
                }
            }
        }
    }

    /**
     * Checks if there are any dynamically added aspect rules since initialization.
     * @return true if new aspect rules have been added, false otherwise
     */
    public boolean hasNewAspectRules() {
        return !newAspectRules.isEmpty();
    }

    @Override
    protected void doInitialize() {
        // Nothing to do
    }

    @Override
    protected void doDestroy() {
        aspectRuleMap.clear();
        aspectRules.clear();
        newAspectRules.clear();
        softCache.clear();
        weakCache.clear();
    }

    /**
     * Retrieves a holder of relevant aspect rules for a given pointcut pattern from the soft cache.
     * This cache is suitable for frequently accessed, non-dynamic pointcuts.
     * @param pointcutPattern the pointcut pattern to match
     * @return a holder containing the relevant aspect rules
     */
    public RelevantAspectRuleHolder retrieveFromSoftCache(PointcutPattern pointcutPattern) {
        return softCache.get(pointcutPattern);
    }

    /**
     * Retrieves a holder of relevant aspect rules for a given pointcut pattern from the weak cache.
     * This cache is suitable for pointcuts that are less frequently accessed.
     * @param pointcutPattern the pointcut pattern to match
     * @return a holder containing the relevant aspect rules
     */
    public RelevantAspectRuleHolder retrieveFromWeakCache(PointcutPattern pointcutPattern) {
        return weakCache.get(pointcutPattern);
    }

    /**
     * Creates a {@link RelevantAspectRuleHolder} for a given pointcut pattern.
     * This method is used by the caches to generate a new holder when one is not found.
     * It iterates through all registered aspect rules to find matches.
     * @param pointcutPattern the pointcut pattern to match against
     * @return a new holder containing the matched aspect rules
     */
    private RelevantAspectRuleHolder createRelevantAspectRuleHolder(PointcutPattern pointcutPattern) {
        AdviceRulePostRegister postRegister = new AdviceRulePostRegister();
        List<AspectRule> dynamicAspectRuleList = new ArrayList<>();
        for (AspectRule aspectRule : aspectRules) {
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

        AdviceRuleRegistry adviceRuleRegistry = postRegister.getAdviceRuleRegistry();
        if (adviceRuleRegistry != null || !dynamicAspectRuleList.isEmpty()) {
            return new RelevantAspectRuleHolder(adviceRuleRegistry, dynamicAspectRuleList);
        } else {
            return EMPTY_HOLDER;
        }
    }

    /**
     * Determines if an aspect rule is relevant to bean proxying.
     * An aspect is considered relevant if its joinpoint target is a method, or if its
     * pointcut specifies any bean, class, or method patterns.
     * @param aspectRule the aspect rule to check
     */
    private void determineBeanRelevant(@NonNull AspectRule aspectRule) {
        JoinpointTargetType joinpointTargetType = aspectRule.getJoinpointTargetType();
        if (joinpointTargetType == JoinpointTargetType.METHOD) {
            aspectRule.setBeanRelevant(true);
        } else {
            Pointcut pointcut = aspectRule.getPointcut();
            if (pointcut != null) {
                List<PointcutPatternRule> pointcutPatternRuleList = pointcut.getPointcutPatternRuleList();
                if (pointcutPatternRuleList != null) {
                    for (PointcutPatternRule ppr : pointcutPatternRuleList) {
                        PointcutPattern pp = ppr.getPointcutPattern();
                        if (pp != null) {
                            if (pp.getBeanIdPattern() != null ||
                                    pp.getClassNamePattern() != null ||
                                    pp.getMethodNamePattern() != null) {
                                aspectRule.setBeanRelevant(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
