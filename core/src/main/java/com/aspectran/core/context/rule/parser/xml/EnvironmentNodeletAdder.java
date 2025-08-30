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

import com.aspectran.core.context.rule.DescriptionRule;
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

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("environment")
            .nodelet(attrs -> {
                String profile = attrs.get("profile");

                EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile);
                AspectranNodeParser.current().pushObject(environmentRule);
            })
            .endNodelet(text -> {
            EnvironmentRule environmentRule = AspectranNodeParser.current().popObject();
            AspectranNodeParser.current().getAssistant().addEnvironmentRule(environmentRule);
        }).child("description")
            .nodelet(attrs -> {
                String profile = attrs.get("profile");
                String style = attrs.get("style");

                DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                AspectranNodeParser.current().pushObject(descriptionRule);
            })
            .endNodelet(text -> {
                DescriptionRule descriptionRule = AspectranNodeParser.current().popObject();
                EnvironmentRule environmentRule = AspectranNodeParser.current().peekObject();

                descriptionRule.setContent(text);
                descriptionRule = AspectranNodeParser.current().getAssistant().profiling(descriptionRule, environmentRule.getDescriptionRule());
                environmentRule.setDescriptionRule(descriptionRule);
            })
        .parent().child("properties")
            .nodelet(attrs -> {
                ItemRuleMap irm = new ItemRuleMap();
                irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                AspectranNodeParser.current().pushObject(irm);
            })
            .with(AspectranNodeletGroup.itemNodeletAdder)
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParser.current().popObject();
                EnvironmentRule environmentRule = AspectranNodeParser.current().peekObject();
                environmentRule.addPropertyItemRuleMap(irm);
            });
    }

}
