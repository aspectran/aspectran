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
/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule;

import com.aspectran.core.context.AspectranCheckedException;

/**
 * This exception will be thrown if an illegal rule is found.
 * 
 * <p>Created: 2017. 11. 10.</p>
 */
public class IllegalRuleException extends AspectranCheckedException {

    /** @serial */
    private static final long serialVersionUID = 4725758105730046172L;

    /**
     * Simple constructor
     */
    public IllegalRuleException() {
        super();
    }

    /**
     * Constructor to create exception with a message
     *
     * @param msg a message to associate with the exception
     */
    public IllegalRuleException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception
     *
     * @param cause the real cause of the exception
     */
    public IllegalRuleException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message
     *
     * @param msg a message to associate with the exception
     * @param cause the real cause of the exception
     */
    public IllegalRuleException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
