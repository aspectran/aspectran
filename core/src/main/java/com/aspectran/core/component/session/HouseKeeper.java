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
package com.aspectran.core.component.session;

import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Scheduler;

import java.util.concurrent.TimeUnit;

/**
 * The housekeeper for session scavenging.
 * There is 1 session HouseKeeper per SessionManager instance.
 */
public class HouseKeeper {

    private static final Log log = LogFactory.getLog(HouseKeeper.class);

    private final SessionHandler sessionHandler;

    private final Scheduler scheduler;

    protected Scheduler.Task task; // scavenge task

    protected Runner runner;

    /** 10 minute default */
    private long scavengingInterval = 1000L * 60 * 10;

    /**
     * @param sessionHandler SessionHandler associated with this scavenger
     */
    public HouseKeeper(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
        this.scheduler = sessionHandler.getScheduler();
    }

    /**
     * Get the period between scavenge cycles.
     *
     * @return the interval (in seconds)
     */
    public int getScavengingInterval() {
        return (int)(scavengingInterval / 1000L);
    }

    /**
     * Set the period between scavenge cycles.
     *
     * @param intervalInSecs the interval (in seconds)
     */
    public void setScavengingInterval(int intervalInSecs) {
        scavengingInterval = intervalInSecs * 1000L;
    }

    public boolean isScavengable() {
        return (scavengingInterval > 0L);
    }

    public void startScavenging() {
        startScavenging(getScavengingInterval());
    }

    /**
     * If scavenging is not scheduled, schedule it.
     *
     * @param intervalInSecs the interval (in seconds)
     */
    public void startScavenging(int intervalInSecs) {
        synchronized (this) {
            if (intervalInSecs < 10) {
                log.warn(sessionHandler.getComponentName() + " Short interval of " + intervalInSecs +
                        "sec for session scavenging");
            }
            setScavengingInterval(intervalInSecs);
            // cancel any previous task
            if (task != null) {
                task.cancel();
            }
            if (runner == null) {
                runner = new Runner();
            }
            log.info(sessionHandler.getComponentName() + " Scavenging every " + scavengingInterval + " ms");
            task = scheduler.schedule(runner, scavengingInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * If scavenging is scheduled, stop it.
     */
    public void stopScavenging() {
        synchronized (this) {
            if (task != null) {
                task.cancel();
                log.info(sessionHandler.getComponentName() + " Stopped scavenging");
            }
            task = null;
        }
        runner = null;
    }

    /**
     * Periodically do session housekeeping.
     */
    private void scavenge() {
        // don't attempt to scavenge if we are shutting down
        if (!scheduler.isRunning()) {
            return;
        }
        try {
            sessionHandler.scavenge();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
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
                if (scheduler != null && scheduler.isRunning()) {
                    task = scheduler.schedule(this, scavengingInterval, TimeUnit.MILLISECONDS);
                }
            }
        }

    }

}
