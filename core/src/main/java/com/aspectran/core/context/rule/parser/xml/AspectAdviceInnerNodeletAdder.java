/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class AspectAdviceInnerNodeletAdder.
 *
 * @since 2013. 8. 11.
 */
class AspectAdviceInnerNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();

        parser.setXpath(xpath + "/before");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.BEFORE);
            parser.pushObject(aspectAdviceRule);
        });
        nodeParser.addActionNodelets();
        parser.addNodeEndlet(text -> parser.popObject());
        parser.setXpath(xpath + "/after");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AFTER);
            parser.pushObject(aspectAdviceRule);
        });
        nodeParser.addActionNodelets();
        parser.addNodeEndlet(text -> parser.popObject());
        parser.setXpath(xpath + "/around");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AROUND);
            parser.pushObject(aspectAdviceRule);
        });
        nodeParser.addActionNodelets();
        parser.addNodeEndlet(text -> parser.popObject());
        parser.setXpath(xpath + "/finally");
        parser.addNodelet(attrs -> {
            AspectRule aspectRule = parser.peekObject();
            AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.FINALLY);
            parser.pushObject(aspectAdviceRule);
        });
        nodeParser.addActionNodelets();
        parser.addNodeEndlet(text -> parser.popObject());
        parser.setXpath(xpath + "/finally/thrown");
        parser.addNodelet(attrs -> {
            String exceptionType = attrs.get("type");

            AspectAdviceRule aspectAdviceRule = parser.peekObject();
            ExceptionThrownRule etr = new ExceptionThrownRule(aspectAdviceRule);
            if (exceptionType != null) {
                String[] exceptionTypes = StringUtils.splitCommaDelimitedString(exceptionType);
                etr.setExceptionTypes(exceptionTypes);
            }
            parser.pushObject(etr);
        });
        nodeParser.addActionNodelets();
        nodeParser.addResponseInnerNodelets();
        parser.addNodeEndlet(text -> {
            ExceptionThrownRule etr = parser.popObject();
            AspectAdviceRule aspectAdviceRule = parser.peekObject();
            aspectAdviceRule.setExceptionThrownRule(etr);
        });
    }

}
