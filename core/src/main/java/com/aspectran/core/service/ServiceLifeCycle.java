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

/**
 * Defines the lifecycle management operations for an Aspectran service.
 * <p>This interface provides methods to control the state of a service, including
 * starting, stopping, restarting, pausing, and resuming. It also defines the
 * hierarchical relationship between services, allowing for nested service management.
 */
public interface ServiceLifeCycle {

    /**
     * Returns the name of this service.
     * @return the name of this service
     */
    String getServiceName();

    /**
     * Returns the root service in the service hierarchy.
     * The root service is the top-level service that manages all other services.
     * @return the root service
     */
    CoreService getRootService();

    /**
     * Returns the parent service in the service hierarchy.
     * @return the parent service, or {@code null} if this is the root service
     */
    CoreService getParentService();

    /**
     * Returns whether this service is the root service in the hierarchy.
     * @return {@code true} if this is the root service, {@code false} otherwise
     */
    boolean isRootService();

    /**
     * Returns whether the service should be started separately
     * after the root service has started.
     * @return true if the service should start separately; false otherwise
     */
    boolean isOrphan();

    /**
     * Returns whether this service is derived from a parent service.
     * A derived service typically reuses the {@link com.aspectran.core.context.ActivityContext} of its parent.
     * @return {@code true} if this service is derived, {@code false} otherwise
     */
    boolean isDerived();

    /**
     * Sets the listener for service state changes.
     * @param serviceStateListener the new service state listener
     */
    void setServiceStateListener(ServiceStateListener serviceStateListener);

    /**
     * Adds a sub-service to be managed by this service.
     * Derived services typically follow the life cycle of the root service.
     * @param serviceLifeCycle the service life cycle of the sub-service
     */
    void addService(ServiceLifeCycle serviceLifeCycle);

    /**
     * Removes a sub-service from being managed by this service.
     * @param serviceLifeCycle the service life cycle of the sub-service to remove
     */
    void removeService(ServiceLifeCycle serviceLifeCycle);

    /**
     * Withdraws this service from its parent service, effectively removing it
     * from the service hierarchy.
     */
    void withdraw();

    /**
     * Starts the service.
     * @throws Exception if an error occurs during service startup
     */
    void start() throws Exception;

    /**
     * Restarts the service.
     * @throws Exception if an error occurs during service restart
     */
    void restart() throws Exception;

    /**
     * Restarts the service, optionally providing a message.
     * @param message a message to be delivered to the system before restart
     * @throws Exception if an error occurs during service restart
     */
    void restart(String message) throws Exception;

    /**
     * Pauses the service.
     * @throws Exception if an error occurs during service pause
     */
    void pause() throws Exception;

    /**
     * Pauses the service for a specified period of time.
     * @param timeout the maximum time to wait in milliseconds.
     * @throws Exception if an error occurs during service pause
     */
    void pause(long timeout) throws Exception;

    /**
     * Resumes the service after it has been paused.
     * @throws Exception if an error occurs during service resume
     */
    void resume() throws Exception;

    /**
     * Stops the service.
     * This method also destroys any services and resources that are dependent on this service.
     */
    void stop();

    /**
     * Returns whether this service is currently started and active.
     * @return {@code true} if the service is active; {@code false} otherwise
     */
    boolean isActive();

    /**
     * Returns whether this service has any work in progress.
     * @return {@code true} if this service is busy; {@code false} otherwise
     */
    boolean isBusy();

}
