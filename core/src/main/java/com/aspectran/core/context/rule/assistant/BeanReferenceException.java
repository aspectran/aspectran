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

import com.aspectran.core.context.rule.parser.ActivityContextParserException;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
     * @param brokenReferences the list of beans that can not find
     */
    BeanReferenceException(Collection<BeanReferenceInspector.RefererKey> brokenReferences) {
        super(getMessage(brokenReferences));
    }

    /**
     * Gets the detail message.
     * @param brokenReferences the list of beans that can not find
     * @return the message
     */
    @NonNull
    private static String getMessage(@NonNull Collection<BeanReferenceInspector.RefererKey> brokenReferences) {
        if (brokenReferences.size() == 1) {
            return "Unable to resolve reference to bean " + new ArrayList<>(brokenReferences).get(0);
        } else {
            return "Unable to resolve reference to bean " + Arrays.toString(new ArrayList<>(brokenReferences).toArray());
        }
    }

}
