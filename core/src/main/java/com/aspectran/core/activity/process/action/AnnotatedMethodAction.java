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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.Required;
import com.aspectran.core.context.rule.AnnotatedMethodActionRule;
import com.aspectran.core.context.rule.ParameterMappingRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AnnotatedMethodAction that invoking method in the bean instance.
 * 
 * <p>Created: 2016. 2. 9.</p>
 *
 * @since 2.0.0
 */
public class AnnotatedMethodAction extends AbstractAction {

    private static final Log log = LogFactory.getLog(AnnotatedMethodAction.class);

    private static final Object UNKNOWN_VALUE_TYPE = new Object();

    private final AnnotatedMethodActionRule annotatedMethodActionRule;

    /**
     * Instantiates a new AnnotatedMethodAction.
     *
     * @param annotatedMethodActionRule the annotated method action rule
     */
    public AnnotatedMethodAction(AnnotatedMethodActionRule annotatedMethodActionRule) {
        this.annotatedMethodActionRule = annotatedMethodActionRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        try {
            Object bean;
            if (!Modifier.isInterface(annotatedMethodActionRule.getBeanClass().getModifiers())) {
                bean = activity.getConfiguredBean(annotatedMethodActionRule.getBeanClass());
            } else {
                bean = null;
            }
            Method method = annotatedMethodActionRule.getMethod();
            ParameterMappingRule[] parameterMappingRules = annotatedMethodActionRule.getParameterMappingRules();
            return invokeMethod(activity, bean, method, parameterMappingRules);
        } catch (Exception e) {
            log.error("Failed to execute annotated bean method action " + annotatedMethodActionRule);
            throw e;
        }
    }

    /**
     * Returns the annotated bean method action rule.
     *
     * @return the annotated bean method action rule
     */
    public AnnotatedMethodActionRule getAnnotatedMethodActionRule() {
        return annotatedMethodActionRule;
    }

