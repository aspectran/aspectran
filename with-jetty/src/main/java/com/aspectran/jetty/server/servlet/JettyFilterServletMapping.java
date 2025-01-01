/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.FilterMapping;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * <p>Created: 4/23/24</p>
 */
public class JettyFilterServletMapping extends FilterMapping {

    JettyFilterServletMapping(String filterName, String mapping) {
        this(filterName, mapping, DispatcherType.REQUEST);
    }

    JettyFilterServletMapping(String filterName, String mapping, DispatcherType... dispatchers) {
        super();
        setFilterName(filterName);
        setServletName(mapping);
        if (dispatchers != null && dispatchers.length > 0) {
            setDispatcherTypes(EnumSet.of(dispatchers[0], dispatchers));
        }
    }

    @NonNull
    static List<JettyFilterServletMapping> of(String filterName, @NonNull JettyFilterMapping jettyFilterMapping) {
        DispatcherType[] dispatchers = jettyFilterMapping.getDispatchers();
        List<JettyFilterServletMapping> list = new ArrayList<>(dispatchers.length);
        for (DispatcherType dispatcherType : dispatchers) {
            list.add(new JettyFilterServletMapping(filterName, jettyFilterMapping.getTarget(), dispatcherType));
        }
        return list;
    }

}
