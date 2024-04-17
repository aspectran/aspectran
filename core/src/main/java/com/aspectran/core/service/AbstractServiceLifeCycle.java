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
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.wildcard.PluralWildcardPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractServiceLifeCycle.
 */
public abstract class AbstractServiceLifeCycle implements ServiceLifeCycle {

    private final Logger logger = LoggerFactory.getLogger(AbstractServiceLifeCycle.class);

    private final Object lock = new Object();

    private final List<ServiceLifeCycle> derivedServices;

    private CoreService rootService;

    private ServiceStateListener serviceStateListener;

    private PluralWildcardPattern exposableTransletNamesPattern;

    /** Flag that indicates whether this service is active */
    private volatile boolean active;

    public AbstractServiceLifeCycle(boolean derivable) {
        if (derivable) {
            derivedServices = new ArrayList<>();
        } else {
            derivedServices = null;
        }
    }

    @Override
    public String getServiceName() {
        return ObjectUtils.simpleIdentityToString(this);
    }

    @Override
    public CoreService getRootService() {
        return rootService;
    }

    protected void setRootService(CoreService rootService) {
        this.rootService = rootService;
    }

    @Override
    public void setServiceStateListener(ServiceStateListener serviceStateListener) {
        this.serviceStateListener = serviceStateListener;
    }

    protected void joinDerivedService(ServiceLifeCycle serviceLifeCycle) {
        if (derivedServices == null) {
            throw new UnsupportedOperationException("No support to control derived services");
        }
        derivedServices.add(serviceLifeCycle);
    }

    protected void withdrawDerivedService(ServiceLifeCycle serviceLifeCycle) {
        if (derivedServices == null) {
            throw new UnsupportedOperationException("No support to control derived services");
        }
        derivedServices.remove(serviceLifeCycle);
    }

    protected void clearDerivedServices() {
        if (derivedServices != null) {
            derivedServices.clear();
        }
    }

    protected boolean isExposable(String requestName) {
        return (exposableTransletNamesPattern == null || exposableTransletNamesPattern.matches(requestName));
    }

    protected void setExposals(String[] includePatterns, String[] excludePatterns) {
        if ((includePatterns != null && includePatterns.length > 0) ||
            excludePatterns != null && excludePatterns.length > 0) {
            exposableTransletNamesPattern = new PluralWildcardPattern(includePatterns, excludePatterns,
                ActivityContext.NAME_SEPARATOR_CHAR);
        }
    }

    protected abstract boolean isRootService();

    protected abstract boolean isDerived();

    protected abstract boolean isOrphan();

    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    protected Object getLock() {
        return lock;
    }

    @Override
    public void start() throws Exception {
        synchronized (lock) {
            Assert.state(!active, getServiceName() + " is already started");

            logger.info("Starting " + getServiceName());

            doStart();

            logger.info(getServiceName() + " started successfully");

            if (derivedServices != null) {
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.start();
                }
            }

            if (serviceStateListener != null) {
                serviceStateListener.started();
            }

            active = true;
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (!active) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getServiceName() + " is not running, will do nothing");
                }
                return;
            }

            if (serviceStateListener != null) {
                try {
                    serviceStateListener.paused();
                    serviceStateListener.stopped();
                } catch (Exception e) {
                    logger.warn(e);
                }
            }

            if (derivedServices != null) {
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.stop();
                }
            }

            try {
                logger.info("Stopping " + getServiceName());

                doStop();

                logger.info(getServiceName() + " stopped successfully");
            } catch (Exception e) {
                logger.error(getServiceName() + " was not stopped normally", e);
            }

            active = false;
        }
    }

    @Override
    public void restart() throws Exception {
        synchronized (lock) {
            Assert.state(isRootService(), "Only root service can be restarted");
            Assert.state(active, getServiceName() + " is not yet started");

            logger.info("Restarting " + getServiceName());

            if (serviceStateListener != null) {
                serviceStateListener.paused();
            }

            if (derivedServices != null) {
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.stop();
                }
            }

            active = false;
            doStop();

            if (serviceStateListener != null) {
                try {
                    serviceStateListener.stopped();
                } catch (Exception e) {
                    logger.warn(e);
                }
            }

            doStart();
            active = true;

            if (derivedServices != null) {
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.start();
                }
            }

            logger.info(getServiceName() + " restarted successfully");
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
                logger.warn(getServiceName() + " is not yet started");
                return;
            }

            if (!isDerived()) {
                if (derivedServices != null) {
                    for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                        serviceLifeCycle.pause();
                    }
                }
            }

            if (serviceStateListener != null) {
                serviceStateListener.paused();
            }

            logger.info(getServiceName() + " is paused");
        }
    }

    @Override
    public void pause(long timeout) throws Exception {
        synchronized (lock) {
            if (!active) {
                logger.warn(getServiceName() + " is not yet started");
                return;
            }

            if (!isDerived()) {
                if (derivedServices != null) {
                    for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                        serviceLifeCycle.pause(timeout);
                    }
                }
            }

            if (serviceStateListener != null) {
                serviceStateListener.paused(timeout);
            }

            logger.info(getServiceName() + " is paused and will resume after " + timeout + " ms");
        }
    }

    @Override
    public void resume() throws Exception {
        synchronized (lock) {
            if (!active) {
                logger.warn(getServiceName() + " is not yet started");
                return;
            }

            if (!isDerived()) {
                if (derivedServices != null) {
                    for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                        serviceLifeCycle.resume();
                    }
                }
            }

            if (serviceStateListener != null) {
                serviceStateListener.resumed();
            }

            logger.info(getServiceName() + " is resumed");
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

}
