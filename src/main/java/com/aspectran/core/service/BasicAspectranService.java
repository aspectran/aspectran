/*
 * Copyright 2008-2017 Juho Jeong
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

import java.util.concurrent.atomic.AtomicBoolean;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.thread.ShutdownHooks;
import com.aspectran.scheduler.service.SchedulerServiceException;

/**
 * The Class BasicAspectranService.
 */
public class BasicAspectranService extends AbstractAspectranService {

    private final boolean derived;

    private AspectranServiceControlListener aspectranServiceControlListener;

    /** Flag that indicates whether this service is currently active */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Flag that indicates whether this service has been closed already */
    private final AtomicBoolean closed = new AtomicBoolean();

    /** Synchronization Monitor used for this service control */
    private final Object serviceControlMonitor = new Object();

    /** Reference to the shutdown task, if registered */
    private ShutdownHooks.Task shutdownTask;

    /**
     * Instantiates a new Basic aspectran service.
     *
     * @param applicationAdapter the application adapter
     */
    public BasicAspectranService(ApplicationAdapter applicationAdapter) {
        super(applicationAdapter);
        this.derived = false;
    }

    public BasicAspectranService(AspectranService rootAspectranService) {
        super(rootAspectranService);
        this.derived = true;
    }

    @Override
    public void setAspectranServiceControlListener(AspectranServiceControlListener aspectranServiceControlListener) {
        this.aspectranServiceControlListener = aspectranServiceControlListener;
    }

    /**
     * This method is executed immediately after the ActivityContext is loaded.
     */
    protected void afterContextLoaded() {
    }

    /**
     * This method executed just before the ActivityContext is destroyed.
     */
    protected void beforeContextDestroy() {
    }

    @Override
    public void start() throws AspectranServiceException {
        if (!this.derived) {
            synchronized (this.serviceControlMonitor) {
                if (this.closed.get()) {
                    throw new AspectranServiceException("Could not start AspectranService because it has already been destroyed");
                }
                if (this.active.get()) {
                    throw new AspectranServiceException("Could not start AspectranService because it has already been started");
                }

                loadActivityContext();
                registerShutdownTask();

                afterContextLoaded();

                startSchedulerService();

                this.active.set(true);

                log.info("AspectranService has been started successfully");
                log.info("Welcome to Aspectran " + Aspectran.VERSION);

                if (aspectranServiceControlListener != null) {
                    aspectranServiceControlListener.started();
                }
            }
        }
    }

    @Override
    public void restart() throws AspectranServiceException {
        if (!this.derived) {
            synchronized (this.serviceControlMonitor) {
                if (this.closed.get()) {
                    log.warn("Could not restart AspectranService because it has already been destroyed");
                    return;
                }
                if (!this.active.get()) {
                    log.warn("Could not restart AspectranService because it is currently stopped");
                    return;
                }

                doDestroy();

                log.info("AspectranService has been stopped successfully");

                loadActivityContext();

                afterContextLoaded();

                startSchedulerService();

                this.closed.set(false);
                this.active.set(true);

                log.info("AspectranService has been restarted successfully");

                if (aspectranServiceControlListener != null) {
                    aspectranServiceControlListener.restarted(isHardReload());
                }
            }
        }
    }

    @Override
    public void pause() throws SchedulerServiceException {
        if (!this.derived) {
            synchronized (this.serviceControlMonitor) {
                if (this.closed.get()) {
                    log.warn("Could not pause AspectranService because it has already been destroyed");
                    return;
                }

                try {
                    pauseSchedulerService();
                } catch (SchedulerServiceException e) {
                    log.error("Could not pause AspectranService", e);
                    throw e;
                }

                log.info("AspectranService has been paused");

                if (aspectranServiceControlListener != null) {
                    aspectranServiceControlListener.paused();
                }
            }
        }
    }

    @Override
    public void pause(long timeout) throws AspectranServiceException {
        if (!this.derived) {
            synchronized (this.serviceControlMonitor) {
                if (this.closed.get()) {
                    log.warn("Could not pause AspectranService because it has already been destroyed");
                    return;
                }

                try {
                    pauseSchedulerService();
                } catch (SchedulerServiceException e) {
                    throw new AspectranServiceException("Could not pause AspectranService", e);
                }

                log.info("AspectranService has been paused and will resume after " + timeout + " ms");

                if (aspectranServiceControlListener != null) {
                    aspectranServiceControlListener.paused(timeout);
                }
            }
        }
    }

    @Override
    public void resume() throws AspectranServiceException {
        if (!this.derived) {
            synchronized (this.serviceControlMonitor) {
                if (this.closed.get()) {
                    log.warn("Could not resume AspectranService because it has already been destroyed");
                    return;
                }

                try {
                    resumeSchedulerService();
                } catch (SchedulerServiceException e) {
                    throw new AspectranServiceException("Could not resume AspectranService", e);
                }

                log.info("AspectranService has been resumed");

                if (aspectranServiceControlListener != null) {
                    aspectranServiceControlListener.resumed();
                }
            }
        }
    }

    @Override
    public void stop() {
        if (!this.derived) {
            synchronized (this.serviceControlMonitor) {
                doDestroy();
                removeShutdownTask();
            }
        }
    }

    /**
     * Actually performs destroys the singletons in the bean registry.
     * Called by both {@code shutdown()} and a JVM shutdown hook, if any.
     */
    private void doDestroy() {
        if (this.active.get() && this.closed.compareAndSet(false, true)) {
            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.paused();
            }

            log.info("Destroying all cached resources");

            shutdownSchedulerService();

            beforeContextDestroy();

            destroyActivityContext();

            log.info("AspectranService has been stopped successfully");

            this.active.set(false);

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.stopped();
            }
        }
    }

    /**
     * Registers a shutdown hook with the JVM runtime, closing this context
     * on JVM shutdown unless it has already been closed at that time.
     */
    private void registerShutdownTask() {
        if (this.shutdownTask == null) {
            // Register a task to destroy the activity context on shutdown
            this.shutdownTask = ShutdownHooks.add(() -> {
                synchronized (serviceControlMonitor) {
                    doDestroy();
                    removeShutdownTask();
                }
            });
        }
    }

    /**
     * De-registers a shutdown hook with the JVM runtime.
     */
    private void removeShutdownTask() {
        // If we registered a JVM shutdown hook, we don't need it anymore now:
        // We've already explicitly closed the context.
        if (this.shutdownTask != null) {
            ShutdownHooks.remove(this.shutdownTask);
            this.shutdownTask = null;
        }
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

}