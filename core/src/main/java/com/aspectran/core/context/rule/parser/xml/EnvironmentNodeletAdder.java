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

import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class EnvironmentNodeletAdder.
 * 
 * <p>Created: 2016. 01. 09</p>
 */
class EnvironmentNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ItemNodeletAdder itemNodeletAdder = nodeParser.getItemNodeletAdder();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/environment");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");

            EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile, null);
            parser.pushObject(environmentRule);
        });
        parser.addNodeEndlet(text -> {
            EnvironmentRule environmentRule = parser.popObject();
            assistant.addEnvironmentRule(environmentRule);
        });
        parser.setXpath(xpath + "/environment/description");
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
                EnvironmentRule environmentRule = parser.peekObject();
                environmentRule.setDescription(text);
            }
        });
        parser.setXpath(xpath + "/environment/properties");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            parser.pushObject(irm);
        });
        parser.addNodelet(itemNodeletAdder);
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            EnvironmentRule environmentRule = parser.peekObject();
            environmentRule.setPropertyItemRuleMap(irm);
        });
    }

}