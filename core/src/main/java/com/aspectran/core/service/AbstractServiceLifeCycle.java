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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
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

    private final List<ServiceLifeCycle> derivedServices = new ArrayList<>();

    private final CoreService rootService;

    private final CoreService parentService;

    private ServiceStateListener serviceStateListener;

    private PluralWildcardPattern exposableTransletNamesPattern;

    /** Flag that indicates whether this service is active */
    private volatile boolean active;

    public AbstractServiceLifeCycle(@Nullable CoreService parentService) {
        this.parentService = parentService;
        if (parentService != null) {
            this.rootService = parentService.getRootService();
            this.rootService.joinDerivedService(this);
        } else {
            this.rootService = (CoreService)this;
        }
    }

    @Override
    public String getServiceName() {
        return ObjectUtils.simpleIdentityToString(this);
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

    protected void joinDerivedService(ServiceLifeCycle serviceLifeCycle) {
        derivedServices.add(serviceLifeCycle);
    }

    protected void withdrawDerivedService(ServiceLifeCycle serviceLifeCycle) {
        derivedServices.remove(serviceLifeCycle);
    }

    protected void clearDerivedServices() {
        derivedServices.clear();
    }

    protected Object getLock() {
        return lock;
    }

    protected abstract boolean isDerived();

    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    @Override
    public void start() throws Exception {
        synchronized (lock) {
            Assert.state(!active, getServiceName() + " is already started");

            logger.info("Starting " + getServiceName());

            doStart();

            logger.info(getServiceName() + " started successfully");

            for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                serviceLifeCycle.start();
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

            for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                serviceLifeCycle.stop();
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

            for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                serviceLifeCycle.stop();
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

            logger.info(getServiceName() + " restarted successfully");

            for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                serviceLifeCycle.start();
            }

            if (serviceStateListener != null) {
                serviceStateListener.started();
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
                logger.warn(getServiceName() + " is not yet started");
                return;
            }

            if (!isDerived()) {
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.pause();
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
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.pause(timeout);
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
                for (ServiceLifeCycle serviceLifeCycle : derivedServices) {
                    serviceLifeCycle.resume();
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
