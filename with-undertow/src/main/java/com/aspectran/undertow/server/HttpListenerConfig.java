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
package com.aspectran.undertow.server;

import io.undertow.Undertow;

/**
 * <p>Created: 2019-08-21</p>
 */
public class HttpListenerConfig {

    private int port;

    private String host;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    Undertow.ListenerBuilder getListenerBuilder() {
        Undertow.ListenerBuilder listenerBuilder = new Undertow.ListenerBuilder();
        listenerBuilder.setType(Undertow.ListenerType.HTTP);
        listenerBuilder.setPort(port);
        listenerBuilder.setHost(host);
        return listenerBuilder;
    }

}
