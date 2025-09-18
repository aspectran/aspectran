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
package com.aspectran.core.context.asel.token;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.BeanTypeUtils;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.PropertiesLoaderUtils;
import com.aspectran.utils.ReflectionUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The default implementation of the {@link TokenEvaluator} interface.
 * <p>This class provides the concrete logic for resolving {@link Token} values from
 * various sources within an {@link Activity}, such as parameters, attributes, beans,
 * properties, and templates. It interprets the token's type, name, and directives
 * to fetch the appropriate data.</p>
 *
 * <p>Created: 2008. 03. 29 AM 12:59:16</p>
 */
public class TokenEvaluation implements TokenEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(TokenEvaluation.class);

    private final Activity activity;

    /**
     * Instantiates a new TokenEvaluation.
     * @param activity the current Activity
     */
    public TokenEvaluation(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        this.activity = activity;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public Object evaluate(Token token) {
        try {
            TokenType tokenType = token.getType();
            Object value = null;
            if (tokenType == TokenType.TEXT) {
                value = token.getDefaultValue();
            } else if (tokenType == TokenType.BEAN) {
                value = getBean(token);
            } else if (tokenType == TokenType.TEMPLATE) {
                value = getTemplate(token);
            } else if (tokenType == TokenType.PARAMETER) {
                String[] values = getParameterValues(token.getName());
                if (values == null || values.length == 0) {
                    value = token.getDefaultValue();
                } else if (values.length > 1 && token.getDefaultValue() == null) {
                    value = values;
                } else {
                    value = (values[0] != null ? values[0] : token.getDefaultValue());
                }
            } else if (tokenType == TokenType.ATTRIBUTE) {
                value = getAttribute(token);
            } else if (tokenType == TokenType.PROPERTY) {
                value = getProperty(token);
            }
            return value;
        } catch (Exception e) {
            throw new TokenEvaluationException(token, e);
        }
    }

    @Override
    public Object evaluate(Token[] tokens) {
        if (tokens == null || tokens.length == 0) {
            return null;
        }
        if (tokens.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (Token t : tokens) {
                Object value = evaluate(t);
                if (value != null) {
                    if (value instanceof Object[]) {
                        sb.append(Arrays.toString((Object[])value));
                    } else {
                        sb.append(value);
                    }
                }
            }
            return sb.toString();
        } else {
            return evaluate(tokens[0]);
        }
    }

    @Override
    public void evaluate(Token[] tokens, Writer writer) throws IOException {
        if (tokens == null || tokens.length == 0) {
            return;
        }
        for (Token t : tokens) {
            Object value = evaluate(t);
            if (value != null) {
                writer.write(value.toString());
            }
        }
        writer.flush();
    }

    @Override
    public String evaluateAsString(Token[] tokens) {
        Object value = evaluate(tokens);
        if (value instanceof Object[]) {
            return Arrays.toString((Object[])value);
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    @Override
    public List<Object> evaluateAsList(List<Token[]> tokensList) {
        if (tokensList == null || tokensList.isEmpty()) {
            return null;
        }
        List<Object> valueList = new ArrayList<>(tokensList.size());
        for (Token[] tokens : tokensList) {
            Object value = evaluate(tokens);
            valueList.add(value);
        }
        return valueList;
    }

    @Override
    public Set<Object> evaluateAsSet(Set<Token[]> tokensSet) {
        if (tokensSet == null || tokensSet.isEmpty()) {
            return null;
        }
        Set<Object> valueSet = new HashSet<>();
        for (Token[] tokens : tokensSet) {
            Object value = evaluate(tokens);
            valueSet.add(value);
        }
        return valueSet;
    }

    @Override
    public Map<String, Object> evaluateAsMap(Map<String, Token[]> tokensMap) {
        if (tokensMap == null || tokensMap.isEmpty()) {
            return null;
        }
        Map<String, Object> valueMap = new LinkedHashMap<>();
        for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
            Object value = evaluate(entry.getValue());
            valueMap.put(entry.getKey(), value);
        }
        return valueMap;
    }

    /**
     * Retrieves all values for a given request parameter name.
     * @param name a {@code String} specifying the name of the parameter
     * @return an array of {@code String} objects containing the parameter's values,
     *      or {@code null} if the parameter does not exist
     */
    protected String[] getParameterValues(String name) {
        if (activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getParameterValues(name);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the value of a named attribute from the activity context.
     * <p>It first checks for an action result, then for a request attribute.
     * If a getter name is specified in the token, it attempts to invoke it on the
     * retrieved object.</p>
     * @param token the token
     * @return the attribute value, or the token's default value if not found
     */
    protected Object getAttribute(Token token) {
        Object object = null;
        if (activity.getProcessResult() != null) {
            object = activity.getProcessResult().getResultValue(token.getName());
        }
        if (object == null && activity.getRequestAdapter() != null) {
            object = activity.getRequestAdapter().getAttribute(token.getName());
        }
        if (object != null && token.getGetterName() != null) {
            object = getBeanProperty(object, token.getGetterName());
        }
        return (object != null ? object : token.getDefaultValue());
    }

    /**
     * Retrieves a bean instance from the bean registry based on the token's definition.
     * <p>This method handles various bean access directives, such as retrieving a bean
     * by its ID or class, and invoking static or instance fields/methods.</p>
     * @param token the token
     * @return the resolved bean instance or property value
     */
    protected Object getBean(@NonNull Token token) {
        Object value;
        if (token.getValueProvider() != null) {
            if (token.getDirectiveType() == TokenDirectiveType.FIELD) {
                Field field = (Field)token.getValueProvider();
                if (Modifier.isStatic(field.getModifiers())) {
                    value = ReflectionUtils.getField(field, null);
                } else {
                    Class<?> beanClass = field.getDeclaringClass();
                    Object target = activity.getBean(beanClass);
                    value = ReflectionUtils.getField(field, target);
                }
            } else if (token.getDirectiveType() == TokenDirectiveType.METHOD) {
                Method method = (Method)token.getValueProvider();
                if (Modifier.isStatic(method.getModifiers())) {
                    value = ReflectionUtils.invokeMethod(method, null);
                } else {
                    Class<?> beanClass = method.getDeclaringClass();
                    Object target = activity.getBean(beanClass);
                    value = ReflectionUtils.invokeMethod(method, target);
                }
            } else {
                Class<?> beanClass = (Class<?>)token.getValueProvider();
                String getterName = token.getGetterName();
                if (getterName != null && beanClass.isEnum()) {
                    Object[] enums = beanClass.getEnumConstants();
                    if (enums != null) {
                        for (Object en : enums) {
                            if (getterName.equals(en.toString())) {
                                return en;
                            }
                        }
                    }
                }
                try {
                    value = activity.getBean(beanClass);
                } catch (NoSuchBeanException | NoUniqueBeanException e) {
                    if (getterName != null) {
                        try {
                            value = BeanTypeUtils.getProperty(beanClass, getterName);
                            if (value == null) {
                                value = token.getDefaultValue();
                            }
                            return value;
                        } catch (InvocationTargetException e2) {
                            // ignore
                        }
                    }
                    throw e;
                }
                if (value != null && getterName != null) {
                    value = getBeanProperty(value, getterName);
                }
            }
        } else {
            value = activity.getBean(token.getName());
            if (value != null && token.getGetterName() != null) {
                value = getBeanProperty(value, token.getGetterName());
            }
        }
        if (value == null) {
            value = token.getDefaultValue();
        }
        return value;
    }

    /**
     * Retrieves the value of a specified property from a bean object.
     * @param bean the bean object
     * @param propertyName the property name
     * @return the property value, or {@code null} if an invocation error occurs
     */
    protected Object getBeanProperty(Object bean, String propertyName) {
        Object value;
        try {
            value = BeanUtils.getProperty(bean, propertyName);
        } catch (InvocationTargetException e) {
            value = null;
        }
        return value;
    }

    /**
     * Retrieves a property from the environment.
     * <p>Example usage:
     * <pre>
     *   %{classpath:com/aspectran/sample.properties}
     *   %{classpath:com/aspectran/sample.properties^propertyName:defaultValue}
     *   %{system:test.url}
     *   %{property.name^getterName:defaultValue}
     * </pre>
     * @param token the token
     * @return an environment variable
     * @throws Exception if an error has occurred
     */
    protected Object getProperty(@NonNull Token token) throws Exception {
        if (token.getDirectiveType() == TokenDirectiveType.CLASSPATH) {
            try {
                Properties props = PropertiesLoaderUtils.loadPropertiesAsUtf8(token.getValue(), activity.getClassLoader());
                Object value = (token.getGetterName() != null ? props.get(token.getGetterName()) : props);
                return (value != null ? value : token.getDefaultValue());
            } catch (EncryptionInitializationException | EncryptionOperationNotPossibleException e) {
                // Most of these occur when the password used for encryption is different
                logger.error("Failed to decrypt values of encrypted properties while evaluating token {}; " +
                        "Most of these occur when the password used for encryption is different", token, e);
                return null;
            }
        } else if (token.getDirectiveType() == TokenDirectiveType.SYSTEM) {
            return SystemUtils.getProperty(token.getValue(), token.getDefaultValue());
        } else {
            Object value = activity.getEnvironment().getProperty(token.getName(), activity);
            if (value != null && token.getGetterName() != null) {
                value = getBeanProperty(value, token.getGetterName());
            }
            if (value == null) {
                value = token.getDefaultValue();
            }
            return value;
        }
    }

    /**
     * Renders a template specified by the token and returns the generated output.
     * <p>This is done by creating a new {@link InstantActivity} to isolate the
     * template rendering process.</p>
     * @param token the token
     * @return the rendered template content as a string
     */
    protected String getTemplate(@NonNull Token token) throws ActivityPerformException {
        InstantActivity instantActivity = new InstantActivity(activity, false);
        return instantActivity.perform(() -> {
            activity.getTemplateRenderer().render(token.getName(), instantActivity);
            String result = instantActivity.getResponseAdapter().getWriter().toString();
            return (result != null ? result : token.getDefaultValue());
        });
    }

}
