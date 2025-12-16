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

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing the {@code <aspect>} element and its sub-elements,
 * creating an {@link com.aspectran.core.context.rule.AspectRule}.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 * @see com.aspectran.core.context.rule.AspectRule
 */
class AspectNodeletAdder implements NodeletAdder {

    private static volatile AspectNodeletAdder INSTANCE;

    static AspectNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (AspectNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AspectNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("aspect")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String order = StringUtils.emptyToNull(attrs.get("order"));
                Boolean isolated = BooleanUtils.toNullableBooleanObject(attrs.get("isolated"));
                Boolean disabled = BooleanUtils.toNullableBooleanObject(attrs.get("disabled"));

                AspectRule aspectRule = AspectRule.newInstance(id, order, isolated, disabled);
                AspectranNodeParsingContext.pushObject(aspectRule);
            })
            .with(DiscriptionNodeletAdder.instance())
            .endNodelet(text -> {
                AspectRule aspectRule = AspectranNodeParsingContext.popObject();
                AspectranNodeParsingContext.getCurrentRuleParsingContext().addAspectRule(aspectRule);
            })
            .child("joinpoint")
                .nodelet(attrs -> {
                    String target = StringUtils.emptyToNull(attrs.get("target"));
                    AspectranNodeParsingContext.pushObject(target);
                })
                .endNodelet(text -> {
                    String target = AspectranNodeParsingContext.popObject();
                    AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                    AspectRule.updateJoinpoint(aspectRule, target, text);
                })
            .parent().child("settings")
                .nodelet(attrs -> {
                    AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                    SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule);
                    AspectranNodeParsingContext.pushObject(sar);
                })
                .endNodelet(text -> {
                    SettingsAdviceRule sar = AspectranNodeParsingContext.popObject();
                    AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                    aspectRule.setSettingsAdviceRule(sar);
                })
                .child("setting")
                    .nodelet(attrs -> {
                        String name = attrs.get("name");
                        String value = attrs.get("value");
                        AspectranNodeParsingContext.pushObject(value);
                        AspectranNodeParsingContext.pushObject(name);
                    })
                    .endNodelet(text -> {
                        String name = AspectranNodeParsingContext.popObject();
                        String value = AspectranNodeParsingContext.popObject();
                        SettingsAdviceRule sar = AspectranNodeParsingContext.peekObject();
                        if (value != null) {
                            sar.putSetting(name, value);
                        } else if (text != null) {
                            sar.putSetting(name, text);
                        }
                    })
                .parent()
            .parent().child("advice")
                .nodelet(attrs -> {
                    String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
                    if (beanIdOrClass != null) {
                        AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                        aspectRule.setAdviceBeanId(beanIdOrClass);
                        AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveAdviceBeanClass(aspectRule);
                    }
                })
                .with(AdviceInnerNodeletAdder.instance())
            .parent().child("exception")
                .nodelet(attrs -> {
                    ExceptionRule exceptionRule = new ExceptionRule();
                    AspectranNodeParsingContext.pushObject(exceptionRule);
                })
                .with(ExceptionInnerNodeletAdder.instance())
                .endNodelet(text -> {
                    ExceptionRule exceptionRule = AspectranNodeParsingContext.popObject();
                    AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                    aspectRule.setExceptionRule(exceptionRule);
                });
    }

}
