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
package com.aspectran.core.service;

import java.util.EventListener;

/**
 * Listener interface for receiving service state change events.
 * <p>Implementations of this interface can react to various lifecycle events
 * of a {@link ServiceLifeCycle}, such as starting, stopping, pausing, and resuming.
 */
public interface ServiceStateListener extends EventListener {

    /**
     * Called when the service has been successfully started.
     */
    void started();

    /**
     * Called when the service has been successfully stopped.
     */
    void stopped();

    /**
     * Called when the service has been paused for a specific duration.
     * @param millis the number of milliseconds the service should pause execution
     */
    void paused(long millis);

    /**
     * Called when the service has been paused indefinitely.
     */
    void paused();

    /**
     * Called when the service has been resumed after being paused.
     */
    void resumed();

}
