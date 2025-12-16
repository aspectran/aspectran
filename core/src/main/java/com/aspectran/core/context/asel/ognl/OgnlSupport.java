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
package com.aspectran.core.context.asel.ognl;

import com.aspectran.utils.Assert;
import ognl.DefaultTypeConverter;
import ognl.Node;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.TypeConverter;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * A central utility class providing support for OGNL (Object-Graph Navigation Language)
 * expression evaluation. This class is the core executor for AsEL expressions.
 *
 * <p>This class acts as a facade for the underlying OGNL library, offering static helper
 * methods and constants for common OGNL operations within Aspectran. It is responsible for:
 * <ul>
 *   <li>Creating a default, security-restricted OGNL context using custom components
 *       like {@link OgnlClassResolver} and {@link OgnlMemberAccess}.</li>
 *   <li>Providing a consistent entry point for evaluating pre-parsed OGNL expression
 *       trees ({@link ognl.Node}).</li>
 * </ul>
 *
 * <p>It does not handle the initial parsing of expression strings; rather, it focuses on
 * the secure execution of already-parsed expressions. The result of an evaluation can be
 * any object (an AsEL Expression).
 */
public abstract class OgnlSupport {

    /** A shared, reusable instance of the custom OGNL class resolver. */
    public static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();

    /** A shared, reusable instance of the default OGNL type converter. */
    public static final TypeConverter TYPE_CONVERTER = new DefaultTypeConverter();

    /** A shared, reusable instance of the custom OGNL member access controller. */
    public static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();

    /**
     * Creates a default {@link OgnlContext} configured with security restrictions.
     * <p>The returned context uses a custom {@link OgnlClassResolver} for class resolution,
     * a default {@link TypeConverter}, and a security-enforcing {@link OgnlMemberAccess}.</p>
     * @return a new, pre-configured {@code OgnlContext} instance
     */
    @NonNull
    public static OgnlContext createDefaultContext() {
        return new OgnlContext(CLASS_RESOLVER, TYPE_CONVERTER, MEMBER_ACCESS);
    }

    /**
     * Creates a default {@link OgnlContext} configured with security restrictions and
     * populated with the given context variables.
     * @param contextVariables a map of variables to be added to the OGNL context
     * @return a new, pre-configured and populated {@code OgnlContext} instance
     */
    @NonNull
    public static OgnlContext createDefaultContext(Map<String, Object> contextVariables) {
        OgnlContext ognlContext = createDefaultContext();
        if (contextVariables != null) {
            ognlContext.setValues(contextVariables);
        }
        return ognlContext;
    }

    /**
     * Evaluates the given OGNL expression tree against the root object.
     * @param tree the parsed OGNL expression tree (as a {@link Node})
     * @param ognlContext the OGNL context for the evaluation
     * @param root the root object for the expression
     * @return the result of the expression evaluation
     * @throws OgnlException if an error occurs during evaluation
     */
    public static Object getValue(Object tree, OgnlContext ognlContext, Object root) throws OgnlException {
        return getValue(tree, ognlContext, root, null);
    }

    /**
     * Evaluates the given OGNL expression tree against the root object and converts
     * the result to the specified type.
     * @param tree the parsed OGNL expression tree (as a {@link Node})
     * @param ognlContext the OGNL context for the evaluation
     * @param root the root object for the expression
     * @param resultType the desired type for the result
     * @return the result of the expression evaluation, converted to the {@code resultType}
     * @throws OgnlException if an error occurs during evaluation or type conversion
     */
    public static Object getValue(Object tree, OgnlContext ognlContext, Object root, Class<?> resultType) throws OgnlException {
        Assert.notNull(tree, "tree must not be null");
        Assert.notNull(ognlContext, "ognlContext must not be null");
        Object result;
        Node node = (Node)tree;
        if (node.getAccessor() != null) {
            result = node.getAccessor().get(ognlContext, root);
        } else {
            result = node.getValue(ognlContext, root);
        }
        if (resultType != null && ognlContext.getTypeConverter() != null) {
            result = ognlContext.getTypeConverter().convertValue(ognlContext, root, null, null, result, resultType);
        }
        return result;
    }

}
