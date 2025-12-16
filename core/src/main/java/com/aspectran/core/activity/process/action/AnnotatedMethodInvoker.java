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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.converter.TypeConversionException;
import com.aspectran.core.component.converter.TypeConverter;
import com.aspectran.core.component.converter.TypeConverterRegistry;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.utils.BeanDescriptor;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.TypeUtils;
import com.aspectran.utils.apon.Parameters;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract utility class that provides the core logic for invoking methods whose
 * parameters are bound using Aspectran's parameter annotations.
 *
 * <p>This helper handles the complex process of resolving arguments from the current
 * {@link Activity} and {@link Translet}, performing type conversions, and then invoking
 * the target method. It is the engine behind annotated action execution.</p>
 *
 * <p>Created: 2025. 5. 14.</p>
 */
public abstract class AnnotatedMethodInvoker {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatedMethodInvoker.class);

    /**
     * Invokes the specified annotated method on the target bean with parameter binding.
     * @param activity the current activity, used as the source for resolving arguments
     * @param bean the target bean instance on which to invoke the method
     * @param method the method to be invoked
     * @param parameterBindingRules an array of rules that describe how to bind and convert each parameter
     * @return the result of the method invocation
     * @throws ParameterBindingException if an error occurs during parameter resolution or binding
     * @throws Exception if the underlying method invocation throws an exception
     */
    public static Object invoke(
            @NonNull Activity activity, @Nullable Object bean, @NonNull Method method,
            @Nullable ParameterBindingRule[] parameterBindingRules) throws Exception {
        ParameterBindingRule pbr = null;
        try {
            if (parameterBindingRules == null) {
                return method.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
            }

            Object[] args = new Object[parameterBindingRules.length];
            for (int i = 0; i < parameterBindingRules.length; i++) {
                pbr = parameterBindingRules[i];
                Exception thrown = null;
                try {
                    if (activity.hasTranslet()) {
                        args[i] = resolveArgumentWithTranslet(activity, pbr);
                    } else {
                        args[i] = resolveArgument(activity, pbr);
                    }
                } catch (TypeConversionException e) {
                    thrown = e;
                    if (e.getCause() instanceof NumberFormatException && pbr.getType().isPrimitive()) {
                        args[i] = TypeUtils.getPrimitiveDefaultValue(pbr.getType());
                    }
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    thrown = e;
                }

                if (pbr.isRequired() && (args[i] == null || thrown != null)) {
                    if (thrown != null) {
                        throw new IllegalArgumentException("Missing required parameter '" + pbr.getName() +
                                "'; Cause: " + thrown.getMessage(), thrown);
                    } else {
                        throw new IllegalArgumentException("Missing required parameter '" + pbr.getName() + "'");
                    }
                }

                if (thrown != null) {
                    if (logger.isDebugEnabled()) {
                        Throwable rootCause = ExceptionUtils.getRootCause(thrown);
                        logger.debug("Failed to bind parameter '{}' (required type: {}). Reason: {}",
                                pbr.getName(),
                                pbr.getType().getSimpleName(),
                                (rootCause != null ? rootCause.getMessage() : thrown.getMessage()));
                    }
                }
            }

            pbr = null;
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            throw ExceptionUtils.getCause(e);
        } catch (Exception e) {
            if (pbr != null) {
                throw new ParameterBindingException(pbr, e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Resolves the argument for a single method parameter when a {@link Translet} is available.
     * <p>It handles various parameter types, including the Translet itself, request parameters,
     * maps, collections, and APON objects. It can also perform bean lookups and model binding.</p>
     * @param activity the current activity
     * @param pbr the binding rule for the parameter to resolve
     * @return the resolved argument value
     * @throws Exception if resolution fails for any reason
     */
    private static Object resolveArgumentWithTranslet(@NonNull Activity activity, @NonNull ParameterBindingRule pbr)
            throws Exception {
        Translet translet = activity.getTranslet();
        Class<?> type = pbr.getType();
        String name = pbr.getName();

        Object result;
        if (type == Translet.class) {
            result = translet;
        } else if (type.isArray()) {
            if (type.getComponentType() == Translet.class) {
                result = new Translet[] { translet };
            } else {
                Object value = translet.getParameterValues(name);
                result = resolveValue(value, type, pbr.getAnnotations(), activity);
                if (result == Void.TYPE) {
                    result = null;
                }
            }
        } else if (type == ParameterMap.class) {
            ParameterMap parameterMap = new ParameterMap();
            for (String paramName : translet.getParameterNames()) {
                parameterMap.setParameterValues(paramName, translet.getParameterValues(paramName));
            }
            result = parameterMap;
        } else if (Map.class.isAssignableFrom(type)) {
            if (!type.isInterface()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>)ClassUtils.createInstance(type);
                map.putAll(translet.getAllParameters());
                result = map;
            } else {
                result = new HashMap<>(translet.getAllParameters());
            }
        } else if (Collection.class.isAssignableFrom(type)) {
            String[] values = translet.getParameterValues(name);
            if (!type.isInterface()) {
                @SuppressWarnings("unchecked")
                Collection<String> collection = (Collection<String>)ClassUtils.createInstance(type);
                if (values != null) {
                    collection.addAll(Arrays.asList(values));
                }
                result = collection;
            } else {
                if (values != null) {
                    result = new ArrayList<>(Arrays.asList(values));
                } else {
                    result = new ArrayList<>();
                }
            }
        } else if (Parameters.class.isAssignableFrom(type)) {
            Parameters parameters;
            if (type.isInterface()) {
                parameters = translet.getRequestAdapter().getBodyAsParameters();
                if (parameters == null) {
                    parameters = translet.getRequestAdapter().getParameters();
                }
            } else {
                @SuppressWarnings("unchecked")
                Class<? extends Parameters> requiredType = (Class<? extends Parameters>)type;
                parameters = translet.getRequestAdapter().getBodyAsParameters(requiredType);
                if (parameters == null) {
                    parameters = translet.getRequestAdapter().getParameters(requiredType);
                }
            }
            return parameters;
        } else {
            Object value = translet.getParameter(name);
            result = resolveValue(value, type, pbr.getAnnotations(), activity);
            if (result == Void.TYPE) {
                if (type.isAnnotationPresent(Component.class)) {
                    try {
                        result = translet.getBean(type);
                    } catch (NoUniqueBeanException e) {
                        result = translet.getBean(type, name);
                    }
                } else {
                    result = bindModel(activity, type);
                }
            }
        }
        return result;
    }

    /**
     * Resolves the argument for a single method parameter in a non-translet environment (e.g., daemon).
     * <p>Resolution is typically limited to injecting beans from the activity context.</p>
     * @param activity the current activity
     * @param pbr the binding rule for the parameter to resolve
     * @return the resolved argument value (usually a bean)
     */
    private static Object resolveArgument(@NonNull Activity activity, @NonNull ParameterBindingRule pbr) {
        Class<?> type = pbr.getType();
        String name = pbr.getName();

        Object result;
        if (type == Translet.class) {
            result = null;
        } else if (type.isArray() && type.getComponentType() == Translet.class) {
            result = new Translet[] { };
        } else {
            try {
                result = activity.getBean(type);
            } catch (NoUniqueBeanException e) {
                result = activity.getBean(type, name);
            }
        }
        return result;
    }

    /**
     * Creates an instance of the specified type and populates its properties from request parameters.
     * This is used for binding complex objects (models) from the request.
     * @param activity the current activity, which provides access to the translet and request parameters
     * @param type the type of the model object to create and bind
     * @return the populated model object
     * @throws Exception if the model cannot be instantiated or a property cannot be set
     */
    @NonNull
    private static Object bindModel(@NonNull Activity activity, Class<?> type) throws Exception {
        Translet translet = activity.getTranslet();
        Object model = ClassUtils.createInstance(type);
        BeanDescriptor bd = BeanDescriptor.getInstance(type);
        for (String name : bd.getWritablePropertyNames()) {
            Method method = bd.getSetter(name);
            Class<?> setterType = bd.getSetterType(name);
            Annotation[] methodAnnos = method.getAnnotations();
            Annotation[] paramAnnos = method.getParameterAnnotations()[0];
            Annotation[] annotations = new Annotation[methodAnnos.length + paramAnnos.length];
            System.arraycopy(methodAnnos, 0, annotations, 0, methodAnnos.length);
            System.arraycopy(paramAnnos, 0, annotations, methodAnnos.length, paramAnnos.length);

            String paramName = name;
            for (Annotation anno : annotations) {
                if (anno.annotationType() == Qualifier.class) {
                    Qualifier qualifier = (Qualifier)anno;
                    if (qualifier.value() != null && !qualifier.value().isEmpty()) {
                        paramName = qualifier.value();
                    }
                    break;
                }
            }

            Object value;
            if (setterType.isArray()) {
                value = translet.getParameterValues(paramName);
            } else {
                value = translet.getParameter(paramName);
            }

            try {
                Object result = resolveValue(value, setterType, annotations, activity);
                if (result != null && result != Void.TYPE) {
                    BeanUtils.setProperty(model, name, result);
                }
            } catch (TypeConversionException e) {
                if (logger.isDebugEnabled()) {
                    Throwable rootCause = ExceptionUtils.getRootCause(e);
                    logger.debug("Failed to bind property '{}' (required type: {}) for bean '{}'. Value: '{}'. Reason: {}",
                            paramName, setterType.getSimpleName(), type.getSimpleName(), value,
                            (rootCause != null ? rootCause.getMessage() : e.getMessage()));
                }
            }
        }
        return model;
    }

    /**
     * Resolves the given value by converting it to the target type using a registered {@link TypeConverter}.
     * <p>This method handles both single values and arrays of values, applying type conversion
     * for each element as needed.</p>
     * @param value the value to resolve (can be a String or String[])
     * @param targetType the target type to convert to
     * @param annotations annotations on the target parameter, which can influence conversion (e.g., @Format)
     * @param activity the current activity
     * @return the converted value, or {@code null} if the input value is null,
     *         or {@link Void#TYPE} if no suitable converter is found
     * @throws TypeConversionException if the type conversion fails
     */
    @Nullable
    private static Object resolveValue(
            Object value, @NonNull Class<?> targetType, Annotation[] annotations, @NonNull Activity activity)
            throws TypeConversionException {
        TypeConverterRegistry registry = activity.getActivityContext().getTypeConverterRegistry();
        if (targetType.isArray()) {
            Class<?> componentType = targetType.getComponentType();
            TypeConverter<?> converter = registry.getConverter(componentType);
            if (converter == null) {
                return Void.TYPE;
            }
            if (value == null) {
                return null;
            }
            String[] values = (value instanceof String[] ? (String[])value : new String[] { value.toString() });
            Object array = Array.newInstance(componentType, values.length);
            for (int i = 0; i < values.length; i++) {
                try {
                    Array.set(array, i, converter.convert(values[i], annotations, activity));
                } catch (TypeConversionException e) {
                    throw e;
                } catch (Exception e) {
                    throw new TypeConversionException(values[i], componentType, e);
                }
            }
            return array;
        } else {
            TypeConverter<?> converter = registry.getConverter(targetType);
            if (converter == null) {
                return Void.TYPE;
            }
            String stringValue = (value instanceof String[] ? ((String[])value)[0] : (value != null ? value.toString() : null));
            try {
                Object result = converter.convert(stringValue, annotations, activity);
                if (result == null && targetType.isPrimitive()) {
                    return TypeUtils.getPrimitiveDefaultValue(targetType);
                }
                return result;
            } catch (TypeConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new TypeConversionException(stringValue, targetType, e);
            }
        }
    }

}
