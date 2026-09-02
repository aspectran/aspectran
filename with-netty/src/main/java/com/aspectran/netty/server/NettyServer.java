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

import com.aspectran.utils.lifecycle.LifeCycle;
import io.netty.channel.Channel;
import io.netty.util.Version;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract for an embedded Netty server managed by Aspectran.
 * <p>This interface extends Aspectran's {@link LifeCycle} to manage the server's
 * start and stop operations, active ports, and underlying Netty channels.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public interface NettyServer extends LifeCycle {

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

}
