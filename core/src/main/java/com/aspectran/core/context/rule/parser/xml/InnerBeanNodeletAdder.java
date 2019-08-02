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
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.TextStyler;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class BeanInnerNodeletAdder.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class InnerBeanNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/bean");
        parser.addNodelet(attrs -> {
            String className = StringUtils.emptyToNull(assistant.resolveAliasType(attrs.get("class")));
            String factoryBean = StringUtils.emptyToNull(attrs.get("factoryBean"));
            String factoryMethod = StringUtils.emptyToNull(attrs.get("factoryMethod"));
            String initMethod = StringUtils.emptyToNull(attrs.get("initMethod"));
            String destroyMethod = StringUtils.emptyToNull(attrs.get("destroyMethod"));

            BeanRule beanRule;
            if (className == null && factoryBean != null) {
                beanRule = BeanRule.newOfferedFactoryBeanInstance(null, factoryBean, factoryMethod,
                        initMethod, destroyMethod, null, false, null, null);
            } else {
                beanRule = BeanRule.newInstance(null, className, null, null, initMethod, destroyMethod,
                        factoryMethod, null, false, null, null);
            }

            parser.pushObject(beanRule);
        });
        parser.addNodeEndlet(text -> {
            BeanRule beanRule = parser.popObject();
            assistant.resolveBeanClass(beanRule);
            assistant.resolveFactoryBeanClass(beanRule);

            ItemRule itemRule = parser.peekObject();
            if (itemRule.isListableType()) {
                itemRule.addBeanRule(beanRule);
            } else if (itemRule.isMappableType()) {
                String name = parser.peekObject();
                itemRule.putBeanRule(name, beanRule);
            } else {
                itemRule.setBeanRule(beanRule);
            }
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
        parser.setXpath(xpath + "/bean/constructor/arguments");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.addDeeplyItemNodelets();
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
        nodeParser.addDeeplyItemNodelets();
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            BeanRule beanRule = parser.peekObject();
            irm = assistant.profiling(irm, beanRule.getPropertyItemRuleMap());
            beanRule.setPropertyItemRuleMap(irm);
        });
    }

}
