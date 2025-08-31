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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class TransletNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class TransletNodeletAdder implements NodeletAdder {

    private static final TransletNodeletAdder INSTANCE = new TransletNodeletAdder();

    static TransletNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("translet")
            .nodelet(attrs -> {
                String name = attrs.get("name");
                String scan = attrs.get("scan");
                String mask = attrs.get("mask");
                String method = attrs.get("method");
                Boolean async = BooleanUtils.toNullableBooleanObject(attrs.get("async"));
                String timeout = attrs.get("timeout");

                TransletRule transletRule = TransletRule.newInstance(name, scan, mask, method, async, timeout);
                AspectranNodeParser.current().pushObject(transletRule);
            })
            .with(DiscriptionNodeletAdder.instance())
            .with(ActionInnerNodeletAdder.instance())
            .with(ResponseInnerNodeletAdder.instance())
            .mount(ChooseNodeletGroup.instance())
            .endNodelet(text -> {
                TransletRule transletRule = AspectranNodeParser.current().popObject();
                AspectranNodeParser.current().getAssistant().addTransletRule(transletRule);
            })
            .child("request")
                .nodelet(attrs -> {
                    String method = attrs.get("method");
                    String encoding = attrs.get("encoding");

                    RequestRule requestRule = RequestRule.newInstance(method, encoding);
                    AspectranNodeParser.current().pushObject(requestRule);
                })
                .endNodelet(text -> {
                    RequestRule requestRule = AspectranNodeParser.current().popObject();
                    TransletRule transletRule = AspectranNodeParser.current().peekObject();
                    transletRule.setRequestRule(requestRule);
                })
                .child("parameters")
                    .nodelet(attrs -> {
                        ItemRuleMap irm = new ItemRuleMap();
                        irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                        AspectranNodeParser.current().pushObject(irm);
                    })
                    .mount(ItemNodeletGroup.instance())
                    .endNodelet(text -> {
                        ItemRuleMap irm = AspectranNodeParser.current().popObject();
                        RequestRule requestRule = AspectranNodeParser.current().peekObject();
                        irm = AspectranNodeParser.current().getAssistant().profiling(irm, requestRule.getParameterItemRuleMap());
                        requestRule.setParameterItemRuleMap(irm);
                    })
                .parent().child("attributes")
                    .nodelet(attrs -> {
                        ItemRuleMap irm = new ItemRuleMap();
                        irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                        AspectranNodeParser.current().pushObject(irm);
                    })
                    .mount(ItemNodeletGroup.instance())
                    .endNodelet(text -> {
                        ItemRuleMap irm = AspectranNodeParser.current().popObject();
                        RequestRule requestRule = AspectranNodeParser.current().peekObject();
                        irm = AspectranNodeParser.current().getAssistant().profiling(irm, requestRule.getAttributeItemRuleMap());
                        requestRule.setAttributeItemRuleMap(irm);
                    })
                .parent()
            .parent().child("parameters")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .mount(ItemNodeletGroup.instance())
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    TransletRule transletRule = AspectranNodeParser.current().peekObject();
                    RequestRule requestRule = transletRule.touchRequestRule(false);
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, requestRule.getParameterItemRuleMap());
                    requestRule.setParameterItemRuleMap(irm);
                })
            .parent().child("attributes")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .mount(ItemNodeletGroup.instance())
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    TransletRule transletRule = AspectranNodeParser.current().peekObject();
                    RequestRule requestRule = transletRule.touchRequestRule(false);
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, requestRule.getAttributeItemRuleMap());
                    requestRule.setAttributeItemRuleMap(irm);
                })
            .parent().child("contents")
                .nodelet(attrs -> {
                    String name = attrs.get("name");

                    ContentList contentList = ContentList.newInstance(name);
                    AspectranNodeParser.current().pushObject(contentList);
                })
                .endNodelet(text -> {
                    ContentList contentList = AspectranNodeParser.current().popObject();
                    if (!contentList.isEmpty()) {
                        TransletRule transletRule = AspectranNodeParser.current().peekObject();
                        transletRule.setContentList(contentList);
                    }
                })
                .child("content")
                    .nodelet(attrs -> {
                        String name = attrs.get("name");

                        ActionList actionList = ActionList.newInstance(name);
                        AspectranNodeParser.current().pushObject(actionList);
                    })
                    .with(ActionInnerNodeletAdder.instance())
                    .with(ResponseInnerNodeletAdder.instance())
                    .mount(ChooseNodeletGroup.instance())
                    .endNodelet(text -> {
                        ActionList actionList = AspectranNodeParser.current().popObject();
                        if (!actionList.isEmpty()) {
                            ContentList contentList = AspectranNodeParser.current().peekObject();
                            contentList.addActionList(actionList);
                        }
                    })
            .parent().child("content")
                .nodelet(attrs -> {
                    String name = attrs.get("name");

                    ActionList actionList = ActionList.newInstance(name);
                    AspectranNodeParser.current().pushObject(actionList);
                })
                .with(ActionInnerNodeletAdder.instance())
                .with(ResponseInnerNodeletAdder.instance())
                .mount(ChooseNodeletGroup.instance())
                .endNodelet(text -> {
                    ActionList actionList = AspectranNodeParser.current().popObject();
                    if (!actionList.isEmpty()) {
                        ContentList contentList = new ContentList(false);
                        contentList.add(actionList);

                        TransletRule transletRule = AspectranNodeParser.current().peekObject();
                        transletRule.setContentList(contentList);
                    }
                })
            .parent().child("response")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String encoding = attrs.get("encoding");

                    ResponseRule responseRule = ResponseRule.newInstance(name, encoding);
                    AspectranNodeParser.current().pushObject(responseRule);
                })
                .with(ActionInnerNodeletAdder.instance())
                .with(ResponseInnerNodeletAdder.instance())
                .mount(ChooseNodeletGroup.instance())
                .endNodelet(text -> {
                    ResponseRule responseRule = AspectranNodeParser.current().popObject();
                    TransletRule transletRule = AspectranNodeParser.current().peekObject();
                    transletRule.addResponseRule(responseRule);
                })
            .parent().child("exception")
                .nodelet(attrs -> {
                    ExceptionRule exceptionRule = new ExceptionRule();
                    AspectranNodeParser.current().pushObject(exceptionRule);
                })
                .with(ExceptionInnerNodeletAdder.instance())
                .endNodelet(text -> {
                    ExceptionRule exceptionRule = AspectranNodeParser.current().popObject();
                    TransletRule transletRule = AspectranNodeParser.current().peekObject();
                    transletRule.setExceptionRule(exceptionRule);
                });
    }

}
