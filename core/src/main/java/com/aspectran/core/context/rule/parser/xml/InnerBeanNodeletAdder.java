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
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

import static com.aspectran.core.context.rule.parser.xml.AspectranNodeletGroup.MAX_INNER_BEAN_DEPTH;

/**
 * The Class InnerBeanNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class InnerBeanNodeletAdder implements NodeletAdder {

    private final int depth;

    InnerBeanNodeletAdder(int depth) {
        this.depth = depth;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("bean")
            .nodelet(attrs -> {
                if (depth >= MAX_INNER_BEAN_DEPTH) {
                    StringBuilder sb = new StringBuilder("Inner beans can be nested up to ");
                    if (MAX_INNER_BEAN_DEPTH > 1) {
                        sb.append(MAX_INNER_BEAN_DEPTH);
                        sb.append(" times");
                    } else {
                        sb.append("at most once");
                    }
                    throw new IllegalRuleException(sb.toString());
                }

                String className = StringUtils.emptyToNull(AspectranNodeParser.current().getAssistant().resolveAliasType(attrs.get("class")));
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

                AspectranNodeParser.current().pushObject(beanRule);
            })
            .endNodelet(text -> {
                BeanRule beanRule = AspectranNodeParser.current().popObject();
                AspectranNodeParser.current().getAssistant().resolveBeanClass(beanRule);
                AspectranNodeParser.current().getAssistant().resolveFactoryBeanClass(beanRule);
                AspectranNodeParser.current().getAssistant().addInnerBeanRule(beanRule);

                ItemRule itemRule = AspectranNodeParser.current().peekObject();
                if (itemRule.isListableType()) {
                    itemRule.addBeanRule(beanRule);
                } else if (itemRule.isMappableType()) {
                    String name = AspectranNodeParser.current().peekObject();
                    itemRule.putBeanRule(name, beanRule);
                } else {
                    itemRule.setBeanRule(beanRule);
                }
            })
            .child("description")
                .nodelet(attrs -> {
                    String profile = attrs.get("profile");
                    String style = attrs.get("style");

                    DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                    AspectranNodeParser.current().pushObject(descriptionRule);
                })
                .endNodelet(text -> {
                    DescriptionRule descriptionRule = AspectranNodeParser.current().popObject();
                    BeanRule beanRule = AspectranNodeParser.current().peekObject();

                    descriptionRule.setContent(text);
                    descriptionRule = AspectranNodeParser.current().getAssistant().profiling(descriptionRule, beanRule.getDescriptionRule());
                    beanRule.setDescriptionRule(descriptionRule);
                })
            .parent().child("arguments")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdders[depth])
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    BeanRule beanRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, beanRule.getConstructorArgumentItemRuleMap());
                    beanRule.setConstructorArgumentItemRuleMap(irm);
                })
            .parent().child("properties")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .with(AspectranNodeletGroup.itemNodeletAdders[depth])
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    BeanRule beanRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, beanRule.getPropertyItemRuleMap());
                    beanRule.setPropertyItemRuleMap(irm);
                });
    }

}
