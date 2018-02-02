/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.component.aspect.AspectException;
import com.aspectran.core.context.rule.AspectAdviceRule;

/**
 * The Class AspectAdviceException.
 */
public class AspectAdviceException extends AspectException {

    /** @serial */
    private static final long serialVersionUID = 6813516429436576091L;

    private AspectAdviceRule aspectAdviceRule;

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg a message to associate with the exception
     * @param aspectAdviceRule the aspect advice rule
     * @param cause the real cause of the exception
     */
    public AspectAdviceException(String msg, AspectAdviceRule aspectAdviceRule, Throwable cause) {
        super(msg, cause);
        this.aspectAdviceRule = aspectAdviceRule;
    }

    /**
     * Returns the aspect advice rule.
     *
     * @return the aspect advice rule
     */
    public AspectAdviceRule getAspectAdviceRule() {
        return aspectAdviceRule;
    }

}
