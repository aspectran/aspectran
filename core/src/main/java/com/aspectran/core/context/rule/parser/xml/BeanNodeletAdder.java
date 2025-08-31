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
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class BeanNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class BeanNodeletAdder implements NodeletAdder {

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("bean")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String className = StringUtils.emptyToNull(AspectranNodeParser.current().getAssistant().resolveAliasType(attrs.get("class")));
                String factoryBean = StringUtils.emptyToNull(attrs.get("factoryBean"));
                String factoryMethod = StringUtils.emptyToNull(attrs.get("factoryMethod"));
                String scan = attrs.get("scan");
                String mask = attrs.get("mask");
                String initMethod = StringUtils.emptyToNull(attrs.get("initMethod"));
                String destroyMethod = StringUtils.emptyToNull(attrs.get("destroyMethod"));
                String scope = attrs.get("scope");
                Boolean singleton = BooleanUtils.toNullableBooleanObject(attrs.get("singleton"));
                Boolean lazyInit = BooleanUtils.toNullableBooleanObject(attrs.get("lazyInit"));
                Boolean lazyDestroy = BooleanUtils.toNullableBooleanObject(attrs.get("lazyDestroy"));
                Boolean important = BooleanUtils.toNullableBooleanObject(attrs.get("important"));

                BeanRule beanRule;
                if (className == null && scan == null && factoryBean != null) {
                    beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod,
                            initMethod, destroyMethod, scope, singleton, lazyInit, lazyDestroy, important);
                } else {
                    beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, destroyMethod,
                            factoryMethod, scope, singleton, lazyInit, lazyDestroy, important);
                }

                AspectranNodeParser.current().pushObject(beanRule);
            })
            .endNodelet(text -> {
                BeanRule beanRule = AspectranNodeParser.current().popObject();
                AspectranNodeParser.current().getAssistant().resolveBeanClass(beanRule);
                AspectranNodeParser.current().getAssistant().resolveFactoryBeanClass(beanRule);
                AspectranNodeParser.current().getAssistant().addBeanRule(beanRule);
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
            .parent().child("filter")
                .nodelet(attrs -> {
                    String filterClassName = attrs.get("class");
                    FilterParameters filterParameters = new FilterParameters();
                    if (StringUtils.hasText(filterClassName)) {
                        filterParameters.putValue(FilterParameters.filterClass, filterClassName);
                    }
                    AspectranNodeParser.current().pushObject(filterParameters);
                })
                .endNodelet(text -> {
                    FilterParameters filterParameters = AspectranNodeParser.current().popObject();
                    if (StringUtils.hasText(text)) {
                        filterParameters = new FilterParameters();
                        filterParameters.readFrom(text);
                    }
                    if (filterParameters.hasFilterClass() || filterParameters.hasPatterns()) {
                        BeanRule beanRule = AspectranNodeParser.current().peekObject();
                        beanRule.setFilterParameters(filterParameters);
                    }
                })
            .parent().child("arguments")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
//            .with(AspectranNodeParser.current().itemNodeletAdder)
                .mount(AspectranNodeParser.current().getItemNodeletGroup())
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
//            .with(AspectranNodeParser.current().itemNodeletAdder)
                .mount(AspectranNodeParser.current().getItemNodeletGroup())
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    BeanRule beanRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, beanRule.getPropertyItemRuleMap());
                    beanRule.setPropertyItemRuleMap(irm);
                });
    }

}
