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
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

import static com.aspectran.core.context.rule.parser.xml.AspectranNodeletGroup.MAX_CHOOSE_DEPTH;

/**
 * The Class ChooseNodeParser.
 *
 * @since 6.0.0
 */
class ChooseNodeletAdder implements NodeletAdder {

    private final int depth;

    ChooseNodeletAdder(int depth) {
        this.depth = depth;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("choose")
            .nodelet(attrs -> {
                if (depth >= MAX_CHOOSE_DEPTH) {
                    StringBuilder sb = new StringBuilder("The <choose> element can be nested up to ");
                    if (MAX_CHOOSE_DEPTH > 1) {
                        sb.append(MAX_CHOOSE_DEPTH);
                        sb.append(" times");
                    } else {
                        sb.append("at most once");
                    }
                    throw new IllegalRuleException(sb.toString());
                }

                ChooseRule chooseRule = ChooseRule.newInstance();
                AspectranNodeParser.current().pushObject(chooseRule);
            })
            .endNodelet(text -> {
                ChooseRule chooseRule = AspectranNodeParser.current().popObject();
                ActionRuleApplicable applicable = AspectranNodeParser.current().peekObject();
                applicable.applyActionRule(chooseRule);
            })
            .child("when")
                .nodelet(attrs -> {
                    String expression = StringUtils.emptyToNull(attrs.get("test"));

                    ChooseRule chooseRule = AspectranNodeParser.current().peekObject();
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
                    chooseWhenRule.setExpression(expression);
                    AspectranNodeParser.current().pushObject(chooseWhenRule);
                })
                .with(AspectranNodeletGroup.actionNodeletAdder)
                .with(AspectranNodeletGroup.responseInnerNodeletAdder)
                .with(() -> {
                    if (depth < MAX_CHOOSE_DEPTH) {
                        return AspectranNodeletGroup.chooseNodeletAdders[depth + 1];
                    } else {
                        return null;
                    }
                })
                .endNodelet(text -> {
                    AspectranNodeParser.current().popObject();
                })

            .parent().child("otherwise")
                .nodelet(attrs -> {
                    ChooseRule chooseRule = AspectranNodeParser.current().peekObject();
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
                    AspectranNodeParser.current().pushObject(chooseWhenRule);
                })
                .with(AspectranNodeletGroup.actionNodeletAdder)
                .with(AspectranNodeletGroup.responseInnerNodeletAdder)
                .with(() -> {
                    if (depth < MAX_CHOOSE_DEPTH) {
                        return AspectranNodeletGroup.chooseNodeletAdders[depth + 1];
                    } else {
                        return null;
                    }
                })
                .endNodelet(text -> {
                    AspectranNodeParser.current().popObject();
                });
    }

}
