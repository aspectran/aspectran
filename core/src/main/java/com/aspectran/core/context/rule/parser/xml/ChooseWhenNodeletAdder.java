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

import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class CaseWhenNodeletAdder.
 *
 * @since 2011. 1. 9.
 */
class ChooseWhenNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActionNodeletAdder actionNodeletAdder = nodeParser.getActionNodeletAdder();
        ResponseInnerNodeletAdder responseInnerNodeletAdder = nodeParser.getResponseInnerNodeletAdder();

        parser.setXpath(xpath + "/when");
        parser.addNodelet(attrs -> {
            String test = StringUtils.emptyToNull(attrs.get("test"));

            ChooseRule chooseRule = parser.peekObject();
            ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
            chooseWhenRule.setExpression(test);
            parser.pushObject(chooseWhenRule);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodelet(responseInnerNodeletAdder);
        parser.addNodeEndlet(text -> {
            parser.popObject();
        });
        parser.setXpath(xpath + "/otherwise");
        parser.addNodelet(attrs -> {
            ChooseRule chooseRule = parser.peekObject();
            ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
            parser.pushObject(chooseWhenRule);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodelet(responseInnerNodeletAdder);
        parser.addNodeEndlet(text -> {
            parser.popObject();
        });
    }

}
