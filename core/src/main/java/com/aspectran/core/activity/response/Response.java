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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;

/**
 * The Interface Response.
 * 
 * <p>Created: 2008. 03. 23 PM 12:52:04</p>
 */
public interface Response extends Replicable<Response> {

    /**
     * The result of the activity is processed into a specific response
     * form and then sent to the client.
     *
     * @param activity the current Activity
     * @throws ResponseException the response exception
     */
    void commit(Activity activity);

    /**
     * Gets the response type.
     *
     * @return the response type
     */
    ResponseType getResponseType();

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    String getContentType();

    /**
     * Gets the action list.
     *
     * @return the action list
     */
    ActionList getActionList();

    /**
     * Replicates and returns this response.
     *
     * @return the new response
     */
    Response replicate();

}
