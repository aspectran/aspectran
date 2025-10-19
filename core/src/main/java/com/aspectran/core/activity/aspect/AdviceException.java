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
 * <p>
 * This exception is typically thrown by the framework when a problem arises while
 * invoking advice code. It preserves the original {@code Throwable} cause and
 * retains a reference to the {@code AdviceRule} that was being processed.</p>
 */
public class AdviceException extends ActivityException {

    @Serial
    private static final long serialVersionUID = 6813516429436576091L;

    private final AdviceRule adviceRule;

    /**
     * Creates a new {@code AdviceException} with the specified detail message,
     * associated {@link AdviceRule}, and root cause.
     * @param msg        the detail message explaining the error
     * @param adviceRule the {@code AdviceRule} that was being executed when the error occurred
     * @param cause      the underlying exception that caused this failure (may be {@code null})
     */
    public AdviceException(String msg, AdviceRule adviceRule, Throwable cause) {
        super(msg, cause);
        this.adviceRule = adviceRule;
    }

    /**
     * Returns the {@link AdviceRule}
     * that was executing when this exception was thrown.
     * @return the associated {@code AdviceRule}
     */
    public AdviceRule getAdviceRule() {
        return adviceRule;
    }

}
