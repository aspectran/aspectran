/*
 * Copyright 2008-2017 Juho Jeong
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

import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.context.builder.assistant.ContextBuilderAssistant;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class BeanInnerNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class BeanNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new BeanNodeletAdder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	BeanNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/bean", (node, attributes, text) -> {
            String id = StringUtils.emptyToNull(attributes.get("id"));
            String className = StringUtils.emptyToNull(assistant.resolveAliasType(attributes.get("class")));
            String factoryBean = StringUtils.emptyToNull(attributes.get("factoryBean"));
			String factoryMethod = StringUtils.emptyToNull(attributes.get("factoryMethod"));
            String scan = attributes.get("scan");
            String mask = attributes.get("mask");
            String initMethod = StringUtils.emptyToNull(attributes.get("initMethod"));
			String destroyMethod = StringUtils.emptyToNull(attributes.get("destroyMethod"));
			String scope = attributes.get("scope");
            Boolean singleton = BooleanUtils.toNullableBooleanObject(attributes.get("singleton"));
            Boolean lazyInit = BooleanUtils.toNullableBooleanObject(attributes.get("lazyInit"));
            Boolean important = BooleanUtils.toNullableBooleanObject(attributes.get("important"));

            BeanRule beanRule;

            if (className == null && scan == null && factoryBean != null) {
                beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod, initMethod, destroyMethod, scope, singleton, lazyInit, important);
				assistant.resolveFactoryBeanClass(factoryBean, beanRule);
            } else {
                beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, destroyMethod, factoryMethod, scope, singleton, lazyInit, important);
            }

            assistant.pushObject(beanRule);
        });
		parser.addNodelet(xpath, "/bean/description", (node, attributes, text) -> {
            if (text != null) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setDescription(text);
            }
        });
		parser.addNodelet(xpath, "/bean/filter", (node, attributes, text) -> {
            String classScanFilterClassName = attributes.get("class");
            Parameters filterParameters = null;

            if (StringUtils.hasText(text)) {
                filterParameters = new FilterParameters(text);
            }
            if (StringUtils.hasText(classScanFilterClassName)) {
                if (filterParameters == null)
                    filterParameters = new FilterParameters();
                filterParameters.putValue(FilterParameters.filterClass, classScanFilterClassName);
            }
            if (filterParameters != null) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setFilterParameters(filterParameters);
            }
        });
		parser.addNodelet(xpath, "/bean/constructor/arguments", (node, attributes, text) -> {
            if (StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                BeanRule.updateConstructorArgument(beanRule, text);
            }

            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
		parser.addNodelet(xpath, "/bean/constructor/arguments", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/bean/constructor/arguments/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if (!irm.isEmpty()) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setConstructorArgumentItemRuleMap(irm);
            }
        });
		parser.addNodelet(xpath, "/bean/properties", (node, attributes, text) -> {
            if (StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                BeanRule.updateProperty(beanRule, text);
            }

            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
		parser.addNodelet(xpath, "/bean/properties", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/bean/properties/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if (!irm.isEmpty()) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setPropertyItemRuleMap(irm);
            }
        });
		parser.addNodelet(xpath, "/bean/end()", (node, attributes, text) -> {
            BeanRule beanRule = assistant.popObject();
            assistant.addBeanRule(beanRule);
        });
	}

}