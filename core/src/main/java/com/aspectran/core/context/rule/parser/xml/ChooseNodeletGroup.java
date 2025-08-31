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
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class ChooseNodeParser.
 *
 * @since 6.0.0
 */
class ChooseNodeletGroup extends NodeletGroup {

    private static final ChooseNodeletGroup INSTANCE = new ChooseNodeletGroup();

    static ChooseNodeletGroup instance() {
        return INSTANCE;
    }

    ChooseNodeletGroup() {
        super("choose");
        nodelet(attrs -> {
            ChooseRule chooseRule = ChooseRule.newInstance();
            AspectranNodeParsingContext.pushObject(chooseRule);
        })
        .endNodelet(text -> {
            ChooseRule chooseRule = AspectranNodeParsingContext.popObject();
            HasActionRules applicable = AspectranNodeParsingContext.peekObject();
            applicable.putActionRule(chooseRule);
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
                AspectranNodeParsingContext.popObject();
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
                AspectranNodeParsingContext.popObject();
            });
    }

}
