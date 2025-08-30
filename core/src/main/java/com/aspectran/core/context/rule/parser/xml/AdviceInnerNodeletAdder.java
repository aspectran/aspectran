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

import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class AdviceInnerNodeParser.
 *
 * @since 2013. 8. 11.
 */
class AdviceInnerNodeletAdder implements NodeletAdder {

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("before")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                AdviceRule adviceRule = aspectRule.newBeforeAdviceRule();
                AspectranNodeParser.current().pushObject(adviceRule);
            })
            .with(AspectranNodeletGroup.actionNodeletAdder)
            .endNodelet(text -> AspectranNodeParser.current().popObject())
        .parent().child("after")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                AdviceRule adviceRule = aspectRule.newAfterAdviceRule();
                AspectranNodeParser.current().pushObject(adviceRule);
            })
            .with(AspectranNodeletGroup.actionNodeletAdder)
            .endNodelet(text -> AspectranNodeParser.current().popObject())
        .parent().child("around")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                AdviceRule adviceRule = aspectRule.newAroundAdviceRule();
                AspectranNodeParser.current().pushObject(adviceRule);
            })
            .with(AspectranNodeletGroup.actionNodeletAdder)
            .endNodelet(text -> AspectranNodeParser.current().popObject())
        .parent().child("finally")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParser.current().peekObject();
                AdviceRule adviceRule = aspectRule.newFinallyAdviceRule();
                AspectranNodeParser.current().pushObject(adviceRule);
            })
            .with(AspectranNodeletGroup.actionNodeletAdder)
            .endNodelet(text -> AspectranNodeParser.current().popObject())
        .parent().child("finally/thrown")
            .nodelet(attrs -> {
                String exceptionType = attrs.get("type");
                AdviceRule adviceRule = AspectranNodeParser.current().peekObject();
                ExceptionThrownRule etr = new ExceptionThrownRule(adviceRule);
                if (exceptionType != null) {
                    String[] exceptionTypes = StringUtils.splitWithComma(exceptionType);
                    etr.setExceptionTypes(exceptionTypes);
                }
                AspectranNodeParser.current().pushObject(etr);
            })
            .with(AspectranNodeletGroup.actionNodeletAdder)
            .with(AspectranNodeletGroup.responseInnerNodeletAdder)
            .endNodelet(text -> {
                ExceptionThrownRule etr = AspectranNodeParser.current().popObject();
                AdviceRule adviceRule = AspectranNodeParser.current().peekObject();
                adviceRule.setExceptionThrownRule(etr);
            });
    }

}
