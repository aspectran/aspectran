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
package com.aspectran.core.activity.aspect;

import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.context.rule.AdviceRule;

import java.io.Serial;

/**
 * Checked exception thrown if an error occurs while executing the advice.
 */
public class AdviceException extends ActivityException {

    @Serial
    private static final long serialVersionUID = 6813516429436576091L;

    private final AdviceRule adviceRule;

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     * @param msg a message to associate with the exception
     * @param adviceRule the advice rule
     * @param cause the real cause of the exception
     */
    public AdviceException(String msg, AdviceRule adviceRule, Throwable cause) {
        super(msg, cause);
        this.adviceRule = adviceRule;
    }

    /**
     * Returns the advice rule.
     * @return the advice rule
     */
    public AdviceRule getAdviceRule() {
        return adviceRule;
    }

}
