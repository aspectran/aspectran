/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.context.rule.assistant.BeanReferenceInspector.RefererInfo;
import com.aspectran.core.context.rule.assistant.BeanReferenceInspector.RefererKey;
import com.aspectran.core.context.rule.parser.ActivityContextParserException;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.Map;

/**
 * This exception will be thrown when cannot resolve reference to bean.
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class BeanReferenceException extends ActivityContextParserException {

    @Serial
    private static final long serialVersionUID = -244633940486989865L;

    /**
     * Constructor to create exception with a message.
     * @param brokenReferences the map of beans that can not find
     */
    BeanReferenceException(Map<RefererInfo, RefererKey> brokenReferences) {
        super(getMessage(brokenReferences));
    }

    /**
     * Gets the detail message.
     * @param brokenReferences the list of beans that can not find
     * @return the message
     */
    @NonNull
    private static String getMessage(@NonNull Map<RefererInfo, RefererKey> brokenReferences) {
        Map.Entry<RefererInfo, RefererKey> first = brokenReferences.entrySet().iterator().next();
        return getDetailMessage(first.getValue(), first.getKey()) +
                (brokenReferences.size() > 1 ? " (and " + (brokenReferences.size() - 1) + " more)" : "");
    }

    @NonNull
    private static String getDetailMessage(RefererKey refererKey, RefererInfo refererInfo) {
        if (refererKey != null) {
            String beanId = refererKey.getQualifier();
            Class<?> beanClass = refererKey.getType();
            if (beanId != null) {
                return "Cannot resolve reference to bean " + refererKey +
                        "; Referer: " + refererInfo;
            } else {
                return "No unique bean of type [" + beanClass + "] is defined; Referer: " + refererInfo;
            }
        } else {
            return "Cannot resolve reference: " + refererInfo;
        }
    }

}
