/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.activity.response.transform.apon;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Converts a ProcessResult object to a APON object.
 * 
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class ContentsAponAssembler {

    public static Parameters assemble(ProcessResult processResult) throws InvocationTargetException {
        if (processResult == null || processResult.isEmpty()) {
            return null;
        }

        if (processResult.size() == 1) {
            ContentResult contentResult = processResult.get(0);
            if (contentResult.getName() == null && contentResult.size() == 1) {
                ActionResult actionResult = contentResult.get(0);
                Object resultValue = actionResult.getResultValue();
                if (actionResult.getActionId() == null) {
                    if (resultValue instanceof Parameters) {
                        return (Parameters)resultValue;
                    } else {
                        return null;
                    }
                } else {
                    Parameters container = new VariableParameters();
                    putValue(container, actionResult.getActionId(), resultValue);
                    return container;
                }
            }
        }

        Parameters container = new VariableParameters();
        for (ContentResult contentResult : processResult) {
            assemble(contentResult, container);
        }
        return container;
    }

    private static void assemble(ContentResult contentResult, Parameters container) throws InvocationTargetException {
        if (contentResult.isEmpty()) {
            return;
        }
        if (contentResult.getName() != null) {
            Parameters p = new VariableParameters();
            container.putValue(contentResult.getName(), p);
            container = p;
        }
        for (ActionResult actionResult : contentResult) {
            String actionId = actionResult.getActionId();
            if (actionId != null) {
                Object resultValue = actionResult.getResultValue();
                putValue(container, actionId, resultValue);
            }
        }
    }

    private static void putValue(Parameters container, String name, Object value) throws InvocationTargetException {
        if (value != null) {
            if (value instanceof Collection<?>) {
                for (Object o : ((Collection<?>)value)) {
                    if (o != null) {
                        container.putValue(name, assemble(o));
                    }
                }
            } else if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                for (int i = 0; i < len; i++) {
                    Object o = Array.get(value, i);
                    if (o != null) {
                        container.putValue(name, assemble(o));
                    }
                }
            } else {
                container.putValue(name, assemble(value));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object assemble(Object object) throws InvocationTargetException {
        if (object instanceof Parameters
                || object instanceof String
                || object instanceof Number
                || object instanceof Boolean
                || object instanceof Date) {
            return object;
        } else if (object instanceof Map<?, ?>) {
            Parameters p = new VariableParameters();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>)object).entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(object, value);
                p.putValue(name, assemble(value));
            }
            return p;
        } else if (object instanceof Collection<?>) {
            return object.toString();
        } else if (object.getClass().isArray()) {
            return object.toString();
        } else {
            String[] readablePropertyNames = BeanUtils.getReadablePropertyNamesWithoutNonSerializable(object);
            if (readablePropertyNames != null && readablePropertyNames.length > 0) {
                Parameters p = new VariableParameters();
                for (String name : readablePropertyNames) {
                    Object value = BeanUtils.getProperty(object, name);
                    checkCircularReference(object, value);
                    p.putValue(name, assemble(value));
                }
                return p;
            } else {
                return object.toString();
            }
        }
    }

    private static void checkCircularReference(Object wrapper, Object member) {
        if (wrapper.equals(member)) {
            throw new IllegalArgumentException("APON Serialization Failure: A circular reference was detected " +
                    "while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

}
