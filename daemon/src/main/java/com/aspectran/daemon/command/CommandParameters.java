/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

import java.util.List;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class CommandParameters extends AbstractParameters {

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

    public CommandParameters() {
        super(parameterKeys);
    }

    public String getCommandName() {
        return getString(command);
    }

    public CommandParameters setCommandName(String commandName) {
        putValue(command, commandName);
        return this;
    }

    public String getTransletName() {
        return getString(translet);
    }

    public CommandParameters setTransletName(String transletName) {
        putValue(translet, transletName);
        return this;
    }

    public String getBeanName() {
        return getString(bean);
    }

    public CommandParameters setBeanName(String beanName) {
        putValue(bean, beanName);
        return this;
    }

    public String getMethodName() {
        return getString(method);
    }

    public CommandParameters setMethodName(String methodName) {
        putValue(method, methodName);
        return this;
    }

    public ItemRuleList getArgumentItemRuleList() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(arguments);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleList(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getArgumentItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(arguments);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getPropertyItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(properties);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getParameterItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(parameters);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getAttributeItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(attributes);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
            return ItemRuleUtils.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public boolean isRequeuable() {
        if (hasValue(requeuable)) {
            return getBoolean(requeuable, false);
        } else {
            return true;
        }
    }

    public CommandParameters setRequeuable(boolean requeuable) {
        putValue(CommandParameters.requeuable, requeuable);
        return this;
    }

    public String getResult() {
        return getString(result);
    }

    public CommandParameters setResult(String resultText) {
        putValue(result, resultText);
        return this;
    }

}
