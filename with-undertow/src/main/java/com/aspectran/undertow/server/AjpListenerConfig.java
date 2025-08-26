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
package com.aspectran.undertow.server;

import io.undertow.Undertow;

/**
 * A bean-style configuration class for an Undertow AJP listener.
 * <p>This class holds the host and port for an AJP connector, which is typically
 * used to connect a reverse proxy (e.g., Apache httpd) to the Undertow server.</p>
 *
 * <p>Created: 2019-08-21</p>
 */
public class AjpListenerConfig {

    private int port;

    private String host;

    /**
     * Returns the port number on which the AJP listener will accept connections.
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number for the AJP listener.
     * @param port the port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the host name or IP address to which the AJP listener will bind.
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name or IP address for the AJP listener.
     * @param host the host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Creates and returns an Undertow {@link Undertow.ListenerBuilder} based on this configuration.
     * @return a configured listener builder for an AJP connector
     */
    Undertow.ListenerBuilder getListenerBuilder() {
        Undertow.ListenerBuilder listenerBuilder = new Undertow.ListenerBuilder();
        listenerBuilder.setType(Undertow.ListenerType.AJP);
        listenerBuilder.setPort(port);
        listenerBuilder.setHost(host);
        return listenerBuilder;
    }

}
