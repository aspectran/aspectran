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
import com.aspectran.core.adapter.SessionAdapter;

import java.util.Enumeration;
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

    private static final Object EMPTY_VALUE = new Object();

    private final Activity activity;

    /**
     * Instantiates a new ActivityDataMap.
     *
     * @param activity the activity
     */
    public ActivityDataMap(Activity activity) {
        this.activity = activity;

        fillToEmptyValues();
    }

    private void fillToEmptyValues() {
        if (activity.getRequestAdapter() != null) {
            for (String name : activity.getRequestAdapter().getParameterNames()) {
                put(name, EMPTY_VALUE);
            }
            for (String name : activity.getRequestAdapter().getAttributeNames()) {
                put(name, EMPTY_VALUE);
            }
        }
        if (activity.getProcessResult() != null) {
            for (ContentResult cr : activity.getProcessResult()) {
                for (ActionResult ar : cr) {
                    if (ar.getActionId() != null) {
                        put(ar.getActionId(), EMPTY_VALUE);
                    }
                }
            }
        }
        if (activity.getSessionAdapter() != null) {
            Enumeration<String> e = activity.getSessionAdapter().getAttributeNames();
            if (e != null) {
                while (e.hasMoreElements()) {
                    put(e.nextElement(), EMPTY_VALUE);
                }
            }
        }
    }

    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value != null && !value.equals(EMPTY_VALUE)) {
            return value;
        }
        if (key == null) {
            return null;
        }

        String name = key.toString();
        Object data = getActionResultWithoutCache(name);
        if (data != null) {
            if (value == null) {
                put(name, EMPTY_VALUE);
            }
            return data;
        }

        data = getAttributeWithoutCache(name);
        if (data != null) {
            if (value == null) {
                put(name, EMPTY_VALUE);
            }
            return data;
        }

        data = getParameterWithoutCache(name);
        if (data != null) {
            if (value == null) {
                put(name, EMPTY_VALUE);
            }
            return data;
        }

        data = getSessionAttributeWithoutCache(name);
        if (data != null) {
            if (value == null) {
                put(name, EMPTY_VALUE);
            }
            return data;
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
        if (activity.getRequestAdapter() != null) {
            String[] values = activity.getRequestAdapter().getParameterValues(name);
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
        if (activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getAttribute(name);
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
     */
    public Object getActionResultWithoutCache(String name) {
        if (activity.getProcessResult() != null) {
            return activity.getProcessResult().getResultValue(name);
        } else {
            return null;
        }
    }

    /**
     * Returns the value of the named attribute from the session adapter
     * without storing it in the cache.
     * If no attribute of the given name exists, returns null.
     *
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *         or {@code null} if the attribute does not exist
     * @see SessionAdapter#getAttribute
     */
    public Object getSessionAttributeWithoutCache(String name) {
        if (activity.getSessionAdapter() != null) {
            return activity.getSessionAdapter().getAttribute(name);
        } else {
            return null;
        }
    }

    protected Activity getActivity() {
        return activity;
    }

}
