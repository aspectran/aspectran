/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.context.expr.ognl;

import com.aspectran.core.context.expr.ExpressionParserException;
import com.aspectran.core.util.ConcurrentReferenceHashMap;
import com.aspectran.core.util.StringUtils;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.util.Map;

/**
 * Support for expressions using OGNL.
 *
 * @since 6.0.0
 */
public class OgnlSupport {

    private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();

    private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();

    private static final Map<String, Object> cache = new ConcurrentReferenceHashMap<>();

    private OgnlSupport() {
    }

    public static Object parseExpression(String expression) throws ExpressionParserException {
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
            throw new ExpressionParserException(expression, e);
        }
    }

    public static OgnlContext createDefaultContext() {
        return new OgnlContext(CLASS_RESOLVER, null, MEMBER_ACCESS);
    }

}
