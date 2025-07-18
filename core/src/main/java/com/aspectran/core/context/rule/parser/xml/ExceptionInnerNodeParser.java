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

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletParser;
import com.aspectran.utils.nodelet.SubnodeParser;

/**
 * The Class ExceptionInnerNodeParser.
 *
 * @since 2013. 8. 11.
 */
class ExceptionInnerNodeParser implements SubnodeParser {

    @Override
    public void parse(@NonNull String xpath, @NonNull NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/description");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addEndNodelet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            ExceptionRule exceptionRule = parser.peekObject();

            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, exceptionRule.getDescriptionRule());
            exceptionRule.setDescriptionRule(descriptionRule);
        });
        parser.setXpath(xpath + "/thrown");
        parser.addNodelet(attrs -> {
            String exceptionType = attrs.get("type");

            ExceptionThrownRule etr = new ExceptionThrownRule();
            if (exceptionType != null) {
                String[] exceptionTypes = StringUtils.splitWithComma(exceptionType);
                etr.setExceptionTypes(exceptionTypes);
            }

            parser.pushObject(etr);
        });
        nodeParser.parseActionNode();
        nodeParser.parseResponseInnerNode();
        parser.addEndNodelet(text -> {
            ExceptionThrownRule etr = parser.popObject();
            ExceptionRule exceptionRule = parser.peekObject();
            exceptionRule.putExceptionThrownRule(etr);
        });
    }

}
