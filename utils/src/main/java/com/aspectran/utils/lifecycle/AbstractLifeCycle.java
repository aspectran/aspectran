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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * An abstract base class that provides a default implementation for the {@link LifeCycle} interface.
 * <p>This class manages the internal state transitions (STOPPED, STARTING, STARTED, STOPPING, FAILED)
 * and provides a mechanism for notifying registered {@link LifeCycle.Listener}s of state changes.
 * Subclasses must implement the {@link #doStart()} and {@link #doStop()} methods to provide
 * their specific startup and shutdown logic.</p>
 */
public abstract class AbstractLifeCycle implements LifeCycle, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLifeCycle.class);

    private final ReentrantLock lock = new ReentrantLock();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private volatile State state = State.STOPPED;

    /**
     * Starts the component.
     * <p>This method handles state transitions and listener notifications.
     * Subclasses should implement their specific startup logic in {@link #doStart()}.</p>
     * @throws Exception If the component fails to start
     */
    @Override
    public final void start() throws Exception {
        lock.lock();
        try {
            if (state == State.STARTED) {
                return;
            }
            if (state == State.STARTING) {
                throw new IllegalStateException("Starting");
            }
            setStarting();
            doStart();
            setStarted();
        } catch (Exception e) {
            setFailed(e);
            throw e;
        } finally {
            lock.unlock();
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
        lock.lock();
        try {
            if (state == State.STOPPED) {
                return;
            }
            if (state == State.STOPPING) {
                throw new IllegalStateException("Stopping");
            }
            setStopping();
            doStop();
            setStopped();
        } catch (Exception e) {
            setFailed(e);
            throw e;
        } finally {
            lock.unlock();
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
        return state.isRunning();
    }

    @Override
    public boolean isStarted() {
        return state.isStarted();
    }

    @Override
    public boolean isStarting() {
        return state.isStarting();
    }

    @Override
    public boolean isStopping() {
        return state.isStopping();
    }

    @Override
    public boolean isStopped() {
        return state.isStopped();
    }

    @Override
    public boolean isStoppable() {
        State current = state;
        return (current == State.STARTED || current == State.FAILED);
    }

    @Override
    public boolean isFailed() {
        return state.isFailed();
    }

    /**
     * Adds a {@link LifeCycle.Listener} to this component.
     * @param listener the listener to add
     */
    @Override
    public void addLifeCycleListener(LifeCycle.Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a {@link LifeCycle.Listener} from this component.
     * @param listener the listener to remove
     */
    @Override
    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Returns the current state of the component.
     * @return the current state
     */
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    private void setStarting() {
        transition(State.STARTING, listener -> listener.lifeCycleStarting(this));
    }

    private void setStarted() {
        transition(State.STARTED, listener -> listener.lifeCycleStarted(this));
    }

    private void setStopping() {
        transition(State.STOPPING, listener -> listener.lifeCycleStopping(this));
    }

    private void setStopped() {
        transition(State.STOPPED, listener -> listener.lifeCycleStopped(this));
    }

    /**
     * Sets the component's state to FAILED and notifies listeners.
     * @param cause the cause of the failure
     */
    private void setFailed(Throwable cause) {
        state = State.FAILED;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is FAILED", this, cause);
        }
        notifyListeners(listener -> listener.lifeCycleFailure(this, cause));
    }

    private void transition(State newState, Consumer<Listener> notifier) {
        state = newState;
        if (logger.isDebugEnabled()) {
            logger.debug("{} is {}", this, newState);
        }
        notifyListeners(notifier);
    }

    private void notifyListeners(Consumer<Listener> notifier) {
        if (notifier != null) {
            for (Listener listener : listeners) {
                try {
                    notifier.accept(listener);
                } catch (Throwable t) {
                    logger.warn("Exception while notifying LifeCycle listener: {}", listener, t);
                }
            }
        }
    }

}
