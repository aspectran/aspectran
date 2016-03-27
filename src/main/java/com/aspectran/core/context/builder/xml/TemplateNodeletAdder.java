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

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class TemplateNodeletAdder.
 * 
 * <p>Created: 2016. 01. 09</p>
 */
class TemplateNodeletAdder implements NodeletAdder {

	protected final ContextBuilderAssistant assistant;

	/**
	 * Instantiates a new TemplateNodeletAdder.
	 *
	 * @param assistant the assistant
	 */
	TemplateNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/template", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            String engine = StringUtils.emptyToNull(attributes.get("engine"));
            String name = StringUtils.emptyToNull(attributes.get("name"));
            String file = StringUtils.emptyToNull(attributes.get("file"));
            String resource = StringUtils.emptyToNull(attributes.get("resource"));
            String url = StringUtils.emptyToNull(attributes.get("url"));
            String encoding = attributes.get("encoding");
            Boolean noCache = BooleanUtils.toNullableBooleanObject(attributes.get("noCache"));

			if(id == null)
				throw new IllegalArgumentException("The <template> element requires an id attribute.");

			TemplateRule templateRule = TemplateRule.newInstance(id, engine, name, file, resource, url, text, encoding, noCache);

            if(engine != null) {
                assistant.putBeanReference(engine, templateRule);
            }

            assistant.addTemplateRule(templateRule);
        });
	}

}