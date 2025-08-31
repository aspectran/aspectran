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
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class InnerBeanNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class InnerBeanNodeletGroup extends NodeletGroup {

    private static final InnerBeanNodeletGroup INSTANCE = new InnerBeanNodeletGroup();

    static InnerBeanNodeletGroup instance() {
        return INSTANCE;
    }

    InnerBeanNodeletGroup() {
        super("bean");
        nodelet(attrs -> {
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
        .with(DiscriptionNodeletAdder.instance())
        .with(ArgumentsNodeletAdder.instance())
        .with(PropertiesNodeletAdder.instance())
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
        });
    }

}
