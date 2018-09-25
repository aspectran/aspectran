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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractServiceController.
 */
public abstract class AbstractServiceController implements ServiceController {

    private static Log log = LogFactory.getLog(AbstractServiceController.class);

    private final Object lock = new Object();

    /** Flag that indicates whether this service is active */
    private volatile boolean active;

    private ServiceStateListener serviceStateListener;

    @Override
    public String getServiceName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setServiceStateListener(ServiceStateListener serviceStateListener) {
        this.serviceStateListener = serviceStateListener;
    }

    protected abstract void doStart() throws Exception;

    protected abstract void doRestart() throws Exception;

    protected abstract void doPause() throws Exception;

    protected abstract void doPause(long timeout) throws Exception;

    protected abstract void doResume() throws Exception;

    protected abstract void doStop() throws Exception;

    protected Object getLock() {
        return lock;
    }

    @Override
    public void start() throws Exception {
        synchronized (lock) {
            if (active) {
                throw new IllegalStateException(getServiceName() + " is already started");
            }

            log.info("Starting the service...");

            if (!isDerived()) {
                doStart();
            }

            log.info(getServiceName() + " started successfully");

            if (serviceStateListener != null) {
                serviceStateListener.started();
            }

            active = true;
        }
    }

    @Override
    public void restart() throws Exception {
        synchronized (lock) {
            if (!active) {
                throw new IllegalStateException(getServiceName() + " is not yet started");
            }

            log.info("Restarting the service...");

            if (serviceStateListener != null) {
                serviceStateListener.paused();
            }

            if (!isDerived()) {
                doRestart();
            }

            log.info(getServiceName() + " restarted successfully");

            if (serviceStateListener != null) {
                serviceStateListener.restarted();
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
                log.warn(getServiceName() + " is not yet started");
                return;
            }

            doPause();

            log.info(getServiceName() + " is paused");

            if (serviceStateListener != null) {
                serviceStateListener.paused();
            }
        }
    }

    @Override
    public void pause(long timeout) throws Exception {
        synchronized (lock) {
            if (!active) {
                log.warn(getServiceName() + " is not yet started");
                return;
            }

            doPause(timeout);

            log.info(getServiceName() + " is paused and will resume after " + timeout + " ms");

            if (serviceStateListener != null) {
                serviceStateListener.paused(timeout);
            }
        }
    }

    @Override
    public void resume() throws Exception {
        synchronized (lock) {
            if (!active) {
                log.warn(getServiceName() + " is not yet started");
                return;
            }

            doResume();

            log.info(getServiceName() + " is resumed");

            if (serviceStateListener != null) {
                serviceStateListener.resumed();
            }
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (!active) {
                log.debug(getServiceName() + " is already stopped");
                return;
            }

            log.info("Stopping the service...");

            if (serviceStateListener != null) {
                try {
                    serviceStateListener.stopped();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }

            if (!isDerived()) {
                try {
                    doStop();
                    log.info(getServiceName() + " stopped successfully");
                } catch (Exception e) {
                    log.error(getServiceName() + " was not stopped normally", e);
                }
            }

            active = false;
        }
    }

    @Override
    public boolean isActive() {
        synchronized (lock) {
            return active;
        }
    }

    @Override
    public boolean isBusy() {
        return false;
    }

}