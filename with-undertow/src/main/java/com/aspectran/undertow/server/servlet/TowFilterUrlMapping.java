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

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.servlet.api.FilterMappingInfo;
import jakarta.servlet.DispatcherType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowFilterUrlMapping extends FilterMappingInfo {

    TowFilterUrlMapping(String filterName, String mapping) {
        this(filterName, mapping, DispatcherType.REQUEST);
    }

    TowFilterUrlMapping(String filterName, String mapping, DispatcherType dispatcher) {
        super(filterName, MappingType.URL, mapping, dispatcher);
    }

    @NonNull
    static List<TowFilterUrlMapping> of(String filterName, @NonNull TowFilterMapping towFilterMapping) {
        DispatcherType[] dispatchers = towFilterMapping.getDispatchers();
        List<TowFilterUrlMapping> list = new ArrayList<>(dispatchers.length);
        for (DispatcherType dispatcherType : dispatchers) {
            list.add(new TowFilterUrlMapping(filterName, towFilterMapping.getTarget(), dispatcherType));
        }
        return list;
    }

}
