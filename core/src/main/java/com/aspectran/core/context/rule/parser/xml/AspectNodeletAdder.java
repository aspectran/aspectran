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
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class AspectNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class AspectNodeletAdder implements NodeletAdder {

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("aspect")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String order = StringUtils.emptyToNull(attrs.get("order"));
                Boolean isolated = BooleanUtils.toNullableBooleanObject(attrs.get("isolated"));
                Boolean disabled = BooleanUtils.toNullableBooleanObject(attrs.get("disabled"));

                AspectRule aspectRule = AspectRule.newInstance(id, order, isolated, disabled);
                AspectranNodeParser.current().pushObject(aspectRule);
            })
                .endNodelet(text -> {
                AspectRule aspectRule = AspectranNodeParser.current().popObject();
                AspectranNodeParser.current().getAssistant().addAspectRule(aspectRule);
            })
            .child("description")
                .nodelet(attrs -> {
                    String profile = attrs.get("profile");
                    String style = attrs.get("style");

                    DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                    AspectranNodeParser.current().pushObject(descriptionRule);
                })
                .endNodelet(text -> {
                    DescriptionRule descriptionRule = AspectranNodeParser.current().popObject();
                    AspectRule aspectRule = AspectranNodeParser.current().peekObject();

                    descriptionRule.setContent(text);
                    descriptionRule = AspectranNodeParser.current().getAssistant().profiling(descriptionRule, aspectRule.getDescriptionRule());
                    aspectRule.setDescriptionRule(descriptionRule);
                })
        .parent().child("joinpoint")
            .nodelet(attrs -> {
                String target = StringUtils.emptyToNull(attrs.get("target"));
                AspectranNodeParser.current().pushObject(target);
            })
                .endNodelet(text -> {
                String target = AspectranNodeParser.current().popObject();
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                AspectRule.updateJoinpoint(aspectRule, target, text);
            })
        .parent().child("settings")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule);
                AspectranNodeParser.current().pushObject(sar);
            })
            .endNodelet(text -> {
                SettingsAdviceRule sar = AspectranNodeParser.current().popObject();
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                aspectRule.setSettingsAdviceRule(sar);
            })
            .child("setting")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String value = attrs.get("value");
                    AspectranNodeParser.current().pushObject(value);
                    AspectranNodeParser.current().pushObject(name);
                })
                .endNodelet(text -> {
                    String name = AspectranNodeParser.current().popObject();
                    String value = AspectranNodeParser.current().popObject();
                    SettingsAdviceRule sar = AspectranNodeParser.current().peekObject();
                    if (value != null) {
                        sar.putSetting(name, value);
                    } else if (text != null) {
                        sar.putSetting(name, text);
                    }
                })
        .parent().parent().child("advice")
            .nodelet(attrs -> {
                String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
                if (beanIdOrClass != null) {
                    AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                    aspectRule.setAdviceBeanId(beanIdOrClass);
                    AspectranNodeParser.current().getAssistant().resolveAdviceBeanClass(aspectRule);
                }
            })
            .with(AspectranNodeletGroup.adviceInnerNodeAdder)
        .parent().child("exception")
            .nodelet(attrs -> {
                ExceptionRule exceptionRule = new ExceptionRule();
                AspectranNodeParser.current().pushObject(exceptionRule);
            })
            .with(AspectranNodeletGroup.exceptionInnerNodeletAdder)
            .endNodelet(text -> {
                ExceptionRule exceptionRule = AspectranNodeParser.current().popObject();
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                aspectRule.setExceptionRule(exceptionRule);
            });
    }

}
