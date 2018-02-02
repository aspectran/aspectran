/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
 * The listener interface for receiving service state change events.
 */
public interface ServiceStateListener extends EventListener {

    /**
     * This method is called when the service is started.
     */
    void started();

    /**
     * This method is called when the service is restarted.
     */
    void restarted();

    /**
     * This method is called when the service is paused.
     *
     * @param millis the number of seconds the service should pause execution
     */
    void paused(long millis);

    /**
     * This method is called when the service is paused for a period of time.
     */
    void paused();

    /**
     * This method is called when the service is resumed.
     */
    void resumed();

    /**
     * This method is called when the service is stopped.
     */
    void stopped();

}