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
package com.aspectran.core.context.expr;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.component.bean.RequiredTypeBeanNotFoundException;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.PropertiesLoaderUtils;
import com.aspectran.core.util.ReflectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The Class TokenExpression.
 * 
 * <p>Created: 2008. 03. 29 AM 12:59:16</p>
 */
public class TokenExpression implements TokenEvaluator {

    protected final Activity activity;

    /**
     * Instantiates a new token expression parser.
     *
     * @param activity the current Activity
     */
    public TokenExpression(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        this.activity = activity;
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
                value = getParameter(token.getName(), token.getDefaultValue());
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
                    sb.append(value.toString());
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
        return (value != null ? value.toString() : null);
    }

    @Override
    public Object evaluate(String parameterName, Token[] tokens) {
        if (tokens == null || tokens.length == 0) {
            String[] values = getParameterValues(parameterName);
            if (values != null) {
                if (values.length == 1) {
                    return values[0];
                } else if (values.length > 1) {
                    return values;
                }
            }
            return null;
        }
        return evaluate(tokens);
    }

    @Override
    public String evaluateAsString(String parameterName, Token[] tokens) {
        Object value = evaluate(parameterName, tokens);
        return (value != null ? value.toString() : null);
    }

    @Override
    public List<Object> evaluateAsList(String parameterName, List<Token[]> tokensList) {
        if (tokensList == null || tokensList.isEmpty()) {
            return cast(getParameterAsList(parameterName));
        }
        List<Object> valueList = new ArrayList<>(tokensList.size());
        for (Token[] tokens : tokensList) {
            Object value = evaluate(parameterName, tokens);
            valueList.add(value);
        }
        return valueList;
    }

    @Override
    public Set<Object> evaluateAsSet(String parameterName, Set<Token[]> tokensSet) {
        if (tokensSet == null || tokensSet.isEmpty()) {
            return cast(getParameterAsSet(parameterName));
        }
        Set<Object> valueSet = new HashSet<>();
        for (Token[] tokens : tokensSet) {
            Object value = evaluate(parameterName, tokens);
            valueSet.add(value);
        }
        return valueSet;
    }

