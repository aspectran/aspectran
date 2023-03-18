/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.type.TextStyleType;
import com.aspectran.core.util.TextStyler;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class DescriptionParameters extends AbstractParameters {

    public static final ParameterKey profile;
    public static final ParameterKey style;
    public static final ParameterKey content;

    private static final ParameterKey[] parameterKeys;

    static {
        profile = new ParameterKey("profile", ValueType.STRING);
        style = new ParameterKey("style", ValueType.STRING);
        content = new ParameterKey("content", new String[] {"description"}, ValueType.TEXT);

        parameterKeys = new ParameterKey[] {
                profile,
                style,
                content
        };
    }

    public DescriptionParameters() {
        super(parameterKeys);
    }

    public DescriptionParameters(DescriptionRule descriptionRule) {
        super(parameterKeys);
        putValueNonNull(profile, descriptionRule.getProfile());
        putValueNonNull(style, descriptionRule.getContentStyle());
        if (descriptionRule.getContentStyle() == TextStyleType.APON) {
            putValueNonNull(content, TextStyler.stripAponStyle(descriptionRule.getContent()));
        } else {
            putValueNonNull(content, descriptionRule.getContent());
        }
    }

}
