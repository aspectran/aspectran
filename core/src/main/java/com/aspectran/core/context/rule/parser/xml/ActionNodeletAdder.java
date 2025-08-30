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
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
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
class ActionNodeletAdder implements NodeletAdder {

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("headers")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                HeaderActionRule headersActionRule = HeaderActionRule.newInstance(id, hidden);
                AspectranNodeParser.current().pushObject(headersActionRule);

                ItemRuleMap irm = new ItemRuleMap();
                AspectranNodeParser.current().pushObject(irm);
            })
            .with(AspectranNodeletGroup.itemNodeletAdder)
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParser.current().popObject();
                HeaderActionRule headersActionRule = AspectranNodeParser.current().popObject();

                headersActionRule.setHeaderItemRuleMap(irm);

                ActionRuleApplicable applicable = AspectranNodeParser.current().peekObject();
                applicable.applyActionRule(headersActionRule);
            })
        .parent().child("echo")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
                AspectranNodeParser.current().pushObject(echoActionRule);

                ItemRuleMap irm = new ItemRuleMap();
                AspectranNodeParser.current().pushObject(irm);
            })
            .with(AspectranNodeletGroup.itemNodeletAdder)
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParser.current().popObject();
                EchoActionRule echoActionRule = AspectranNodeParser.current().popObject();

                if (echoActionRule.getEchoItemRuleMap() == null && !irm.isEmpty()) {
                    echoActionRule.setEchoItemRuleMap(irm);
                }

                ActionRuleApplicable applicable = AspectranNodeParser.current().peekObject();
                applicable.applyActionRule(echoActionRule);
            })
        .parent().child("action")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
                String methodName = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(id, beanIdOrClass, methodName, hidden);
                AspectranNodeParser.current().getAssistant().resolveActionBeanClass(invokeActionRule);
                AspectranNodeParser.current().pushObject(invokeActionRule);
            })
            .endNodelet(text -> {
                InvokeActionRule invokeActionRule = AspectranNodeParser.current().popObject();
                ActionRuleApplicable applicable = AspectranNodeParser.current().peekObject();
                applicable.applyActionRule(invokeActionRule);
            })
            .child("arguments")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdder)
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    InvokeActionRule invokeActionRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, invokeActionRule.getArgumentItemRuleMap());
                    invokeActionRule.setArgumentItemRuleMap(irm);
                })
            .parent().child("properties")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                        irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                        AspectranNodeParser.current().pushObject(irm);
                    })
                .with(AspectranNodeletGroup.itemNodeletAdder)
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    InvokeActionRule invokeActionRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, invokeActionRule.getPropertyItemRuleMap());
                    invokeActionRule.setPropertyItemRuleMap(irm);
                })
            .parent()
        .parent().child("invoke")
            .nodelet(attrs -> {
                String methodName = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(methodName, hidden);
                AspectranNodeParser.current().getAssistant().resolveActionBeanClass(invokeActionRule);
                AspectranNodeParser.current().pushObject(invokeActionRule);
            })
            .endNodelet(text -> {
                InvokeActionRule invokeActionRule = AspectranNodeParser.current().popObject();
                ActionRuleApplicable applicable = AspectranNodeParser.current().peekObject();
                applicable.applyActionRule(invokeActionRule);
            }).child("arguments")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdder)
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    InvokeActionRule invokeActionRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, invokeActionRule.getArgumentItemRuleMap());
                    invokeActionRule.setArgumentItemRuleMap(irm);
                })
            .parent().child("properties")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdder)
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    InvokeActionRule invokeActionRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, invokeActionRule.getPropertyItemRuleMap());
                    invokeActionRule.setPropertyItemRuleMap(irm);
                })
            .parent()
        .parent().child("include")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String transletName = StringUtils.emptyToNull(attrs.get("translet"));
                String methodType = StringUtils.emptyToNull(attrs.get("method"));
                Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

                transletName = AspectranNodeParser.current().getAssistant().applyTransletNamePattern(transletName);

                IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, transletName, methodType, hidden);
                AspectranNodeParser.current().pushObject(includeActionRule);
            })
            .endNodelet(text -> {
                IncludeActionRule includeActionRule = AspectranNodeParser.current().popObject();
                ActionRuleApplicable applicable = AspectranNodeParser.current().peekObject();
                applicable.applyActionRule(includeActionRule);
            })
            .child("parameters")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdder)
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    IncludeActionRule includeActionRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, includeActionRule.getParameterItemRuleMap());
                    includeActionRule.setParameterItemRuleMap(irm);
                })
            .parent().child("attributes")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdder)
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    IncludeActionRule includeActionRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, includeActionRule.getAttributeItemRuleMap());
                    includeActionRule.setAttributeItemRuleMap(irm);
                });
    }

}
