/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.IdleTimeout;
import com.aspectran.core.util.thread.Scheduler;

/**
 * Timer to handle session timeout due to inactivity.
 *
 * Each Session has a timer associated with it that fires whenever it has been idle
 * for a configurable amount of time, or the Session expires.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public class SessionInactivityTimer extends IdleTimeout {

    private static final Log log = LogFactory.getLog(SessionInactivityTimer.class);

    private final Session session;

    public SessionInactivityTimer(Scheduler scheduler, Session session) {
        super(scheduler);
        this.session = session;
    }

    @Override
    public boolean isValid() {
        return (session.isValid() && session.isResident());
    }

    @Override
    public void idleExpired() {
        if (session.getRequests() <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Timer expired for session " + session.getId());
            }
            session.invalidate();
        }
    }

}
