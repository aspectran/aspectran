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
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing advice elements like {@code <before>},
 * {@code <after>}, {@code <around>}, and {@code <finally>}.
 *
 * @since 2013. 8. 11.
 */
class AdviceInnerNodeletAdder implements NodeletAdder {

    private static volatile AdviceInnerNodeletAdder INSTANCE;

    static AdviceInnerNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (AdviceInnerNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdviceInnerNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("before")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                AdviceRule adviceRule = aspectRule.newBeforeAdviceRule();
                AspectranNodeParsingContext.pushObject(adviceRule);
            })
            .with(ActionInnerNodeletAdder.instance())
            .endNodelet(text -> AspectranNodeParsingContext.popObject())
        .parent().child("after")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                AdviceRule adviceRule = aspectRule.newAfterAdviceRule();
                AspectranNodeParsingContext.pushObject(adviceRule);
            })
            .with(ActionInnerNodeletAdder.instance())
            .endNodelet(text -> AspectranNodeParsingContext.popObject())
        .parent().child("around")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                AdviceRule adviceRule = aspectRule.newAroundAdviceRule();
                AspectranNodeParsingContext.pushObject(adviceRule);
            })
            .with(ActionInnerNodeletAdder.instance())
            .endNodelet(text -> AspectranNodeParsingContext.popObject())
        .parent().child("finally")
            .nodelet(attrs -> {
                AspectRule aspectRule = AspectranNodeParsingContext.peekObject();
                AdviceRule adviceRule = aspectRule.newFinallyAdviceRule();
                AspectranNodeParsingContext.pushObject(adviceRule);
            })
            .with(ActionInnerNodeletAdder.instance())
            .endNodelet(text -> AspectranNodeParsingContext.popObject())
        .parent().child("finally/thrown")
            .nodelet(attrs -> {
                String exceptionType = attrs.get("type");
                AdviceRule adviceRule = AspectranNodeParsingContext.peekObject();
                ExceptionThrownRule etr = new ExceptionThrownRule(adviceRule);
                if (exceptionType != null) {
                    String[] exceptionTypes = StringUtils.splitWithComma(exceptionType);
                    etr.setExceptionTypes(exceptionTypes);
                }
                AspectranNodeParsingContext.pushObject(etr);
            })
            .with(ActionInnerNodeletAdder.instance())
            .with(ResponseInnerNodeletAdder.instance())
            .endNodelet(text -> {
                ExceptionThrownRule etr = AspectranNodeParsingContext.popObject();
                AdviceRule adviceRule = AspectranNodeParsingContext.peekObject();
                adviceRule.setExceptionThrownRule(etr);
            });
    }

}
