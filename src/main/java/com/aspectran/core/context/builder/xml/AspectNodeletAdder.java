/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.builder.xml;

import com.aspectran.core.context.builder.assistant.ContextBuilderAssistant;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class AspectNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new AspectNodeletAdder.
	 *
	 * @param assistant the assistant
	 */
	AspectNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/aspect", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            String order = StringUtils.emptyToNull(attributes.get("order"));
            Boolean isolated = BooleanUtils.toNullableBooleanObject(attributes.get("isolated"));

            AspectRule aspectRule = AspectRule.newInstance(id, order, isolated);
            assistant.pushObject(aspectRule);
        });
		parser.addNodelet(xpath, "/aspect/description", (node, attributes, text) -> {
            if (text != null) {
                AspectRule aspectRule = assistant.peekObject();
                aspectRule.setDescription(text);
            }
        });
		parser.addNodelet(xpath, "/aspect/joinpoint", (node, attributes, text) -> {
            String type = StringUtils.emptyToNull(attributes.get("type"));

            AspectRule aspectRule = assistant.peekObject();
            AspectRule.updateJoinpoint(aspectRule, type, text);
        });
		parser.addNodelet(xpath, "/aspect/settings", (node, attributes, text) -> {
            AspectRule aspectRule = assistant.peekObject();

            SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule, text);
            assistant.pushObject(sar);
        });
		parser.addNodelet(xpath, "/aspect/settings/setting", (node, attributes, text) -> {
            String name = StringUtils.emptyToNull(attributes.get("name"));
            String value = attributes.get("value");

            if (name != null) {
                SettingsAdviceRule sar = assistant.peekObject();
                sar.putSetting(name, value);
            }
        });
		parser.addNodelet(xpath, "/aspect/settings/end()", (node, attributes, text) -> {
            SettingsAdviceRule sar = assistant.popObject();
            AspectRule aspectRule = assistant.peekObject();
            aspectRule.setSettingsAdviceRule(sar);
        });
		parser.addNodelet(xpath, "/aspect/advice", (node, attributes, text) -> {
            String beanIdOrClass = StringUtils.emptyToNull(attributes.get("bean"));

            if (beanIdOrClass != null) {
                AspectRule aspectRule = assistant.peekObject();
                aspectRule.setAdviceBeanId(beanIdOrClass);
                assistant.resolveBeanClass(beanIdOrClass, aspectRule);
            }
        });
		parser.addNodelet(xpath, "/aspect/advice", new AspectAdviceInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/aspect/exception", (node, attributes, text) -> {
			ExceptionRule exceptionRule = new ExceptionRule();
			assistant.pushObject(exceptionRule);
		});
		parser.addNodelet(xpath, "/aspect/exception", new ExceptionInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/aspect/exception/end()", (node, attributes, text) -> {
			ExceptionRule exceptionRule = assistant.popObject();
			AspectRule aspectRule = assistant.peekObject();
			aspectRule.setExceptionRule(exceptionRule);
		});
		parser.addNodelet(xpath, "/aspect/end()", (node, attributes, text) -> {
            AspectRule aspectRule = assistant.popObject();
            assistant.addAspectRule(aspectRule);
        });
	}

}