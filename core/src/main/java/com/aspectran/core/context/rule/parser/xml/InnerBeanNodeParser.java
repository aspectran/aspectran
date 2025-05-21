/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletParser;
import com.aspectran.utils.nodelet.SubnodeParser;

/**
 * The Class InnerBeanNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class InnerBeanNodeParser implements SubnodeParser {

    private final int depth;

    InnerBeanNodeParser(int depth) {
        this.depth = depth;
    }

    @Override
    public void parse(@NonNull String xpath, @NonNull NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/bean");
        parser.addNodelet(attrs -> {
            String className = StringUtils.emptyToNull(assistant.resolveAliasType(attrs.get("class")));
            String factoryBean = StringUtils.emptyToNull(attrs.get("factoryBean"));
            String factoryMethod = StringUtils.emptyToNull(attrs.get("factoryMethod"));
            String initMethod = StringUtils.emptyToNull(attrs.get("initMethod"));
            String destroyMethod = StringUtils.emptyToNull(attrs.get("destroyMethod"));

            BeanRule beanRule;
            if (className == null && factoryBean != null) {
                beanRule = BeanRule.newInnerOfferedFactoryBeanRule(factoryBean, factoryMethod, initMethod, destroyMethod);
            } else {
                beanRule = BeanRule.newInnerBeanRule(className, initMethod, destroyMethod, factoryMethod);
            }

            parser.pushObject(beanRule);
        });
        parser.addEndNodelet(text -> {
            BeanRule beanRule = parser.popObject();
            assistant.resolveBeanClass(beanRule);
            assistant.resolveFactoryBeanClass(beanRule);
            assistant.addInnerBeanRule(beanRule);

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
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addEndNodelet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            BeanRule beanRule = parser.peekObject();

            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, beanRule.getDescriptionRule());
            beanRule.setDescriptionRule(descriptionRule);
        });
        parser.setXpath(xpath + "/bean/arguments");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.parseItemNode(depth);
        parser.addEndNodelet(text -> {
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
        nodeParser.parseItemNode(depth);
        parser.addEndNodelet(text -> {
            ItemRuleMap irm = parser.popObject();
            BeanRule beanRule = parser.peekObject();
            irm = assistant.profiling(irm, beanRule.getPropertyItemRuleMap());
            beanRule.setPropertyItemRuleMap(irm);
        });
    }

}
