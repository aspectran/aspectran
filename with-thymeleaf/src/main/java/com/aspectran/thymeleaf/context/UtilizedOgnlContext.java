/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.thymeleaf.context;

import com.aspectran.core.context.asel.ognl.OgnlSupport;
import com.aspectran.utils.annotation.jsr305.NonNull;
import ognl.OgnlContext;
import org.thymeleaf.expression.IExpressionObjects;

/**
 * <p>Created: 2024. 11. 27.</p>
 */
public final class UtilizedOgnlContext extends OgnlContext {

    private final IExpressionObjects expressionObjects;

    public UtilizedOgnlContext(IExpressionObjects expressionObjects) {
        super(OgnlSupport.CLASS_RESOLVER, null, OgnlSupport.MEMBER_ACCESS);
        this.expressionObjects = expressionObjects;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean exists = super.containsKey(key);
        if (!exists && expressionObjects.containsObject(key.toString())) {
            exists = true;
        }
        return exists;
    }

    @Override
    public Object get(@NonNull Object key) {
        Object value = super.get(key);
        if (value == null && expressionObjects.containsObject(key.toString())) {
            value = expressionObjects.getObject(key.toString());
            super.put(key.toString(), value);
        }
        return value;
    }

    @Override
    public Object put(String key, Object value) {
        if (expressionObjects.containsObject(key)) {
            throw new IllegalArgumentException(
                "Cannot put entry with key \"" + key + "\" into Expression Objects wrapper map: key matches the " +
                    "name of one of the expression objects");
        }
        return super.put(key, value);
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

}
