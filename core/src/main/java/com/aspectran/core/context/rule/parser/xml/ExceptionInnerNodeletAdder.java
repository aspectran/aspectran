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
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class ExceptionInnerNodeParser.
 *
 * @since 2013. 8. 11.
 */
class ExceptionInnerNodeletAdder implements NodeletAdder {

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("description")
            .nodelet(attrs -> {
                String profile = attrs.get("profile");
                String style = attrs.get("style");

                DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                AspectranNodeParser.current().pushObject(descriptionRule);
            })
            .endNodelet(text -> {
                DescriptionRule descriptionRule = AspectranNodeParser.current().popObject();
                ExceptionRule exceptionRule = AspectranNodeParser.current().peekObject();

                descriptionRule.setContent(text);
                descriptionRule = AspectranNodeParser.current().getAssistant().profiling(descriptionRule, exceptionRule.getDescriptionRule());
                exceptionRule.setDescriptionRule(descriptionRule);
            })
        .parent().child("thrown")
            .nodelet(attrs -> {
                String exceptionType = attrs.get("type");

                ExceptionThrownRule etr = new ExceptionThrownRule();
                if (exceptionType != null) {
                    String[] exceptionTypes = StringUtils.splitWithComma(exceptionType);
                    etr.setExceptionTypes(exceptionTypes);
                }

                AspectranNodeParser.current().pushObject(etr);
            })
            .with(AspectranNodeParser.current().getActionNodeletAdder())
            .with(AspectranNodeParser.current().getResponseInnerNodeletAdder())
            .endNodelet(text -> {
                ExceptionThrownRule etr = AspectranNodeParser.current().popObject();
                ExceptionRule exceptionRule = AspectranNodeParser.current().peekObject();
                exceptionRule.putExceptionThrownRule(etr);
            });
    }

}
