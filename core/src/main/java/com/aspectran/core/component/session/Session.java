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

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.Locker.Lock;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The basic Session object.
 *
 * <p>Created: 2017. 6. 13.</p>
 */
public class Session {

    private static final Log log = LogFactory.getLog(Session.class);
    
    private final Locker locker = new Locker();

    private final SessionHandler sessionHandler;

    private final String id;
    
    private SessionData sessionData;

    private SessionInactivityTimer sessionInactivityTimer;

    private boolean newSession;

    private boolean resident;

    private int requests;

    private State state = State.VALID;

    /**
     * state of the session: valid, invalid or being invalidated
     */
    public enum State {
        VALID,
        INVALID,
        INVALIDATING
    }

    protected Session(SessionHandler sessionHandler, SessionData sessionData) {
        this(sessionHandler, sessionData, false);
    }

    protected Session(SessionHandler sessionHandler, SessionData sessionData, boolean newSession) {
        this.sessionHandler = sessionHandler;
        this.id = sessionData.getId();
        this.sessionData = sessionData;
        this.newSession = newSession;
        if (newSession) {
            this.sessionData.setDirty(true);
            this.requests = 1;
        }
    }

    public String getId() {
        return id;
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    protected SessionScope getSessionScope() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForRead();
            return sessionData.getSessionScope();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForRead();
            return sessionData.getAttribute(name);
        }
    }

    public void setAttribute(String name, Object value) {
        Object old;
        try (Lock ignored = locker.lockIfNotHeld()) {
            //if session is not valid, don't accept the set
            checkValidForWrite();
            old = sessionData.setAttribute(name, value);
        }
        if (value == null && old == null) {
            return; //if same as remove attribute but attribute was already removed, no change
        }
        sessionHandler.sessionAttributeChanged(this, name, old, value);
    }

    public Collection<String> getAttributeNames() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForRead();
            return sessionData.getAttributeNames();
        }
    }

    public void removeAttribute(String name) {
        setAttribute(name, null);
    }

    public long getCreationTime() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForRead();
            return sessionData.getCreationTime();
        }
    }

    public long getLastAccessedTime() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return sessionData.getLastAccessedTime();
        }
    }

    public int getMaxInactiveInterval() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (sessionData.getMaxInactiveInterval() > 0L) {
                return (int)(sessionData.getMaxInactiveInterval() / 1000L);
            } else {
                return -1;
            }
        }
    }

    public void setMaxInactiveInterval(int secs) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            sessionData.setMaxInactiveInterval((long)secs * 1000L);
            sessionData.calcAndSetExpiryTime();
            sessionData.setDirty(true);
            updateInactivityTimer();
            if (log.isDebugEnabled()) {
                if (secs <= 0) {
                    log.debug("Session " + id + " is now immortal (maxInactiveInterval=" + secs + ")");
                } else {
                    log.debug("Session " + id + " maxInactiveInterval=" + secs);
                }
            }
        }
    }

    /**
     * Set the inactivity timer to the smaller of the session maxInactivity
     * (ie session-timeout from web.xml), or the inactive eviction time.
     */
    public void updateInactivityTimer () {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (log.isDebugEnabled()) {
                log.debug("updateInactivityTimer");
            }

            long maxInactive =  sessionData.getMaxInactiveInterval();
            int evictionPolicy = sessionHandler.getSessionCache().getEvictionPolicy();

            if (maxInactive <= 0) {
                //sessions are immortal, they never expire
                if (evictionPolicy < SessionCache.EVICT_ON_INACTIVITY) {
                    //we do not want to evict inactive sessions
                    setInactivityTimer(-1L);
                    if (log.isDebugEnabled()) {
                        log.debug("Session is immortal && no inactivity eviction: timer cancelled");
                    }
                } else {
                    //sessions are immortal but we want to evict after inactivity
                    setInactivityTimer(TimeUnit.SECONDS.toMillis(evictionPolicy));
                    if (log.isDebugEnabled()) {
                        log.debug("Session is immortal; evict after " + evictionPolicy + " sec inactivity");
                    }
                }
            } else {
                //sessions are not immortal
                if (evictionPolicy < SessionCache.EVICT_ON_INACTIVITY) {
                    //don't want to evict inactive sessions, set the timer for the session's maxInactive setting
                    setInactivityTimer(sessionData.getMaxInactiveInterval());
                    if (log.isDebugEnabled()) {
                        log.debug("No inactive session eviction");
                    }
                } else {
                    //set the time to the lesser of the session's maxInactive and eviction timeout
                    setInactivityTimer(Math.min(maxInactive, TimeUnit.SECONDS.toMillis(evictionPolicy)));
                    if (log.isDebugEnabled()) {
                        log.debug("Inactivity timer set to lesser of maxInactive=" + maxInactive +
                                " and inactivityEvict=" + evictionPolicy);
                    }
                }
            }
        }
    }

    /**
     * Set the session inactivity timer.
     *
     * @param ms value in millisec, -1 disables it
     */
    private void setInactivityTimer(long ms) {
        if (sessionInactivityTimer == null) {
            sessionInactivityTimer = new SessionInactivityTimer(sessionHandler.getScheduler(), this);
        }
        sessionInactivityTimer.setIdleTimeout(ms);
    }

    /**
     * Stop the session inactivity timer.
     */
    protected void stopInactivityTimer() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (sessionInactivityTimer != null) {
                sessionInactivityTimer.setIdleTimeout(-1);
                sessionInactivityTimer = null;
                if (log.isDebugEnabled()) {
                    log.debug("Session inactivity timer stopped");
                }
            }
        }
    }

    /**
     * Called by users to invalidate a session, or called by the
     * access method as a request enters the session if the session
     * has expired, or called by manager as a result of scavenger
     * expiring session.
     */
    public void invalidate() {
        sessionHandler.invalidate(id);
    }

    protected boolean beginInvalidate() {
        boolean result = false;
        try (Lock ignored = locker.lockIfNotHeld()) {
            switch (state) {
                case INVALID:
                    // spec does not allow invalidate of already invalid session
                    throw new IllegalStateException();
                case VALID:
                    // only first change from valid to invalidating should be actionable
                    state = State.INVALIDATING;
                    result = true;
                    break;
                default:
                    if (log.isDebugEnabled()) {
                        log.debug("Session " + id + " already being invalidated");
                    }
            }
        }
        return result;
    }

    protected void finishInvalidate() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Invalidate session " + this);
                }
                if (state == State.VALID || state == State.INVALIDATING) {
                    SessionScope sessionScope = sessionData.getSessionScope();
                    sessionScope.destroy();

                    Set<String> keys;
                    do {
                        keys = sessionData.getKeys();
                        for (String key : keys) {
                            Object old = sessionData.setAttribute(key, null);
                            if (old != null) {
                                sessionHandler.sessionAttributeChanged(this, key, old, null);
                            }
                        }
                    } while (!keys.isEmpty());
                }
            } finally {
                // mark as invalid
                state = State.INVALID;
            }
        }
    }

    public boolean isNew() throws IllegalStateException {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForRead();
            return newSession;
        }
    }

    public boolean isValid() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (state == State.VALID);
        }
    }

    protected boolean isResident() {
        return resident;
    }

    protected void setResident(boolean resident) {
        this.resident = resident;
    }

    /**
     * Check to see if session has expired as at the time given.
     *
     * @param time the time since the epoch in ms
     * @return true if expired
     */
    protected boolean isExpiredAt(long time) {
        try (Lock ignored = locker.lockIfNotHeld()) {
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
    protected boolean isIdleLongerThan (int sec) {
        long now = System.currentTimeMillis();
        try (Lock ignored = locker.lockIfNotHeld()) {
            return ((sessionData.getAccessedTime() + (sec * 1000)) <= now);
        }
    }

    /**
     * Check that the session can be modified.
     *
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForWrite() throws IllegalStateException {
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
    protected void checkValidForRead() throws IllegalStateException {
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

    protected void checkLocked() throws IllegalStateException {
        if (!locker.isLocked()) {
            throw new IllegalStateException("Session not locked");
        }
    }

    /**
     * Grab the lock on the session.
     *
     * @return the lock
     */
    public Lock lock() {
        return locker.lock();
    }

    /**
     * Grab the lock on the session if it isn't locked already.
     *
     * @return the lock
     */
    public Lock lockIfNotHeld() {
        return locker.lockIfNotHeld();
    }

    protected boolean access(long time) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (!isValid()) {
                return false;
            }
            newSession = false;
            long lastAccessedTime = sessionData.getAccessedTime();
            if (sessionInactivityTimer != null) {
                sessionInactivityTimer.notIdle();
            }
            sessionData.setAccessedTime(time);
            sessionData.setLastAccessedTime(lastAccessedTime);
            sessionData.calcAndSetExpiryTime(time);
            if (isExpiredAt(time)) {
                invalidate();
                return false;
            }
            requests++;
            return true;
        }
    }

    protected void complete() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            requests--;
        }
    }

    /**
     * Returns the current number of requests that are active in the Session.
     *
     * @return the number of active requests for this session
     */
    public long getRequests() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return requests;
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", getId());
        tsb.append("state", state);
        tsb.append("requests", requests);
        tsb.append("resident", resident);
        tsb.append("newSession", newSession);
        tsb.append("sessionData", sessionData);
        return tsb.toString();
    }

}
