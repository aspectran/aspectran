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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.DispatchRule;

import java.io.Serial;

/**
 * The Class DispatchResponseException.
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class DispatchResponseException extends ResponseException {

    @Serial
    private static final long serialVersionUID = 6318844460136930428L;

    private final DispatchRule dispatchRule;

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     * @param dispatchRule the dispatch rule
     * @param cause the real cause of the exception
     */
    public DispatchResponseException(DispatchRule dispatchRule, Throwable cause) {
        super("Failed to dispatch " + dispatchRule, cause);
        this.dispatchRule = dispatchRule;
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     * @param dispatchRule the dispatch rule
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public DispatchResponseException(DispatchRule dispatchRule, String msg, Throwable cause) {
        super(msg + " " + dispatchRule, cause);
        this.dispatchRule = dispatchRule;
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     * @param dispatchRule the dispatch rule
     * @param msg the detail message
     */
    public DispatchResponseException(DispatchRule dispatchRule, String msg) {
        super(msg + " " + dispatchRule);
        this.dispatchRule = dispatchRule;
    }

    /**
     * Gets the dispatch rule.
     * @return the dispatch rule
     */
    public DispatchRule getDispatchRule() {
        return dispatchRule;
    }

}
