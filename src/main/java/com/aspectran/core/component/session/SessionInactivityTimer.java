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
package com.aspectran.core.component.session;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Scheduler;
import com.aspectran.core.util.thread.Scheduler.Task;

/**
 * <p>Created: 2017. 6. 25.</p>
 */
public class SessionInactivityTimer {

    private static final Log log = LogFactory.getLog(SessionInactivityTimer.class);

    private final AtomicReference<Task> timeout = new AtomicReference<>();

    private final Scheduler scheduler;

    private final BasicSession session;

    private volatile long idleTimeout;

    private volatile long idleTimestamp = System.currentTimeMillis();

    private final Runnable idleTask = () -> {
        long idleLeft = checkIdleTimeout();
        if (idleLeft >= 0) {
            scheduleIdleTimeout(idleLeft > 0 ? idleLeft : getIdleTimeout());
        }
    };

    public SessionInactivityTimer(Scheduler scheduler, BasicSession session) {
        this.scheduler = scheduler;
        this.session = session;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        long old = this.idleTimeout;
        this.idleTimeout = idleTimeout;

        if(old > 0L) {
            if(old <= idleTimeout) {
                return;
            }
            deactivate();
        }

        if(session.isValid() && session.isResident()) {
            activate();
        }
    }

    public void notIdle() {
        idleTimestamp = System.currentTimeMillis();
    }

    private void activate() {
        if (idleTimeout > 0) {
            idleTask.run();
        }
    }

    private void deactivate() {
        Task oldTimeout = timeout.getAndSet(null);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    private void scheduleIdleTimeout(long delay) {
        Task newTimeout = null;
        if (session.isValid() && session.isResident() && delay > 0L) {
            newTimeout = scheduler.schedule(idleTask, delay, TimeUnit.MILLISECONDS);
        }

        Task oldTimeout = timeout.getAndSet(newTimeout);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    private long checkIdleTimeout() {
        if (!session.isValid() || !session.isResident()) {
            return -1;
        }

        long idleTimestamp = this.idleTimestamp;
        long idleTimeout = this.idleTimeout;
        long idleElapsed = System.currentTimeMillis() - idleTimestamp;
        long idleLeft = idleTimeout - idleElapsed;

        if (log.isTraceEnabled()) {
            log.trace(this + " idle timeout check, elapsed: " + idleElapsed + " ms, remaining: " + idleLeft + " ms");
        }

        if (idleTimestamp != 0 && idleTimeout > 0) {
            if (idleLeft <= 0) {
                try {
                    idleExpired();
                } finally {
                    notIdle();
                }
            }
        }

        return (idleLeft >= 0 ? idleLeft : 0);
    }

    private void idleExpired() {
        if (session.getRequests() > 0) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Timer expired for session " + session);
        }
        session.invalidate();
    }

}
