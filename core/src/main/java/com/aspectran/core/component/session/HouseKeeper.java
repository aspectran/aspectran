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
package com.aspectran.core.component.session;

import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import com.aspectran.utils.thread.AutoLock;
import com.aspectran.utils.thread.Scheduler;
import com.aspectran.utils.thread.ThreadContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * The housekeeper for session scavenging.
 * There is 1 session HouseKeeper per SessionManager instance.
 */
public class HouseKeeper extends AbstractLifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(HouseKeeper.class);

    public static final int DEFAULT_SCAVENGING_INTERVAL = 60 * 10; // default of 10 minutes

    private final AutoLock lock = new AutoLock();

    private final AbstractSessionManager sessionManager;

    private final Scheduler scheduler;

    private long scavengingInterval;

    private Scheduler.Task task; // scavenge task

    private Runner runner;

    /**
     * @param sessionManager SessionManager associated with this scavenger
     */
    public HouseKeeper(@NonNull AbstractSessionManager sessionManager) {
        this(sessionManager, DEFAULT_SCAVENGING_INTERVAL);
    }

    /**
     * @param sessionManager SessionManager associated with this scavenger
     * @param scavengingIntervalInSecs the period between scavenge cycles
     */
    public HouseKeeper(@NonNull AbstractSessionManager sessionManager, int scavengingIntervalInSecs) {
        this.sessionManager = sessionManager;
        this.scheduler = sessionManager.getScheduler();
        this.scavengingInterval = TimeUnit.SECONDS.toMillis(scavengingIntervalInSecs);
    }

    /**
     * Get the period between scavenge cycles.
     * @return the interval (in seconds)
     */
    public int getScavengingInterval() {
        return (int)TimeUnit.MILLISECONDS.toSeconds(scavengingInterval);
    }

    /**
     * Set the period between scavenge cycles.
     * @param intervalInSecs the interval (in seconds)
     */
    public void setScavengingInterval(int intervalInSecs) {
        try (AutoLock ignored = lock.lock()) {
            if (isStarted() || isStarting()) {
                if (intervalInSecs <= 0) {
                    scavengingInterval = 0L;
                    logger.info("{} scavenging disabled", sessionManager.getComponentName());
                    stopScavenging();
                } else {
                    if (intervalInSecs < 10) {
                        logger.warn("Short interval of {} secs for session scavenging", intervalInSecs);
                    }
                    scavengingInterval = TimeUnit.SECONDS.toMillis(intervalInSecs);
                    startScavenging();
                }
            } else {
                scavengingInterval = TimeUnit.SECONDS.toMillis(intervalInSecs);
            }
        }
    }

    /**
     * If scavenging is not scheduled, schedule it.
     */
    protected void startScavenging() {
        try (AutoLock ignored = lock.lock()) {
            // cancel any previous task
            if (task != null) {
                task.cancel();
            }
            if (runner != null) {
                runner.stop();
            }
            if (logger.isTraceEnabled()) {
                logger.trace("{} is scavenging every {} ms", this, scavengingInterval);
            }
            runner = new Runner();
            task = scheduler.schedule(runner, scavengingInterval, TimeUnit.MILLISECONDS, true);
        }
    }

    /**
     * If scavenging is scheduled, stop it.
     */
    protected void stopScavenging() {
        try (AutoLock ignored = lock.lock()) {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (runner != null) {
                runner.stop();
                runner = null;
            }
        }
    }

    /**
     * Periodically do session housekeeping.
     */
    private void scavenge() {
        // don't attempt to scavenge if we are shutting down
        if (isStopping() || isStopped()) {
            return;
        }

        // To help distinguish logging groups
        ThreadContextHelper.run(sessionManager.getClassLoader(), () -> {
            try {
                sessionManager.scavenge(scavengingInterval);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        });
    }

    @Override
    protected void doStart() throws Exception {
        setScavengingInterval(getScavengingInterval());
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        stopScavenging();
        super.doStop();
    }

    @Override
    public boolean isRunning() {
        return (super.isRunning() && scavengingInterval > 0L);
    }

    @Override
    public String toString() {
        if (isStopped()) {
            ToStringBuilder tsb = new ToStringBuilder(super.toString());
            tsb.append("scavengingInterval", scavengingInterval);
            return tsb + " used by " + sessionManager.getComponentName();
        } else if (sessionManager.getWorkerName() != null) {
            return super.toString() + "(" + sessionManager.getWorkerName() + ")";
        } else {
            return super.toString();
        }
    }

    private class Runner implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            if (running) {
                try {
                    scavenge();
                } finally {
                    try (AutoLock ignored = lock.lock()) {
                        if (scheduler != null && scheduler.isRunning()) {
                            // cancel any previous task
                            if (task != null) {
                                task.cancel();
                            }
                            task = scheduler.schedule(this, scavengingInterval, TimeUnit.MILLISECONDS, true);
                        }
                    }
                }
            }
        }

        public void stop() {
            running = false;
        }

    }

}
