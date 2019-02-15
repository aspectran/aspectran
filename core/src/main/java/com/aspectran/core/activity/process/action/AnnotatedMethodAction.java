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
import com.aspectran.core.context.rule.AnnotatedMethodActionRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
            AutowireRule autowireRule = annotatedMethodActionRule.getAutowireRule();
            return invokeMethod(activity, bean, method, autowireRule);
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

    public static Object invokeMethod(Activity activity, Object bean, Method method, AutowireRule autowireRule)
            throws InvocationTargetException, IllegalAccessException {
        Translet translet = activity.getTranslet();
        String[] qualifiers = autowireRule.getQualifiers();
        String[] formats = autowireRule.getFormats();
        Class<?>[] argTypes = autowireRule.getTypes();
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Class<?> type = argTypes[i];
            String name = qualifiers[i];
            String format = formats[i];
            args[i] = parseArgument(translet, type, name, format);
            if (autowireRule.isRequired() && args[i] == null) {
                throw new IllegalArgumentException("");
            }
        }
        return method.invoke(bean, args);
    }

    private static Object parseArgument(Translet translet, Class<?> type, String name, String format) {
        Object result = null;
        if (translet != null) {
            try {
                if (type == Translet.class) {
                    result = translet;
                } else if (type.isArray()) {
                    type = type.getComponentType();
                    if (type == Translet.class) {
                        result = new Translet[] { translet };
                    } else {
                        String[] values = translet.getParameterValues(name);
                        result = parseValue(type, values, format);
                        if (result == UNKNOWN_VALUE_TYPE) {
                            result = null;
                        }
                    }
                } else if (Collection.class.isAssignableFrom(type)) {
                    if (!type.isInterface()) {
                        @SuppressWarnings("unchecked")
                        Collection<String> collection = (Collection<String>)ClassUtils.createInstance(type);
                        collection.addAll(Arrays.asList(translet.getParameterValues(name)));
                    } else {
                        result = new HashMap<>(translet.getAllParameters());
                    }
                } else if (Map.class.isAssignableFrom(type)) {
                    if (!type.isInterface()) {
                        result = ClassUtils.createInstance(type, translet.getAllParameters());
                    } else {
                        result = new HashMap<>(translet.getAllParameters());
                    }
                } else {
                    String value = translet.getParameter(name);
                    if (StringUtils.hasLength(value)) {
                        result = parseValue(type, value, format);
                        if (result == UNKNOWN_VALUE_TYPE) {
                            Object obj = ClassUtils.createInstance(type);
                            BeanDescriptor bd = BeanDescriptor.getInstance(type);
                            for (String setterName : bd.getWritablePropertyNames()) {
                                Class<?> setterType = bd.getSetterType(setterName);
                                Object val = parseValue(setterType, setterName, null);
                                if (val != null) {
                                    BeanUtils.setProperty(obj, setterName, val);
                                }
                            }
                            result = obj;
                        }
                    }
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to autowire parameter '" + name + "'", e);
                }
            }
        }
        return result;
    }

    private static Object parseValue(Class<?> type, String value, String format) {
        Object result;
        if (type == String.class) {
            result = value;
        } else if (type == Date.class) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime ldt = LocalDateTime.parse(value, formatter);
            result = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } else if (type == LocalDateTime.class) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            result = LocalDateTime.parse(value, formatter);
        } else if (type == LocalDate.class) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            result = LocalDate.parse(value, formatter);
        } else if (type == Boolean.class) {
            result = Boolean.valueOf(value);
        } else if (type == Byte.class) {
            result = Byte.valueOf(value);
        } else if (type == Short.class) {
            result = Short.valueOf(value);
        } else if (type == Integer.class) {
            result = Integer.valueOf(value);
        } else if (type == Long.class) {
            result = Long.valueOf(value);
        } else if (type == Float.class) {
            result = Float.valueOf(value);
        } else if (type == Double.class) {
            result = Double.valueOf(value);
        } else if (type == BigDecimal.class) {
            result = new BigDecimal(value);
        } else if (type == BigInteger.class) {
            result = new BigInteger(value);
        } else {
            result = UNKNOWN_VALUE_TYPE;
        }
        return result;
    }

    private static Object parseValue(Class<?> type, String[] values, String format) {
        Object result = null;
        if (values != null && values.length > 0) {
            if (type == String.class) {
                result = values;
            } else if (type == Date.class) {
                Date[] arr = new Date[values.length];
                int i = 0;
                for (String val : values) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    LocalDateTime ldt = LocalDateTime.parse(val, formatter);
                    arr[i++] = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                }
                result = arr;
            } else if (type == LocalDateTime.class) {
                LocalDateTime[] arr = new LocalDateTime[values.length];
                int i = 0;
                for (String val : values) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    arr[i++] = LocalDateTime.parse(val, formatter);
                }
                result = arr;
            } else if (type == LocalDate.class) {
                LocalDate[] arr = new LocalDate[values.length];
                int i = 0;
                for (String val : values) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    arr[i++] = LocalDate.parse(val, formatter);
                }
                result = arr;
            } else if (type == Boolean.class) {
                Boolean[] arr = new Boolean[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Boolean.valueOf(val);
                }
                result = arr;
            } else if (type == Byte.class) {
                Byte[] arr = new Byte[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Byte.valueOf(val);
                }
                result = arr;
            } else if (type == Short.class) {
                Short[] arr = new Short[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Short.valueOf(val);
                }
                result = arr;
            } else if (type == Integer.class) {
                Integer[] arr = new Integer[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Integer.valueOf(val);
                }
                result = arr;
            } else if (type == Long.class) {
                Long[] arr = new Long[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Long.valueOf(val);
                }
                result = arr;
            } else if (type == Float.class) {
                Float[] arr = new Float[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Float.valueOf(val);
                }
                result = arr;
            } else if (type == Double.class) {
                Double[] arr = new Double[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = Double.valueOf(val);
                }
                result = arr;
            } else if (type == BigDecimal.class) {
                BigDecimal[] arr = new BigDecimal[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = new BigDecimal(val);
                }
                result = arr;
            } else if (type == BigInteger.class) {
                BigInteger[] arr = new BigInteger[values.length];
                int i = 0;
                for (String val : values) {
                    arr[i++] = new BigInteger(val);
                }
                result = arr;
            } else {
                result = UNKNOWN_VALUE_TYPE;
            }
        }
        return result;
    }

}
