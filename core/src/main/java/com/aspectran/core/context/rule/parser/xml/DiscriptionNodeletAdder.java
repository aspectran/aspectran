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
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class DiscriptionNodeletAdder  implements NodeletAdder {

    private static volatile DiscriptionNodeletAdder INSTANCE;

    static DiscriptionNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (DiscriptionNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DiscriptionNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("description")
            .nodelet(attrs -> {
                String profile = attrs.get("profile");
                String style = attrs.get("style");

                DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                AspectranNodeParsingContext.pushObject(descriptionRule);
            })
            .endNodelet(text -> {
                DescriptionRule descriptionRule = AspectranNodeParsingContext.popObject();
                descriptionRule.setContent(text);

                Describable describable;
                if ("/aspectran".equals(group.getXpath())) {
                    describable = AspectranNodeParsingContext.getCurrentRuleParsingContext().getRuleParsingScope();
                } else {
                    describable = AspectranNodeParsingContext.peekObject();
                }

                DescriptionRule oldDescriptionRule = describable.getDescriptionRule();
                descriptionRule = AspectranNodeParsingContext.getCurrentRuleParsingContext().profiling(descriptionRule, oldDescriptionRule);
                describable.setDescriptionRule(descriptionRule);
            });
    }

}
