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

import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.HasProperties;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class PropertiesNodeletAdder implements NodeletAdder {

    private static volatile PropertiesNodeletAdder INSTANCE;

    static PropertiesNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (PropertiesNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PropertiesNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("properties")
            .nodelet(attrs -> {
                ItemRuleMap irm = new ItemRuleMap();
                irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                AspectranNodeParsingContext.pushObject(irm);
            })
            .with(ItemNodeletAdder.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                HasProperties hasProperties = AspectranNodeParsingContext.peekObject();
                irm = AspectranNodeParsingContext.assistant().profiling(irm, hasProperties.getPropertyItemRuleMap());
                hasProperties.setPropertyItemRuleMap(irm);
            });
    }

}
