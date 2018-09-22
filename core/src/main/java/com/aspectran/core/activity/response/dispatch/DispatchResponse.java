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
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JSP or other web resource integration.
 * 
 * <p> Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class DispatchResponse implements Response {

    private final Log log = LogFactory.getLog(DispatchResponse.class);

    private final boolean debugEnabled = log.isDebugEnabled();

    private final static Map<String, ViewDispatcher> viewDispatcherCache = new HashMap<>();

    private final DispatchResponseRule dispatchResponseRule;

    /**
     * Instantiates a new DispatchResponse with specified DispatchResponseRule.
     *
     * @param dispatchResponseRule the dispatch response rule
     */
    public DispatchResponse(DispatchResponseRule dispatchResponseRule) {
        this.dispatchResponseRule = dispatchResponseRule;
    }

    @Override
    public void commit(Activity activity) {
        try {
            if (debugEnabled) {
                log.debug("response " + dispatchResponseRule);
            }

            ViewDispatcher viewDispatcher = getViewDispatcher(activity);
            viewDispatcher.dispatch(activity, dispatchResponseRule);
        } catch (Exception e) {
            throw new DispatchResponseException(dispatchResponseRule, e);
        }
    }

    /**
     * Gets the dispatch response rule.
     *
     * @return the dispatch response rule
     */
    public DispatchResponseRule getDispatchResponseRule() {
        return dispatchResponseRule;
    }

    @Override
    public String getContentType() {
        return dispatchResponseRule.getContentType();
    }

    @Override
    public ResponseType getResponseType() {
        return DispatchResponseRule.RESPONSE_TYPE;
    }

    @Override
    public ActionList getActionList() {
        return dispatchResponseRule.getActionList();
    }

    @Override
    public Response replicate() {
        DispatchResponseRule drr = dispatchResponseRule.replicate();
        return new DispatchResponse(drr);
    }

    /**
     * Determine the view dispatcher.
     *
     * @param activity the current Activity
     * @throws ViewDispatcherException if ViewDispatcher can not be determined
     */
    private ViewDispatcher getViewDispatcher(Activity activity) throws ViewDispatcherException {
        try {
            String viewDispatcherName;
            if (dispatchResponseRule.getDispatcher() != null) {
                viewDispatcherName = dispatchResponseRule.getDispatcher();
            } else {
                viewDispatcherName = activity.getSetting(ViewDispatcher.VIEW_DISPATCHER_SETTING_NAME);
                if (viewDispatcherName == null) {
                    throw new IllegalArgumentException("The settings name '" + ViewDispatcher.VIEW_DISPATCHER_SETTING_NAME +
                            "' has not been specified in the default response rule");
                }
            }

            ViewDispatcher viewDispatcher;
            synchronized (viewDispatcherCache) {
                viewDispatcher = viewDispatcherCache.get(viewDispatcherName);
                if (viewDispatcher == null) {
                    if (viewDispatcherName.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                        String viewDispatcherClassName = viewDispatcherName.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
                        Class<?> viewDispatcherClass = activity.getEnvironment().getClassLoader().loadClass(viewDispatcherClassName);
                        viewDispatcher = (ViewDispatcher)activity.getBean(viewDispatcherClass);
                    } else {
                        viewDispatcher = activity.getBean(viewDispatcherName);
                    }
                    if (viewDispatcher == null) {
                        throw new IllegalArgumentException("No bean named '" + viewDispatcherName + "' is defined");
                    }

                    if (viewDispatcher.isSingleton()) {
                        viewDispatcherCache.put(viewDispatcherName, viewDispatcher);

                        if (log.isDebugEnabled()) {
                            log.debug("cached ViewDispatcher: " + viewDispatcher);
                        }
                    }
                }
            }
            return viewDispatcher;
        } catch(Exception e) {
            throw new ViewDispatcherException("Unable to determine ViewDispatcher", e);
        }
    }

    @Override
    public String toString() {
        return dispatchResponseRule.toString();
    }

}
