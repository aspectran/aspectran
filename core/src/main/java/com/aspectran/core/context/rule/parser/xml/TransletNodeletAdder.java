/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class TransletNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class TransletNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/translet");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String scan = attrs.get("scan");
            String mask = attrs.get("mask");
            String method = attrs.get("method");

            TransletRule transletRule = TransletRule.newInstance(name, scan, mask, method);
            parser.pushObject(transletRule);
        });
        parser.addNodeEndlet(text -> {
            TransletRule transletRule = parser.popObject();
            assistant.addTransletRule(transletRule);
        });
        parser.setXpath(xpath + "/translet/description");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addNodeEndlet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            TransletRule transletRule = parser.peekObject();

            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, transletRule.getDescriptionRule());
            transletRule.setDescriptionRule(descriptionRule);
        });
        parser.setXpath(xpath + "/translet/parameters");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            RequestRule requestRule = transletRule.touchRequestRule(false);
            irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
            requestRule.setParameterItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/translet/attributes");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            RequestRule requestRule = transletRule.touchRequestRule(false);
            irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
            requestRule.setAttributeItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/translet/request");
        parser.addNodelet(attrs -> {
            String method = attrs.get("method");
            String encoding = attrs.get("encoding");

            RequestRule requestRule = RequestRule.newInstance(method, encoding);
            parser.pushObject(requestRule);
        });
        parser.addNodeEndlet(text -> {
            RequestRule requestRule = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            transletRule.setRequestRule(requestRule);
        });
        parser.setXpath(xpath + "/translet/request/parameters");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            RequestRule requestRule = parser.peekObject();
            irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
            requestRule.setParameterItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/translet/request/attributes");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            RequestRule requestRule = parser.peekObject();
            irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
            requestRule.setAttributeItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/translet");
        nodeParser.addNestedActionNodelets();
        parser.setXpath(xpath + "/translet/contents");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");

            ContentList contentList = ContentList.newInstance(name);
            parser.pushObject(contentList);
        });
        parser.addNodeEndlet(text -> {
            ContentList contentList = parser.popObject();
            if (!contentList.isEmpty()) {
                TransletRule transletRule = parser.peekObject();
                transletRule.setContentList(contentList);
            }
        });
        parser.setXpath(xpath + "/translet/contents/content");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");

            ActionList actionList = ActionList.newInstance(name);
            parser.pushObject(actionList);
        });
        nodeParser.addNestedActionNodelets();
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            if (!actionList.isEmpty()) {
                ContentList contentList = parser.peekObject();
                contentList.addActionList(actionList);
            }
        });
        parser.setXpath(xpath + "/translet/content");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");

            ActionList actionList = ActionList.newInstance(name);
            parser.pushObject(actionList);
        });
        nodeParser.addNestedActionNodelets();
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            if (!actionList.isEmpty()) {
                ContentList contentList = new ContentList(false);
                contentList.add(actionList);

                TransletRule transletRule = parser.peekObject();
                transletRule.setContentList(contentList);
            }
        });
        parser.setXpath(xpath + "/translet");
        nodeParser.addResponseInnerNodelets();
        parser.setXpath(xpath + "/translet/response");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String encoding = attrs.get("encoding");

            ResponseRule responseRule = ResponseRule.newInstance(name, encoding);
            parser.pushObject(responseRule);
        });
        nodeParser.addNestedActionNodelets();
        nodeParser.addResponseInnerNodelets();
        parser.addNodeEndlet(text -> {
            ResponseRule responseRule = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            transletRule.addResponseRule(responseRule);
        });
        parser.setXpath(xpath + "/translet/exception");
        parser.addNodelet(attrs -> {
            ExceptionRule exceptionRule = new ExceptionRule();
            parser.pushObject(exceptionRule);
        });
        nodeParser.addExceptionInnerNodelets();
        parser.addNodeEndlet(text -> {
            ExceptionRule exceptionRule = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            transletRule.setExceptionRule(exceptionRule);
        });
    }

}
