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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.DispatchRule;

/**
 * Defines a contract for dispatching a request to a specific view technology for rendering.
 *
 * <p>Implementations of this interface are responsible for integrating with various
 * view layers (e.g., JSP, Thymeleaf, FreeMarker) by forwarding the request and
 * response to the appropriate view resource. This allows Aspectran to remain
 * decoupled from specific view technologies while providing a flexible rendering
 * mechanism.</p>
 */
public interface ViewDispatcher {

    /**
     * The name of the setting that specifies the default view dispatcher bean.
     */
    String VIEW_DISPATCHER_SETTING_NAME = "viewDispatcher";

    /**
     * Returns the content type of the view.
     * @return the content type
     */
    String getContentType();

    /**
     * Dispatches the request to the view specified in the given {@code DispatchRule}.
     * @param activity the current activity
     * @param dispatchRule the rule that defines the view to dispatch to
     * @throws ViewDispatcherException if an error occurs during dispatch
     */
    void dispatch(Activity activity, DispatchRule dispatchRule) throws ViewDispatcherException;

    /**
     * Returns whether this view dispatcher is a singleton.
     * <p>Singleton dispatchers are cached by {@link DispatchResponse} for performance.</p>
     * @return true if the dispatcher is a singleton, false otherwise
     */
    boolean isSingleton();

}
