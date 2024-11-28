package com.aspectran.thymeleaf.expression;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
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
 * <p>Created: 2024. 11. 25.</p>
 */
public class ExpressionCache {

    private static final String EXPRESSION_CACHE_TYPE_STANDARD_EXPRESSION = "expr";

    private static final String EXPRESSION_CACHE_TYPE_ASSIGNATION_SEQUENCE = "aseq";

    private static final String EXPRESSION_CACHE_TYPE_EXPRESSION_SEQUENCE = "eseq";

    private static final String EXPRESSION_CACHE_TYPE_EACH = "each";

    private static final String EXPRESSION_CACHE_TYPE_FRAGMENT_SIGNATURE = "fsig";

    private ExpressionCache() {
        super();
    }

    @Nullable
    static <V> V getFromCache(@NonNull IEngineConfiguration configuration, String input, String type) {
        ICacheManager cacheManager = configuration.getCacheManager();
        if (cacheManager != null) {
            ICache<ExpressionCacheKey, Object> cache = cacheManager.getExpressionCache();
            if (cache != null) {
                //noinspection unchecked
                return (V)cache.get(new ExpressionCacheKey(type, input));
            }
        }
        return null;
    }

    static <V> void putIntoCache(@NonNull IEngineConfiguration configuration, String input, V value, String type) {
        ICacheManager cacheManager = configuration.getCacheManager();
        if (cacheManager != null) {
            ICache<ExpressionCacheKey, Object> cache = cacheManager.getExpressionCache();
            if (cache != null) {
                cache.put(new ExpressionCacheKey(type, input), value);
            }
        }
    }

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
