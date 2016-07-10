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
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectAdviceRuleNodeletAdder.
 *
 * @since 2013. 8. 11.
 */
class AspectAdviceNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	private AspectAdviceType aspectAdviceType;
	
	/**
	 * Instantiates a new AspectAdviceNodeletAdder.
	 *
	 * @param assistant the assistant for Context Builder
	 * @param aspectAdviceType the aspect advice type
	 */
	AspectAdviceNodeletAdder(ContextBuilderAssistant assistant, AspectAdviceType aspectAdviceType) {
		this.assistant = assistant;
		this.aspectAdviceType = aspectAdviceType;
	}

	@Override
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, (node, attributes, text) -> {
            AspectRule aspectRule = assistant.peekObject();
            AspectAdviceRule aar = AspectAdviceRule.newInstance(aspectRule, aspectAdviceType);
            assistant.pushObject(aar);
        });
		parser.addNodelet(xpath, new ActionInnerNodeletAdder(assistant));
		parser.addNodelet(xpath, "/end()", (node, attributes, text) -> {
            AspectAdviceRule aspectAdviceRule = assistant.popObject();
            AspectRule aspectRule = assistant.peekObject();
            aspectRule.addAspectAdviceRule(aspectAdviceRule);
        });
	}

}
