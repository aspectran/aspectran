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
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.Locker.Lock;
import com.aspectran.core.util.timer.CyclicTimeout;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A basic {@link Session} implementation.
 *
 * <p>Created: 2017. 6. 13.</p>
 */
public class BasicSession implements Session {

    private static final Log log = LogFactory.getLog(BasicSession.class);

    private final Locker locker = new Locker();

    private final SessionData sessionData;

    private final SessionHandler sessionHandler;

    private final SessionInactivityTimer sessionInactivityTimer;

    private boolean newSession;

    private boolean resident;

    private int requests;

    private State state = State.VALID;

    /**
     * state of the session: valid, invalid or being invalidated
     */
    private enum State {
        VALID,
        INVALID,
        INVALIDATING
    }

    private Session.DestroyedReason destroyedReason;

    protected BasicSession(SessionData sessionData, SessionHandler sessionHandler, boolean newSession) {
        this.sessionData = sessionData;
        this.sessionHandler = sessionHandler;
        this.newSession = newSession;
        if (newSession) {
            this.sessionData.setDirty(true);
            this.requests = 1;
        }
        this.sessionInactivityTimer = new SessionInactivityTimer();
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    @Override
    public String getId() {
        try (Lock ignored = locker.lock()) {
            return sessionData.getId();
        }
    }

    @Override
    public <T> T getAttribute(String name) {
        try (Lock ignored = locker.lock()) {
            checkValidForRead();
            return sessionData.getAttribute(name);
        }
    }

    @Override
    public Object setAttribute(String name, Object value) {
        Object old;
        try (Lock ignored = locker.lock()) {
            // if session is not valid, don't accept the set
            checkValidForWrite();
            old = sessionData.setAttribute(name, value);
        }
        if (value == null && old == null) {
            return null; // if same as remove attribute but attribute was already removed, no change
        }
        fireSessionAttributeListeners(name, old, value);
        return old;
    }

    @Override
    public Set<String> getAttributeNames() {
        try (Lock ignored = locker.lock()) {
            checkValidForRead();
            return sessionData.getKeys();
        }
    }

    @Override
    public Object removeAttribute(String name) {
        return setAttribute(name, null);
    }

    @Override
    public long getCreationTime() {
        try (Lock ignored = locker.lock()) {
            checkValidForRead();
            return sessionData.getCreationTime();
        }
    }

    @Override
    public long getLastAccessedTime() {
        try (Lock ignored = locker.lock()) {
            return sessionData.getLastAccessedTime();
        }
    }

    @Override
    public int getMaxInactiveInterval() {
        try (Lock ignored = locker.lock()) {
            if (sessionData.getMaxInactiveInterval() > 0L) {
                return (int)(sessionData.getMaxInactiveInterval() / 1000L);
            } else {
                return -1;
            }
        }
    }

    @Override
    public void setMaxInactiveInterval(int secs) {
        try (Lock ignored = locker.lock()) {
            sessionData.setMaxInactiveInterval((long)secs * 1000L);
            sessionData.calcAndSetExpiry();
            sessionData.setDirty(true);
            if (log.isDebugEnabled()) {
                if (secs <= 0) {
                    log.debug("Session " + sessionData.getId() + " is now immortal (maxInactiveInterval=" + secs + ")");
                } else {
                    log.debug("Session " + sessionData.getId() + " maxInactiveInterval=" + secs);
                }
            }
        }
    }

    @Override
    public boolean access() {
        try (Lock ignored = locker.lock()) {
            if (!isValid()) {
                return false;
            }

            newSession = false;

            long now = System.currentTimeMillis();
            sessionData.setAccessedTime(now);
            sessionData.calcAndSetExpiry(now);
            if (isExpiredAt(now)) {
                invalidate();
                return false;
            }

            requests++;

            // temporarily stop the idle timer
            if (log.isDebugEnabled()) {
                log.debug("Session " + getId() + " accessed, stopping timer, active requests=" + requests);
            }
            sessionInactivityTimer.cancel();

            return true;
        }
    }

    @Override
    public void complete() {
        try (Lock ignored = locker.lock()) {
            requests--;

            if (log.isDebugEnabled()) {
                log.debug("Session " + getId() + " complete, active requests=" + requests);
            }

            // start the inactivity timer if necessary
            if (requests == 0) {
                // update the expiry time to take account of the time all requests spent inside of the session
                long now = System.currentTimeMillis();
                sessionData.calcAndSetExpiry(now);
                sessionData.setLastAccessedTime(sessionData.getAccessedTime());
                sessionHandler.releaseSession(this);
                sessionInactivityTimer.schedule(calculateInactivityTimeout(now));
            }
        }
    }

    /**
     * Returns the current number of requests that are active in the Session.
     *
     * @return the number of active requests for this session
     */
    protected long getRequests() {
        try (Lock ignored = locker.lock()) {
            return requests;
        }
    }

    /**
     * Calculate what the session timer setting should be based on:
     * the time remaining before the session expires
     * and any idle eviction time configured.
     * The timer value will be the lesser of the above.
     *
     * @param now the time at which to calculate remaining expiry
     * @return the time remaining before expiry or inactivity timeout
     */
    private long calculateInactivityTimeout(long now) {
        long time;
        try (Lock ignored = locker.lock()) {
            long remaining = sessionData.getExpiryTime() - now;
            long maxInactive = sessionData.getMaxInactiveInterval();
            int evictionIdleSecs = sessionHandler.getSessionCache().getEvictionIdleSecs();
            if (maxInactive <= 0L) {
                // sessions are immortal, they never expire
                if (evictionIdleSecs < SessionCache.EVICT_ON_INACTIVITY) {
                    // we do not want to evict inactive sessions
                    time = -1;
                    if (log.isDebugEnabled()) {
                        log.debug("Session " + getId() + " is immortal && no inactivity eviction");
                    }
                } else {
                    // sessions are immortal but we want to evict after inactivity
                    time = TimeUnit.SECONDS.toMillis(evictionIdleSecs);
                    if (log.isDebugEnabled()) {
                        log.debug("Session " + getId() + " is immortal; evict after " + evictionIdleSecs +
                                " sec inactivity");
                    }
                }
            } else {
                // sessions are not immortal
                if (evictionIdleSecs == SessionCache.NEVER_EVICT) {
                    // timeout is the time remaining until its expiry
                    time = (remaining > 0 ? remaining : 0);
                    if (log.isTraceEnabled()) {
                        log.trace("Session " + getId() + " no eviction");
                    }
                } else if (evictionIdleSecs == SessionCache.EVICT_ON_SESSION_EXIT) {
                    // session will not remain in the cache, so no timeout
                    time = -1;
                    if (log.isDebugEnabled()) {
                        log.debug("Session " + getId() + " evict on exit");
                    }
                } else {
                    // want to evict on idle: timer is lesser of the session's
                    // expiration remaining and the time to evict
                    time = (remaining > 0 ? Math.min(maxInactive, TimeUnit.SECONDS.toMillis(evictionIdleSecs)) : 0L);
                    if (log.isDebugEnabled()) {
                        log.debug("Session " + getId() + " timer set to lesser of maxIdleSeconds=" +
                                (maxInactive / 1000L) + " and evictionIdleSeconds=" + evictionIdleSecs);
                    }
                }
            }
        }
        return time;
    }

    /**
     * Called by users to invalidate a session, or called by the
     * access method as a request enters the session if the session
     * has expired, or called by manager as a result of scavenger
     * expiring session.
     */
    @Override
    public void invalidate() {
        boolean result = beginInvalidate();
        if (result) {
            if (getDestroyedReason() == null) {
                setDestroyedReason(DestroyedReason.INVALIDATED);
            }
            try {
                try {
                    // do the invalidation
                    sessionHandler.fireSessionDestroyedListeners(this);
                } finally {
                    // call the attribute removed listeners and finally mark it
                    // as invalid
                    finishInvalidate();
                }
                sessionHandler.removeSession(sessionData.getId(), false);
            } catch (Exception e) {
                log.warn("Failed to invalidate session", e);
            }
        }
    }

    protected boolean beginInvalidate() {
        boolean result = false;
        try (Lock ignored = locker.lock()) {
            switch (state) {
                case INVALID:
                    // spec does not allow invalidate of already invalid session
                    throw new IllegalStateException();
                case INVALIDATING:
                    if (log.isDebugEnabled()) {
                        log.debug("Session " + sessionData.getId() + " already being invalidated");
                    }
                    break;
                case VALID:
                    // only first change from valid to invalidating should be actionable
                    state = State.INVALIDATING;
                    result = true;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return result;
    }

    protected void finishInvalidate() {
        try (Lock ignored = locker.lock()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Invalidate session " + sessionData.getId());
                }
                if (state == State.VALID || state == State.INVALIDATING) {
                    Set<String> keys;
                    do {
                        keys = sessionData.getKeys();
                        for (String key : keys) {
                            Object old = sessionData.setAttribute(key, null);
                            if (old != null) {
                                fireSessionAttributeListeners(key, old, null);
                            }
                        }
                    } while (!keys.isEmpty());
                }
            } finally {
                // mark as invalid
                state = State.INVALID;
                sessionHandler.recordSessionTime(this);
            }
        }
    }

    @Override
    public DestroyedReason getDestroyedReason() {
        return destroyedReason;
    }

    protected void setDestroyedReason(DestroyedReason destroyedReason) {
        this.destroyedReason = destroyedReason;
    }

    @Override
    public boolean isNew() {
        try (Lock ignored = locker.lock()) {
            checkValidForRead();
            return newSession;
        }
    }

    @Override
    public boolean isValid() {
        try (Lock ignored = locker.lock()) {
            return (state == State.VALID);
        }
    }

    protected boolean isResident() {
        return resident;
    }

    protected void setResident(boolean resident) {
        this.resident = resident;
        if (!resident) {
            sessionInactivityTimer.destroy();
        }
    }

    /**
     * Check to see if session has expired as at the time given.
     *
     * @param time the time since the epoch in ms
     * @return true if expired
     */
    protected boolean isExpiredAt(long time) {
        try (Lock ignored = locker.lock()) {
            checkValidForRead();
            return sessionData.isExpiredAt(time);
        }
    }

    /**
     * Check if the Session has been idle longer than a number of seconds.
     *
     * @param sec the number of seconds
     * @return true if the session has been idle longer than the interval
     */
    protected boolean isIdleLongerThan(int sec) {
        long now = System.currentTimeMillis();
        try (Lock ignored = locker.lock()) {
            return ((sessionData.getAccessedTime() + (sec * 1000)) <= now);
        }
    }

    /**
     * Call binding and attribute listeners based on the new and old values of
     * the attribute.
     *
     * @param name name of the attribute
     * @param newValue new value of the attribute
     * @param oldValue previous value of the attribute
     * @throws IllegalStateException if no session manager can be find
     */
    protected void fireSessionAttributeListeners(String name, Object oldValue, Object newValue) {
        if (newValue == null || !newValue.equals(oldValue)) {
            if (oldValue != null) {
                unbindValue(name, oldValue);
            }
            if (newValue != null) {
                bindValue(name, newValue);
            }
        }
        sessionHandler.fireSessionAttributeListeners(this, name, oldValue, newValue);
    }

    /**
     * Unbind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueUnbound(Session, String, Object)})
     *
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    protected void unbindValue(String name, Object value) {
        if (value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueUnbound(this, name, value);
        }
    }

    /**
     * Bind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueBound(Session, String, Object)})
     *
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    protected void bindValue(String name, Object value) {
        if (value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueBound(this, name, value);
        }
    }

    /**
     * Check that the session can be modified.
     *
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForWrite() {
        checkLocked();
        if (state == State.INVALID) {
            throw new IllegalStateException("Not valid for write: session " + this);
        }
        if (state == State.INVALIDATING) {
            return;  // in the process of being invalidated, listeners may try to remove attributes
        }
        if (!isResident()) {
            throw new IllegalStateException("Not valid for write: session " + this);
        }
    }

    /**
     * Check that the session data can be read.
     *
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForRead() {
        checkLocked();
        if (state == State.INVALID) {
            throw new IllegalStateException("Invalid for read: session " + this);
        }
        if (state == State.INVALIDATING) {
            return;
        }
        if (!isResident()) {
            throw new IllegalStateException("Invalid for read: session " + this);
        }
    }

    protected void checkLocked() {
        if (!locker.isLocked()) {
            throw new IllegalStateException("Session not locked");
        }
    }

    /**
     * Grab the lock on the session.
     *
     * @return the lock
     */
    protected Lock lock() {
        return locker.lock();
    }

    @Override
    public String toString() {
        try (Lock ignored = locker.lock()) {
            ToStringBuilder tsb = new ToStringBuilder(getClass().getSimpleName() + "@" + hashCode());
            tsb.append("id", sessionData.getId());
            tsb.append("state", state);
            tsb.append("requests", requests);
            tsb.append("resident", resident);
            return tsb.toString();
        }
    }

    /**
     * The Class SessionInactivityTimer.
     * Each Session has a timer associated with it that fires whenever it has
     * been idle (ie not accessed by a request) for a configurable amount of
     * time, or the Session expires.
     */
    public class SessionInactivityTimer {

        protected final CyclicTimeout timer;

        SessionInactivityTimer() {
            timer = new CyclicTimeout((getSessionHandler().getScheduler())) {
                @Override
                public void onTimeoutExpired() {
                    if (log.isDebugEnabled()) {
                        log.debug("Timer expired for session " + getId());
                    }
                    long now = System.currentTimeMillis();
                    // handle what to do with the session after the timer expired
                    getSessionHandler().sessionInactivityTimerExpired(BasicSession.this, now);
                    try (Lock ignored = BasicSession.this.lock()) {
                        // grab the lock and check what happened to the session: if it didn't get evicted and
                        // it hasn't expired, we need to reset the timer
                        if (BasicSession.this.isResident() && BasicSession.this.getRequests() <= 0 &&
                            BasicSession.this.isValid() && !BasicSession.this.isExpiredAt(now)) {
                            // session wasn't expired or evicted, we need to reset the timer
                            SessionInactivityTimer.this.schedule(BasicSession.this.calculateInactivityTimeout(now));
                        }
                    }
                }
            };
        }

        /**
         * @param time the timeout to set; -1 means that the timer will not be scheduled
         */
        public void schedule (long time) {
            if (time >= 0) {
                if (log.isTraceEnabled()) {
                    log.trace("(Re)starting timer for session " + getId() + " at " + time + "ms");
                }
                timer.schedule(time, TimeUnit.MILLISECONDS);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Not starting timer for session " + getId());
                }
            }
        }

        public void cancel() {
            timer.cancel();
            if (log.isTraceEnabled()) {
                log.trace("Cancelled timer for session " + getId());
            }
        }

        public void destroy() {
            timer.destroy();
            if (log.isTraceEnabled()) {
                log.trace("Destroyed timer for session " + getId());
            }
        }
    }

}
