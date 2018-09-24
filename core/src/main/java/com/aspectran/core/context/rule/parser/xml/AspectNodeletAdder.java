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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class AspectNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class AspectNodeletAdder implements NodeletAdder {

    protected final ContextRuleAssistant assistant;

    /**
     * Instantiates a new AspectNodeletAdder.
     *
     * @param assistant the assistant
     */
    AspectNodeletAdder(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void process(String xpath, NodeletParser parser) {
        parser.setXpath(xpath + "/aspect");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            String order = StringUtils.emptyToNull(attrs.get("order"));
            Boolean isolated = BooleanUtils.toNullableBooleanObject(attrs.get("isolated"));

            AspectRule aspectRule = AspectRule.newInstance(id, order, isolated);
            parser.pushObject(aspectRule);
        });
        parser.addNodeEndlet(text -> {
            AspectRule aspectRule = parser.popObject();
            assistant.addAspectRule(aspectRule);
        });
        parser.setXpath(xpath + "/aspect/description");
        parser.addNodelet(attrs -> {
            String style = attrs.get("style");
            parser.pushObject(style);
        });
        parser.addNodeEndlet(text -> {
            String style = parser.popObject();
            if (style != null) {
                text = ContentStyleType.styling(text, style);
            }
            if (StringUtils.hasText(text)) {
                AspectRule aspectRule = parser.peekObject();
                aspectRule.setDescription(text);
            }
        });
        parser.setXpath(xpath + "/aspect/joinpoint");
        parser.addNodelet(attrs -> {
            String type = StringUtils.emptyToNull(attrs.get("type"));
            parser.pushObject(type);
        });
        parser.addNodeEndlet(text -> {
            String type = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            AspectRule.updateJoinpoint(aspectRule, type, text);
        });
        parser.setXpath(xpath + "/aspect/settings");
        parser.addNodelet(text -> {
            AspectRule aspectRule = parser.peekObject();
            SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule);
            parser.pushObject(sar);
        });
        parser.addNodeEndlet(text -> {
            SettingsAdviceRule sar = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            SettingsAdviceRule.updateSettingsAdviceRule(sar, text);
            aspectRule.setSettingsAdviceRule(sar);
        });
        parser.setXpath(xpath + "/aspect/settings/setting");
        parser.addNodelet(attrs -> {
            String name = StringUtils.emptyToNull(attrs.get("name"));
            String value = attrs.get("value");

            if (name != null) {
                SettingsAdviceRule sar = parser.peekObject();
                sar.putSetting(name, value);
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
        parser.addNodelet(new AspectAdviceInnerNodeletAdder(assistant));
        parser.setXpath(xpath + "/aspect/exception");
        parser.addNodelet(attrs -> {
            ExceptionRule exceptionRule = new ExceptionRule();
            parser.pushObject(exceptionRule);
        });
        parser.addNodelet(new ExceptionInnerNodeletAdder(assistant));
        parser.addNodeEndlet(text -> {
            ExceptionRule exceptionRule = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            aspectRule.setExceptionRule(exceptionRule);
        });
    }

}