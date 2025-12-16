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

import com.aspectran.utils.concurrent.AutoLock;
import com.aspectran.utils.thread.ThreadContextHelper;
import com.aspectran.utils.timer.CyclicTimeout;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Manages a timer for an individual session that fires when the session has been
 * idle for a configurable amount of time.
 *
 * <p>The timer is scheduled when a request is no longer accessing the session.
 * If a new request accesses the session while the timer is active, the timer is
 * cancelled to prevent premature expiration.</p>
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class SessionInactivityTimer {

    private static final Logger logger = LoggerFactory.getLogger(SessionInactivityTimer.class);

    private final ManagedSession session;

    private final CyclicTimeout timer;

    /**
     * Instantiates a new SessionInactivityTimer.
     * @param sessionManager the session manager
     * @param session the session to monitor
     */
    public SessionInactivityTimer(@NonNull AbstractSessionManager sessionManager, @NonNull ManagedSession session) {
        this.session = session;
        this.timer = new CyclicTimeout(sessionManager.getScheduler()) {
            @Override
            public void onTimeoutExpired() {
                if (logger.isTraceEnabled()) {
                    logger.trace("Timer expired for session id={}", session.getId());
                }
                long now = System.currentTimeMillis();
                try (AutoLock ignored = session.lock()) {
                    if (session.getRequests() > 0) {
                        return; // session can't expire or be idle if there is a request in it
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Inspecting session id={}, valid={}", session.getId(), session.isValid());
                    }
                    if (!session.isValid()) {
                        return; // do nothing, session is no longer valid
                    }

                    boolean expired = false;
                    try {
                        // To help distinguish logging groups
                        expired = ThreadContextHelper.call(sessionManager.getClassLoader(), () ->
                                sessionManager.sessionInactivityTimerExpired(session, now));
                    } catch (Exception e) {
                        logger.warn(e.getMessage(), e);
                    }

                    // grab the lock and check what happened to the session: if it didn't get evicted and
                    // it hasn't expired, we need to reset the timer
                    if (!expired && session.isResident()) {
                        // session wasn't expired or evicted, we need to reset the timer
                        SessionInactivityTimer.this.schedule(session.calculateInactivityTimeout(now));
                    }
                }
            }
        };
    }

    /**
     * Schedules the inactivity timer.
     * @param time the timeout in milliseconds; a value of -1 will prevent the timer from being scheduled
     */
    public void schedule(long time) {
        if (time >= 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("(Re)starting timer for session {} at {}ms", session.getId(), time);
            }
            timer.schedule(time, TimeUnit.MILLISECONDS);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Not starting timer for session {}", session.getId());
            }
        }
    }

    /**
     * Cancels the currently scheduled timer.
     */
    public void cancel() {
        timer.cancel();
        if (logger.isTraceEnabled()) {
            logger.trace("Cancelled timer for session {}", session.getId());
        }
    }

    /**
     * Destroys the timer, preventing any further scheduling.
     */
    public void destroy() {
        timer.destroy();
        if (logger.isTraceEnabled()) {
            logger.trace("Destroyed timer for session {}", session.getId());
        }
    }

}
