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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSP or other web resource integration.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class DispatchResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(DispatchResponse.class);

    private static final Map<String, ViewDispatcher> cache = new ConcurrentHashMap<>();

    private final DispatchRule dispatchRule;

    /**
     * Instantiates a new DispatchResponse with specified DispatchRule.
     * @param dispatchRule the dispatch rule
     */
    public DispatchResponse(DispatchRule dispatchRule) {
        this.dispatchRule = dispatchRule;
    }

    @Override
    public void commit(Activity activity) throws ResponseException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Response " + dispatchRule);
            }

            ViewDispatcher viewDispatcher = getViewDispatcher(activity);
            viewDispatcher.dispatch(activity, dispatchRule);
        } catch (Exception e) {
            throw new DispatchResponseException(dispatchRule, e);
        }
    }

    /**
     * Gets the dispatch rule.
     * @return the dispatch rule
     */
    public DispatchRule getDispatchRule() {
        return dispatchRule;
    }

    @Override
    public ResponseType getResponseType() {
        return DispatchRule.RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        return dispatchRule.getContentType();
    }

    @Override
    public Response replicate() {
        return new DispatchResponse(dispatchRule.replicate());
    }

    /**
     * Determine the view dispatcher.
     * @param activity the current Activity
     * @throws ViewDispatcherException if ViewDispatcher can not be determined
     */
    private ViewDispatcher getViewDispatcher(Activity activity) throws ViewDispatcherException {
        if (dispatchRule.getViewDispatcher() != null) {
            return dispatchRule.getViewDispatcher();
        }

        try {
            String dispatcherName = dispatchRule.getDispatcherName();
            if (dispatcherName == null) {
                dispatcherName = activity.getSetting(ViewDispatcher.VIEW_DISPATCHER_SETTING_NAME);
                if (dispatcherName == null) {
                    throw new IllegalArgumentException("Could not find the '" +
                            ViewDispatcher.VIEW_DISPATCHER_SETTING_NAME +
                            "' setting in the translet " + activity.getTranslet());
                }
            }

            ViewDispatcher viewDispatcher = cache.get(dispatcherName);
            if (viewDispatcher == null) {
                if (dispatcherName.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                    String dispatcherClassName = dispatcherName.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
                    Class<?> dispatcherClass = activity.getApplicationAdapter().getClassLoader().loadClass(dispatcherClassName);
                    viewDispatcher = (ViewDispatcher)activity.getBean(dispatcherClass);
                } else {
                    viewDispatcher = activity.getBean(dispatcherName);
                }
                if (viewDispatcher == null) {
                    throw new IllegalArgumentException("No bean named '" + dispatcherName + "' is defined");
                }
                if (viewDispatcher.isSingleton()) {
                    ViewDispatcher existing = cache.putIfAbsent(dispatcherName, viewDispatcher);
                    if (existing != null) {
                        viewDispatcher = existing;
                    }
                }
            }
            return viewDispatcher;
        } catch (Exception e) {
            throw new ViewDispatcherException("Unable to determine ViewDispatcher", e);
        }
    }

    @Override
    public String toString() {
        return dispatchRule.toString();
    }

    /**
     * Save processing results as request attributes.
     * @param requestAdapter the request adapter
     * @param processResult the process result
     */
    public static void saveAttributes(RequestAdapter requestAdapter, ProcessResult processResult) {
        if (processResult != null) {
            for (ContentResult contentResult : processResult) {
                for (ActionResult actionResult : contentResult) {
                    Object actionResultValue = actionResult.getResultValue();
                    if (actionResultValue instanceof ProcessResult) {
                        saveAttributes(requestAdapter, (ProcessResult)actionResultValue);
                    } else {
                        String actionId = actionResult.getActionId();
                        if (actionId != null) {
                            requestAdapter.setAttribute(actionId, actionResultValue);
                        } else if (actionResultValue instanceof Map<?, ?>) {
                            for (Map.Entry<?, ?> entry : ((Map<?, ?>)actionResultValue).entrySet()) {
                                String name = entry.getKey().toString();
                                Object value = entry.getValue();
                                requestAdapter.setAttribute(name, value);
                            }
                        }
                    }
                }
            }
        }
    }

}
