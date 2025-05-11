/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletParser;
import com.aspectran.utils.nodelet.SubnodeParser;

/**
 * The Class AdviceInnerNodeParser.
 *
 * @since 2013. 8. 11.
 */
class AdviceInnerNodeParser implements SubnodeParser {

    @Override
    public void parse(@NonNull String xpath, @NonNull NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();

        parser.setXpath(xpath + "/before");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AdviceRule adviceRule = aspectRule.newBeforeAdviceRule();
            parser.pushObject(adviceRule);
        });
        nodeParser.parseActionNode();
        parser.addEndNodelet(text -> parser.popObject());
        parser.setXpath(xpath + "/after");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AdviceRule adviceRule = aspectRule.newAfterAdviceRule();
            parser.pushObject(adviceRule);
        });
        nodeParser.parseActionNode();
        parser.addEndNodelet(text -> parser.popObject());
        parser.setXpath(xpath + "/around");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AdviceRule adviceRule = aspectRule.newAroundAdviceRule();
            parser.pushObject(adviceRule);
        });
        nodeParser.parseActionNode();
        parser.addEndNodelet(text -> parser.popObject());
        parser.setXpath(xpath + "/finally");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AdviceRule adviceRule = aspectRule.newFinallyAdviceRule();
            parser.pushObject(adviceRule);
        });
        nodeParser.parseActionNode();
        parser.addEndNodelet(text -> parser.popObject());
        parser.setXpath(xpath + "/finally/thrown");
        parser.addNodelet(attrs -> {
            String exceptionType = attrs.get("type");
            AdviceRule adviceRule = parser.peekObject();
            ExceptionThrownRule etr = new ExceptionThrownRule(adviceRule);
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
            AdviceRule adviceRule = parser.peekObject();
            adviceRule.setExceptionThrownRule(etr);
        });
    }

}
