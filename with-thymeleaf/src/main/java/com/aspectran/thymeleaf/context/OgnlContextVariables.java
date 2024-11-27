package com.aspectran.thymeleaf.context;

import com.aspectran.core.context.asel.ognl.OgnlSupport;
import com.aspectran.utils.annotation.jsr305.NonNull;
import ognl.OgnlContext;
import org.thymeleaf.expression.IExpressionObjects;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created: 2024. 11. 27.</p>
 */
public final class OgnlContextVariables extends OgnlContext {

    private final IExpressionObjects expressionObjects;

    public OgnlContextVariables(IExpressionObjects expressionObjects) {
        super(OgnlSupport.CLASS_RESOLVER, null, OgnlSupport.MEMBER_ACCESS);
        this.expressionObjects = expressionObjects;
    }

    @Override
    public int size() {
        return (super.size() + expressionObjects.size());
    }

    @Override
    public boolean isEmpty() {
        return (expressionObjects.size() == 0 && super.isEmpty());
    }

    @Override
    public Object get(@NonNull Object key) {
        if (expressionObjects.containsObject(key.toString())) {
            return expressionObjects.getObject(key.toString());
        } else {
            return super.get(key);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return (expressionObjects.containsObject(key.toString()) || super.containsKey(key));
    }

    @Override
    public Object put(@NonNull String key, Object value) {
        if (expressionObjects.containsObject(key)) {
            throw new IllegalArgumentException(
                "Cannot put entry with key \"" + key + "\" into Expression Objects wrapper map: key matches the " +
                    "name of one of the expression objects");
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        // This will call put, and therefore perform the key name check
        super.putAll(m);
    }

    @Override
    public Object remove(@NonNull Object key) {
        if (expressionObjects.containsObject(key.toString())) {
            throw new IllegalArgumentException(
                "Cannot remove entry with key \"" + key + "\" from Expression Objects wrapper map: key matches the " +
                    "name of one of the expression objects");
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot clear Expression Objects wrapper map");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Cannot perform by-value search on Expression Objects wrapper map");
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Cannot clone Expression Objects wrapper map");
    }

    @Override
    @NonNull
    public Set<String> keySet() {
        if (super.isEmpty()) {
            return expressionObjects.getObjectNames();
        }
        Set<String> keys = new LinkedHashSet<>(expressionObjects.getObjectNames());
        keys.addAll(super.keySet());
        return keys;
    }

    @Override
    @NonNull
    public Collection<Object> values() {
        return super.values();
    }

    @Override
    @NonNull
    public Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException(
                "Cannot retrieve a complete entry set for Expression Objects wrapper map. Get a key set instead");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException(
                "Cannot execute equals operation on Expression Objects wrapper map");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
                "Cannot execute hashCode operation on Expression Objects wrapper map");
    }

    @Override
    @NonNull
    public String toString() {
        return "{EXPRESSION OBJECTS WRAPPER CONTEXT FOR KEYS: " + keySet() + "}";
    }

}
