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
package com.aspectran.daemon.command;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Holds all parameters required to execute a daemon command.
 * <p>
 * This class encapsulates the details of a command request, supporting various
 * execution models:
 * <ul>
 *   <li><b>command</b>: The name of a built-in command to run.</li>
 *   <li><b>translet</b>: The name of a translet to execute.</li>
 *   <li><b>bean</b> and <b>method</b>: A specific bean method to invoke.</li>
 * </ul>
 * It also carries optional payloads such as arguments, properties, request
 * parameters, and attributes. The {@code requeuable} flag controls behavior
 * in file-based polling, and the {@code result} field stores output from
 * the command execution.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public class CommandParameters extends DefaultParameters {

    private static final ParameterKey command;
    private static final ParameterKey translet;
    private static final ParameterKey bean;
    private static final ParameterKey method;
    private static final ParameterKey arguments;
    private static final ParameterKey properties;
    private static final ParameterKey parameters;
    private static final ParameterKey attributes;
    private static final ParameterKey requeuable;
    private static final ParameterKey result;
    private static final ParameterKey error;
    private static final ParameterKey source;

    private static final ParameterKey[] parameterKeys;

    static {
        command = new ParameterKey("command", ValueType.STRING);
        translet = new ParameterKey("translet", ValueType.STRING);
        bean = new ParameterKey("bean", ValueType.STRING);
        method = new ParameterKey("method", ValueType.STRING);
        arguments = new ParameterKey("arguments", ItemHolderParameters.class);
        properties = new ParameterKey("properties", ItemHolderParameters.class);
        parameters = new ParameterKey("parameters", ItemHolderParameters.class);
        attributes = new ParameterKey("attributes", ItemHolderParameters.class);
        requeuable = new ParameterKey("requeuable", ValueType.BOOLEAN);
        result = new ParameterKey("result", ValueType.TEXT);
        error = new ParameterKey("error", ValueType.TEXT);
        source = new ParameterKey("source", ValueType.TEXT);

        parameterKeys = new ParameterKey[] {
                command,
                translet,
                bean,
                method,
                arguments,
                properties,
                parameters,
                attributes,
                requeuable,
                result,
                error,
                source
        };
    }

    /**
     * Instantiates a new CommandParameters.
     */
    public CommandParameters() {
        super(parameterKeys);
    }

    /**
     * Returns the name of the command to execute.
     * @return the command name
     */
    public String getCommandName() {
        return getString(command);
    }

    /**
     * Sets the name of the command to execute.
     * @param commandName the command name
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setCommandName(String commandName) {
        putValue(command, commandName);
        return this;
    }

    /**
     * Returns the name of the translet to execute.
     * @return the translet name
     */
    public String getTransletName() {
        return getString(translet);
    }

    /**
     * Sets the name of the translet to execute.
     * @param transletName the translet name
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setTransletName(String transletName) {
        putValue(translet, transletName);
        return this;
    }

    /**
     * Returns the name of the bean to invoke.
     * @return the bean name
     */
    public String getBeanName() {
        return getString(bean);
    }

    /**
     * Sets the name of the bean to invoke.
     * @param beanName the bean name
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setBeanName(String beanName) {
        putValue(bean, beanName);
        return this;
    }

    /**
     * Returns the name of the method to invoke on the bean.
     * @return the method name
     */
    public String getMethodName() {
        return getString(method);
    }

    /**
     * Sets the name of the method to invoke on the bean.
     * @param methodName the method name
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setMethodName(String methodName) {
        putValue(method, methodName);
        return this;
    }

    /**
     * Returns the argument item rules as a map.
     * @return the map of item rules for arguments
     * @throws IllegalRuleException if an error occurs while parsing the rules
     */
    public ItemRuleMap getArgumentItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(arguments);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    /**
     * Returns the argument item rules as a list.
     * @return the list of item rules for arguments
     * @throws IllegalRuleException if an error occurs while parsing the rules
     */
    public ItemRuleList getArgumentItemRuleList() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(arguments);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleList(itemParametersList);
        } else {
            return null;
        }
    }

    /**
     * Adds an argument item rule.
     * @param itemRule the item rule to add
     */
    public void putArgument(ItemRule itemRule) {
        ItemHolderParameters ihp = touchParameters(CommandParameters.arguments);
        ihp.addItemParameters(RulesToParameters.toItemParameters(itemRule));
        putValue(arguments, ihp);
    }

    /**
     * Adds a command argument with automatic {@link ItemValueType} inference.
     * @param value the argument value
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters addArgument(Object value) {
        ItemValueType inferredType = inferItemValueType(value);
        return addArgument(value, inferredType);
    }

    /**
     * Adds a command argument with the specified value and {@link ItemValueType}.
     * @param value the argument value
     * @param valueType the item value type
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters addArgument(Object value, ItemValueType valueType) {
        ItemHolderParameters holder = touchParameters(arguments);
        ItemParameters item = holder.newItemParameters();
        if (valueType != null && valueType != ItemValueType.STRING) {
            item.putValue(ItemParameters.valueType, valueType.toString());
        }
        if (value != null) {
            item.putValue(ItemParameters.value, value);
        }
        return this;
    }

    /**
     * Returns the property item rules as a map.
     * @return the map of item rules for properties
     * @throws IllegalRuleException if an error occurs while parsing the rules
     */
    public ItemRuleMap getPropertyItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(properties);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    /**
     * Adds a property with the specified name and string value.
     * @param name the property name
     * @param value the property value
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setProperty(String name, String value) {
        return setProperty(name, value, ItemValueType.STRING);
    }

    /**
     * Adds a property with automatic {@link ItemValueType} inference.
     * @param name the property name
     * @param value the property value
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setProperty(String name, Object value) {
        ItemValueType inferredType = inferItemValueType(value);
        return setProperty(name, value, inferredType);
    }

    /**
     * Adds a property with the specified name, value, and {@link ItemValueType}.
     * @param name the property name
     * @param value the property value
     * @param valueType the item value type
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setProperty(String name, Object value, ItemValueType valueType) {
        ItemHolderParameters holder = touchParameters(properties);
        ItemParameters item = holder.newItemParameters();
        item.putValue(ItemParameters.name, name);
        if (valueType != null && valueType != ItemValueType.STRING) {
            item.putValue(ItemParameters.valueType, valueType.toString());
        }
        if (value != null) {
            item.putValue(ItemParameters.value, value);
        }
        return this;
    }

    /**
     * Returns the parameter item rules as a map.
     * @return the map of item rules for parameters
     * @throws IllegalRuleException if an error occurs while parsing the rules
     */
    public ItemRuleMap getParameterItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(parameters);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    /**
     * Adds a request parameter with the specified name and string value.
     * @param name the parameter name
     * @param value the parameter value
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setParameter(String name, String value) {
        return setParameter(name, value, ItemValueType.STRING);
    }

    /**
     * Adds a request parameter with automatic {@link ItemValueType} inference.
     * @param name the parameter name
     * @param value the parameter value
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setParameter(String name, Object value) {
        if (value instanceof String[] values) {
            return setParameter(name, values);
        }
        if (value instanceof Collection<?> collection) {
            return setParameter(name, collection);
        }
        ItemValueType inferredType = inferItemValueType(value);
        return setParameter(name, value, inferredType);
    }

    /**
     * Adds a request parameter with the specified name, value, and {@link ItemValueType}.
     * @param name the parameter name
     * @param value the parameter value
     * @param valueType the item value type
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setParameter(String name, Object value, ItemValueType valueType) {
        ItemHolderParameters holder = touchParameters(parameters);
        ItemParameters item = holder.newItemParameters();
        item.putValue(ItemParameters.name, name);
        if (valueType != null && valueType != ItemValueType.STRING) {
            item.putValue(ItemParameters.valueType, valueType.toString());
        }
        if (value != null) {
            item.putValue(ItemParameters.value, value);
        }
        return this;
    }

    /**
     * Adds a multi-valued request parameter as an array.
     * @param name the parameter name
     * @param values the parameter values
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setParameter(String name, String[] values) {
        if (values != null && values.length > 0) {
            ItemHolderParameters holder = touchParameters(parameters);
            ItemParameters item = holder.newItemParameters();
            item.putValue(ItemParameters.name, name);
            item.putValue(ItemParameters.type, ItemType.ARRAY.toString());
            item.putValue(ItemParameters.value, values);
        }
        return this;
    }

    /**
     * Adds a multi-valued request parameter as a collection.
     * @param name the parameter name
     * @param values the parameter values
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setParameter(String name, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            String[] stringValues = values.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toArray(String[]::new);
            return setParameter(name, stringValues);
        }
        return this;
    }

    /**
     * Extracts and returns the request parameters as a {@link ParameterMap}.
     * @return the parameter map, or {@code null} if no parameters exist
     */
    public ParameterMap getParameterMap() {
        ItemHolderParameters itemHolderParameters = getParameters(parameters);
        if (itemHolderParameters == null) {
            return null;
        }
        List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
        if (itemParametersList == null || itemParametersList.isEmpty()) {
            return null;
        }
        ParameterMap parameterMap = new ParameterMap();
        for (ItemParameters ip : itemParametersList) {
            String name = ip.getString(ItemParameters.name);
            if (name != null) {
                Parameter pv = ip.getParameter(ItemParameters.value);
                if (pv != null) {
                    if (pv.isArray()) {
                        List<String> list = pv.getValueAsStringList();
                        if (list != null) {
                            parameterMap.put(name, list.toArray(new String[0]));
                        }
                    } else {
                        String val = pv.getValueAsString();
                        if (val != null) {
                            parameterMap.setParameter(name, val);
                        }
                    }
                }
            }
        }
        return (!parameterMap.isEmpty() ? parameterMap : null);
    }

    /**
     * Returns the attribute item rules as a map.
     * @return the map of item rules for attributes
     * @throws IllegalRuleException if an error occurs while parsing the rules
     */
    public ItemRuleMap getAttributeItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(attributes);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    /**
     * Adds an attribute with the specified name and value.
     * @param name the attribute name
     * @param value the attribute value
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setAttribute(String name, Object value) {
        ItemValueType inferredType = inferItemValueType(value);
        return setAttribute(name, value, inferredType);
    }

    /**
     * Adds an attribute with the specified name, value, and {@link ItemValueType}.
     * @param name the attribute name
     * @param value the attribute value
     * @param valueType the item value type
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setAttribute(String name, Object value, ItemValueType valueType) {
        ItemHolderParameters holder = touchParameters(attributes);
        ItemParameters item = holder.newItemParameters();
        item.putValue(ItemParameters.name, name);
        if (valueType != null && valueType != ItemValueType.STRING) {
            item.putValue(ItemParameters.valueType, valueType.toString());
        }
        if (value != null) {
            item.putValue(ItemParameters.value, value);
        }
        return this;
    }

    /**
     * Checks if the command is requeuable.
     * Defaults to {@code true} if not explicitly set.
     * @return {@code true} if the command can be re-queued, {@code false} otherwise
     */
    public boolean isRequeuable() {
        if (hasValue(requeuable)) {
            return getBoolean(requeuable, false);
        } else {
            return true;
        }
    }

    /**
     * Sets whether the command is requeuable.
     * @param requeuable {@code true} to allow re-queuing, {@code false} otherwise
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setRequeuable(boolean requeuable) {
        putValue(CommandParameters.requeuable, requeuable);
        return this;
    }

    /**
     * Returns the result of the command execution.
     * @return the result text, or {@code null} if no result is set
     */
    public String getResult() {
        return getString(result);
    }

    /**
     * Sets the result of the command execution.
     * @param resultText the result text
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setResult(String resultText) {
        putValue(result, resultText);
        return this;
    }

    /**
     * Returns the system error message if any.
     * @return the error message, or {@code null} if no error is set
     */
    public String getError() {
        return getString(error);
    }

    /**
     * Sets the system error message.
     * @param errorText the error message
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setError(String errorText) {
        putValue(error, errorText);
        return this;
    }

    /**
     * Returns the original raw content of the command file.
     * @return the raw content
     */
    public String getSource() {
        return getString(source);
    }

    /**
     * Sets the original raw content of the command file.
     * @param sourceText the raw content
     * @return this {@code CommandParameters} instance
     */
    public CommandParameters setSource(String sourceText) {
        putValue(source, sourceText);
        return this;
    }

    @Override
    public String toString() {
        try {
            return new AponWriter()
                    .nullWritable(false)
                    .write(this)
                    .toString();
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }
    }

    private static ItemValueType inferItemValueType(Object value) {
        if (value instanceof Integer) {
            return ItemValueType.INT;
        } else if (value instanceof Long) {
            return ItemValueType.LONG;
        } else if (value instanceof Boolean) {
            return ItemValueType.BOOLEAN;
        } else if (value instanceof Float) {
            return ItemValueType.FLOAT;
        } else if (value instanceof Double) {
            return ItemValueType.DOUBLE;
        } else {
            return ItemValueType.STRING;
        }
    }

}
