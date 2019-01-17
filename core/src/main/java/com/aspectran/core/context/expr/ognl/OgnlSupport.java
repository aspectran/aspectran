package com.aspectran.core.context.expr.ognl;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.util.ConcurrentReferenceHashMap;
import com.aspectran.core.util.StringUtils;
import ognl.DefaultClassResolver;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;

/**
 * Support for expressions using OGNL.
 *
 * @since 6.0.0
 */
public class OgnlSupport {

    private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();

    private static final DefaultClassResolver CLASS_RESOLVER = new DefaultClassResolver();

    private static final Map<String, Object> cache = new ConcurrentReferenceHashMap<>();

    private OgnlSupport() {
    }

    public static Object parseExpression(String expression) throws IllegalRuleException {
        if (!StringUtils.hasLength(expression)) {
            return null;
        }
        try {
            Object node = cache.get(expression);
            if (node == null) {
                node = Ognl.parseExpression(expression);
                Object existing = cache.putIfAbsent(expression, node);
                if (existing != null) {
                    node = existing;
                }
            }
            return node;
        } catch (OgnlException e) {
            throw new IllegalRuleException("Error parsing expression '" + expression + "'. Cause: " + e, e);
        }
    }

    public static Boolean evaluateAsBoolean(String expression, Object root) throws IllegalRuleException {
        return evaluateAsBoolean(expression, null, root);
    }

    public static Boolean evaluateAsBoolean(String expression, Object represented, Object root) throws IllegalRuleException {
        if (represented == null) {
            represented = parseExpression(expression);
        }
        if (represented == null) {
            return false;
        }
        try {
            Map context = createDefaultContext(root);
            return (Boolean)Ognl.getValue(represented, context, root, Boolean.class);
        } catch (OgnlException e) {
            throw new IllegalRuleException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    private static Map createDefaultContext(Object root) {
        return Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
    }

}
