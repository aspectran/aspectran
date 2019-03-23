/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.apon.InvalidParameterException;
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
public class ContentsAponReader {

    public static Parameters read(ProcessResult processResult) {
        if (processResult == null || processResult.isEmpty()) {
            return null;
        }

        if (processResult.size() == 1) {
            ContentResult contentResult = processResult.get(0);
            if (contentResult.getName() == null && contentResult.size() == 1) {
                ActionResult actionResult = contentResult.get(0);
                if (actionResult.getActionId() == null) {
                    Object resultValue = actionResult.getResultValue();
                    if (resultValue instanceof Parameters) {
                        return (Parameters)resultValue;
                    }
                }
            }
        }

        Parameters container = new VariableParameters();
        for (ContentResult contentResult : processResult) {
            read(contentResult, container);
        }
        return container;
    }

    private static void read(ContentResult contentResult, Parameters container) {
        if (contentResult.getName() != null) {
            Parameters p = new VariableParameters();
            container.putValue(contentResult.getName(), p);
            container = p;
        }
        for (ActionResult actionResult : contentResult) {
            putValue(container, actionResult.getActionId(), actionResult.getResultValue());
        }
    }

    private static void putValue(Parameters container, String name, Object value) {
        if (value == null) {
            return;
        }
        if (name == null) {
            Object o = valuelize(value);
            if (o instanceof Parameters) {
                container.putAll((Parameters)o);
            }
        } else {
            if (container.hasParameter(name)) {
                container.clearValue(name);
            }
            if (value instanceof Collection<?>) {
                for (Object o : ((Collection<?>)value)) {
                    if (o != null) {
                        container.putValue(name, valuelize(o));
                    }
                }
            } else if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                for (int i = 0; i < len; i++) {
                    Object o = Array.get(value, i);
                    if (o != null) {
                        container.putValue(name, valuelize(o));
                    }
                }
            } else {
                container.putValue(name, valuelize(value));
            }
        }
    }

    private static Object valuelize(Object object) {
        if (object instanceof Parameters
                || object instanceof String
                || object instanceof Number
                || object instanceof Boolean
                || object instanceof Date) {
            return object;
        } else if (object instanceof Map<?, ?>) {
            Parameters p = new VariableParameters();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(object, value);
                p.putValue(name, valuelize(value));
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
                    Object value;
                    try {
                        value = BeanUtils.getProperty(object, name);
                    } catch (InvocationTargetException e) {
                        throw new InvalidParameterException(e);
                    }
                    checkCircularReference(object, value);
                    p.putValue(name, valuelize(value));
                }
                return p;
            } else {
                return object.toString();
            }
        }
    }

    private static void checkCircularReference(Object wrapper, Object member) {
        if (wrapper.equals(member)) {
            throw new InvalidParameterException("APON Serialization Failure: A circular reference was detected " +
                    "while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

}
