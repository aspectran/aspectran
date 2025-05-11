/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletParser;
import com.aspectran.utils.nodelet.SubnodeParser;

/**
 * The Class AspectNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class AspectNodeParser implements SubnodeParser {

    @Override
    public void parse(@NonNull String xpath, @NonNull NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/aspect");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            String order = StringUtils.emptyToNull(attrs.get("order"));
            Boolean isolated = BooleanUtils.toNullableBooleanObject(attrs.get("isolated"));
            Boolean disabled = BooleanUtils.toNullableBooleanObject(attrs.get("disabled"));

            AspectRule aspectRule = AspectRule.newInstance(id, order, isolated, disabled);
            parser.pushObject(aspectRule);
        });
        parser.addEndNodelet(text -> {
            AspectRule aspectRule = parser.popObject();
            assistant.addAspectRule(aspectRule);
        });
        parser.setXpath(xpath + "/aspect/description");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addEndNodelet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            AspectRule aspectRule = parser.peekObject();

            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, aspectRule.getDescriptionRule());
            aspectRule.setDescriptionRule(descriptionRule);
        });
        parser.setXpath(xpath + "/aspect/joinpoint");
        parser.addNodelet(attrs -> {
            String target = StringUtils.emptyToNull(attrs.get("target"));
            parser.pushObject(target);
        });
        parser.addEndNodelet(text -> {
            String target = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            AspectRule.updateJoinpoint(aspectRule, target, text);
        });
        parser.setXpath(xpath + "/aspect/settings");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule);
            parser.pushObject(sar);
        });
        parser.addEndNodelet(text -> {
            SettingsAdviceRule sar = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            aspectRule.setSettingsAdviceRule(sar);
        });
        parser.setXpath(xpath + "/aspect/settings/setting");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String value = attrs.get("value");
            parser.pushObject(value);
            parser.pushObject(name);
        });
        parser.addEndNodelet(text -> {
            String name = parser.popObject();
            String value = parser.popObject();
            SettingsAdviceRule sar = parser.peekObject();
            if (value != null) {
                sar.putSetting(name, value);
            } else if (text != null) {
                sar.putSetting(name, text);
            }
        });
        parser.setXpath(xpath + "/aspect/advice");
        parser.addNodelet(attrs -> {
            String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
            if (beanIdOrClass != null) {
                AspectRule aspectRule = parser.peekObject();
                aspectRule.setAdviceBeanId(beanIdOrClass);
                assistant.resolveAdviceBeanClass(aspectRule);
            }
        });
        nodeParser.parseAdviceInnerNode();
        parser.setXpath(xpath + "/aspect/exception");
        parser.addNodelet(attrs -> {
            ExceptionRule exceptionRule = new ExceptionRule();
            parser.pushObject(exceptionRule);
        });
        nodeParser.parseExceptionInnerNode();
        parser.addEndNodelet(text -> {
            ExceptionRule exceptionRule = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            aspectRule.setExceptionRule(exceptionRule);
        });
    }

}
