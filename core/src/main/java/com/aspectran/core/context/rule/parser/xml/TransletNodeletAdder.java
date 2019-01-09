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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseRuleMap;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.util.BooleanUtils;
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
        ActionNodeletAdder actionNodeletAdder = nodeParser.getActionNodeletAdder();
        ExceptionInnerNodeletAdder exceptionInnerNodeletAdder = nodeParser.getExceptionInnerNodeletAdder();
        ItemNodeletAdder itemNodeletAdder = nodeParser.getItemNodeletAdder();
        ResponseInnerNodeletAdder responseInnerNodeletAdder = nodeParser.getResponseInnerNodeletAdder();
        ChooseWhenNodeletAdder chooseWhenNodeletAdder = nodeParser.getChooseWhenNodeletAdder();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/translet");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String scan = attrs.get("scan");
            String mask = attrs.get("mask");
            String method = attrs.get("method");

            TransletRule transletRule = TransletRule.newInstance(name, scan, mask, method);
            parser.pushObject(transletRule);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodelet(responseInnerNodeletAdder);
        parser.addNodeEndlet(text -> {
            TransletRule transletRule = parser.popObject();
            assistant.addTransletRule(transletRule);
        });
        parser.setXpath(xpath + "/translet/description");
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
                TransletRule transletRule = parser.peekObject();
                transletRule.setDescription(text);
            }
        });
        parser.setXpath(xpath + "/translet/parameters");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        parser.addNodelet(itemNodeletAdder);
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            RequestRule requestRule = transletRule.touchRequestRule(true);
            irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
            requestRule.setParameterItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/translet/attributes");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        parser.addNodelet(itemNodeletAdder);
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            RequestRule requestRule = transletRule.touchRequestRule(true);
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
        parser.addNodelet(itemNodeletAdder);
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
        parser.addNodelet(itemNodeletAdder);
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            RequestRule requestRule = parser.peekObject();
            irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
            requestRule.setAttributeItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/translet/choose");
        parser.addNodelet(attrs -> {
            TransletRule transletRule = parser.peekObject();

            ChooseRuleMap chooseRuleMap = transletRule.touchChooseRuleMap();
            ChooseRule chooseRule = chooseRuleMap.newChooseRule();

            parser.pushObject(chooseRule);
        });
        parser.addNodelet(chooseWhenNodeletAdder);
        parser.addNodeEndlet(text -> {
            ChooseRule chooseRule = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            chooseRule.join(transletRule);
        });
        parser.setXpath(xpath + "/translet/contents");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            Boolean omittable = BooleanUtils.toNullableBooleanObject(attrs.get("omittable"));

            ContentList contentList = ContentList.newInstance(name, omittable);
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
            Boolean omittable = BooleanUtils.toNullableBooleanObject(attrs.get("omittable"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            ActionList actionList = ActionList.newInstance(name, omittable, hidden);
            parser.pushObject(actionList);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            if (!actionList.isEmpty()) {
                ContentList contentList = parser.peekObject();
                contentList.addActionList(actionList);
            }
        });
        parser.setXpath(xpath + "/translet/contents/content/choose");
        parser.addNodelet(attrs -> {
            TransletRule transletRule = parser.peekObject(2);

            ChooseRuleMap chooseRuleMap = transletRule.touchChooseRuleMap();
            ChooseRule chooseRule = chooseRuleMap.newChooseRule();

            parser.pushObject(chooseRule);
        });
        parser.addNodelet(chooseWhenNodeletAdder);
        parser.addNodeEndlet(text -> {
            ChooseRule chooseRule = parser.popObject();
            TransletRule transletRule = parser.peekObject(2);
            chooseRule.join(transletRule);
        });
        parser.setXpath(xpath + "/translet/content");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            Boolean omittable = BooleanUtils.toNullableBooleanObject(attrs.get("omittable"));
            Boolean hidden = BooleanUtils.toNullableBooleanObject(attrs.get("hidden"));

            ActionList actionList = ActionList.newInstance(name, omittable, hidden);
            parser.pushObject(actionList);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            if (!actionList.isEmpty()) {
                TransletRule transletRule = parser.peekObject();
                ContentList contentList = transletRule.touchContentList(true, true);
                contentList.addActionList(actionList);
            }
        });
        parser.setXpath(xpath + "/translet/content/choose");
        parser.addNodelet(attrs -> {
            TransletRule transletRule = parser.peekObject(1);

            ChooseRuleMap chooseRuleMap = transletRule.touchChooseRuleMap();
            ChooseRule chooseRule = chooseRuleMap.newChooseRule();

            parser.pushObject(chooseRule);
        });
        parser.addNodelet(chooseWhenNodeletAdder);
        parser.addNodeEndlet(text -> {
            ChooseRule chooseRule = parser.popObject();
            TransletRule transletRule = parser.peekObject(1);
            chooseRule.join(transletRule);
        });
        parser.setXpath(xpath + "/translet/response");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String encoding = attrs.get("encoding");

            ResponseRule responseRule = ResponseRule.newInstance(name, encoding);
            parser.pushObject(responseRule);
        });
        parser.addNodelet(responseInnerNodeletAdder);
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
        parser.addNodelet(exceptionInnerNodeletAdder);
        parser.addNodeEndlet(text -> {
            ExceptionRule exceptionRule = parser.popObject();
            TransletRule transletRule = parser.peekObject();
            transletRule.setExceptionRule(exceptionRule);
        });
    }

}