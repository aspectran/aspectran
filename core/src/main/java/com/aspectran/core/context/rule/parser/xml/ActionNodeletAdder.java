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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class ActionNodeletAdder.
 *
 * @since 2011. 1. 9.
 */
class ActionNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/headers");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            HeaderActionRule headersActionRule = HeaderActionRule.newInstance(id, hidden);
            parser.pushObject(headersActionRule);

            ItemRuleMap irm = new ItemRuleMap();
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            HeaderActionRule headersActionRule = parser.popObject();

            headersActionRule.setHeaderItemRuleMap(irm);

            ActionRuleApplicable applicable = parser.peekObject();
            applicable.applyActionRule(headersActionRule);
        });
        parser.setXpath(xpath + "/echo");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
            parser.pushObject(echoActionRule);

            ItemRuleMap irm = new ItemRuleMap();
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            EchoActionRule echoActionRule = parser.popObject();

            if (echoActionRule.getEchoItemRuleMap() == null && !irm.isEmpty()) {
                echoActionRule.setEchoItemRuleMap(irm);
            }

            ActionRuleApplicable applicable = parser.peekObject();
            applicable.applyActionRule(echoActionRule);
        });
        parser.setXpath(xpath + "/action");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
            String methodName = StringUtils.emptyToNull(attrs.get("method"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(id, beanIdOrClass, methodName, hidden);
            assistant.resolveActionBeanClass(invokeActionRule);
            parser.pushObject(invokeActionRule);
        });
        parser.addNodeEndlet(text -> {
            InvokeActionRule invokeActionRule = parser.popObject();
            ActionRuleApplicable applicable = parser.peekObject();
            applicable.applyActionRule(invokeActionRule);
        });
        parser.setXpath(xpath + "/action/arguments");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            InvokeActionRule invokeActionRule = parser.peekObject();
            irm = assistant.profiling(irm, invokeActionRule.getArgumentItemRuleMap());
            invokeActionRule.setArgumentItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/action/properties");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            InvokeActionRule invokeActionRule = parser.peekObject();
            irm = assistant.profiling(irm, invokeActionRule.getPropertyItemRuleMap());
            invokeActionRule.setPropertyItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/invoke");
        parser.addNodelet(attrs -> {
            String methodName = StringUtils.emptyToNull(attrs.get("method"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(methodName, hidden);
            assistant.resolveActionBeanClass(invokeActionRule);
            parser.pushObject(invokeActionRule);
        });
        parser.addNodeEndlet(text -> {
            InvokeActionRule invokeActionRule = parser.popObject();
            ActionRuleApplicable applicable = parser.peekObject();
            applicable.applyActionRule(invokeActionRule);
        });
        parser.setXpath(xpath + "/invoke/arguments");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            InvokeActionRule invokeActionRule = parser.peekObject();
            irm = assistant.profiling(irm, invokeActionRule.getArgumentItemRuleMap());
            invokeActionRule.setArgumentItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/invoke/properties");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            InvokeActionRule invokeActionRule = parser.peekObject();
            irm = assistant.profiling(irm, invokeActionRule.getPropertyItemRuleMap());
            invokeActionRule.setPropertyItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/include");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            String transletName = StringUtils.emptyToNull(attrs.get("translet"));
            String methodType = StringUtils.emptyToNull(attrs.get("method"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            transletName = assistant.applyTransletNamePattern(transletName);

            IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, transletName, methodType, hidden);
            parser.pushObject(includeActionRule);
        });
        parser.addNodeEndlet(text -> {
            IncludeActionRule includeActionRule = parser.popObject();
            ActionRuleApplicable applicable = parser.peekObject();
            applicable.applyActionRule(includeActionRule);
        });
        parser.setXpath(xpath + "/include/parameters");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            IncludeActionRule includeActionRule = parser.peekObject();
            irm = assistant.profiling(irm, includeActionRule.getParameterItemRuleMap());
            includeActionRule.setParameterItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/include/attributes");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            IncludeActionRule includeActionRule = parser.peekObject();
            irm = assistant.profiling(irm, includeActionRule.getAttributeItemRuleMap());
            includeActionRule.setAttributeItemRuleMap(irm);
        });
    }

}
