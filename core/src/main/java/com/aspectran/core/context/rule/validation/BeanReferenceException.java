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
package com.aspectran.core.context.rule.validation;

import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.parser.ActivityContextRuleParserException;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector.RefererInfo;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector.RefererKey;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.Map;

/**
 * This exception will be thrown when cannot resolve reference to bean.
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class BeanReferenceException extends ActivityContextRuleParserException {

    @Serial
    private static final long serialVersionUID = -244633940486989865L;

    /**
     * Constructor to create exception with a message.
     * @param brokenReferences the map of beans that can not find
     */
    BeanReferenceException(Map<RefererInfo, RefererKey> brokenReferences, Map<RefererKey, BeanRule[]> nonUniqueBeans) {
        super(getMessage(brokenReferences, nonUniqueBeans));
    }

    /**
     * Gets the detail message.
     * @param brokenReferences the list of beans that can not find
     * @return the message
     */
    @NonNull
    private static String getMessage(@NonNull Map<RefererInfo, RefererKey> brokenReferences,
                                     Map<RefererKey, BeanRule[]> nonUniqueBeans) {
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(brokenReferences.size()).append(" broken bean reference(s):\n");
        int count = 0;
        for (Map.Entry<RefererInfo, RefererKey> entry : brokenReferences.entrySet()) {
            RefererKey refererKey = entry.getValue();
            BeanRule[] beanRules = (refererKey != null ? nonUniqueBeans.get(refererKey) : null);
            sb.append(++count).append(". ").append(getDetailMessage(refererKey, entry.getKey(), beanRules));
            if (count < brokenReferences.size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @NonNull
    private static String getDetailMessage(RefererKey refererKey, RefererInfo refererInfo, BeanRule[] beanRules) {
        if (refererKey != null) {
            String beanId = refererKey.getQualifier();
            Class<?> beanClass = refererKey.getType();
            if (beanId != null) {
                return "Cannot resolve reference to bean " + refererKey + "; Referer: " + refererInfo;
            } else {
                if (beanRules != null && beanRules.length > 1) {
                    return "No unique bean of type [" + beanClass.getName() + "] is defined: " +
                            "expected single matching bean but found " + beanRules.length + ": [" +
                            NoUniqueBeanException.getBeanDescriptions(beanRules) + "]; Referer: " + refererInfo;
                } else {
                    return "No unique bean of type [" + beanClass.getName() + "] is defined; Referer: " + refererInfo;
                }
            }
        } else {
            return "Cannot resolve reference: " + refererInfo;
        }
    }

}
