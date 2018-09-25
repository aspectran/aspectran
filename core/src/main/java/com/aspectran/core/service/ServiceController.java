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

/**
 * The Interface ServiceController.
 */
public interface ServiceController {

    String getServiceName();

    /**
     * Sets the service state listener.
     *
     * @param serviceStateListener the new service state listener
     */
    void setServiceStateListener(ServiceStateListener serviceStateListener);

    /**
     * Returns whether this service is derived from another root service.
     *
     * @return whether this service is derived
     */
    boolean isDerived();

    /**
     * Starts the service.
     *
     * @throws Exception if the service control fails
     */
    void start() throws Exception;

    /**
     * Restarts the service.
     *
     * @throws Exception if the service control fails
     */
    void restart() throws Exception;

    /**
     * Restarts the service.
     *
     * @param message the message to be delivered to the system before restart
     * @throws Exception if the service control fails
     */
    void restart(String message) throws Exception;

    /**
     * Pauses the service.
     *
     * @throws Exception if the service control fails
     */
    void pause() throws Exception;

    /**
     * Pauses the service for a specified period of time.
     *
     * @param timeout the maximum time to wait in milliseconds.
     * @throws Exception if the service control fails
     */
    void pause(long timeout) throws Exception;

    /**
     * Continues the service after it has been paused.
     *
     * @throws Exception if the service control fails
     */
    void resume() throws Exception;

    /**
     * Stops the service.
     * Destroys any services and resources that are dependent on this service.
     */
    void stop();

    /**
     * Returns whether this service is currently started and active.
     *
     * @return true, if the service is active; false otherwise
     */
    boolean isActive();

    /**
     * Returns whether this service has any work in progress.
     *
     * @return true, if this service is busy; false otherwise
     */
    boolean isBusy();

}
