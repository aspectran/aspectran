package com.aspectran.thymeleaf.expression;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import ognl.ArrayPropertyAccessor;
import ognl.EnumerationPropertyAccessor;
import ognl.IteratorPropertyAccessor;
import ognl.ListPropertyAccessor;
import ognl.MapPropertyAccessor;
import ognl.ObjectPropertyAccessor;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import ognl.SetPropertyAccessor;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.ExpressionCacheKey;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.context.IContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aspectran.thymeleaf.expression.OgnlContextPropertyAccessor.REQUEST_PARAMETERS_RESTRICTED_VARIABLE_NAME;
import static com.aspectran.thymeleaf.expression.OgnlContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS;

/**
 * <p>Created: 2024. 11. 25.</p>
 */
public class OgnlShortcutExpression {

    private static final String EXPRESSION_CACHE_TYPE_OGNL_SHORTCUT = "ognlsc";

    private static final Object[] NO_PARAMS = new Object[0];

    private final String[] expressionLevels;

    OgnlShortcutExpression(String[] expressionLevels) {
        super();
        this.expressionLevels = expressionLevels;
    }

    Object evaluate(@NonNull IEngineConfiguration configuration, Map<String, Object> context, Object root)
            throws Exception {
        ICacheManager cacheManager = configuration.getCacheManager();
        ICache<ExpressionCacheKey, Object> expressionCache = (cacheManager == null ? null : cacheManager.getExpressionCache());

        Object target = root;
        for (String propertyName : expressionLevels) {
            // If target is null, we will mimic what OGNL does in these cases...
            if (target == null) {
                throw new OgnlException("source is null for getProperty(null, \"" + propertyName + "\")");
            }

            // For the best integration possible, we will ask OGNL which property accessor it would use for
            // this target object, and then depending on the result apply our equivalent or just default to
            // OGNL evaluation if it is a custom property accessor we do not implement.
            Class<?> targetClass = OgnlRuntime.getTargetClass(target);
            PropertyAccessor ognlPropertyAccessor = OgnlRuntime.getPropertyAccessor(targetClass);

            // Depending on the returned OGNL property accessor, we will try to apply ours
            if (target instanceof Class<?>) {
                // Because of the way OGNL works, the "OgnlRuntime.getTargetClass(...)" of a Class object is the class
                // object itself, so we might be trying to apply a PropertyAccessor to a Class instead of a real object,
                // something we avoid by means of this shortcut
                target = getObjectProperty(expressionCache, propertyName, target);
            } else if (org.thymeleaf.standard.expression.OGNLContextPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getContextProperty(propertyName, context, target);
            } else if (ObjectPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getObjectProperty(expressionCache, propertyName, target);
            } else if (MapPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getMapProperty(propertyName, (Map<?, ?>) target);
            } else if (ListPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getListProperty(expressionCache, propertyName, (List<?>) target);
            } else if (SetPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getSetProperty(expressionCache, propertyName, (Set<?>) target);
            } else if (IteratorPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getIteratorProperty(expressionCache, propertyName, (Iterator<?>) target);
            } else if (EnumerationPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getEnumerationProperty(expressionCache, propertyName, (Enumeration<?>) target);
            } else if (ArrayPropertyAccessor.class.equals(ognlPropertyAccessor.getClass())) {
                target = getArrayProperty(expressionCache, propertyName, (Object[]) target);
            } else {
                // OGNL would like to apply a different property accessor (probably a custom one we do not know). In
                // these cases, we must signal the problem with this exception and let the expression evaluator
                // default to normal OGNL evaluation.
                throw new OGNLShortcutExpressionNotApplicableException();
            }
        }
        return target;
    }

    private static Object getContextProperty(String propertyName, Map<String, Object> context, Object target)
            throws OgnlException {
        if (REQUEST_PARAMETERS_RESTRICTED_VARIABLE_NAME.equals(propertyName) &&
                context != null && context.containsKey(RESTRICT_REQUEST_PARAMETERS)) {
            throw new OgnlException(
                    "Access to variable \"" + propertyName +
                    "\" is forbidden in this context. Note some restrictions apply to " +
                    "variable access. For example, accessing request parameters is forbidden in preprocessing and " +
                    "unescaped expressions, and also in fragment inclusion specifications.");
        }
        return ((IContext) target).getVariable(propertyName);
    }

