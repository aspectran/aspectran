/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

import java.util.List;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class CommandParameters extends AbstractParameters {

    public static final ParameterDefinition command;
    public static final ParameterDefinition bean;
    public static final ParameterDefinition method;
    public static final ParameterDefinition arguments;
    public static final ParameterDefinition properties;
    public static final ParameterDefinition translet;
    public static final ParameterDefinition template;
    public static final ParameterDefinition parameters;
    public static final ParameterDefinition attributes;
    public static final ParameterDefinition output;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        command = new ParameterDefinition("command", ParameterValueType.STRING);
        translet = new ParameterDefinition("translet", ParameterValueType.STRING);
        template = new ParameterDefinition("template", ParameterValueType.STRING);
        bean = new ParameterDefinition("bean", ParameterValueType.STRING);
        method = new ParameterDefinition("method", ParameterValueType.STRING);
        arguments = new ParameterDefinition("arguments", ItemHolderParameters.class);
        properties = new ParameterDefinition("properties", ItemHolderParameters.class);
        parameters = new ParameterDefinition("parameters", ItemHolderParameters.class);
        attributes = new ParameterDefinition("attributes", ItemHolderParameters.class);
        output = new ParameterDefinition("output", ParameterValueType.TEXT);

        parameterDefinitions = new ParameterDefinition[] {
                command,
                translet,
                template,
                bean,
                method,
                arguments,
                properties,
                parameters,
                attributes,
                output
        };
    }

    public CommandParameters() {
        super(parameterDefinitions);
    }

    public String getCommandName() {
        return getString(command);
    }

    public void setCommandName(String commandName) {
        putValue(command, commandName);
    }

    public String getTransletName() {
        return getString(translet);
    }

    public void setTransletName(String transletName) {
        putValue(translet, transletName);
    }

    public String getTemplateName() {
        return getString(template);
    }

    public void setTemplateName(String templateName) {
        putValue(template, templateName);
    }

    public String getBeanName() {
        return getString(bean);
    }

    public void setBeanName(String beanName) {
        putValue(bean, beanName);
    }

    public String getMethodName() {
        return getString(method);
    }

    public void setMethodName(String methodName) {
        putValue(method, methodName);
    }

    public ItemRuleList getArgumentItemRuleList() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(arguments);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList();
            return ItemRule.toItemRuleList(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getArgumentItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(arguments);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList();
            return ItemRule.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getPropertyItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(properties);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList();
            return ItemRule.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getParameterItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(parameters);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList();
            return ItemRule.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public ItemRuleMap getAttributeItemRuleMap() throws IllegalRuleException {
        ItemHolderParameters itemHolderParameters = getParameters(attributes);
        if (itemHolderParameters != null) {
            List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList();
            return ItemRule.toItemRuleMap(itemParametersList);
        } else {
            return null;
        }
    }

    public String getOutput() {
        return getString(output);
    }

    public void setOutput(String outputText) {
        putValue(output, outputText);
    }

}