    @Override
    public String getActionId() {
        return annotatedMethodActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ANNOTATED_METHOD;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)getAnnotatedMethodActionRule();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionType", getActionType());
        tsb.append("annotatedMethodActionRule", annotatedMethodActionRule);
        return tsb.toString();
    }

    public static Object invokeMethod(Activity activity, Object bean, Method method, ParameterMappingRule[] parameterMappingRules)
            throws InvocationTargetException, IllegalAccessException {
        if (parameterMappingRules == null) {
            return method.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
        }
        Translet translet = activity.getTranslet();
        Object[] args = new Object[parameterMappingRules.length];
        for (int i = 0; i < parameterMappingRules.length; i++) {
            Class<?> type = parameterMappingRules[i].getType();
            String name = parameterMappingRules[i].getName();
            String format = parameterMappingRules[i].getFormat();
            boolean required = parameterMappingRules[i].isRequired();
            Exception thrown = null;
            try {
                args[i] = parseArgument(translet, type, name, format);
            } catch (NumberFormatException e) {
                thrown = e;
                if (type.isPrimitive()) {
                    args[i] = 0;
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                thrown = e;
            }
            if (thrown != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid parameter '" + name + "'; Cause: " + thrown.getMessage(), thrown);
                }
            }
            if (required && (args[i] == null || thrown != null)) {
                if (thrown != null) {
                    throw new IllegalArgumentException("Missing required parameter '" + name + "'; Cause: " +
                            thrown.getMessage(), thrown);
                } else {
                    throw new IllegalArgumentException("Missing required parameter '" + name + "'");
                }
            }
        }
        return method.invoke(bean, args);
    }

    private static Object parseArgument(Translet translet, Class<?> type, String name, String format) throws Exception {
        Object result = null;
        if (translet != null) {
            if (type == Translet.class) {
                result = translet;
            } else if (type.isArray()) {
                type = type.getComponentType();
                if (type == Translet.class) {
                    result = new Translet[] { translet };
                } else {
                    String[] values = translet.getParameterValues(name);
                    result = parseArrayValues(type, values, format);
                    if (result == UNKNOWN_VALUE_TYPE) {
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
            } else {
                String value = translet.getParameter(name);
                result = parseValue(type, value, format);
                if (result == UNKNOWN_VALUE_TYPE) {
                    result = parseModel(translet, type);
                }
            }
        }
        return result;
    }

    private static Object parseModel(Translet translet, Class<?> modelType) {
        Object model = ClassUtils.createInstance(modelType);
        BeanDescriptor bd = BeanDescriptor.getInstance(modelType);
        List<String> missingProperties = new ArrayList<>();
        for (String name : bd.getWritablePropertyNames()) {
            try {
                Method method = bd.getSetter(name);
                Class<?> type = bd.getSetterType(name);
                Qualifier qualifierAnno = bd.getSetterAnnotation(method, Qualifier.class);
                String paramName = (qualifierAnno != null ? qualifierAnno.value() : name);
                Format formatAnno = bd.getSetterAnnotation(method, Format.class);
                String format = (formatAnno != null ? formatAnno.value() : null);
                Object val;
                if (type.isArray()) {
                    type = type.getComponentType();
                    val = parseArrayValues(type, translet.getParameterValues(paramName), format);
                } else {
                    val = parseValue(type, translet.getParameter(paramName), format);
                }
                if (val != null && val != UNKNOWN_VALUE_TYPE) {
                    BeanUtils.setProperty(model, name, val);
                } else if (method.isAnnotationPresent(Required.class)) {
                    missingProperties.add(name);
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
            }
        }
        if (!missingProperties.isEmpty()) {
            String properties = StringUtils.joinCommaDelimitedList(missingProperties);
            throw new IllegalArgumentException("Missing required properties [" + properties + "] for " + modelType);
        }
        return model;
    }

    private static Object parseValue(Class<?> type, String value, String format) throws Exception {
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
        } else if (type == Date.class) {
            if (value != null) {
                result = new SimpleDateFormat(format).parse(value);
            }
        } else if (type == LocalDate.class) {
            if (value != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                result = LocalDate.parse(value, formatter);
            }
        } else if (type == LocalDateTime.class) {
            if (value != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                result = LocalDateTime.parse(value, formatter);
            }
        } else if (type == boolean.class || type == Boolean.class) {
            if (value != null) {
                result = Boolean.valueOf(value);
            }
        } else if (type == byte.class || type == Byte.class) {
            if (value != null) {
                result = Byte.valueOf(value);
            }
        } else if (type == short.class || type == Short.class) {
            if (value != null) {
                result = Short.valueOf(value);
            }
        } else if (type == int.class || type == Integer.class) {
            if (value != null) {
                result = Integer.valueOf(value);
            }
        } else if (type == long.class || type == Long.class) {
            if (value != null) {
                result = Long.valueOf(value);
            }
        } else if (type == float.class || type == Float.class) {
            if (value != null) {
                result = Float.valueOf(value);
            }
        } else if (type == double.class || type == Double.class) {
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
        } else {
            result = UNKNOWN_VALUE_TYPE;
        }
        return result;
    }

    private static Object parseArrayValues(Class<?> type, String[] values, String format) throws Exception {
        Object result = null;
        if (values != null) {
            if (type == String.class) {
                result = values;
            } else if (type == char.class) {
                char[] arr = new char[values.length];
                for (int i = 0; i < values.length; i++) {
                    if (!values[i].isEmpty()) {
                        arr[i] = values[i].charAt(0);
                    } else {
                        arr[i] = Character.MIN_VALUE;
                    }
                }
                result = arr;
            } else if (type == Character.class) {
                Character[] arr = new Character[values.length];
                for (int i = 0; i < values.length; i++) {
                    if (!values[i].isEmpty()) {
                        arr[i] = values[i].charAt(0);
                    } else {
                        arr[i] = null;
                    }
                }
                result = arr;
            } else if (type == Date.class) {
                Date[] arr = new Date[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = new SimpleDateFormat(format).parse(values[i]);
                }
                result = arr;
            } else if (type == LocalDate.class) {
                LocalDate[] arr = new LocalDate[values.length];
                for (int i = 0; i < values.length; i++) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    arr[i] = LocalDate.parse(values[i], formatter);
                }
                result = arr;
            } else if (type == LocalDateTime.class) {
                LocalDateTime[] arr = new LocalDateTime[values.length];
                for (int i = 0; i < values.length; i++) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    arr[i] = LocalDateTime.parse(values[i], formatter);
                }
                result = arr;
            } else if (type == boolean.class) {
                boolean[] arr = new boolean[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Boolean.valueOf(values[i]);
                }
                result = arr;
            } else if (type == Boolean.class) {
                Boolean[] arr = new Boolean[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Boolean.valueOf(values[i]);
                }
                result = arr;
            } else if (type == byte.class) {
                byte[] arr = new byte[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Byte.parseByte(values[i]);
                }
                result = arr;
            } else if (type == Byte.class) {
                Byte[] arr = new Byte[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Byte.valueOf(values[i]);
                }
                result = arr;
            } else if (type == short.class) {
                short[] arr = new short[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Short.parseShort(values[i]);
                }
                result = arr;
            } else if (type == Short.class) {
                Short[] arr = new Short[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Short.valueOf(values[i]);
                }
                result = arr;
            } else if (type == int.class) {
                int[] arr = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Integer.parseInt(values[i]);
                }
                result = arr;
            } else if (type == Integer.class) {
                Integer[] arr = new Integer[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Integer.valueOf(values[i]);
                }
                result = arr;
            } else if (type == long.class) {
                long[] arr = new long[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Long.parseLong(values[i]);
                }
                result = arr;
            } else if (type == Long.class) {
                Long[] arr = new Long[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Long.valueOf(values[i]);
                }
                result = arr;
            } else if (type == float.class) {
                float[] arr = new float[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Float.parseFloat(values[i]);
                }
                result = arr;
            } else if (type == Float.class) {
                Float[] arr = new Float[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Float.valueOf(values[i]);
                }
                result = arr;
            } else if (type == double.class) {
                double[] arr = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Double.parseDouble(values[i]);
                }
                result = arr;
            } else if (type == Double.class) {
                Double[] arr = new Double[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = Double.valueOf(values[i]);
                }
                result = arr;
            } else if (type == BigInteger.class) {
                BigInteger[] arr = new BigInteger[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = new BigInteger(values[i]);
                }
                result = arr;
            } else if (type == BigDecimal.class) {
                BigDecimal[] arr = new BigDecimal[values.length];
                for (int i = 0; i < values.length; i++) {
                    arr[i] = new BigDecimal(values[i]);
                }
                result = arr;
            } else {
                result = UNKNOWN_VALUE_TYPE;
            }
        }
        return result;
    }

}
