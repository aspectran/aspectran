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

import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * A {@code NodeletAdder} for parsing the {@code <thrown>} element within an
 * {@code <exception>} block.
 *
 * @since 2013. 8. 11.
 */
class ExceptionInnerNodeletAdder implements NodeletAdder {

    private static volatile ExceptionInnerNodeletAdder INSTANCE;

    static ExceptionInnerNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (ExceptionInnerNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExceptionInnerNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.with(DiscriptionNodeletAdder.instance())
            .child("thrown")
                .nodelet(attrs -> {
                    String exceptionType = attrs.get("type");

                    ExceptionThrownRule etr = new ExceptionThrownRule();
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
                    ExceptionRule exceptionRule = AspectranNodeParsingContext.peekObject();
                    exceptionRule.putExceptionThrownRule(etr);
                });
    }

}