    @Override
    public Map<String, Object> evaluateAsMap(String parameterName, Map<String, Token[]> tokensMap) {
        if (tokensMap == null || tokensMap.isEmpty()) {
            String value = getParameter(parameterName);
            if (value == null) {
                return null;
            }
            Map<String, Object> valueMap = new LinkedHashMap<>();
            valueMap.put(parameterName, value);
            return valueMap;
        }

        Map<String, Object> valueMap = new LinkedHashMap<>();
        for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
            Object value = evaluate(entry.getKey(), entry.getValue());
            valueMap.put(entry.getKey(), value);
        }
        return valueMap;
    }

    @Override
    public Properties evaluateAsProperties(String parameterName, Properties tokensProp) {
        if (tokensProp == null || tokensProp.isEmpty()) {
            String value = getParameter(parameterName);
            if (value == null) {
                return null;
            }
            Properties prop = new Properties();
            prop.put(parameterName, value);
            return prop;
        }

        Properties prop = new Properties();
        for (Map.Entry<Object, Object> entry : tokensProp.entrySet()) {
            Object value = evaluate(entry.getKey().toString(), (Token[])entry.getValue());
            prop.put(entry.getKey(), value);
        }
        return prop;
    }

    /**
     * Returns the value of an activity's request parameter as a {@code List},
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code List} objects containing the parameter's values
     */
    private List<String> getParameterAsList(String name) {
        String[] values = getParameterValues(name);
        if (values == null) {
            return null;
        }

        List<String> valueList = new ArrayList<>(values.length);
        Collections.addAll(valueList, values);
        return valueList;
    }

    /**
     * Returns the value of an activity's request parameter as a {@code Set},
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code Set} objects containing the parameter's values
     */
    private Set<String> getParameterAsSet(String name) {
        String[] values = getParameterValues(name);
        if (values == null) {
            return null;
        }

        Set<String> valueSet = new LinkedHashSet<>(values.length);
        Collections.addAll(valueSet, values);
        return valueSet;
    }

    /**
     * Returns the value of an activity's request parameter as a {@code String},
     * or {@code defaultValue} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @param defaultValue the default value
     * @return a {@code String} representing the
     *      single value of the parameter
     */
    private String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        return (value != null ? value : defaultValue);
    }

    /**
     * Returns the value of an activity's request parameter as a {@code String},
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code String} representing the
     *      single value of the parameter
     */
    protected String getParameter(String name) {
        if (activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getParameter(name);
        } else {
            return null;
        }
    }

    /**
     * Returns an array of {@code String} objects containing all
     * of the values the given activity's request parameter has,
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return an array of {@code String} objects
     *      containing the parameter's values
     */
    protected String[] getParameterValues(String name) {
        if (activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getParameterValues(name);
        } else {
            return null;
        }
    }

    /**
     * Returns a {@code FileParameter} object as a given activity's request parameter name,
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code FileParameter} representing the
     *      single value of the parameter
     */
    protected FileParameter getFileParameter(String name) {
        if (activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getFileParameter(name);
        } else {
            return null;
        }
    }

    /**
     * Returns an array of {@code FileParameter} objects containing all
     * of the values the given activity's request parameter has,
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return an array of {@code FileParameter} objects
     *      containing the parameter's values
     */
    protected FileParameter[] getFileParameterValues(String name) {
        if (activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getFileParameterValues(name);
        } else {
            return null;
        }
    }

    /**
     * Returns the value of the named attribute as an {@code Object}
     * of the activity's request attributes or action results.
     *
     * @param token the token
     * @return an {@code Object} containing the value of the attribute,
     *       or {@code null} if the attribute does not exist
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
     * Returns the bean instance that matches the given token.
     *
     * @param token the token
     * @return an instance of the bean
     */
    protected Object getBean(Token token) {
        Object value;
        if (token.getAlternativeValue() != null) {
            if (token.getDirectiveType() == TokenDirectiveType.FIELD) {
                Field field = (Field)token.getAlternativeValue();
                if (Modifier.isStatic(field.getModifiers())) {
                    value = ReflectionUtils.getField(field, null);
                } else {
                    Class<?> cls = field.getDeclaringClass();
                    Object target = activity.getBean(cls);
                    value = ReflectionUtils.getField(field, target);
                }
            } else if (token.getDirectiveType() == TokenDirectiveType.METHOD) {
                Method method = (Method)token.getAlternativeValue();
                if (Modifier.isStatic(method.getModifiers())) {
                    value = ReflectionUtils.invokeMethod(method, null);
                } else {
                    Class<?> cls = method.getDeclaringClass();
                    Object target = activity.getBean(cls);
                    value = ReflectionUtils.invokeMethod(method, target);
                }
            } else {
                Class<?> cls = (Class<?>)token.getAlternativeValue();
                try {
                    value = activity.getBean(cls);
                } catch (RequiredTypeBeanNotFoundException | NoUniqueBeanException e) {
                    if (token.getGetterName() != null) {
                        try {
                            value = BeanUtils.getProperty(cls, token.getGetterName());
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
                if (value != null && token.getGetterName() != null) {
                    value = getBeanProperty(value, token.getGetterName());
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
     * Invoke bean's property.
     *
     * @param object the object
     * @param propertyName the property name
     * @return the object
     */
    protected Object getBeanProperty(final Object object, String propertyName) {
        Object value;
        try {
            value = BeanUtils.getProperty(object, propertyName);
        } catch (InvocationTargetException e) {
            // ignore
            value = null;
        }
        return value;
    }

    /**
     * Returns an Environment variable that matches the given token.
     *
     * <pre>
     *   %{classpath:/com/aspectran/sample.properties}
     *   %{classpath:/com/aspectran/sample.properties^propertyName:defaultValue}
     * </pre>
     *
     * @param token the token
     * @return an environment variable
     * @throws IOException if an I/O error has occurred
     */
    protected Object getProperty(Token token) throws IOException {
        if (token.getDirectiveType() == TokenDirectiveType.CLASSPATH) {
            Properties props = PropertiesLoaderUtils.loadProperties(token.getValue(), activity.getEnvironment().getClassLoader());
            Object value = (token.getGetterName() != null ? props.get(token.getGetterName()) : props);
            return (value != null ? value : token.getDefaultValue());
        } else {
            Object value = activity.getActivityContext().getEnvironment().getProperty(token.getName(), activity);
            if (value != null && token.getGetterName() != null) {
                value = getBeanProperty(value, token.getGetterName());
            }
            return (value != null ? value : token.getDefaultValue());
        }
    }

    /**
     * Executes template, returns the generated output.
     *
     * @param token the token
     * @return the generated output as {@code String}
     */
    protected String getTemplate(Token token) {
        TemplateRenderer templateRenderer = activity.getActivityContext().getTemplateRenderer();

        StringWriter writer = new StringWriter();
        templateRenderer.render(token.getName(), activity, writer);

        String result = writer.toString();
        return (result != null ? result : token.getDefaultValue());
    }

    /**
     * This method will cast {@code Set<?>} to {@code List<T>}
     * assuming {@code ?} is castable to {@code T}.
     *
     * @param <T> the generic type
     * @param list a {@code List} object
     * @return a casted {@code List} object
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> cast(List<?> list) {
        return (List<T>)list;
    }

    /**
     * This method will cast {@code Set<?>} to {@code Set<T>}
     * assuming {@code ?} is castable to {@code T}.
     *
     * @param <T> the generic type
     * @param set a {@code Set} object
     * @return a casted {@code Set} object
     */
    @SuppressWarnings("unchecked")
    private static <T> Set<T> cast(Set<?> set) {
        return (Set<T>)set;
    }

}
