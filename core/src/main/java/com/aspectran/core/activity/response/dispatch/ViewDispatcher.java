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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.DispatchResponseRule;

/**
 * The Interface ViewDispatcher.
 * 
 * @since 2008. 03. 23
 */
public interface ViewDispatcher {

    String VIEW_DISPATCHER_SETTING_NAME = "viewDispatcher";

    /**
     * Dispatch to other resources as the given rule.
     *
     * @param activity the current activity
     * @param dispatchResponseRule the dispatch response rule
     * @throws ViewDispatcherException the view dispatch exception
     */
    void dispatch(Activity activity, DispatchResponseRule dispatchResponseRule) throws ViewDispatcherException;

    /**
     * Return whether this view dispatcher corresponds to a singleton instance.
     *
     * @return whether this view dispatcher corresponds to a singleton instance
     */
    boolean isSingleton();

}
