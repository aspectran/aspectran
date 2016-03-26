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

import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.xml.Nodelet;
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
            String offerBean = StringUtils.emptyToNull(attributes.get("offerBean"));
            String offerMethod = StringUtils.emptyToNull(attributes.get("offerMethod"));
            String scan = attributes.get("scan");
            String mask = attributes.get("mask");
            String scope = attributes.get("scope");
            Boolean singleton = BooleanUtils.toNullableBooleanObject(attributes.get("singleton"));
            String initMethod = StringUtils.emptyToNull(attributes.get("initMethod"));
			String factoryMethod = StringUtils.emptyToNull(attributes.get("factoryMethod"));
			String destroyMethod = StringUtils.emptyToNull(attributes.get("destroyMethod"));
            Boolean lazyInit = BooleanUtils.toNullableBooleanObject(attributes.get("lazyInit"));
            Boolean important = BooleanUtils.toNullableBooleanObject(attributes.get("important"));

            BeanRule beanRule;

            if(className == null && scan == null && offerBean != null) {
                if(offerMethod == null)
                    throw new IllegalArgumentException("The <bean> element requires an offerMethod attribute.");

                beanRule = BeanRule.newOfferedBeanInstance(id, offerBean, offerMethod, initMethod, factoryMethod, destroyMethod, scope, singleton, lazyInit, important);

                if(offerBean != null) {
                    assistant.resolveBeanClass(offerBean, beanRule);
                }
            } else {
                if(className == null && scan == null)
                    throw new IllegalArgumentException("The <bean> element requires a class attribute.");

                beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, factoryMethod, destroyMethod, scope, singleton, lazyInit, important);
            }

            assistant.pushObject(beanRule);
        });
		parser.addNodelet(xpath, "/bean/description", (node, attributes, text) -> {
            if(text != null) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setDescription(text);
            }
        });
		parser.addNodelet(xpath, "/bean/filter", (node, attributes, text) -> {
            String classScanFilterClassName = attributes.get("class");
            Parameters filterParameters = null;

            if(StringUtils.hasText(text)) {
                filterParameters = new FilterParameters(text);
            }
            if(StringUtils.hasText(classScanFilterClassName)) {
                if(filterParameters == null)
                    filterParameters = new FilterParameters();
                filterParameters.putValue(FilterParameters.filterClass, classScanFilterClassName);
            }
            if(filterParameters != null) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setFilterParameters(filterParameters);
            }
        });
		parser.addNodelet(xpath, "/bean/features/class", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setClassName(text.trim());
            }
        });
		parser.addNodelet(xpath, "/bean/features/scope", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                ScopeType scopeType = ScopeType.lookup(text.trim());
                if(scopeType == null)
                    throw new IllegalArgumentException("No scope-type registered for scope '" + text + "'.");

                BeanRule beanRule = assistant.peekObject();
                beanRule.setScopeType(scopeType);
                if(scopeType == ScopeType.SINGLETON)
                    beanRule.setSingleton(Boolean.TRUE);
            }
        });
		parser.addNodelet(xpath, "/bean/features/offerBean", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setOfferBeanId(text.trim());
            }
        });
		parser.addNodelet(xpath, "/bean/features/offerMethod", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(StringUtils.hasText(text)) {
					BeanRule beanRule = assistant.peekObject();
					beanRule.setOfferMethodName(text.trim());
				}
			}
		});
		parser.addNodelet(xpath, "/bean/features/initMethod", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setInitMethodName(text.trim());
            }
        });
		parser.addNodelet(xpath, "/bean/features/factoryMethod", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setFactoryMethodName(text.trim());
            }
        });
		parser.addNodelet(xpath, "/bean/features/destroyMethod", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setDestroyMethodName(text.trim());
            }
        });
		parser.addNodelet(xpath, "/bean/features/lazyInit", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                Boolean lazyInit = BooleanUtils.toBooleanObject(text.trim());
                BeanRule beanRule = assistant.peekObject();
                beanRule.setLazyInit(lazyInit);
            }
        });
		parser.addNodelet(xpath, "/bean/features/important", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                Boolean important = BooleanUtils.toBooleanObject(text.trim());
                BeanRule beanRule = assistant.peekObject();
                beanRule.setImportant(important);
            }
        });
		parser.addNodelet(xpath, "/bean/constructor/argument", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                BeanRule.updateConstructorArgument(beanRule, text);
            }

            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
		parser.addNodelet(xpath, "/bean/constructor/argument", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/bean/constructor/argument/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if(!irm.isEmpty()) {
                BeanRule beanRule = assistant.peekObject();
                beanRule.setConstructorArgumentItemRuleMap(irm);
            }
        });
		parser.addNodelet(xpath, "/bean/property", (node, attributes, text) -> {
            if(StringUtils.hasText(text)) {
                BeanRule beanRule = assistant.peekObject();
                BeanRule.updateProperty(beanRule, text);
            }

            ItemRuleMap irm = new ItemRuleMap();
            assistant.pushObject(irm);
        });
		parser.addNodelet(xpath, "/bean/property", new ItemNodeletAdder(assistant));
		parser.addNodelet(xpath, "/bean/property/end()", (node, attributes, text) -> {
            ItemRuleMap irm = assistant.popObject();

            if(!irm.isEmpty()) {
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