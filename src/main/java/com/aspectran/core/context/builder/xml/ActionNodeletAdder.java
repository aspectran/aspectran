/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.builder.xml;

import com.aspectran.core.context.builder.assistant.ContextBuilderAssistant;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeadingActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ActionNodeletAdder.
 *
 * @since 2011. 1. 9.
 */
class ActionNodeletAdder implements NodeletAdder {

    protected final ContextBuilderAssistant assistant;

    /**
     * Instantiates a new ActionRuleNodeletAdder.
     *
     * @param assistant the ContextBuilderAssistant
     */
    ActionNodeletAdder(ContextBuilderAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void process(String xpath, NodeletParser parser) {
        parser.addNodelet(xpath, "/action", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            String beanIdOrClass = StringUtils.emptyToNull(attributes.get("bean"));
            String methodName = StringUtils.emptyToNull(attributes.get("method"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

            BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanIdOrClass, methodName, hidden);

            // AspectAdviceRule may not have a bean id.
            if (beanIdOrClass != null) {
                assistant.resolveActionBeanClass(beanIdOrClass, beanActionRule);
            }

            assistant.pushObject(beanActionRule);
        });
        parser.addNodelet(xpath, "/action/arguments", (node, attributes, text) -> {
            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
        parser.addNodelet(xpath, "/action/arguments", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/action/arguments/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if (!irm.isEmpty()) {
                BeanActionRule beanActionRule = assistant.peekObject();
                beanActionRule.setArgumentItemRuleMap(irm);
            }
        });
        parser.addNodelet(xpath, "/action/properties", (node, attributes, text) -> {
            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
        parser.addNodelet(xpath, "/action/properties", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/action/properties/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if (!irm.isEmpty()) {
                BeanActionRule beanActionRule = assistant.peekObject();
                beanActionRule.setPropertyItemRuleMap(irm);
            }
        });
        parser.addNodelet(xpath, "/action/end()", (node, attributes, text) -> {
            BeanActionRule beanActionRule = assistant.popObject();
            ActionRuleApplicable applicable = assistant.peekObject();
            applicable.applyActionRule(beanActionRule);
        });
        parser.addNodelet(xpath, "/include", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            String transletName = StringUtils.emptyToNull(attributes.get("translet"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

            transletName = assistant.applyTransletNamePattern(transletName);

            IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, transletName, hidden);
            assistant.pushObject(includeActionRule);
        });
        parser.addNodelet(xpath, "/include/parameters", (node, attributes, text) -> {
            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
        parser.addNodelet(xpath, "/include/parameters", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/include/parameters/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if (!irm.isEmpty()) {
                IncludeActionRule includeActionRule = assistant.peekObject();
                includeActionRule.setParameterItemRuleMap(irm);
            }
        });
        parser.addNodelet(xpath, "/include/attributes", (node, attributes, text) -> {
            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
        parser.addNodelet(xpath, "/include/attributes", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/include/attributes/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if (!irm.isEmpty()) {
                IncludeActionRule includeActionRule = assistant.peekObject();
                includeActionRule.setAttributeItemRuleMap(irm);
            }
        });
        parser.addNodelet(xpath, "/include/end()", (node, attributes, text) -> {
            IncludeActionRule includeActionRule = assistant.popObject();
            ActionRuleApplicable applicable = assistant.peekObject();
            applicable.applyActionRule(includeActionRule);
        });
        parser.addNodelet(xpath, "/echo", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

            EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
            assistant.pushObject(echoActionRule);

            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
        parser.addNodelet(xpath, "/echo/attributes", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/echo", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/echo/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();
            EchoActionRule echoActionRule = assistant.popObject();

            if (!irm.isEmpty()) {
                echoActionRule.setAttributeItemRuleMap(irm);
            }

            ActionRuleApplicable applicable = assistant.peekObject();
            applicable.applyActionRule(echoActionRule);
        });
        parser.addNodelet(xpath, "/headers", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attributes.get("hidden"));

            HeadingActionRule headersActionRule = HeadingActionRule.newInstance(id, hidden);
            assistant.pushObject(headersActionRule);

            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
        parser.addNodelet(xpath, "/headers", new ItemNodeletAdder(assistant));
        parser.addNodelet(xpath, "/headers/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();
            HeadingActionRule headersActionRule = assistant.popObject();

            if (!irm.isEmpty()) {
                headersActionRule.setHeaderItemRuleMap(irm);
            }

            ActionRuleApplicable applicable = assistant.peekObject();
            applicable.applyActionRule(headersActionRule);
        });
    }

}
