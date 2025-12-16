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
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing the {@code <translet>} element and its
 * sub-elements.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 * @see com.aspectran.core.context.rule.TransletRule
 */
class TransletNodeletAdder implements NodeletAdder {

    private static volatile TransletNodeletAdder INSTANCE;

    static TransletNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (TransletNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TransletNodeletAdder();
                }
            }
        }
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
                AspectranNodeParsingContext.pushObject(transletRule);
            })
            .with(DiscriptionNodeletAdder.instance())
            .with(ParameterNodeletAdder.instance())
            .with(ParametersNodeletAdder.instance())
            .with(AttributeNodeletAdder.instance())
            .with(AttributesNodeletAdder.instance())
            .with(ActionInnerNodeletAdder.instance())
            .with(ResponseInnerNodeletAdder.instance())
            .with(ChooseNodeletAdder.instance())
            .endNodelet(text -> {
                TransletRule transletRule = AspectranNodeParsingContext.popObject();
                AspectranNodeParsingContext.getCurrentRuleParsingContext().addTransletRule(transletRule);
            })
            .child("request")
                .nodelet(attrs -> {
                    String method = attrs.get("method");
                    String encoding = attrs.get("encoding");

                    RequestRule requestRule = RequestRule.newInstance(method, encoding);
                    AspectranNodeParsingContext.pushObject(requestRule);
                })
                .with(ParameterNodeletAdder.instance())
                .with(ParametersNodeletAdder.instance())
                .with(AttributeNodeletAdder.instance())
                .with(AttributesNodeletAdder.instance())
                .endNodelet(text -> {
                    RequestRule requestRule = AspectranNodeParsingContext.popObject();
                    TransletRule transletRule = AspectranNodeParsingContext.peekObject();
                    transletRule.setRequestRule(requestRule);
                })
            .parent().child("contents")
                .nodelet(attrs -> {
                    String name = attrs.get("name");

                    ContentList contentList = ContentList.newInstance(name);
                    AspectranNodeParsingContext.pushObject(contentList);
                })
                .endNodelet(text -> {
                    ContentList contentList = AspectranNodeParsingContext.popObject();
                    if (!contentList.isEmpty()) {
                        TransletRule transletRule = AspectranNodeParsingContext.peekObject();
                        transletRule.setContentList(contentList);
                    }
                })
                .child("content")
                    .nodelet(attrs -> {
                        String name = attrs.get("name");

                        ActionList actionList = ActionList.newInstance(name);
                        AspectranNodeParsingContext.pushObject(actionList);
                    })
                    .with(ActionInnerNodeletAdder.instance())
                    .with(ResponseInnerNodeletAdder.instance())
                    .with(ChooseNodeletAdder.instance())
                    .endNodelet(text -> {
                        ActionList actionList = AspectranNodeParsingContext.popObject();
                        if (!actionList.isEmpty()) {
                            ContentList contentList = AspectranNodeParsingContext.peekObject();
                            contentList.addActionList(actionList);
                        }
                    })
                .parent()
            .parent().child("content")
                .nodelet(attrs -> {
                    String name = attrs.get("name");

                    ActionList actionList = ActionList.newInstance(name);
                    AspectranNodeParsingContext.pushObject(actionList);
                })
                .with(ActionInnerNodeletAdder.instance())
                .with(ResponseInnerNodeletAdder.instance())
                .with(ChooseNodeletAdder.instance())
                .endNodelet(text -> {
                    ActionList actionList = AspectranNodeParsingContext.popObject();
                    if (!actionList.isEmpty()) {
                        ContentList contentList = new ContentList(false);
                        contentList.add(actionList);

                        TransletRule transletRule = AspectranNodeParsingContext.peekObject();
                        transletRule.setContentList(contentList);
                    }
                })
            .parent().child("response")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String encoding = attrs.get("encoding");

                    ResponseRule responseRule = ResponseRule.newInstance(name, encoding);
                    AspectranNodeParsingContext.pushObject(responseRule);
                })
                .with(ActionInnerNodeletAdder.instance())
                .with(ResponseInnerNodeletAdder.instance())
                .with(ChooseNodeletAdder.instance())
                .endNodelet(text -> {
                    ResponseRule responseRule = AspectranNodeParsingContext.popObject();
                    TransletRule transletRule = AspectranNodeParsingContext.peekObject();
                    transletRule.addResponseRule(responseRule);
                })
            .parent().child("exception")
                .nodelet(attrs -> {
                    ExceptionRule exceptionRule = new ExceptionRule();
                    AspectranNodeParsingContext.pushObject(exceptionRule);
                })
                .with(ExceptionInnerNodeletAdder.instance())
                .endNodelet(text -> {
                    ExceptionRule exceptionRule = AspectranNodeParsingContext.popObject();
                    TransletRule transletRule = AspectranNodeParsingContext.peekObject();
                    transletRule.setExceptionRule(exceptionRule);
                });
    }

}
