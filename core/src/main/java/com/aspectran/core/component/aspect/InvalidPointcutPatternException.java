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
package com.aspectran.core.component.aspect;

import com.aspectran.core.context.rule.AspectRule;

import java.io.Serial;

/**
 * Exception thrown when a pointcut pattern is invalid.
 * <p>This occurs if a pattern is malformed during parsing or compilation.</p>
 */
public class InvalidPointcutPatternException extends AspectException {

    @Serial
    private static final long serialVersionUID = 3736262494374232352L;

    /**
     * Creates a new InvalidPointcutPatternException.
     */
    public InvalidPointcutPatternException() {
        super();
    }

    /**
     * Creates a new InvalidPointcutPatternException with the specified detail message.
     * @param msg the detail message
     */
    public InvalidPointcutPatternException(String msg) {
        super(msg);
    }

    /**
     * Creates a new InvalidPointcutPatternException with the specified cause.
     * @param cause the root cause
     */
    public InvalidPointcutPatternException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new InvalidPointcutPatternException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the root cause
     */
    public InvalidPointcutPatternException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates a new InvalidPointcutPatternException with a message that includes context
     * from the problematic {@link AspectRule}.
     * @param aspectRule the aspect rule containing the invalid pointcut
     * @param msg the detail message
     */
    public InvalidPointcutPatternException(AspectRule aspectRule, String msg) {
        super(msg + " in " + aspectRule);
    }

}
