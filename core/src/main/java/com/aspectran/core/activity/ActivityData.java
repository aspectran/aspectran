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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map of data for saving activity results.
 * It is often used as a model for providing data used in views.
 *
 * <p>This class is generally not thread-safe.
 * It is primarily designed for use in a single thread only.</p>
 */
public class ActivityData extends HashMap<String, Object> {

    @Serial
    private static final long serialVersionUID = -4557424414862800204L;

    /**
     * Placeholder marker indicating that a value was preemptively cached as absent.
     * Used internally to avoid repeated lookups for missing entries.
     */
    private static final Object PREEMPTED = new Object();

    /**
     * The backing activity used to lazily resolve parameters, attributes,
     * session attributes, and action results.
     */
    private final Activity activity;

    /**
     * Instantiates a new ActivityData.
     * @param activity the activity
     */
    ActivityData(Activity activity) {
        super();
        this.activity = activity;
        refresh();
    }

    /**
     * Returns the value to which the specified key is mapped, performing a lazy lookup
     * from action results, request attributes/parameters, and session attributes if not
     * already cached in this map. Successfully resolved values are cached for subsequent
     * access; missing values are marked to avoid repeated lookups.
     * @param key the key whose associated value is to be returned
     * @return the mapped value, or {@code null} if none
     */
    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value != null && !value.equals(PREEMPTED)) {
            return value;
        }
        if (key == null) {
            return null;
        }

        String name = key.toString();
        value = getActionResultWithoutCache(name);
        if (value != null) {
            preempt(name, value);
            return value;
        }

        value = getAttributeWithoutCache(name);
        if (value != null) {
            preempt(name, value);
            return value;
        }

        value = getParameterWithoutCache(name);
        if (value != null) {
            preempt(name, value);
            return value;
        }

        value = getSessionAttributeWithoutCache(name);
        if (value != null) {
            preempt(name, value);
            return value;
        }

        return null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * Prevents storing this map instance itself as a value to avoid self-references.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if {@code value} is this map instance
     */
    @Override
    public Object put(String key, Object value) {
        if (this == value) {
            throw new IllegalArgumentException("Same instance as this map can not be stored");
        }
        return super.put(key, value);
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     * This method triggers lazy resolution for preempted keys.
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     */
    @Override
    public boolean containsKey(Object key) {
        return (super.get(key) != null);
    }

    /**
     * Returns a Collection view of the values contained in this map.
     * Values are resolved lazily for preempted entries.
     * @return a collection view of the values contained in this map
     */
    @Override
    @NonNull
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>(size());
        for (String name : keySet()) {
            list.add(get(name));
        }
        return list;
    }

    /**
     * Returns a Set view of the mappings contained in this map.
     * Preempted entries are resolved on access so that callers see actual values
     * instead of internal placeholders.
     * @return a set view of the mappings contained in this map
     */
    @Override
    @NonNull
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> set = new HashSet<>();
        for (Map.Entry<String, Object> entry : super.entrySet()) {
            if (entry.getValue() == PREEMPTED) {
                String key = entry.getKey();
                Object value = get(key);
                set.add(new AbstractMap.SimpleEntry<>(key, value));
            } else {
                set.add(entry);
            }
        }
        return set;
    }

    /**
     * Returns the value of the request parameter from the request adapter
     * without storing it in the cache.
     * If the parameter does not exist, returns null.
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
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *         or {@code null} if the attribute does not exist
     * @see SessionAdapter#getAttribute
     */
    public Object getSessionAttributeWithoutCache(String name) {
        if (activity.hasSessionAdapter()) {
            return activity.getSessionAdapter().getAttribute(name);
        } else {
            return null;
        }
    }

    /**
     * Refreshes this map by preemptively marking all known parameter, attribute,
     * session attribute, and action result names for lazy resolution. This avoids
     * immediate value retrieval while guaranteeing that subsequent access will
     * attempt to resolve from the underlying sources.
     */
    public void refresh() {
        if (activity.getRequestAdapter() != null) {
            for (String name : activity.getRequestAdapter().getParameterNames()) {
                preempt(name);
            }
            for (String name : activity.getRequestAdapter().getAttributeNames()) {
                Object value = activity.getRequestAdapter().getAttribute(name);
                if (this != value) {
                    preempt(name);
                }
            }
        }
        if (activity.hasSessionAdapter()) {
            SessionAdapter sessionAdapter = activity.getSessionAdapter();
            Enumeration<String> enumer = sessionAdapter.getAttributeNames();
            if (enumer != null) {
                while (enumer.hasMoreElements()) {
                    String name = enumer.nextElement();
                    Object value = sessionAdapter.getAttribute(name);
                    if (this != value) {
                        preempt(name);
                    }
                }
            }
        }
        if (activity.getProcessResult() != null) {
            for (ContentResult cr : activity.getProcessResult()) {
                for (ActionResult ar : cr) {
                    if (ar.getActionId() != null && this != ar.getResultValue()) {
                        preempt(ar.getActionId());
                    }
                }
            }
        }
    }

    /**
     * Marks the given name as preempted, i.e., eligible for lazy resolution on first access.
     * @param name the entry name to preempt
     */
    private void preempt(String name) {
        preempt(name, super.get(name));
    }

    /**
     * Internal helper to mark a name as preempted only when a current mapping has no value.
     * @param name the entry name
     * @param value the current mapped value (may be {@code null})
     */
    private void preempt(String name, Object value) {
        if (value == null) {
            put(name, PREEMPTED);
        }
    }

}
