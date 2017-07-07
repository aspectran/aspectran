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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.thread.ShutdownHooks;

/**
 * The Class BasicAspectranService.
 */
public class BasicAspectranService extends AbstractAspectranService {

    private final boolean derived;

    private final AspectranService rootAspectranService;

    /** Synchronization Monitor used for this service control */
    private final Object serviceControlMonitor = new Object();

    /** Flag that indicates whether this service is currently active */
    private volatile boolean active;

    /** Flag that indicates whether this service has been destroyed already */
    private volatile boolean destroyed;

    /** Reference to the shutdown task, if registered */
    private ShutdownHooks.Task shutdownTask;

    private AspectranServiceControlListener aspectranServiceControlListener;

    /**
     * Instantiates a new Basic aspectran service.
     *
     * @param applicationAdapter the application adapter
     */
    public BasicAspectranService(ApplicationAdapter applicationAdapter) {
        super(applicationAdapter);
        this.rootAspectranService = null;
        this.derived = false;
    }

    public BasicAspectranService(AspectranService rootAspectranService) {
        super(rootAspectranService);
        this.rootAspectranService = rootAspectranService;
        this.derived = true;
    }

    @Override
    public boolean isDerived() {
        return derived;
    }

    @Override
    public void setAspectranServiceControlListener(AspectranServiceControlListener aspectranServiceControlListener) {
        this.aspectranServiceControlListener = aspectranServiceControlListener;
    }

    /**
     * This method is executed immediately after the ActivityContext is loaded.
     *
     * @throws Exception if an error occurs
     */
    protected void afterContextLoaded() throws Exception {
    }

    /**
     * This method executed just before the ActivityContext is destroyed.
     */
    protected void beforeContextDestroy() {
    }

    @Override
    public void start() throws Exception {
        synchronized (this.serviceControlMonitor) {
            if (destroyed) {
                throw new IllegalStateException("Already destroyed AspectranService can not be reactivated");
            }
            if (active) {
                throw new IllegalStateException("AspectranService is already activated");
            }

            doStart();
            registerShutdownTask();

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.started();
            }

            if (!this.derived) {
                log.info("AspectranService has been started successfully");
                log.info("Welcome to Aspectran " + Aspectran.VERSION);
            } else {
                log.info("Started AspectranService derived from " + rootAspectranService);
            }
        }
    }

    @Override
    public void startIfNotRunning() throws Exception {
        if (!isActive()) {
            start();
        }
    }

    @Override
    public void restart() throws Exception {
        if (this.derived) {
            throw new UnsupportedOperationException("Derived services can not be restarted");
        }
        synchronized (this.serviceControlMonitor) {
            if (destroyed) {
                log.warn("Could not restart AspectranService because it has already been destroyed");
                return;
            }
            if (!active) {
                log.warn("Could not restart AspectranService because it is currently stopped");
                return;
            }

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.paused();
            }

            doDestroy();
            doStart();

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.restarted(isHardReload());
            }
            log.info("AspectranService has been restarted successfully");
        }
    }

    @Override
    public void pause() throws Exception {
        synchronized (this.serviceControlMonitor) {
            if (!active) {
                log.warn("AspectranService is not activated");
                return;
            }

            if (!derived) {
                pauseSchedulerService();
            }

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.paused();
            }
            log.info("AspectranService has been paused");
        }
    }

    @Override
    public void pause(long timeout) throws Exception {
        synchronized (this.serviceControlMonitor) {
            if (!active) {
                log.warn("AspectranService is not activated");
                return;
            }

            if (!derived) {
                pauseSchedulerService();
            }

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.paused(timeout);
            }
            log.info("AspectranService has been paused and will resume after " + timeout + " ms");
        }
    }

    @Override
    public void resume() throws Exception {
        synchronized (this.serviceControlMonitor) {
            if (!active) {
                log.warn("AspectranService is not activated");
                return;
            }

            if (!derived) {
                resumeSchedulerService();
            }

            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.resumed();
            }
            log.info("AspectranService has been resumed");
        }
    }

    @Override
    public void stop() {
        synchronized (this.serviceControlMonitor) {
            if (aspectranServiceControlListener != null) {
                aspectranServiceControlListener.stopped();
            }

            doDestroy();
            removeShutdownTask();

            log.info("AspectranService has been stopped successfully");
        }
    }

    @Override
    public boolean isActive() {
        synchronized (serviceControlMonitor) {
            return active;
        }
    }

    private void doStart() throws Exception {
        if (!derived) {
            loadActivityContext();
            afterContextLoaded();
            startSchedulerService();
        }

        destroyed = false;
        active = true;
    }

    /**
     * Actually performs destroys the singletons in the bean registry.
     * Called by both {@code shutdown()} and a JVM shutdown hook, if any.
     */
    private void doDestroy() {
        if (!derived) {
            log.info("Destroying all cached resources");

            shutdownSchedulerService();
            beforeContextDestroy();
            destroyActivityContext();
        }

        destroyed = true;
        active = false;
    }

    /**
     * Registers a shutdown hook with the JVM runtime, closing this context
     * on JVM shutdown unless it has already been closed at that time.
     */
    private void registerShutdownTask() {
        if (!this.derived && this.shutdownTask == null) {
            // Register a task to destroy the activity context on shutdown
            this.shutdownTask = ShutdownHooks.add(this::stop);
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

}