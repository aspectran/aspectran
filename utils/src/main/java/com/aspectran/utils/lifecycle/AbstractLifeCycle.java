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

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.AutoLock;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Basic implementation of the life cycle interface for components.
 */
public abstract class AbstractLifeCycle implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLifeCycle.class);

    private final CopyOnWriteArrayList<LifeCycle.Listener> listeners = new CopyOnWriteArrayList<>();

    private final AutoLock lock = new AutoLock();

    private static final int STATE_FAILED = -1;

    private static final int STATE_STOPPED = 0;

    private static final int STATE_STARTING = 1;

    private static final int STATE_STARTED = 2;

    private static final int STATE_STOPPING = 3;

    private volatile int state = STATE_STOPPED;

    protected void doStart() throws Exception {
    }

    protected void doStop() throws Exception {
    }

    @Override
    public final void start() throws Exception {
        try (AutoLock ignored = lock.lock()) {
            try {
                if (state == STATE_STARTED || state == STATE_STARTING) {
                    return;
                }
                setStarting();
                doStart();
                setStarted();
            } catch (Throwable e) {
                setFailed(e);
                throw e;
            }
        }
    }

    @Override
    public final void stop() throws Exception {
        try (AutoLock ignored = lock.lock()) {
            try {
                if (state == STATE_STOPPING || state == STATE_STOPPED) {
                    return;
                }
                setStopping();
                doStop();
                setStopped();
            } catch (Throwable e) {
                setFailed(e);
                throw e;
            }
        }
    }

    @Override
    public boolean isRunning() {
        final int state = this.state;
        return (state == STATE_STARTED || state == STATE_STARTING);
    }

    @Override
    public boolean isStarted() {
        return (state == STATE_STARTED);
    }

    @Override
    public boolean isStarting() {
        return (state == STATE_STARTING);
    }

    @Override
    public boolean isStopping() {
        return (state == STATE_STOPPING);
    }

    @Override
    public boolean isStopped() {
        return (state == STATE_STOPPED);
    }

    @Override
    public boolean isFailed() {
        return (state == STATE_FAILED);
    }

    @Override
    public void addLifeCycleListener(LifeCycle.Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getState() {
        switch (state) {
            case STATE_FAILED:
                return FAILED;
            case STATE_STARTING:
                return STARTING;
            case STATE_STARTED:
                return STARTED;
            case STATE_STOPPING:
                return STOPPING;
            case STATE_STOPPED:
                return STOPPED;
        }
        return null;
    }

    public static String getState(LifeCycle lc) {
        if (lc.isStarting()) {
            return STARTING;
        }
        if (lc.isStarted()) {
            return STARTED;
        }
        if (lc.isStopping()) {
            return STOPPING;
        }
        if (lc.isStopped()) {
            return STOPPED;
        }
        return FAILED;
    }

    private void setStarted() {
        state = STATE_STARTED;
        if (logger.isDebugEnabled()) {
            logger.debug(STARTED + " " + this);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleStarted(this);
        }
    }

    private void setStarting() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting " + this);
        }
        state = STATE_STARTING;
        for (Listener listener : listeners) {
            listener.lifeCycleStarting(this);
        }
    }

    private void setStopping() {
        if (logger.isDebugEnabled()) {
            logger.debug("Stopping " + this);
        }
        state = STATE_STOPPING;
        for (Listener listener : listeners) {
            listener.lifeCycleStopping(this);
        }
    }

    private void setStopped() {
        state = STATE_STOPPED;
        if (logger.isDebugEnabled()) {
            logger.debug(STOPPED + " " + this);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleStopped(this);
        }
    }

    private void setFailed(Throwable th) {
        state = STATE_FAILED;
        if (logger.isDebugEnabled()) {
            logger.warn(FAILED + " " + this + ": " + th, th);
        }
        for (Listener listener : listeners) {
            listener.lifeCycleFailure(this, th);
        }
    }

    @Override
    public String toString() {
        Class<?> clazz = getClass();
        String name = clazz.getSimpleName();
        if (StringUtils.isEmpty(name) && clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            name = clazz.getSimpleName();
        }
        return String.format("%s@%x{%s}", name, hashCode(), getState());
    }

}
