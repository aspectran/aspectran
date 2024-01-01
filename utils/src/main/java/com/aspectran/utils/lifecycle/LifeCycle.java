/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.utils.lifecycle;

import java.util.EventListener;

/**
 * The lifecycle interface for generic components.
 * <p>
 * Classes implementing this interface have a defined life cycle
 * defined by the methods of this interface.</p>
 */
public interface LifeCycle {

    String STOPPED = "STOPPED";

    String FAILED = "FAILED";

    String STARTING = "STARTING";

    String STARTED = "STARTED";

    String STOPPING = "STOPPING";

    String RUNNING = "RUNNING";

    /**
     * Starts the component.
     * @throws Exception If the component fails to start
     * @see #isStarted()
     * @see #stop()
     * @see #isFailed()
     */
    void start() throws Exception;

    /**
     * Stops the component.
     * The component may wait for current activities to complete
     * normally, but it can be interrupted.
     * @throws Exception If the component fails to stop
     * @see #isStopped()
     * @see #start()
     * @see #isFailed()
     */
    void stop() throws Exception;

    /**
     * @return true if the component is starting or has been started
     */
    boolean isRunning();

    /**
     * @return true if the component has been started.
     * @see #start()
     * @see #isStarting()
     */
    boolean isStarted();

    /**
     * @return true if the component is starting
     * @see #isStarted()
     */
    boolean isStarting();

    /**
     * @return true if the component is stopping
     * @see #isStopped()
     */
    boolean isStopping();

    /**
     * @return true if the component has been stopped
     * @see #stop()
     * @see #isStopping()
     */
    boolean isStopped();

    /**
     * @return true if the component has failed to start or has failed to stop
     */
    boolean isFailed();

    void addLifeCycleListener(LifeCycle.Listener listener);

    void removeLifeCycleListener(LifeCycle.Listener listener);

    String getState();

    /**
     * A listener for Lifecycle events.
     */
    interface Listener extends EventListener {

        default void lifeCycleStarting(LifeCycle event) {
        }

        default void lifeCycleStarted(LifeCycle event) {
        }

        default void lifeCycleFailure(LifeCycle event, Throwable cause) {
        }

        default void lifeCycleStopping(LifeCycle event) {
        }

        default void lifeCycleStopped(LifeCycle event) {
        }

    }

    /**
     * Utility to start an object if it is a LifeCycle and to convert
     * any exception thrown to a {@link RuntimeException}.
     * @param object the instance to start.
     * @throws RuntimeException if the call to start throws an exception
     */
    static void start(Object object) {
        if (object instanceof LifeCycle) {
            try {
                ((LifeCycle)object).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Utility to stop an object if it is a LifeCycle and to convert
     * any exception thrown to a {@link RuntimeException}.
     * @param object the instance to stop.
     * @throws RuntimeException if the call to stop throws an exception
     */
    static void stop(Object object) {
        if (object instanceof LifeCycle) {
            try {
                ((LifeCycle)object).stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
