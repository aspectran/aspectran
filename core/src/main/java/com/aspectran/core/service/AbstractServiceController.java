/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractServiceController.
 */
public abstract class AbstractServiceController implements ServiceController {

    private static final Log log = LogFactory.getLog(AbstractServiceController.class);

    private final Object lock = new Object();

    private List<ServiceController> derivedServices = new ArrayList<>(5);

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

    protected void joinDerivedService(ServiceController serviceController) {
        derivedServices.add(serviceController);
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

            if (!isDerived()) {
                log.info("Starting the " + getServiceName());

                doStart();

                for (ServiceController serviceController : derivedServices) {
                    serviceController.start();
                }
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
            if (!isDerived()) {
                if (!active) {
                    throw new IllegalStateException(getServiceName() + " is not yet started");
                }

                log.info("Restarting the " + getServiceName());
            } else {
                if (active) {
                    throw new IllegalStateException(getServiceName() + " should never be run separately");
                }
                active = true;
            }

            if (serviceStateListener != null) {
                serviceStateListener.paused();
            }

            if (!isDerived()) {
                derivedServices.clear();
                active = false;
                doRestart();
                active = true;

                for (ServiceController serviceController : derivedServices) {
                    serviceController.restart();
                }
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

            if (!isDerived()) {
                for (ServiceController serviceController : derivedServices) {
                    serviceController.pause();
                }
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

            if (!isDerived()) {
                for (ServiceController serviceController : derivedServices) {
                    serviceController.pause(timeout);
                }
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

            if (!isDerived()) {
                for (ServiceController serviceController : derivedServices) {
                    serviceController.resume();
                }
            }

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

            if (!isDerived()) {
                log.info("Stopping the " + getServiceName());
            }

            if (serviceStateListener != null) {
                try {
                    serviceStateListener.stopped();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }

            if (!isDerived()) {
                for (ServiceController serviceController : derivedServices) {
                    serviceController.stop();
                }

                try {
                    doStop();
                    log.info(getServiceName() + " stopped successfully");
                } catch (Exception e) {
                    log.error(getServiceName() + " was not stopped normally", e);
                }
            } else {
                log.info(getServiceName() + " stopped successfully");
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