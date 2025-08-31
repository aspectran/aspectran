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

import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2016. 01. 09</p>
 */
class EnvironmentNodeletAdder implements NodeletAdder {

    private static final EnvironmentNodeletAdder INSTANCE = new EnvironmentNodeletAdder();

    static EnvironmentNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("environment")
            .nodelet(attrs -> {
                String profile = attrs.get("profile");

                EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile);
                AspectranNodeParsingContext.pushObject(environmentRule);
            })
            .with(DiscriptionNodeletAdder.instance())
            .endNodelet(text -> {
                EnvironmentRule environmentRule = AspectranNodeParsingContext.popObject();
                AspectranNodeParsingContext.assistant().addEnvironmentRule(environmentRule);
            })
            .child("properties")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParsingContext.pushObject(irm);
                })
                .mount(ItemNodeletGroup.instance())
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                    EnvironmentRule environmentRule = AspectranNodeParsingContext.peekObject();
                    environmentRule.addPropertyItemRuleMap(irm);
                });
    }

}
