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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.DispatchResponseRule;

/**
 * The Class DispatchResponseException.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class DispatchResponseException extends ResponseException {

    /** @serial */
    private static final long serialVersionUID = 6318844460136930428L;

    private DispatchResponseRule dispatchResponseRule;

    /**
     * Simple constructor.
     */
    public DispatchResponseException() {
        super();
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param msg a message to associate with the exception
     */
    public DispatchResponseException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public DispatchResponseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public DispatchResponseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param dispatchResponseRule the dispatch response rule
     * @param cause the real cause of the exception
     */
    public DispatchResponseException(DispatchResponseRule dispatchResponseRule, Throwable cause) {
        super("Failed to dispatch " + dispatchResponseRule, cause);
        this.dispatchResponseRule = dispatchResponseRule;
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param dispatchResponseRule the dispatch response rule
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public DispatchResponseException(DispatchResponseRule dispatchResponseRule, String msg, Throwable cause) {
        super(msg + " " + dispatchResponseRule, cause);
        this.dispatchResponseRule = dispatchResponseRule;
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param dispatchResponseRule the dispatch response rule
     * @param msg the detail message
     */
    public DispatchResponseException(DispatchResponseRule dispatchResponseRule, String msg) {
        super(msg + " " + dispatchResponseRule);
        this.dispatchResponseRule = dispatchResponseRule;
    }

    /**
     * Gets the dispatch response rule.
     *
     * @return the dispatch response rule
     */
    public DispatchResponseRule getDispatchResponseRule() {
        return dispatchResponseRule;
    }

}
