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
package com.aspectran.web.websocket.jsr356;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.utils.Assert;
import com.aspectran.utils.logging.LoggingGroupHelper;
import jakarta.websocket.server.HandshakeRequest;

import java.util.List;
import java.util.Map;

/**
 * <p>Created: 2025-03-24</p>
 */
public abstract class AbstractEndpoint {

    private HandshakeRequest request;

    private Map<String, List<String>> parameterMap;

    public HandshakeRequest getRequest() {
        Assert.state(this.request != null, "Handshake request not set");
        return request;
    }

    void setRequest(HandshakeRequest request) {
        Assert.notNull(request, "Handshake request must not be null");
        Assert.state(this.request == null, "Handshake request has already been set");
        this.request = request;
        this.parameterMap = request.getParameterMap();
    }

    public Map<String, List<String>> getParameterMap() {
        Assert.state(parameterMap != null, "Handshake request not set");
        return parameterMap;
    }

    public String getParameter(String name) {
        List<String> values = getParameterMap().get(name);
        return (values != null && !values.isEmpty() ? values.get(0) : null);
    }

    public String[] getParameterValues(String name) {
        List<String> values = getParameterMap().get(name);
        return (values != null ? values.toArray(new String[0]) : null);
    }

    protected void setLoggingGroup() {
        ActivityContext context = CoreServiceHolder.findActivityContext(getClass());
        if (context != null && context.getName() != null) {
            LoggingGroupHelper.set(context.getName());
        }
    }

}
