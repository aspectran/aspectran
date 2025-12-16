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
package com.aspectran.thymeleaf.expression;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.ExpressionCacheKey;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.standard.expression.AssignationSequence;
import org.thymeleaf.standard.expression.Each;
import org.thymeleaf.standard.expression.ExpressionSequence;
import org.thymeleaf.standard.expression.FragmentSignature;
import org.thymeleaf.standard.expression.IStandardExpression;

/**
 * A utility class for caching parsed Thymeleaf expressions.
 *
 * <p>This class centralizes access to Thymeleaf's expression cache, providing
 * convenient methods for getting, putting, and removing various types of
 * parsed expression objects.</p>
 *
 * <p>Created: 2024. 11. 25.</p>
 */
public class ExpressionCache {

    private static final String EXPRESSION_CACHE_TYPE_STANDARD_EXPRESSION = "expr";

    private static final String EXPRESSION_CACHE_TYPE_ASSIGNATION_SEQUENCE = "aseq";

    private static final String EXPRESSION_CACHE_TYPE_EXPRESSION_SEQUENCE = "eseq";

    private static final String EXPRESSION_CACHE_TYPE_EACH = "each";

    private static final String EXPRESSION_CACHE_TYPE_FRAGMENT_SIGNATURE = "fsig";

    private ExpressionCache() {
    }

    /**
     * Retrieves an object from the expression cache.
     * @param <V> the type of the cached object
     * @param configuration the engine configuration
     * @param input the input expression string, used as part of the cache key
     * @param type the type of the expression, used as part of the cache key
     * @return the cached object, or null if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    static <V> V getFromCache(@NonNull IEngineConfiguration configuration, String input, String type) {
        ICacheManager cacheManager = configuration.getCacheManager();
        if (cacheManager != null) {
            ICache<ExpressionCacheKey, Object> cache = cacheManager.getExpressionCache();
            if (cache != null) {
                return (V)cache.get(new ExpressionCacheKey(type, input));
            }
        }
        return null;
    }

    /**
     * Puts an object into the expression cache.
     * @param <V> the type of the object to cache
     * @param configuration the engine configuration
     * @param input the input expression string, used as part of the cache key
     * @param value the object to cache
     * @param type the type of the expression, used as part of the cache key
     */
    static <V> void putIntoCache(@NonNull IEngineConfiguration configuration, String input, V value, String type) {
        ICacheManager cacheManager = configuration.getCacheManager();
        if (cacheManager != null) {
            ICache<ExpressionCacheKey, Object> cache = cacheManager.getExpressionCache();
            if (cache != null) {
                cache.put(new ExpressionCacheKey(type, input), value);
            }
        }
    }

    /**
     * Removes an object from the expression cache.
     * @param configuration the engine configuration
     * @param input the input expression string, used as part of the cache key
     * @param type the type of the expression, used as part of the cache key
     */
    static void removeFromCache(@NonNull IEngineConfiguration configuration, String input, String type) {
        ICacheManager cacheManager = configuration.getCacheManager();
        if (cacheManager != null) {
            ICache<ExpressionCacheKey, Object> cache = cacheManager.getExpressionCache();
            if (cache != null) {
                cache.clearKey(new ExpressionCacheKey(type, input));
            }
        }
    }

    static IStandardExpression getExpressionFromCache(IEngineConfiguration configuration, String input) {
        return getFromCache(configuration, input, EXPRESSION_CACHE_TYPE_STANDARD_EXPRESSION);
    }

    static void putExpressionIntoCache(IEngineConfiguration configuration, String input, IStandardExpression value) {
        putIntoCache(configuration, input, value, EXPRESSION_CACHE_TYPE_STANDARD_EXPRESSION);
    }

    static AssignationSequence getAssignationSequenceFromCache(IEngineConfiguration configuration, String input) {
        return getFromCache(configuration, input, EXPRESSION_CACHE_TYPE_ASSIGNATION_SEQUENCE);
    }

    static void putAssignationSequenceIntoCache(
        IEngineConfiguration configuration, String input, AssignationSequence value) {
        putIntoCache(configuration, input, value, EXPRESSION_CACHE_TYPE_ASSIGNATION_SEQUENCE);
    }

    static ExpressionSequence getExpressionSequenceFromCache(IEngineConfiguration configuration, String input) {
        return getFromCache(configuration, input, EXPRESSION_CACHE_TYPE_EXPRESSION_SEQUENCE);
    }

    static void putExpressionSequenceIntoCache(
        IEngineConfiguration configuration, String input, ExpressionSequence value) {
        putIntoCache(configuration, input, value, EXPRESSION_CACHE_TYPE_EXPRESSION_SEQUENCE);
    }

    static Each getEachFromCache(IEngineConfiguration configuration, String input) {
        return getFromCache(configuration, input, EXPRESSION_CACHE_TYPE_EACH);
    }

    static void putEachIntoCache(IEngineConfiguration configuration, String input, Each value) {
        putIntoCache(configuration, input, value, EXPRESSION_CACHE_TYPE_EACH);
    }

    static FragmentSignature getFragmentSignatureFromCache(IEngineConfiguration configuration, String input) {
        return getFromCache(configuration, input, EXPRESSION_CACHE_TYPE_FRAGMENT_SIGNATURE);
    }

    static void putFragmentSignatureIntoCache(
        IEngineConfiguration configuration, String input, FragmentSignature value) {
        putIntoCache(configuration, input, value, EXPRESSION_CACHE_TYPE_FRAGMENT_SIGNATURE);
    }

}
