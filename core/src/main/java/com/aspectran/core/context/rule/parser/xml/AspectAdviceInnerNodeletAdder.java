/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectAdviceInnerNodeletAdder.
 *
 * @since 2013. 8. 11.
 */
class AspectAdviceInnerNodeletAdder implements NodeletAdder {

    protected final ContextRuleAssistant assistant;

    /**
     * Instantiates a new AspectAdviceNodeletAdder.
     *
     * @param assistant the assistant for Context Builder
     */
    AspectAdviceInnerNodeletAdder(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void process(String xpath, NodeletParser parser) {
        parser.addNodelet(xpath, "/before", (node, attributes, text) -> {
            AspectRule aspectRule = assistant.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.BEFORE);
            assistant.pushObject(aspectAdviceRule);
        });
        parser.addNodelet(xpath, "/before", new ActionNodeletAdder(assistant));
        parser.addNodelet(xpath, "/before/end()", (node, attributes, text) -> assistant.popObject());
        parser.addNodelet(xpath, "/after", (node, attributes, text) -> {
            AspectRule aspectRule = assistant.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AFTER);
            assistant.pushObject(aspectAdviceRule);
        });
        parser.addNodelet(xpath, "/after", new ActionNodeletAdder(assistant));
        parser.addNodelet(xpath, "/after/end()", (node, attributes, text) -> assistant.popObject());
        parser.addNodelet(xpath, "/around", (node, attributes, text) -> {
            AspectRule aspectRule = assistant.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AROUND);
            assistant.pushObject(aspectAdviceRule);
        });
        parser.addNodelet(xpath, "/around", new ActionNodeletAdder(assistant));
        parser.addNodelet(xpath, "/around/end()", (node, attributes, text) -> assistant.popObject());
        parser.addNodelet(xpath, "/finally", (node, attributes, text) -> {
            AspectRule aspectRule = assistant.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.FINALLY);
            assistant.pushObject(aspectAdviceRule);
        });
        parser.addNodelet(xpath, "/finally/thrown", (node, attributes, text) -> {
            String exceptionType = attributes.get("type");

            AspectAdviceRule aspectAdviceRule = assistant.peekObject();
            ExceptionThrownRule etr = new ExceptionThrownRule(aspectAdviceRule);
            if (exceptionType != null) {
                String[] exceptionTypes = StringUtils.splitCommaDelimitedString(exceptionType);
                etr.setExceptionTypes(exceptionTypes);
            }
            assistant.pushObject(etr);
        });
        parser.addNodelet(xpath, "/finally/thrown", new ActionNodeletAdder(assistant));
        parser.addNodelet(xpath, "/finally/thrown", new ResponseInnerNodeletAdder(assistant));
        parser.addNodelet(xpath, "/finally/thrown/end()", (node, attributes, text) -> {
            ExceptionThrownRule etr = assistant.popObject();
            AspectAdviceRule aspectAdviceRule = assistant.peekObject();
            aspectAdviceRule.setExceptionThrownRule(etr);
        });
        parser.addNodelet(xpath, "/finally", new ActionNodeletAdder(assistant));
        parser.addNodelet(xpath, "/finally/end()", (node, attributes, text) -> assistant.popObject());
    }

}
