/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.AutoLock;
import com.aspectran.utils.thread.Scheduler;

import java.util.concurrent.TimeUnit;

/**
 * The housekeeper for session scavenging.
 * There is 1 session HouseKeeper per SessionManager instance.
 */
public class HouseKeeper extends AbstractLifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(HouseKeeper.class);

    public static final long DEFAULT_SCAVENGING_INTERVAL_MS = 1000 * 60 * 10; //default of 10min

    private final AutoLock lock = new AutoLock();

    private final SessionHandler sessionHandler;

    private final Scheduler scheduler;

    private Scheduler.Task task; // scavenge task

    private Runner runner;

    /** 10 minute default */
    private long scavengingInterval = DEFAULT_SCAVENGING_INTERVAL_MS;

    /**
     * @param sessionHandler SessionHandler associated with this scavenger
     */
    public HouseKeeper(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
        this.scheduler = sessionHandler.getScheduler();
    }

    /**
     * Get the period between scavenge cycles.
     * @return the interval (in seconds)
     */
    public int getScavengingInterval() {
        return (int)(scavengingInterval / 1000L);
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
                    logger.info("Scavenging disabled");
                    stopScavenging();
                } else {
                    if (intervalInSecs < 10) {
                        logger.warn("Short interval of " + intervalInSecs + " secs for session scavenging");
                    }
                    scavengingInterval = intervalInSecs * 1000L;
                    if (isStarting() || isStarted()) {
                        startScavenging();
                    }
                }
            } else {
                scavengingInterval = intervalInSecs * 1000L;
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
            if (runner == null) {
                runner = new Runner();
            }
            if (logger.isTraceEnabled()) {
                logger.trace(this + " is scavenging every " + scavengingInterval + " ms");
            }
            task = scheduler.schedule(runner, scavengingInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * If scavenging is scheduled, stop it.
     */
    protected void stopScavenging() {
        try (AutoLock ignored = lock.lock()) {
            if (task != null) {
                task.cancel();
                if (logger.isTraceEnabled()) {
                    logger.trace(this + "  stopped scavenging");
                }
            }
            task = null;
            runner = null;
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
        try {
            sessionHandler.scavenge(scavengingInterval);
        } catch (Exception e) {
            logger.warn(e);
        }
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
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(super.toString());
        tsb.append("scavengingInterval", scavengingInterval);
        return tsb.toString();
    }

    /**
     * Runner
     */
    private class Runner implements Runnable {

        @Override
        public void run() {
            try {
                scavenge();
            } finally {
                try (AutoLock ignored = lock.lock()) {
                    if (scheduler != null && scheduler.isRunning()) {
                        task = scheduler.schedule(this, scavengingInterval, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }

    }

}
