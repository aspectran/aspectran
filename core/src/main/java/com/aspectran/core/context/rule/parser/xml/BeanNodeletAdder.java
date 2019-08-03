/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.TextStyler;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class BeanInnerNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class BeanNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

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
                beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod,
                        initMethod, destroyMethod, scope, singleton, lazyInit, important);
            } else {
                beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, destroyMethod,
                        factoryMethod, scope, singleton, lazyInit, important);
            }

            parser.pushObject(beanRule);
        });
        parser.addNodeEndlet(text -> {
            BeanRule beanRule = parser.popObject();
            assistant.resolveBeanClass(beanRule);
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
                text = TextStyler.styling(text, style);
            }
            if (StringUtils.hasText(text)) {
                BeanRule beanRule = parser.peekObject();
                beanRule.setDescription(text);
            }
        });
        parser.setXpath(xpath + "/bean/filter");
        parser.addNodelet(attrs -> {
            String classScanFilterClassName = attrs.get("class");
            FilterParameters filterParameters = new FilterParameters();
            if (StringUtils.hasText(classScanFilterClassName)) {
                filterParameters.putValue(FilterParameters.filterClass, classScanFilterClassName);
            }
            parser.pushObject(filterParameters);
        });
        parser.addNodeEndlet(text -> {
            FilterParameters filterParameters = parser.popObject();
            if (StringUtils.hasText(text)) {
                filterParameters = new FilterParameters();
                filterParameters.readFrom(text);
            }
            if (filterParameters.hasValue(FilterParameters.filterClass) ||
                    filterParameters.hasValue(FilterParameters.exclude)) {
                BeanRule beanRule = parser.peekObject();
                beanRule.setFilterParameters(filterParameters);
            }
        });
        parser.setXpath(xpath + "/bean/arguments");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            BeanRule beanRule = parser.peekObject();
            irm = assistant.profiling(irm, beanRule.getConstructorArgumentItemRuleMap());
            beanRule.setConstructorArgumentItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/bean/properties");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            BeanRule beanRule = parser.peekObject();
            irm = assistant.profiling(irm, beanRule.getPropertyItemRuleMap());
            beanRule.setPropertyItemRuleMap(irm);
        });
    }

}
