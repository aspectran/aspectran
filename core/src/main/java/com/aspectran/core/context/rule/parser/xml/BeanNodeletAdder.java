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

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class BeanInnerNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class BeanNodeletAdder implements NodeletAdder {

    protected final ContextRuleAssistant assistant;

    /**
     * Instantiates a new BeanNodeletAdder.
     *
     * @param assistant the assistant for Context Builder
     */
    BeanNodeletAdder(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void process(String xpath, NodeletParser parser) {
        parser.setXpath(xpath + "/bean");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            String className = StringUtils.emptyToNull(assistant.resolveAliasType(attrs.get("class")));
            String factoryBean = StringUtils.emptyToNull(attrs.get("factoryBean"));
            String factoryMethod = StringUtils.emptyToNull(attrs.get("factoryMethod"));
            String scan = attrs.get("scan");
            String mask = attrs.get("mask");
            String initMethod = StringUtils.emptyToNull(attrs.get("initMethod"));
            String destroyMethod = StringUtils.emptyToNull(attrs.get("destroyMethod"));
            String scope = attrs.get("scope");
            Boolean singleton = BooleanUtils.toNullableBooleanObject(attrs.get("singleton"));
            Boolean lazyInit = BooleanUtils.toNullableBooleanObject(attrs.get("lazyInit"));
            Boolean important = BooleanUtils.toNullableBooleanObject(attrs.get("important"));

            BeanRule beanRule;
            if (className == null && scan == null && factoryBean != null) {
                beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod, initMethod, destroyMethod, scope, singleton, lazyInit, important);
            } else {
                beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, destroyMethod, factoryMethod, scope, singleton, lazyInit, important);
            }

            parser.pushObject(beanRule);
        });
        parser.addNodeEndlet(text -> {
            BeanRule beanRule = parser.popObject();
            assistant.resolveFactoryBeanClass(beanRule);
            assistant.addBeanRule(beanRule);
        });
        parser.setXpath(xpath + "/bean/description");
        parser.addNodelet(attrs -> {
            String style = attrs.get("style");
            parser.pushObject(style);
        });
        parser.addNodeEndlet(text -> {
            String style = parser.popObject();
            if (text != null) {
                text = ContentStyleType.styling(text, style);
            }
            if (StringUtils.hasText(text)) {
                BeanRule beanRule = parser.peekObject();
                beanRule.setDescription(text);
            }
        });
        parser.setXpath(xpath + "/bean/filter");
        parser.addNodelet(attrs -> {
            String classScanFilterClassName = attrs.get("class");
            Parameters filterParameters = new FilterParameters();
            if (StringUtils.hasText(classScanFilterClassName)) {
                filterParameters.putValue(FilterParameters.filterClass, classScanFilterClassName);
            }
            parser.pushObject(filterParameters);
        });
        parser.addNodeEndlet(text -> {
            Parameters filterParameters = parser.popObject();
            if (StringUtils.hasText(text)) {
                filterParameters = new FilterParameters();
                filterParameters.readFrom(text);
            }
            if (filterParameters.isValueAssigned(FilterParameters.filterClass) &&
                    filterParameters.isValueAssigned(FilterParameters.exclude)) {
                BeanRule beanRule = parser.peekObject();
                beanRule.setFilterParameters(filterParameters);
            }
        });
        parser.setXpath(xpath + "/bean/constructor/arguments");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            parser.pushObject(irm);
        });
        parser.addNodelet(new ItemNodeletAdder(assistant));
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            if (!irm.isEmpty()) {
                BeanRule beanRule = parser.peekObject();
                beanRule.setConstructorArgumentItemRuleMap(irm);
            } else if (StringUtils.hasText(text)) {
                BeanRule beanRule = parser.peekObject();
                BeanRule.updateConstructorArgument(beanRule, text);
            }
        });
        parser.setXpath(xpath + "/bean/properties");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            parser.pushObject(irm);
        });
        parser.addNodelet(new ItemNodeletAdder(assistant));
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            if (!irm.isEmpty()) {
                BeanRule beanRule = parser.peekObject();
                beanRule.setPropertyItemRuleMap(irm);
            } else if (StringUtils.hasText(text)) {
                BeanRule beanRule = parser.peekObject();
                BeanRule.updateProperty(beanRule, text);
            }
        });
    }

}