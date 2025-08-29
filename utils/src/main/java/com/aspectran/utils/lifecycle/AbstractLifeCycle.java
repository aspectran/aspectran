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
package com.aspectran.utils.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An abstract base class that provides a default implementation for the {@link LifeCycle} interface.
 * <p>This class manages the internal state transitions (STOPPED, STARTING, STARTED, STOPPING, FAILED)
 * and provides a mechanism for notifying registered {@link LifeCycle.Listener}s of state changes.
 * Subclasses must implement the {@link #doStart()} and {@link #doStop()} methods to provide
 * their specific startup and shutdown logic.</p>
 */
public abstract class AbstractLifeCycle implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLifeCycle.class);

    private static final int _STOPPED = 0;
    private static final int _STARTING = 1;
    private static final int _STARTED = 2;
    private static final int _STOPPING = 3;
    private static final int _FAILED = 4;

    private final Object _lock = new Object();
    private volatile int state = _STOPPED;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Starts the component.
     * <p>This method handles state transitions and listener notifications.
     * Subclasses should implement their specific startup logic in {@link #doStart()}.</p>
     * @throws Exception If the component fails to start
     */
    @Override
    public final void start() throws Exception {
        synchronized (_lock) {
            try {
                if (state == _STARTED) {
                    return;
                }
                if (state == _STARTING) {
                    throw new IllegalStateException("Starting");
                }
                setStarting();
                doStart();
                setStarted();
            } catch (Exception e) {
                setFailed(e);
                throw e;
            }
        }
    }

    /**
     * Stops the component.
     * <p>This method handles state transitions and listener notifications.
     * Subclasses should implement their specific shutdown logic in {@link #doStop()}.</p>
     * @throws Exception If the component fails to stop
     */
    @Override
    public final void stop() throws Exception {
        synchronized (_lock) {
            try {
                if (state == _STOPPED) {
                    return;
                }
                if (state == _STOPPING) {
                    throw new IllegalStateException("Stopping");
                }
                setStopping();
                doStop();
                setStopped();
            } catch (Exception e) {
                setFailed(e);
                throw e;
            }
        }
    }

    /**
     * Implements the specific startup logic for the component.
     * Subclasses must override this method.
     * @throws Exception if the component fails to start
     */
    protected abstract void doStart() throws Exception;

    /**
     * Implements the specific shutdown logic for the component.
     * Subclasses must override this method.
     * @throws Exception if the component fails to stop
     */
    protected abstract void doStop() throws Exception;

    @Override
    public boolean isRunning() {
        return (state == _STARTED || state == _STARTING);
    }

    @Override
    public boolean isStarted() {
        return (state == _STARTED);
    }

    @Override
    public boolean isStarting() {
        return (state == _STARTING);
    }

    @Override
    public boolean isStopping() {
        return (state == _STOPPING);
    }

    @Override
    public boolean isStopped() {
        return (state == _STOPPED);
    }

    @Override
    public boolean isStoppable() {
        return (isStarted() || isFailed());
    }

    @Override
    public boolean isFailed() {
        return (state == _FAILED);
    }

    /**
     * Adds a {@link LifeCycle.Listener} to this component.
     * @param listener the listener to add
     */
    @Override
    public void addLifeCycleListener(LifeCycle.Listener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a {@link LifeCycle.Listener} from this component.
     * @param listener the listener to remove
     */
    @Override
    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the current state of the component as a string.
     * @return the current state string (e.g., "STARTED", "STOPPED")
     */
    @Override
    public String getState() {
        return switch (state) {
            case _STARTING -> LifeCycle.STARTING;
            case _STARTED -> LifeCycle.STARTED;
            case _STOPPING -> LifeCycle.STOPPING;
            case _STOPPED -> LifeCycle.STOPPED;
            case _FAILED -> LifeCycle.FAILED;
            default -> throw new IllegalStateException("State: " + state);
        };
    }

    private void setStarting() {
        state = _STARTING;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is STARTING", this);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleStarting(this);
        }
    }

    private void setStarted() {
        state = _STARTED;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is STARTED", this);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleStarted(this);
        }
    }

    private void setStopping() {
        state = _STOPPING;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is STOPPING", this);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleStopping(this);
        }
    }

    private void setStopped() {
        state = _STOPPED;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is STOPPED", this);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleStopped(this);
        }
    }

    /**
     * Sets the component's state to FAILED and notifies listeners.
     * @param cause the cause of the failure
     */
    private void setFailed(Throwable cause) {
        state = _FAILED;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is FAILED", this, cause);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleFailure(this, cause);
        }
    }

}
