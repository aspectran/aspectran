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
package com.aspectran.jetty.server.servlet;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.FilterMapping;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A {@link FilterMapping} that maps a filter to a URL pattern.
 *
 * <p>Created: 4/23/24</p>
 */
public class JettyFilterUrlMapping extends FilterMapping {

    /**
     * Constructs a {@code JettyFilterUrlMapping} instance with a specified filter name and mapping.
     * @param filterName the name of the filter to be mapped
     * @param mapping the target URL or servlet name to map the filter to
     */
    JettyFilterUrlMapping(String filterName, String mapping) {
        this(filterName, mapping, DispatcherType.REQUEST);
    }

    /**
     * Constructs a {@code JettyFilterUrlMapping} instance with a specified filter name, mapping, and dispatcher types.
     * @param filterName the name of the filter to be mapped
     * @param mapping the target URL or servlet name to map the filter to
     * @param dispatchers the dispatcher types this filter will apply to; if not specified, default dispatcher types may apply
     */
    JettyFilterUrlMapping(String filterName, String mapping, DispatcherType... dispatchers) {
        super();
        setFilterName(filterName);
        setPathSpec(mapping);
        if (dispatchers != null && dispatchers.length > 0) {
            setDispatcherTypes(EnumSet.of(dispatchers[0], dispatchers));
        }
    }

    /**
     * Constructs a {@code JettyFilterUrlMapping} instance with a specified filter name and mapping.
     * @param filterName the name of the filter to be mapped
     * @param jettyFilterMapping the filter mapping containing the target URL or servlet name to map the filter to
     * @return a list of JettyFilterUrlMapping instances
     */
    @NonNull
    static List<JettyFilterUrlMapping> of(String filterName, @NonNull JettyFilterMapping jettyFilterMapping) {
        DispatcherType[] dispatchers = jettyFilterMapping.getDispatchers();
        List<JettyFilterUrlMapping> list = new ArrayList<>(dispatchers.length);
        for (DispatcherType dispatcherType : dispatchers) {
            list.add(new JettyFilterUrlMapping(filterName, jettyFilterMapping.getTarget(), dispatcherType));
        }
        return list;
    }

}
