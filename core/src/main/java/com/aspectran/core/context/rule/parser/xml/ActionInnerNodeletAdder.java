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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class ActionNodeParser.
 *
 * @since 2011. 1. 9.
 */
class ActionInnerNodeletAdder implements NodeletAdder {

    private static final ActionInnerNodeletAdder INSTANCE = new ActionInnerNodeletAdder();

    static ActionInnerNodeletAdder instance() {
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
            .mount(ItemNodeletGroup.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                HeaderActionRule headersActionRule = AspectranNodeParsingContext.popObject();

                headersActionRule.setHeaderItemRuleMap(irm);

                HasActionRules applicable = AspectranNodeParsingContext.peekObject();
                applicable.putActionRule(headersActionRule);
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
            .mount(ItemNodeletGroup.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                EchoActionRule echoActionRule = AspectranNodeParsingContext.popObject();

                if (echoActionRule.getEchoItemRuleMap() == null && !irm.isEmpty()) {
                    echoActionRule.setEchoItemRuleMap(irm);
                }

                HasActionRules applicable = AspectranNodeParsingContext.peekObject();
                applicable.putActionRule(echoActionRule);
            })
        .parent().child("action")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
                String methodName = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(id, beanIdOrClass, methodName, hidden);
                AspectranNodeParsingContext.assistant().resolveActionBeanClass(invokeActionRule);
                AspectranNodeParsingContext.pushObject(invokeActionRule);
            })
            .endNodelet(text -> {
                InvokeActionRule invokeActionRule = AspectranNodeParsingContext.popObject();
                HasActionRules applicable = AspectranNodeParsingContext.peekObject();
                applicable.putActionRule(invokeActionRule);
            })
            .with(ArgumentsNodeletAdder.instance())
            .with(PropertiesNodeletAdder.instance())
        .parent().child("invoke")
            .nodelet(attrs -> {
                String methodName = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(methodName, hidden);
                AspectranNodeParsingContext.assistant().resolveActionBeanClass(invokeActionRule);
                AspectranNodeParsingContext.pushObject(invokeActionRule);
            })
            .endNodelet(text -> {
                InvokeActionRule invokeActionRule = AspectranNodeParsingContext.popObject();
                HasActionRules applicable = AspectranNodeParsingContext.peekObject();
                applicable.putActionRule(invokeActionRule);
            })
            .with(ArgumentsNodeletAdder.instance())
            .with(PropertiesNodeletAdder.instance())
        .parent().child("include")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String transletName = StringUtils.emptyToNull(attrs.get("translet"));
                String methodType = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                transletName = AspectranNodeParsingContext.assistant().applyTransletNamePattern(transletName);

                IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, transletName, methodType, hidden);
                AspectranNodeParsingContext.pushObject(includeActionRule);
            })
            .endNodelet(text -> {
                IncludeActionRule includeActionRule = AspectranNodeParsingContext.popObject();
                HasActionRules applicable = AspectranNodeParsingContext.peekObject();
                applicable.putActionRule(includeActionRule);
            })
            .with(ParametersNodeletAdder.instance())
            .with(AttributesNodeletAdder.instance());
    }

}
