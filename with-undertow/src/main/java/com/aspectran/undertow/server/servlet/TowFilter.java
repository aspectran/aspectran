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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.ClassUtils;
import io.undertow.servlet.api.FilterInfo;
import jakarta.servlet.Filter;

import java.util.Map;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowFilter extends FilterInfo {

    @SuppressWarnings("unchecked")
    public TowFilter(String name, String filterClass) throws ClassNotFoundException {
        this(name, (Class<? extends Filter>) ClassUtils.loadClass(filterClass));
    }

    public TowFilter(String name, Class<? extends Filter> filterClass) {
        super(name, filterClass);
    }

    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParam(entry.getKey(), entry.getValue());
            }
        }
    }

}
