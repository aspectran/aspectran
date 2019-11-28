/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.adapter;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;

import java.util.Map;

/**
 * The Class BasicRequestAdapter.
  *
 * @since 2016. 2. 13.
*/
public class BasicRequestAdapter extends AbstractRequestAdapter {

    /**
     * Instantiates a new BasicRequestAdapter.
     *
     * @param requestMethod the request method
     * @param adaptee the adaptee object
     */
    public BasicRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod, adaptee);
    }

    public void preparse(ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (parameterMap != null) {
            setParameterMap(parameterMap);
        }
        if (attributeMap != null) {
            setAttributeMap(attributeMap);
        }
    }

    public void preparse(RequestAdapter requestAdapter) {
        getParameterMap().putAll(requestAdapter.getParameterMap());
        setAttributeMap(requestAdapter.getAttributeMap());
        setLocale(requestAdapter.getLocale());
    }

}
