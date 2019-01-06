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
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.CaseRule;
import com.aspectran.core.context.rule.CaseWhenRule;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class CaseWhenNodeletAdder.
 *
 * @since 2011. 1. 9.
 */
class CaseWhenNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActionNodeletAdder actionNodeletAdder = nodeParser.getActionNodeletAdder();

        parser.setXpath(xpath + "/when");
        parser.addNodelet(attrs -> {
            String test = StringUtils.emptyToNull(attrs.get("test"));

            CaseRule caseRule = parser.peekObject();
            CaseWhenRule caseWhenRule = caseRule.newCaseWhenRule();
            caseWhenRule.setExpression(test);
            parser.pushObject(caseWhenRule);

            ActionList actionList = new ActionList();
            parser.pushObject(actionList);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            CaseWhenRule caseWhenRule = parser.popObject();
            ActionRuleApplicable applicable = parser.peekObject(1);

            if (!actionList.isEmpty()) {
                actionList.get(0).setCaseWhenFirst(true);
                actionList.get(actionList.size() - 1).setCaseWhenLast(true);
                for (Executable action : actionList) {
                    action.setCaseWhenNo(caseWhenRule.getCaseWhenNo());
                    applicable.applyActionRule(action);
                }
            }
        });
        parser.setXpath(xpath + "/else");
        parser.addNodelet(attrs -> {
            CaseRule caseRule = parser.peekObject();
            CaseWhenRule caseWhenRule = caseRule.newCaseWhenRule();
            parser.pushObject(caseWhenRule);

            ActionList actionList = new ActionList();
            parser.pushObject(actionList);
        });
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            CaseWhenRule caseWhenRule = parser.popObject();
            ActionRuleApplicable applicable = parser.peekObject(1);

            if (!actionList.isEmpty()) {
                actionList.get(0).setCaseWhenFirst(true);
                actionList.get(actionList.size() - 1).setCaseWhenLast(true);
                for (Executable action : actionList) {
                    action.setCaseWhenNo(caseWhenRule.getCaseWhenNo());
                    applicable.applyActionRule(action);
                }
            }
        });
    }

}
