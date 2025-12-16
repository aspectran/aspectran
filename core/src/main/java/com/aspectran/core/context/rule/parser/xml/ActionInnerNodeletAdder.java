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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing inner action elements such as
 * {@code <headers>}, {@code <echo>}, {@code <action>}, {@code <invoke>},
 * and {@code <include>}.
 *
 * @since 2011. 1. 9.
 */
class ActionInnerNodeletAdder implements NodeletAdder {

    private static volatile ActionInnerNodeletAdder INSTANCE;

    static ActionInnerNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (ActionInnerNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ActionInnerNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("headers")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                HeaderActionRule headersActionRule = HeaderActionRule.newInstance(id, hidden);
                AspectranNodeParsingContext.pushObject(headersActionRule);

                ItemRuleMap irm = new ItemRuleMap();
                AspectranNodeParsingContext.pushObject(irm);
            })
            .with(ItemNodeletAdder.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                HeaderActionRule headersActionRule = AspectranNodeParsingContext.popObject();

                headersActionRule.setHeaderItemRuleMap(irm);

                HasActionRules hasActionRules = AspectranNodeParsingContext.peekObject();
                hasActionRules.putActionRule(headersActionRule);
            })
        .parent().child("echo")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
                AspectranNodeParsingContext.pushObject(echoActionRule);

                ItemRuleMap irm = new ItemRuleMap();
                AspectranNodeParsingContext.pushObject(irm);
            })
            .with(ItemNodeletAdder.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                EchoActionRule echoActionRule = AspectranNodeParsingContext.popObject();

                if (echoActionRule.getEchoItemRuleMap() == null && !irm.isEmpty()) {
                    echoActionRule.setEchoItemRuleMap(irm);
                }

                HasActionRules hasActionRules = AspectranNodeParsingContext.peekObject();
                hasActionRules.putActionRule(echoActionRule);
            })
        .parent().child("action")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
                String methodName = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(id, beanIdOrClass, methodName, hidden);
                AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveActionBeanClass(invokeActionRule);
                AspectranNodeParsingContext.pushObject(invokeActionRule);
            })
            .with(ArgumentNodeletAdder.instance())
            .with(ArgumentsNodeletAdder.instance())
            .with(PropertyNodeletAdder.instance())
            .with(PropertiesNodeletAdder.instance())
            .endNodelet(text -> {
                InvokeActionRule invokeActionRule = AspectranNodeParsingContext.popObject();
                HasActionRules hasActionRules = AspectranNodeParsingContext.peekObject();
                hasActionRules.putActionRule(invokeActionRule);
            })
        .parent().child("invoke")
            .nodelet(attrs -> {
                String methodName = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(methodName, hidden);
                AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveActionBeanClass(invokeActionRule);
                AspectranNodeParsingContext.pushObject(invokeActionRule);
            })
            .with(ArgumentNodeletAdder.instance())
            .with(ArgumentsNodeletAdder.instance())
            .with(PropertyNodeletAdder.instance())
            .with(PropertiesNodeletAdder.instance())
            .endNodelet(text -> {
                InvokeActionRule invokeActionRule = AspectranNodeParsingContext.popObject();
                HasActionRules hasActionRules = AspectranNodeParsingContext.peekObject();
                hasActionRules.putActionRule(invokeActionRule);
            })
        .parent().child("include")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String transletName = StringUtils.emptyToNull(attrs.get("translet"));
                String methodType = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                transletName = AspectranNodeParsingContext.getCurrentRuleParsingContext().applyTransletNamePattern(transletName);

                IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, transletName, methodType, hidden);
                AspectranNodeParsingContext.pushObject(includeActionRule);
            })
            .with(ParameterNodeletAdder.instance())
            .with(ParametersNodeletAdder.instance())
            .with(AttributeNodeletAdder.instance())
            .with(AttributesNodeletAdder.instance())
            .endNodelet(text -> {
                IncludeActionRule includeActionRule = AspectranNodeParsingContext.popObject();
                HasActionRules hasActionRules = AspectranNodeParsingContext.peekObject();
                hasActionRules.putActionRule(includeActionRule);
            });
    }

}
