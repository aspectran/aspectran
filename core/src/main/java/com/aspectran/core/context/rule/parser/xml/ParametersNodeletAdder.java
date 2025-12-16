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
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.HasParameters;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing the {@code <parameters>} element, which serves as
 * a container for multiple {@code <item>} or {@code <parameter>} elements and supports
 * profile-based conditional inclusion.
 *
 * <p>Created: 2016. 2. 13.</p>
 */
class ParametersNodeletAdder implements NodeletAdder {

    private static volatile ParametersNodeletAdder INSTANCE;

    static ParametersNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (ParametersNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ParametersNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("parameters")
            .nodelet(attrs -> {
                ItemRuleMap irm = new ItemRuleMap();
                irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                AspectranNodeParsingContext.pushObject(irm);
            })
            .with(ItemNodeletAdder.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                Object object = AspectranNodeParsingContext.peekObject();
                if (object instanceof TransletRule transletRule) {
                    HasParameters hasParameters = transletRule.touchRequestRule(false);
                    irm = AspectranNodeParsingContext.getCurrentRuleParsingContext().profiling(irm, hasParameters.getParameterItemRuleMap());
                    hasParameters.setParameterItemRuleMap(irm);
                } else {
                    HasParameters hasParameters = (HasParameters)object;
                    irm = AspectranNodeParsingContext.getCurrentRuleParsingContext().profiling(irm, hasParameters.getParameterItemRuleMap());
                    hasParameters.setParameterItemRuleMap(irm);
                }
            });
    }

}
