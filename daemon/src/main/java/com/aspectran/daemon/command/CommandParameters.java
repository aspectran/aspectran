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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

import java.util.List;
import java.util.Map;

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
                result
        };
    }

    private Activity activity;

    /**
     * Instantiates a new CommandParameters.
     */
    public CommandParameters() {
        super(parameterKeys);
    }

    /**
     * Sets the activity, which is required to evaluate item rules for arguments,
     * parameters, and attributes.
     * @param activity the activity instance
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
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
     * Evaluates and returns the arguments for a bean method invocation.
     * @return an array of evaluated argument values
     * @throws IllegalRuleException if an error occurs while evaluating the rules
     */
    public Object[] getArguments() throws IllegalRuleException {
        if (activity == null) {
            throw new IllegalStateException("No available activity");
        }
        ItemRuleList itemRuleList = getArgumentItemRuleList();
        if (itemRuleList != null) {
            ItemEvaluator evaluator = activity.getItemEvaluator();
            Object[] args = new Object[itemRuleList.size()];
            for (int i = 0; i < itemRuleList.size(); i++) {
                args[i] = evaluator.evaluate(itemRuleList.get(i));
            }
            return args;
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
     * Evaluates and returns the request parameters as a {@link ParameterMap}.
     * @return the evaluated parameter map
     * @throws IllegalRuleException if an error occurs while evaluating the rules
     */
    public ParameterMap getParameterMap() throws IllegalRuleException {
        if (activity == null) {
            throw new IllegalStateException("No available activity");
        }
        ItemRuleMap parameterItemRuleMap = getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = activity.getItemEvaluator();
            return evaluator.evaluateAsParameterMap(parameterItemRuleMap);
        } else {
            return null;
        }
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
     * Evaluates and returns the request attributes as a map.
     * @return the evaluated attribute map
     * @throws IllegalRuleException if an error occurs while evaluating the rules
     */
    public Map<String, Object> getAttributeMap() throws IllegalRuleException {
        if (activity == null) {
            throw new IllegalStateException("No available activity");
        }
        ItemRuleMap attributeItemRuleMap = getAttributeItemRuleMap();
        if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = activity.getItemEvaluator();
            return evaluator.evaluate(attributeItemRuleMap);
        } else {
            return null;
        }
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

}