    private static Object getObjectProperty(
            ICache<ExpressionCacheKey, Object> expressionCache, String propertyName, Object target) {
        Class<?> currClass = OgnlRuntime.getTargetClass(target);
        ExpressionCacheKey cacheKey = computeMethodCacheKey(currClass, propertyName);
        Method readMethod = null;
        if (expressionCache != null) {
            readMethod = (Method)expressionCache.get(cacheKey);
        }
        if (readMethod == null) {
            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(currClass);
            } catch (IntrospectionException e) {
                // Something went wrong during introspection - wash hands, just let OGNL decide what to do
                throw new OGNLShortcutExpressionNotApplicableException();
            }
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null) {
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor.getName().equals(propertyName)) {
                        readMethod = propertyDescriptor.getReadMethod();
                        if (readMethod != null && expressionCache != null) {
                            expressionCache.put(cacheKey, readMethod);
                        }
                        break;
                    }
                }
            }
        }

        if (readMethod == null) {
            // The property name does not match any getter methods - better let OGNL decide what to do
            throw new OGNLShortcutExpressionNotApplicableException();
        }

        try {
            return readMethod.invoke(target, NO_PARAMS);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Oops! we better let OGNL take care of this its own way...
            throw new OGNLShortcutExpressionNotApplicableException();
        }
    }

    private static Object getMapProperty(@NonNull String propertyName, Map<?, ?> map) {
        /*
         * This method will try to mimic the behaviour of the ognl.MapPropertyAccessor class, with the exception
         * that OGNLShortcutExpressions do not process indexed map access (map['key']), only normal property map
         * access (map.key), so indexed access will not be taken into account in this accessor method.
         *
         * The main reason for not implementing support for indexed map access in OgnlShortcutExpression is that
         * in an indexed access expression in OGNL a variable could be used as index instead of a literal
         * (note that this is not allowed in SpringEL, but it is in OGNL), and resolving such index variable or more
         * complex expression would add quite a lot of complexity to this supposedly-simple mechanism. So in those
         * cases, it is just better to allow OGNL to do its job.
         */
        return switch (propertyName) {
            case "size" -> map.size();
            case "keys", "keySet" -> map.keySet();
            case "values" -> map.values();
            case "isEmpty" -> map.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
            default -> map.get(propertyName);
        };
    }

    public static Object getListProperty(
            ICache<ExpressionCacheKey,Object> expressionCache, @NonNull String propertyName, List<?> list) {
        /*
         * This method will try to mimic the behaviour of the ognl.ListPropertyAccessor class, with the exception
         * that OGNLShortcutExpressions do not process indexed list access (list[3]), only access to the properties
         * of the list object like 'size', 'iterator', etc.
         *
         * The main reason for not implementing support for indexed list access in OgnlShortcutExpression is similar
         * to that of indexed map access (with the difference that typical literal-based indexed access to lists
         * is based on numeric literals instead of text literals).
         */
        return switch (propertyName) {
            case "size" -> list.size();
            case "iterator" -> list.iterator();
            case "isEmpty", "empty" -> list.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
            default ->
                // Default to treating the list object as any other object
                getObjectProperty(expressionCache, propertyName, list);
        };
    }

    public static Object getArrayProperty(
            ICache<ExpressionCacheKey,Object> expressionCache, @NonNull String propertyName, Object[] array) {
        /*
         * This method will try to mimic the behaviour of the ognl.ArrayPropertyAccessor class, with the exception
         * that OGNLShortcutExpressions do not process indexed array access (array[3]), only access to the properties
         * of the array object, namely 'length'.
         *
         * The main reason for not implementing support for indexed array access in OgnlShortcutExpression is similar
         * to that of indexed map access (with the difference that typical literal-based indexed access to arrays
         * is based on numeric literals instead of text literals).
         */
        if (propertyName.equals("length")) {
            return Array.getLength(array);
        }
        // Default to treating the array object as any other object
        return getObjectProperty(expressionCache, propertyName, array);
    }

    public static Object getEnumerationProperty(
            ICache<ExpressionCacheKey,Object> expressionCache, @NonNull String propertyName, Enumeration<?> enumeration) {
        /*
         * This method will try to mimic the behaviour of the ognl.EnumerationPropertyAccessor class, with the exception
         * that OGNLShortcutExpressions do not process indexed array access (array[3]), only access to the properties
         * of the enumeration object.
         */
        return switch (propertyName) {
            case "next", "nextElement" -> enumeration.nextElement();
            case "hasNext", "hasMoreElements" -> enumeration.hasMoreElements() ? Boolean.TRUE : Boolean.FALSE;
            default ->
                // Default to treating the enumeration object as any other object
                getObjectProperty(expressionCache, propertyName, enumeration);
        };
    }

    public static Object getIteratorProperty(
            ICache<ExpressionCacheKey,Object> expressionCache, @NonNull String propertyName, Iterator<?> iterator) {
        /*
         * This method will try to mimic the behaviour of the ognl.IteratorPropertyAccessor class, with the exception
         * that OGNLShortcutExpressions do not process indexed iterator access (array[3]), only access to the properties
         * of the iterator object.
         */
        return switch (propertyName) {
            case "next" -> iterator.next();
            case "hasNext" -> iterator.hasNext() ? Boolean.TRUE : Boolean.FALSE;
            default ->
                // Default to treating the iterator object as any other object
                getObjectProperty(expressionCache, propertyName, iterator);
        };
    }

    public static Object getSetProperty(
            ICache<ExpressionCacheKey,Object> expressionCache, @NonNull String propertyName, Set<?> set) {
        /*
         * This method will try to mimic the behaviour of the ognl.IteratorPropertyAccessor class, with the exception
         * that OGNLShortcutExpressions do not process indexed iterator access (array[3]), only access to the properties
         * of the iterator object.
         */
        return switch (propertyName) {
            case "size" -> set.size();
            case "iterator" -> set.iterator();
            case "isEmpty" -> set.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
            default ->
                // Default to treating the set object as any other object
                getObjectProperty(expressionCache, propertyName, set);
        };
    }

    static String[] parse(String expression) {
        return doParseExpr(expression, 0, 0, expression.length());
    }

    @Nullable
    private static String[] doParseExpr(String expression, int level, int offset, int len) {
        int codepoint;
        int i = offset;
        boolean firstChar = true;
        while (i < len) {
            codepoint = Character.codePointAt(expression, i);
            if (codepoint == '.') {
                break;
            }
            if (firstChar) {
                if (!Character.isJavaIdentifierStart(codepoint)) {
                    return null;
                }
                firstChar = false;
            } else {
                if (!Character.isJavaIdentifierPart(codepoint)) {
                    return null;
                }
            }
            i++;
        }

        String[] result;
        if (i < len) {
            result = doParseExpr(expression, level + 1, i + 1, len);
            if (result == null) {
                return null;
            }
        } else {
            result = new String[level + 1];
        }
        result[level] = expression.substring(offset, i);
        if ("true".equalsIgnoreCase(result[level]) ||
                "false".equalsIgnoreCase(result[level]) ||
                "null".equalsIgnoreCase(result[level])) {
            return null;
        }
        return result;
    }

    @NonNull
    private static ExpressionCacheKey computeMethodCacheKey(@NonNull Class<?> targetClass, String propertyName) {
        return new ExpressionCacheKey(EXPRESSION_CACHE_TYPE_OGNL_SHORTCUT, targetClass.getName(), propertyName);
    }

    /*
     * This exception signals that the OgnlShortcutExpression mechanism is not applicable for the current
     * expression, and therefore the OGNLVariableExpressionEvaluator should default to standard pure-OGNL
     * evaluation.
     *
     * Most common reason for this is the existence of a custom property accessor registered in OGNL for accessing
     * the properties of one of the objects involved in the expression, which behaviour (the custom property accessor's)
     * cannot be replicated by OGNLShortcutExpressions.
     */
    static class OGNLShortcutExpressionNotApplicableException extends RuntimeException {

        OGNLShortcutExpressionNotApplicableException() {
            super();
        }

    }

}
