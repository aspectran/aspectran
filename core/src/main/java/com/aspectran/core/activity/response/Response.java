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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;

/**
 * Defines the contract for generating a response after an activity has processed a request.
 *
 * <p>Implementations of this interface are responsible for a specific response strategy,
 * such as transforming content, redirecting, forwarding, or dynamically writing output.
 * The {@link Activity} engine invokes the {@link #respond(Activity)} method to produce the final output.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public interface Response extends Replicable<Response> {

    /**
     * Gets the response type.
     * @return the response type
     */
    ResponseType getResponseType();

    /**
     * Gets the content type.
     * @return the content type
     */
    String getContentType();

    /**
     * Generates the response based on the current state of the activity.
     * @param activity the current activity, which provides access to the translet,
     *      adapters, and results
     * @throws ResponseException if an error occurs during response generation
     */
    void respond(Activity activity) throws ResponseException;

    /**
     * Creates and returns a new {@code Response} instance that is a replica of this object.
     * The replicated instance will have the same configuration but will be independent
     * of the original, allowing for safe modification or concurrent use.
     * @return a replicated {@code Response} instance
     */
    Response replicate();

}
