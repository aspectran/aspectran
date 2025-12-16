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
package com.aspectran.undertow.server.servlet;

import io.undertow.servlet.api.FilterMappingInfo;
import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a filter mapping for a servlet name.
 *
 * <p>Created: 2019-08-05</p>
 */
public class TowFilterServletMapping extends FilterMappingInfo {

    /**
     * Creates a new servlet filter mapping with the specified filter name and servlet name.
     * @param filterName the filter name
     * @param mapping the servlet name
     */
    TowFilterServletMapping(String filterName, String mapping) {
        this(filterName, mapping, DispatcherType.REQUEST);
    }

    /**
     * Creates a new servlet filter mapping with the specified filter name, servlet name, and dispatcher type.
     * @param filterName the filter name
     * @param mapping the servlet name
     * @param dispatcher the dispatcher type
     */
    TowFilterServletMapping(String filterName, String mapping, DispatcherType dispatcher) {
        super(filterName, MappingType.SERVLET, mapping, dispatcher);
    }

    /**
     * Creates a list of {@code TowFilterServletMapping}s from the given filter name and {@code TowFilterMapping}.
     * @param filterName the filter name
     * @param towFilterMapping the {@code TowFilterMapping}
     * @return a list of {@code TowFilterServletMapping}s
     */
    @NonNull
    static List<TowFilterServletMapping> of(String filterName, @NonNull TowFilterMapping towFilterMapping) {
        DispatcherType[] dispatchers = towFilterMapping.getDispatchers();
        List<TowFilterServletMapping> list = new ArrayList<>(dispatchers.length);
        for (DispatcherType dispatcherType : dispatchers) {
            list.add(new TowFilterServletMapping(filterName, towFilterMapping.getTarget(), dispatcherType));
        }
        return list;
    }

}
