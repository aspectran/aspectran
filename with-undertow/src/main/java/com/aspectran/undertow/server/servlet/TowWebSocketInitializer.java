/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

/**
 * Initializer for WebSocket Support in Undertow.
 */
public class TowWebSocketInitializer  {

    private boolean directBuffers = false;

    private int bufferSize = 1024;

    private int maximumPoolSize = -1;

    private int threadLocalCacheSize = 12;

    public void setDirectBuffers(boolean directBuffers) {
        this.directBuffers = directBuffers;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public void setThreadLocalCacheSize(int threadLocalCacheSize) {
        this.threadLocalCacheSize = threadLocalCacheSize;
    }

    public void initialize(DeploymentInfo deploymentInfo) {
        if (!deploymentInfo.getServletContextAttributes().containsKey(WebSocketDeploymentInfo.ATTRIBUTE_NAME)) {
            deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME,
                    new WebSocketDeploymentInfo().setBuffers(
                            new DefaultByteBufferPool(directBuffers, bufferSize, maximumPoolSize, threadLocalCacheSize)));
            deploymentInfo.addSessionListener(new TowServletContext.WebSocketConnectionsUnboundListener());
        }
    }

}
