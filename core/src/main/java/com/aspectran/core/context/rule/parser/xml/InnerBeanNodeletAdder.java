/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.core.context.rule.ItemEntry;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * A {@code NodeletAdder} for parsing a nested {@code <bean>} element, which defines
 * an inner bean.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class InnerBeanNodeletAdder implements NodeletAdder {

    private static volatile InnerBeanNodeletAdder INSTANCE;

    static InnerBeanNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (InnerBeanNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InnerBeanNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child(InnerBeanNodeletGroup.instance().getName())
            .nodelet(attrs -> {
                String className = StringUtils.emptyToNull(AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveAliasType(attrs.get("class")));
                String factoryBean = StringUtils.emptyToNull(attrs.get("factoryBean"));
                String factoryMethod = StringUtils.emptyToNull(attrs.get("factoryMethod"));
                String initMethod = StringUtils.emptyToNull(attrs.get("initMethod"));
                String destroyMethod = StringUtils.emptyToNull(attrs.get("destroyMethod"));

                BeanRule beanRule;
                if (className == null && factoryBean != null) {
                    beanRule = BeanRule.newInnerByFactoryMethod(factoryBean, factoryMethod, initMethod, destroyMethod);
                } else {
                    beanRule = BeanRule.newInnerInstance(className, initMethod, destroyMethod, factoryMethod);
                }

                AspectranNodeParsingContext.pushObject(beanRule);
            })
            .with(DiscriptionNodeletAdder.instance())
            .with(ArgumentNodeletAdder.instance())
            .with(ArgumentsNodeletAdder.instance())
            .with(PropertyNodeletAdder.instance())
            .with(PropertiesNodeletAdder.instance())
            .endNodelet(text -> {
                BeanRule beanRule = AspectranNodeParsingContext.popObject();
                AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveBeanClass(beanRule);
                AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveFactoryBeanClass(beanRule);
                AspectranNodeParsingContext.getCurrentRuleParsingContext().addInnerBeanRule(beanRule);

                Object object = AspectranNodeParsingContext.peekObject();
                if (object instanceof ItemRule itemRule) {
                    if (itemRule.isListableType()) {
                        itemRule.addBeanRule(beanRule);
                    } else if (itemRule.isMappableType()) {
                        String name = AspectranNodeParsingContext.peekObject();
                        itemRule.putBeanRule(name, beanRule);
                    } else {
                        itemRule.setBeanRule(beanRule);
                    }
                } else if (object instanceof ItemEntry itemEntry) {
                    ItemRule itemRule = itemEntry.getItemRule();
                    if (itemRule.getType() == null || itemRule.isMappableType()) {
                        itemRule.putBeanRule(itemEntry.getName(), beanRule);
                    }
                }
            });
    }

}
