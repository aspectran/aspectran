/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.builder.xml;

import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class TemplateNodeletAdder.
 * 
 * <p>Created: 2016. 01. 09</p>
 */
public class TemplateNodeletAdder implements NodeletAdder {

	protected ContextBuilderAssistant assistant;

	/**
	 * Instantiates a new TemplateNodeletAdder.
	 *
	 * @param assistant the assistant
	 */
	public TemplateNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.util.xml.NodeletAdder#process(java.lang.String, com.aspectran.core.util.xml.NodeletParser)
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/template", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = attributes.get("id");
				String engine = attributes.get("engine");
				String resource = attributes.get("resource");
				String file = attributes.get("file");
				String url = attributes.get("url");
				String encoding = attributes.get("encoding");
				Boolean noCache = BooleanUtils.toNullableBooleanObject(attributes.get("noCache"));

				TemplateRule templateRule = TemplateRule.newInstance(id, engine, file, resource, url, text, encoding, noCache);
				assistant.pushObject(templateRule);

				if(!StringUtils.isEmpty(engine)) {
					assistant.putBeanReference(engine, templateRule);
				}
			}
		});
		parser.addNodelet(xpath, "/template/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				TemplateRule templateRule = assistant.popObject();
				assistant.addTemplateRule(templateRule);
			}
		});
	}

}