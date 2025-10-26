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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class providing a skeletal implementation of the {@link ServiceLifeCycle} interface.
 * <p>This class handles the common logic for managing service states (started, stopped, paused, resumed),
 * and orchestrates the lifecycle of sub-services. It ensures thread-safe operations for lifecycle
 * transitions and provides hooks for custom startup and shutdown logic.
 */
public abstract class AbstractServiceLifeCycle implements ServiceLifeCycle {

    private final Logger logger = LoggerFactory.getLogger(AbstractServiceLifeCycle.class);

    private final Object lock = new Object();

    private final List<ServiceLifeCycle> subServices = new ArrayList<>();

    private final CoreService rootService;

    private final CoreService parentService;

    private ServiceStateListener serviceStateListener;

    /** Flag that indicates whether this service is active */
    private volatile boolean active;

    public AbstractServiceLifeCycle(@Nullable CoreService parentService) {
        this.parentService = parentService;
        if (parentService != null) {
            this.rootService = parentService.getRootService();
            this.parentService.getServiceLifeCycle().addService(this);
        } else {
            this.rootService = (CoreService)this;
        }
    }

    @Override
    @NonNull
    public CoreService getRootService() {
        return rootService;
    }

    @Override
    public CoreService getParentService() {
        return parentService;
    }

    @Override
    public boolean isRootService() {
        return (parentService == null);
    }

    @Override
    public boolean isOrphan() {
        return (parentService == null || parentService.getServiceLifeCycle().isActive());
    }

    @Override
    public void setServiceStateListener(ServiceStateListener serviceStateListener) {
        this.serviceStateListener = serviceStateListener;
    }

    @Override
    public void addService(ServiceLifeCycle serviceLifeCycle) {
        synchronized (lock) {
            subServices.add(serviceLifeCycle);
        }
    }

    @Override
    public void removeService(@NonNull ServiceLifeCycle serviceLifeCycle) {
        Assert.state(!serviceLifeCycle.isRootService(), "Root service cannot be withdrawn");
        Assert.state(!serviceLifeCycle.isActive(), "Not yet stopped service: " + serviceLifeCycle);
        synchronized (lock) {
            subServices.remove(serviceLifeCycle);
        }
    }

    @Override
    public void withdraw() {
        if (getParentService() != null) {
            getParentService().getServiceLifeCycle().removeService(this);
        }
    }

    @Override
    public void start() throws Exception {
        synchronized (lock) {
            Assert.state(!active, getServiceName() + " has already started");

            String oldThreadName = changeThreadName();
            try {
                logger.info("Starting {}", getServiceName());

                doStart();

                logger.info("Started {}", getServiceName());

                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.start();
                }

                if (serviceStateListener != null) {
                    serviceStateListener.started();
                }

                active = true;
            } finally {
                restoreThreadName(oldThreadName);
            }
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (!active) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is not running, will do nothing", getServiceName());
                }
                return;
            }

            String oldThreadName = changeThreadName();
            try {
                if (serviceStateListener != null) {
                    try {
                        serviceStateListener.paused();
                        serviceStateListener.stopped();
                    } catch (Exception e) {
                        logger.warn(e.getMessage(), e);
                    }
                }

                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.stop();
                }

                try {
                    logger.info("Stopping {}", getServiceName());

                    doStop();

                    logger.info("Stopped {}", getServiceName());
                } catch (Exception e) {
                    logger.error("{} did not stop normally", getServiceName(), e);
                }

                active = false;
            } finally {
                restoreThreadName(oldThreadName);
            }
        }
    }

    @Override
    public void restart() throws Exception {
        synchronized (lock) {
            Assert.state(isRootService(), "Must be a root service to restart");
            Assert.state(active, getServiceName() + " is not running");

            String oldThreadName = changeThreadName();
            try {
                logger.info("Restarting {}", getServiceName());

                if (serviceStateListener != null) {
                    serviceStateListener.paused();
                }

                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.stop();
                }

                active = false;
                doStop();

                if (serviceStateListener != null) {
                    try {
                        serviceStateListener.stopped();
                    } catch (Exception e) {
                        logger.warn(e.getMessage(), e);
                    }
                }

                doStart();
                active = true;

                logger.info("Restarted {}", getServiceName());

                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.start();
                }

                if (serviceStateListener != null) {
                    serviceStateListener.started();
                }
            } finally {
                restoreThreadName(oldThreadName);
            }
        }
    }

    @Override
    public void restart(String message) throws Exception {
        restart();
    }

    @Override
    public void pause() throws Exception {
        synchronized (lock) {
            if (!active) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is not running, will do nothing", getServiceName());
                }
                return;
            }

            String oldThreadName = changeThreadName();
            try {
                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.pause();
                }

                if (serviceStateListener != null) {
                    serviceStateListener.paused();
                }

                logger.info("Pause {}", getServiceName());
            } finally {
                restoreThreadName(oldThreadName);
            }
        }
    }

    @Override
    public void pause(long timeout) throws Exception {
        synchronized (lock) {
            if (!active) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is not running, will do nothing", getServiceName());
                }
                return;
            }

            String oldThreadName = changeThreadName();
            try {
                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.pause(timeout);
                }

                if (serviceStateListener != null) {
                    serviceStateListener.paused(timeout);
                }

                logger.info("Pause {}, resume after {}ms", getServiceName(), timeout);
            } finally {
                restoreThreadName(oldThreadName);
            }
        }
    }

    @Override
    public void resume() throws Exception {
        synchronized (lock) {
            if (!active) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is not running, will do nothing", getServiceName());
                }
                return;
            }

            String oldThreadName = changeThreadName();
            try {
                for (ServiceLifeCycle serviceLifeCycle : subServices) {
                    serviceLifeCycle.resume();
                }

                if (serviceStateListener != null) {
                    serviceStateListener.resumed();
                }

                logger.info("Resume {}", getServiceName());
            } finally {
                restoreThreadName(oldThreadName);
            }
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public String getServiceName() {
        return ObjectUtils.simpleIdentityToString(this);
    }

    /**
     * Returns the name of the context associated with this service.
     * <p>This implementation returns {@code null}. Subclasses may override this
     * to provide a specific context name, which can be used for purposes
     * like thread naming during service lifecycle operations.
     * @return the context name, or {@code null} if not applicable
     */
    public String getContextName() {
        return null;
    }

    /**
     * Returns the synchronization lock object for this service.
     * All lifecycle state transitions are synchronized on this lock.
     * @return the lock object
     */
    protected Object getLock() {
        return lock;
    }

    /**
     * Subclasses must implement this method to perform the actual startup logic.
     * This method is called by {@link #start()} within a synchronized block.
     * @throws Exception if an error occurs during startup
     */
    protected abstract void doStart() throws Exception;

    /**
     * Subclasses must implement this method to perform the actual shutdown logic.
     * This method is called by {@link #stop()} within a synchronized block.
     * @throws Exception if an error occurs during shutdown
     */
    protected abstract void doStop() throws Exception;

    private String changeThreadName() {
        String oldThreadName = null;
        if (getContextName() != null) {
            oldThreadName = Thread.currentThread().getName();
            String newContextName = (isRootService() ? getContextName() : oldThreadName + ":" + getContextName());
            Thread.currentThread().setName(newContextName);
        }
        return oldThreadName;
    }

    private void restoreThreadName(String oldThreadName) {
        if (oldThreadName != null) {
            Thread.currentThread().setName(oldThreadName);
        }
    }

}
