/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class ChooseNodeletAdder.
 *
 * @since 6.0.0
 */
class ChooseNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();

        parser.setXpath(xpath + "/choose");
        parser.addNodelet(attrs -> {
            ChooseRule chooseRule = ChooseRule.newInstance();
            parser.pushObject(chooseRule);
        });
        parser.addNodeEndlet(text -> {
            ChooseRule chooseRule = parser.popObject();
            ActionRuleApplicable applicable = parser.peekObject();
            applicable.applyActionRule(chooseRule);
        });
        parser.setXpath(xpath + "/choose/when");
        parser.addNodelet(attrs -> {
            String expression = StringUtils.emptyToNull(attrs.get("test"));

            ChooseRule chooseRule = parser.peekObject();
            ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
            chooseWhenRule.setExpression(expression);
            parser.pushObject(chooseWhenRule);
        });
        nodeParser.addActionNodelets();
        nodeParser.addResponseInnerNodelets();
        parser.addNodeEndlet(text -> {
            parser.popObject();
        });
        parser.setXpath(xpath + "/choose/otherwise");
        parser.addNodelet(attrs -> {
            ChooseRule chooseRule = parser.peekObject();
            ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
            parser.pushObject(chooseWhenRule);
        });
        nodeParser.addActionNodelets();
        nodeParser.addResponseInnerNodelets();
        parser.addNodeEndlet(text -> {
            parser.popObject();
        });
    }

}
