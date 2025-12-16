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

import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing the {@code <choose>}, {@code <when>}, and
 * {@code <otherwise>} elements for conditional logic.
 *
 * @since 6.0.0
 */
class ChooseNodeletAdder implements NodeletAdder {

    private static volatile ChooseNodeletAdder INSTANCE;

    static ChooseNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (ChooseNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChooseNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child(ChooseNodeletGroup.instance().getName())
            .nodelet(attrs -> {
                ChooseRule chooseRule = ChooseRule.newInstance();
                AspectranNodeParsingContext.pushObject(chooseRule);
            })
            .endNodelet(text -> {
                ChooseRule chooseRule = AspectranNodeParsingContext.popObject();
                HasActionRules hasActionRules = AspectranNodeParsingContext.peekObject();
                hasActionRules.putActionRule(chooseRule);
            })
            .child("when")
                .nodelet(attrs -> {
                    String expression = StringUtils.emptyToNull(attrs.get("test"));

                    ChooseRule chooseRule = AspectranNodeParsingContext.peekObject();
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
                    chooseWhenRule.setExpression(expression);
                    AspectranNodeParsingContext.pushObject(chooseWhenRule);
                })
                .with(ActionInnerNodeletAdder.instance())
                .with(ResponseInnerNodeletAdder.instance())
                .mount(ChooseNodeletGroup.instance())
                .endNodelet(text -> {
                    AspectranNodeParsingContext.popObject(); // chooseWhenRule
                })
            .parent().child("otherwise")
                .nodelet(attrs -> {
                    ChooseRule chooseRule = AspectranNodeParsingContext.peekObject();
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
                    AspectranNodeParsingContext.pushObject(chooseWhenRule);
                })
                .with(ActionInnerNodeletAdder.instance())
                .with(ResponseInnerNodeletAdder.instance())
                .mount(ChooseNodeletGroup.instance())
                .endNodelet(text -> {
                    AspectranNodeParsingContext.popObject(); // chooseWhenRule
                });
        }

}
