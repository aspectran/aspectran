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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.adapter.RequestAdapter;

import java.util.HashMap;

/**
 * A map of data for saving activity results.
 * It is often used as a model for providing data used in views.
 *
 * <p>This class is generally not thread-safe.
 * It is primarily designed for use in a single thread only.</p>
 */
public class ActivityDataMap extends HashMap<String, Object> {

    /** @serial */
    private static final long serialVersionUID = -4557424414862800204L;

    protected final Activity activity;

    protected RequestAdapter requestAdapter;

    /**
     * Instantiates a new ActivityDataMap.
     *
     * @param activity the activity
     */
    public ActivityDataMap(Activity activity) {
        this(activity, false);
    }

    /**
     * Instantiates a new ActivityDataMap.
     *
     * @param activity the activity
     * @param prefill whether or not to pre-fill the data
     */
    public ActivityDataMap(Activity activity, boolean prefill) {
        this.activity = activity;
        this.requestAdapter = activity.getRequestAdapter();

        if (prefill) {
            prefillData();
        }
    }

    private void prefillData() {
        if (requestAdapter != null) {
            requestAdapter.extractParameters(this);
            requestAdapter.extractAttributes(this);
        }
        if (activity.getProcessResult() != null) {
            for (ContentResult cr : activity.getProcessResult()) {
                for (ActionResult ar : cr) {
                    if (ar.getActionId() != null) {
                        put(ar.getActionId(), ar.getResultValue());
                    }
                }
            }
        }
    }

    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value != null) {
            return value;
        }
        if (key != null) {
            String name = key.toString();

            value = getActionResultWithoutCache(name);
            if (value != null) {
                put(name, value);
                return value;
            }

            value = getAttributeWithoutCache(name);
            if (value != null) {
                put(name, value);
                return value;
            }

            value = getParameterWithoutCache(name);
            if (value != null) {
                put(name, value);
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the value of the request parameter from the request adapter
     * without storing it in the cache.
     * If the parameter does not exist, returns null.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return an {@code Object} containing the value of the parameter,
     *         or {@code null} if the parameter does not exist
     * @see RequestAdapter#setParameter
     */
    public Object getParameterWithoutCache(String name) {
        if (requestAdapter != null) {
            String[] values = requestAdapter.getParameterValues(name);
            if (values != null) {
                if (values.length == 1) {
                    return values[0];
                } else {
                    return values;
                }
            }
        }
        return null;
    }

    /**
     * Returns the value of the named attribute from the request adapter
     * without storing it in the cache.
     * If no attribute of the given name exists, returns null.
     *
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *         or {@code null} if the attribute does not exist
     * @see RequestAdapter#getAttribute
     */
    public Object getAttributeWithoutCache(String name) {
        if (requestAdapter != null) {
            return requestAdapter.getAttribute(name);
        } else {
            return null;
        }
    }

    /**
     * Returns the value of the named action's process result
     * without storing it in the cache.
     * If no process result of the given name exists, returns null.
     *
     * @param name a {@code String} specifying the name of the action
     * @return an {@code Object} containing the value of the action result,
     *         or {@code null} if the action result does not exist
     * @see RequestAdapter#getAttribute
     */
    public Object getActionResultWithoutCache(String name) {
        if (activity.getProcessResult() != null) {
            return activity.getProcessResult().getResultValue(name);
        } else {
            return null;
        }
    }

}
