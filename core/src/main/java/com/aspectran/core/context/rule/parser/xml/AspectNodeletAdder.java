/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        AspectAdviceInnerNodeletAdder aspectAdviceInnerNodeletAdder = nodeParser.getAspectAdviceInnerNodeletAdder();
        ExceptionInnerNodeletAdder exceptionInnerNodeletAdder = nodeParser.getExceptionInnerNodeletAdder();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

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
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule);
            parser.pushObject(sar);
        });
        parser.addNodeEndlet(text -> {
            SettingsAdviceRule sar = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            aspectRule.setSettingsAdviceRule(sar);
        });
        parser.setXpath(xpath + "/aspect/settings/setting");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String value = attrs.get("value");

            SettingsAdviceRule sar = parser.peekObject();
            sar.putSetting(name, value);

            parser.pushObject(name);
        });
        parser.addNodeEndlet(text -> {
            String name = parser.popObject();
            if (text != null) {
                SettingsAdviceRule sar = parser.peekObject();
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
        parser.addNodelet(aspectAdviceInnerNodeletAdder);
        parser.setXpath(xpath + "/aspect/exception");
        parser.addNodelet(attrs -> {
            ExceptionRule exceptionRule = new ExceptionRule();
            parser.pushObject(exceptionRule);
        });
        parser.addNodelet(exceptionInnerNodeletAdder);
        parser.addNodeEndlet(text -> {
            ExceptionRule exceptionRule = parser.popObject();
            AspectRule aspectRule = parser.peekObject();
            aspectRule.setExceptionRule(exceptionRule);
        });
    }

}