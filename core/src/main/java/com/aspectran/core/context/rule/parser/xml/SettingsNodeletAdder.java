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

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * A {@code NodeletAdder} for parsing the {@code <settings>} element, which is used
 * to configure default framework settings.
 *
 * <p>Created: 2016. 2. 13.</p>
 */
class SettingsNodeletAdder implements NodeletAdder {

    private static volatile SettingsNodeletAdder INSTANCE;

    static SettingsNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (SettingsNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingsNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("settings")
            .endNodelet(text -> {
                AspectranNodeParsingContext.getCurrentRuleParsingContext().applySettings();
            })
            .child("setting")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String value = attrs.get("value");

                    AspectranNodeParsingContext.pushObject(value);
                    AspectranNodeParsingContext.pushObject(name);
                })
                .endNodelet(text -> {
                    String name = AspectranNodeParsingContext.popObject();
                    String value = AspectranNodeParsingContext.popObject();

                    if (value != null) {
                        AspectranNodeParsingContext.getCurrentRuleParsingContext().putSetting(name, value);
                    } else if (text != null) {
                        AspectranNodeParsingContext.getCurrentRuleParsingContext().putSetting(name, text);
                    }
                });
    }

}
