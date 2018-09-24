/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class ExceptionInnerNodeletAdder.
 *
 * @since 2013. 8. 11.
 */
class ExceptionInnerNodeletAdder implements NodeletAdder {

    protected final ContextRuleAssistant assistant;

    /**
     * Instantiates a new ExceptionInnerNodeletAdder.
     *
     * @param assistant the assistant for Context Builder
     */
    ExceptionInnerNodeletAdder(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void process(String xpath, NodeletParser parser) {
        parser.setXpath(xpath + "/description");
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
                ExceptionRule exceptionRule = parser.peekObject();
                exceptionRule.setDescription(text);
            }
        });
        parser.setXpath(xpath);
        parser.addNodelet(new ActionNodeletAdder(assistant));
        parser.setXpath(xpath + "/thrown");
        parser.addNodelet(attrs -> {
            String exceptionType = attrs.get("type");

            ExceptionThrownRule etr = new ExceptionThrownRule();
            if (exceptionType != null) {
                String[] exceptionTypes = StringUtils.splitCommaDelimitedString(exceptionType);
                etr.setExceptionTypes(exceptionTypes);
            }

            parser.pushObject(etr);
        });
        parser.addNodelet(new ActionNodeletAdder(assistant));
        parser.addNodelet(new ResponseInnerNodeletAdder(assistant));
        parser.addNodeEndlet(text -> {
            ExceptionThrownRule etr = parser.popObject();
            ExceptionRule exceptionRule = parser.peekObject();
            exceptionRule.putExceptionThrownRule(etr);
        });
    }

}
