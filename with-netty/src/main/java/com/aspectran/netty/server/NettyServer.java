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
package com.aspectran.netty.server;

import com.aspectran.core.component.session.SessionManagerProvider;
import com.aspectran.utils.lifecycle.LifeCycle;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.util.Version;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Defines the contract for an embedded Netty server managed by Aspectran.
 * <p>This interface extends Aspectran's {@link LifeCycle} to manage the server's
 * start and stop operations, active ports, and underlying Netty channels.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public interface NettyServer extends LifeCycle, SessionManagerProvider {

    /**
     * Returns the version of the Netty transport library.
     * @return the Netty version string
     */
    static String getVersion() {
        Map<String, Version> versions = Version.identify();
        Version transportVersion = versions.get("netty-transport");
        if (transportVersion != null) {
            return transportVersion.artifactVersion();
        }
        for (Version v : versions.values()) {
            if (v != null && v.artifactVersion() != null) {
                return v.artifactVersion();
            }
        }
        return "unknown";
    }

    /**
     * Returns the list of active Netty server channels.
     * @return the list of active channels
     */
    List<Channel> getActiveChannels();

    /**
     * Returns the port on which the primary listener is bound.
     * @return the active port, or -1 if the server is not running
     */
    int getActivePort();

    /**
     * Returns the port on which the listener at the specified index is bound.
     * @param index the listener index
     * @return the active port
     */
    int getActivePort(int index);

    /**
     * Returns the context router that manages context paths and route dispatch.
     * @return the context router
     */
    NettyContextRouter getContextRouter();

    /**
     * Returns the worker name or thread name prefix.
     * @return the worker name
     */
    String getWorkerName();

    /**
     * Returns the boss event loop group that accepts incoming connections.
     * @return the boss event loop group
     */
    EventLoopGroup getBossGroup();

    /**
     * Returns the worker event loop group that processes I/O for accepted channels.
     * @return the worker event loop group
     */
    EventLoopGroup getWorkerGroup();

    /**
     * Returns the configured number of boss threads.
     * @return the boss threads count
     */
    int getBossThreads();

    /**
     * Returns the configured number of worker threads.
     * @return the worker threads count
     */
    int getWorkerThreads();

    /**
     * Returns the executor service used for asynchronous request dispatching.
     * @return the request executor, or null if not yet initialized or not configured
     */
    ExecutorService getRequestExecutor();

    /**
     * Returns the underlying {@link ThreadPoolExecutor} if standard thread pooling is used.
     * @return the thread pool executor, or null if virtual threads or a custom non-thread-pool executor is used
     */
    ThreadPoolExecutor getThreadPoolExecutor();

    /**
     * Returns whether Java 21 virtual threads are enabled for request dispatching.
     * @return true if virtual threads are enabled; false otherwise
     */
    boolean isVirtualThreads();

    /**
     * Returns the number of active requests currently being processed.
     * @return the active request count
     */
    int getActiveRequests();

    /**
     * Returns the total number of requests dispatched since server start.
     * @return the total request count
     */
    long getTotalRequests();

}
