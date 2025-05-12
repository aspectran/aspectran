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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.Required;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.BeanDescriptor;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code AnnotatedMethodAction} that invokes a method of the bean instance
 * specified by the annotation.
 *
 * <p>Created: 2016. 2. 9.</p>
 *
 * @since 2.0.0
 */
public class AnnotatedAction implements Executable {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatedAction.class);

    private final AnnotatedActionRule annotatedActionRule;

    /**
     * Instantiates a new AnnotatedMethodAction.
     * @param annotatedActionRule the annotated method action rule
     */
    public AnnotatedAction(AnnotatedActionRule annotatedActionRule) {
        this.annotatedActionRule = annotatedActionRule;
    }

    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        try {
            Object bean = resolveBean(activity);
            Method method = annotatedActionRule.getMethod();
            ParameterBindingRule[] parameterBindingRules = annotatedActionRule.getParameterBindingRules();
            if (method.getReturnType() == Void.TYPE) {
                invokeMethod(activity, bean, method, parameterBindingRules);
                return Void.TYPE;
            } else {
                return invokeMethod(activity, bean, method, parameterBindingRules);
            }
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

    protected Object resolveBean(@NonNull Activity activity) throws Exception {
        Object bean = null;
        if (!Modifier.isInterface(annotatedActionRule.getBeanClass().getModifiers())) {
            bean = activity.getBean(annotatedActionRule.getBeanClass());
        }
        return bean;
    }

    /**
     * Returns the annotated bean method action rule.
     * @return the annotated bean method action rule
     */
    public AnnotatedActionRule getAnnotatedActionRule() {
        return annotatedActionRule;
    }

    @Override
    public String getActionId() {
        return annotatedActionRule.getActionId();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.INVOKE_ANNOTATED;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), annotatedActionRule);
        return tsb.toString();
    }

    public static Object invokeMethod(
            @NonNull Activity activity,
            @Nullable Object bean,
            @NonNull Method method,
            @Nullable ParameterBindingRule[] parameterBindingRules) throws Exception {
        ParameterBindingRule parameterBindingRule = null;
        try {
            if (parameterBindingRules == null) {
                return method.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
            }
            Translet translet = activity.getTranslet();
            Object[] args = new Object[parameterBindingRules.length];
            for (int i = 0; i < parameterBindingRules.length; i++) {
                parameterBindingRule = parameterBindingRules[i];
                Class<?> type = parameterBindingRule.getType();
                String name = parameterBindingRule.getName();
                String format = parameterBindingRule.getFormat();
                boolean required = parameterBindingRule.isRequired();
                Exception thrown = null;
                try {
                    if (translet != null) {
                        StringifyContext stringifyContext = activity.getStringifyContext();
                        args[i] = resolveArgumentWithTranslet(translet, type, name, stringifyContext, format);
                    } else {
                        args[i] = resolveArgument(activity, type, name);
                    }
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (MethodArgumentTypeMismatchException e) {
                    thrown = e;
                    if (e.getCause() instanceof NumberFormatException) {
                        if (type.isPrimitive()) {
                            args[i] = 0;
                        }
                    }
                } catch (Exception e) {
                    thrown = e;
                }
                if (required && (args[i] == null || thrown != null)) {
                    if (thrown != null) {
                        throw new IllegalArgumentException("Missing required parameter '" + name + "'; Cause: " +
                                thrown.getMessage(), thrown);
                    } else {
                        throw new IllegalArgumentException("Missing required parameter '" + name + "'");
                    }
                }
                if (thrown != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to bind argument {}; Cause: {}",
                                parameterBindingRule, thrown.getMessage(), thrown);
                    }
                }
            }
            parameterBindingRule = null;
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception)e.getCause();
            } else {
                throw e;
            }
        } catch (Exception e) {
            if (parameterBindingRule != null) {
                throw new ParameterBindingException(parameterBindingRule, e);
            } else {
                throw e;
            }
        }
    }

    private static Object resolveArgumentWithTranslet(
            Translet translet, Class<?> type, String name, StringifyContext stringifyContext, String format)
            throws MethodArgumentTypeMismatchException, RequestParseException {
        Object result;
        if (type == Translet.class) {
            result = translet;
        } else if (type.isArray()) {
            type = type.getComponentType();
            if (type == Translet.class) {
                result = new Translet[] { translet };
            } else {
                String[] values = translet.getParameterValues(name);
                result = resolveValue(type, values, stringifyContext, format);
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
            String value = translet.getParameter(name);
            result = resolveValue(type, value, stringifyContext, format);
            if (result == Void.TYPE) {
                if (type.isAnnotationPresent(Component.class)) {
                    try {
                        result = translet.getBean(type);
                    } catch (NoUniqueBeanException e) {
                        result = translet.getBean(type, name);
                    }
                } else {
                    result = parseModel(translet, type, stringifyContext);
                }
            }
        }
        return result;
    }

    private static Object resolveArgument(Activity activity, Class<?> type, String name)
            throws MethodArgumentTypeMismatchException {
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

    @NonNull
    private static Object parseModel(Translet translet, Class<?> type, StringifyContext stringifyContext) {
        Object model = ClassUtils.createInstance(type);
        BeanDescriptor bd = BeanDescriptor.getInstance(type);
        List<String> missingProperties = new ArrayList<>();
        for (String name : bd.getWritablePropertyNames()) {
            try {
                Method method = bd.getSetter(name);
                Class<?> setterType = bd.getSetterType(name);
                Qualifier qualifierAnno = bd.getSetterAnnotation(method, Qualifier.class);
                String paramName = (qualifierAnno != null ? qualifierAnno.value() : name);
                Format formatAnno = bd.getSetterAnnotation(method, Format.class);
                String format = (formatAnno != null ? formatAnno.value() : null);
                Object result;
                if (setterType.isArray()) {
                    setterType = setterType.getComponentType();
                    String[] values = translet.getParameterValues(paramName);
                    result = resolveValue(setterType, values, stringifyContext, format);
                } else {
                    String value = translet.getParameter(paramName);
                    result = resolveValue(setterType, value, stringifyContext, format);
                }
                if (result != null && result != Void.TYPE) {
                    BeanUtils.setProperty(model, name, result);
                } else if (method.isAnnotationPresent(Required.class)) {
                    missingProperties.add(name);
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        if (!missingProperties.isEmpty()) {
            String properties = StringUtils.joinWithCommas(missingProperties);
            throw new IllegalArgumentException("Missing required properties [" + properties + "] for " + type);
        }
        return model;
    }

    private static Object resolveValue(Class<?> type, String value, StringifyContext stringifyContext, String format)
            throws MethodArgumentTypeMismatchException {
        try {
            Object result = null;
            if (type == String.class) {
                result = value;
            } else if (type == char.class) {
                if (value != null && !value.isEmpty()) {
                    result = value.charAt(0);
                } else {
                    result = Character.MIN_VALUE;
                }
            } else if (type == Character.class) {
                if (value != null && !value.isEmpty()) {
                    result = value.charAt(0);
                }
            } else if (type == boolean.class) {
                result = Boolean.valueOf(value);
            } else if (type == Boolean.class) {
                if (value != null) {
                    result = Boolean.valueOf(value);
                }
            } else if (type == byte.class) {
                if (value != null) {
                    result = Byte.valueOf(value);
                } else {
                    result = 0;
                }
            } else if (type == Byte.class) {
                if (value != null) {
                    result = Byte.valueOf(value);
                }
            } else if (type == short.class) {
                if (value != null) {
                    result = Short.valueOf(value);
                } else {
                    result = 0;
                }
            } else if (type == Short.class) {
                if (value != null) {
                    result = Short.valueOf(value);
                }
            } else if (type == int.class) {
                if (value != null) {
                    result = Integer.valueOf(value);
                } else {
                    result = 0;
                }
            } else if (type == Integer.class) {
                if (value != null) {
                    result = Integer.valueOf(value);
                }
            } else if (type == long.class) {
                if (value != null) {
                    result = Long.valueOf(value);
                } else {
                    result = 0L;
                }
            } else if (type == Long.class) {
                if (value != null) {
                    result = Long.valueOf(value);
                }
            } else if (type == float.class) {
                if (value != null) {
                    result = Float.valueOf(value);
                } else {
                    result = 0f;
                }
            } else if (type == Float.class) {
                if (value != null) {
                    result = Float.valueOf(value);
                }
            } else if (type == double.class) {
                if (value != null) {
                    result = Double.valueOf(value);
                } else {
                    result = 0d;
                }
            } else if (type == Double.class) {
                if (value != null) {
                    result = Double.valueOf(value);
                }
            } else if (type == BigInteger.class) {
                if (value != null) {
                    result = new BigInteger(value);
                }
            } else if (type == BigDecimal.class) {
                if (value != null) {
                    result = new BigDecimal(value);
                }
            } else if (type == LocalDateTime.class) {
                if (value != null) {
                    result = stringifyContext.toLocalDateTime(value, format);
                }
            } else if (type == LocalDate.class) {
                if (value != null) {
                    result = stringifyContext.toLocalDate(value, format);
                }
            } else if (type == LocalTime.class) {
                if (value != null) {
                    result = stringifyContext.toLocalTime(value, format);
                }
            } else if (type == Date.class) {
                if (value != null) {
                    result = stringifyContext.toDate(value, format);
                }
            } else {
                result = Void.TYPE;
            }
            return result;
        } catch (Exception e) {
            throw new MethodArgumentTypeMismatchException(String.class, type, e);
        }
    }

    private static Object resolveValue(
            Class<?> type, String[] values, StringifyContext stringifyContext, String format)
            throws MethodArgumentTypeMismatchException {
        try {
            Object result = null;
            if (type == String.class) {
                result = values;
            } else if (type == char.class) {
                if (values != null) {
                    char[] arr = new char[values.length];
                    for (int i = 0; i < values.length; i++) {
                        if (!values[i].isEmpty()) {
                            arr[i] = values[i].charAt(0);
                        } else {
                            arr[i] = Character.MIN_VALUE;
                        }
                    }
                    result = arr;
                } else {
                    result = new char[0];
                }
            } else if (type == Character.class) {
                if (values != null) {
                    Character[] arr = new Character[values.length];
                    for (int i = 0; i < values.length; i++) {
                        if (!values[i].isEmpty()) {
                            arr[i] = values[i].charAt(0);
                        } else {
                            arr[i] = null;
                        }
                    }
                    result = arr;
                }
            } else if (type == boolean.class) {
                if (values != null) {
                    boolean[] arr = new boolean[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Boolean.parseBoolean(values[i]);
                    }
                    result = arr;
                } else {
                    result = new boolean[0];
                }
            } else if (type == Boolean.class) {
                if (values != null) {
                    Boolean[] arr = new Boolean[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Boolean.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == byte.class) {
                if (values != null) {
                    byte[] arr = new byte[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Byte.parseByte(values[i]);
                    }
                    result = arr;
                } else {
                    result = new byte[0];
                }
            } else if (type == Byte.class) {
                if (values != null) {
                    Byte[] arr = new Byte[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Byte.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == short.class) {
                if (values != null) {
                    short[] arr = new short[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Short.parseShort(values[i]);
                    }
                    result = arr;
                } else {
                    result = new short[0];
                }
            } else if (type == Short.class) {
                if (values != null) {
                    Short[] arr = new Short[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Short.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == int.class) {
                if (values != null) {
                    int[] arr = new int[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Integer.parseInt(values[i]);
                    }
                    result = arr;
                } else {
                    result = new int[0];
                }
            } else if (type == Integer.class) {
                if (values != null) {
                    Integer[] arr = new Integer[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Integer.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == long.class) {
                if (values != null) {
                    long[] arr = new long[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Long.parseLong(values[i]);
                    }
                    result = arr;
                } else {
                    result = new long[0];
                }
            } else if (type == Long.class) {
                if (values != null) {
                    Long[] arr = new Long[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Long.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == float.class) {
                if (values != null) {
                    float[] arr = new float[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Float.parseFloat(values[i]);
                    }
                    result = arr;
                } else {
                    result = new float[0];
                }
            } else if (type == Float.class) {
                if (values != null) {
                    Float[] arr = new Float[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Float.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == double.class) {
                if (values != null) {
                    double[] arr = new double[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Double.parseDouble(values[i]);
                    }
                    result = arr;
                } else {
                    result = new double[0];
                }
            } else if (type == Double.class) {
                if (values != null) {
                    Double[] arr = new Double[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = Double.valueOf(values[i]);
                    }
                    result = arr;
                }
            } else if (type == BigInteger.class) {
                if (values != null) {
                    BigInteger[] arr = new BigInteger[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = new BigInteger(values[i]);
                    }
                    result = arr;
                }
            } else if (type == BigDecimal.class) {
                if (values != null) {
                    BigDecimal[] arr = new BigDecimal[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = new BigDecimal(values[i]);
                    }
                    result = arr;
                }
            } else if (type == LocalDateTime.class) {
                if (values != null) {
                    LocalDateTime[] arr = new LocalDateTime[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = stringifyContext.toLocalDateTime(values[i], format);
                    }
                    result = arr;
                }
            } else if (type == LocalDate.class) {
                if (values != null) {
                    LocalDate[] arr = new LocalDate[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = stringifyContext.toLocalDate(values[i], format);
                    }
                    result = arr;
                }
            } else if (type == LocalTime.class) {
                if (values != null) {
                    LocalTime[] arr = new LocalTime[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = stringifyContext.toLocalTime(values[i], format);
                    }
                    result = arr;
                }
            } else if (type == Date.class) {
                if (values != null) {
                    Date[] arr = new Date[values.length];
                    for (int i = 0; i < values.length; i++) {
                        arr[i] = stringifyContext.toDate(values[i], format);
                    }
                    result = arr;
                }
            } else {
                result = Void.TYPE;
            }
            return result;
        } catch (Exception e) {
            throw new MethodArgumentTypeMismatchException(String[].class, type, e);
        }
    }

}
