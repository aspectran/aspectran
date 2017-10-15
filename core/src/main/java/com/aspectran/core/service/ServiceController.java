/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

/**
 * The Interface ServiceController.
 */
public interface ServiceController {

    String getServiceName();

    /**
     * Sets the aspectran service life-cycle listener.
     *
     * @param aspectranServiceStateListener the new aspectran service life-cycle listener
     */
    void setServiceStateListener(ServiceStateListener aspectranServiceStateListener);

    /**
     * Returns whether this AspectranService is derived.
     *
     * @return whether the AspectranService is derived
     */
    boolean isDerived();

    /**
     * Starts the Aspectran Service.
     *
     * @throws Exception if the service control fails
     */
    void start() throws Exception;

    /**
     * Restarts the Aspectran Service.
     *
     * @throws Exception if the service control fails
     */
    void restart() throws Exception;

    /**
     * Pauses the Aspectran Service.
     *
     * @throws Exception if the service control fails
     */
    void pause() throws Exception;

    /**
     * Pauses the Aspectran Service for a specified period of time.
     *
     * @param timeout the maximum time to wait in milliseconds.
     * @throws Exception if the service control fails
     */
    void pause(long timeout) throws Exception;

    /**
     * Continues the Aspectran Service after it has been paused.
     *
     * @throws Exception if the service control fails
     */
    void resume() throws Exception;

    /**
     * Stops the Aspectran Service.
     * Destroys any services and resources that are dependent on this service.
     */
    void stop();

    /**
     * Returns whether this AspectranService is currently started.
     *
     * @return whether the AspectranService is still active
     */
    boolean isActive();

}
