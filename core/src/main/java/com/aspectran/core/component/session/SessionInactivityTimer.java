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
package com.aspectran.core.component.session;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.AutoLock;
import com.aspectran.utils.thread.ThreadContextHelper;
import com.aspectran.utils.timer.CyclicTimeout;

import java.util.concurrent.TimeUnit;

/**
 * The Class SessionInactivityTimer.
 * <p>Each Session has a timer associated with it that fires whenever it has
 * been idle (ie not accessed by a request) for a configurable amount of
 * time, or the Session expires.</p>
 * <p>The timer is only scheduled when all Requests have exited the Session.
 * If a request enters a Session whose timer is active, it is cancelled.</p>
 */
public class SessionInactivityTimer {

    private static final Logger logger = LoggerFactory.getLogger(SessionInactivityTimer.class);

    private final ManagedSession session;

    private final CyclicTimeout timer;

    public SessionInactivityTimer(@NonNull AbstractSessionHandler sessionHandler, @NonNull ManagedSession session) {
        this.session = session;
        this.timer = new CyclicTimeout(sessionHandler.getScheduler()) {
            @Override
            public void onTimeoutExpired() {
                if (logger.isTraceEnabled()) {
                    logger.trace("Timer expired for session " + session.getId());
                }
                long now = System.currentTimeMillis();
                try (AutoLock ignored = session.lock()) {
                    if (session.getRequests() > 0) {
                        return; // session can't expire or be idle if there is a request in it
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Inspecting session " + session.getId() + ", valid=" + session.isValid());
                    }
                    if (!session.isValid()) {
                        return; // do nothing, session is no longer valid
                    }

                    boolean expired = false;
                    try {
                        // To help distinguish logging groups
                        expired = ThreadContextHelper.call(sessionHandler.getClassLoader(), () ->
                            sessionHandler.sessionInactivityTimerExpired(session, now));
                    } catch (Exception e) {
                        logger.warn(e);
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
     * @param time the timeout to set; -1 means that the timer will not be scheduled
     */
    public void schedule(long time) {
        if (time >= 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("(Re)starting timer for session " + session.getId() + " at " + time + "ms");
            }
            timer.schedule(time, TimeUnit.MILLISECONDS);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Not starting timer for session " + session.getId());
            }
        }
    }

    public void cancel() {
        timer.cancel();
        if (logger.isTraceEnabled()) {
            logger.trace("Cancelled timer for session " + session.getId());
        }
    }

    public void destroy() {
        timer.destroy();
        if (logger.isTraceEnabled()) {
            logger.trace("Destroyed timer for session " + session.getId());
        }
    }

}
