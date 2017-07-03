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

import java.util.Enumeration;
import java.util.Set;

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.Locker.Lock;

/**
 * The basic Session object.
 *
 * <p>Created: 2017. 6. 13.</p>
 */
public class BasicSession implements SessionAccess {

    private static final Log log = LogFactory.getLog(BasicSession.class);
    
    private final Locker locker = new Locker();

    private final SessionManager sessionManager;

    private final String id;
    
    private SessionData sessionData;

    private SessionInactivityTimer sessionInactivityTimer;

    private int requests;

    private boolean resident;

    private boolean newSession;

    private State state = State.VALID;

    /**
     * state of the session:valid,invalid or being invalidated
     */
    public enum State {
        VALID,
        INVALID,
        INVALIDATING
    }

    protected BasicSession(SessionManager sessionManager, SessionData sessionData) {
        this.sessionManager = sessionManager;
        this.id = sessionData.getId();
        this.sessionData = sessionData;
        this.newSession = true;
        updateInactivityTimer();
    }

    public String getId() {
        return id;
    }

    @Override
    public void access() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (!isValid()) {
                return;
            }
            if (!retrieveSessionData()) {
                invalidate();
                return;
            }
            long lastAccessedTime = sessionData.getAccessedTime();
            if (sessionInactivityTimer != null) {
                sessionInactivityTimer.notIdle();
            }
            long now = System.currentTimeMillis();
            sessionData.setAccessedTime(now);
            sessionData.setLastAccessedTime(lastAccessedTime);
            sessionData.calcAndSetExpiryTime(now);
            if (isExpiredAt(now)) {
                invalidate();
            }
            requests++;
        }
    }

    @Override
    public void complete() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (requests == 1) {
                requests = 0;
                storeSessionData();
                notNewSession();
            } else {
                requests--;
            }
        }
    }

    private boolean retrieveSessionData() {
        if (!newSession && requests == 0) {
            sessionData = sessionManager.loadSessionData(id, true);
        }
        return (sessionData != null);
    }

    private void storeSessionData() {
        if (sessionData != null && isValid() && isResident()) {
            sessionManager.storeSessionData(id, sessionData);
        }
    }

    public SessionScope getSessionScope() {
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
        sessionManager.sessionAttributeChanged(this, name, old, value);
    }

    public Enumeration<String> getAttributeNames() {
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
            checkValidForRead();
            return sessionData.getLastAccessedTime();
        }
    }

    public int getMaxInactiveInterval() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForRead();
            if (sessionData.getMaxInactiveInterval() > 0L) {
                return (int)(sessionData.getMaxInactiveInterval() / 1000L);
            } else {
                return -1;
            }
        }
    }

    public void setMaxInactiveInterval(int secs) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            checkValidForWrite();
            sessionData.setMaxInactiveInterval((long)secs * 1000L);
            sessionData.calcAndSetExpiryTime();
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
    private void updateInactivityTimer() {
        long maxInactive =  sessionData.getMaxInactiveInterval();
        if (maxInactive <= 0) {
            stopInactivityTimer();
        } else {
            // sessions are not immortal
            setInactivityTimer(maxInactive);
        }
    }

    /**
     * Set the session inactivity timer.
     *
     * @param ms value in millisec, -1 disables it
     */
    private void setInactivityTimer(long ms) {
        if (sessionInactivityTimer == null) {
            sessionInactivityTimer = new SessionInactivityTimer(sessionManager.getScheduler(), this);
        }
        sessionInactivityTimer.setIdleTimeout(ms);
    }

    /**
     * Stop the session inactivity timer.
     */
    private void stopInactivityTimer() {
        if (sessionInactivityTimer != null) {
            sessionInactivityTimer.setIdleTimeout(-1);
            sessionInactivityTimer = null;
            if (log.isDebugEnabled()) {
                log.debug("Session inactivity timer stopped");
            }
        }
    }

    public void invalidate() {
        sessionManager.invalidate(id);
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
                    stopInactivityTimer();
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

    protected void finishInvalidate() throws IllegalStateException {
        try (Lock ignored = locker.lockIfNotHeld()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Invalidate session " + this);
                }
                if (state == State.VALID || state == State.INVALIDATING) {
                    if (sessionData != null) {
                        SessionScope sessionScope = sessionData.getSessionScope();
                        sessionScope.destroy();

                        Set<String> keys;
                        do {
                            keys = sessionData.getKeys();
                            for (String key : keys) {
                                Object old = sessionData.setAttribute(key, null);
                                if (old != null) {
                                    sessionManager.sessionAttributeChanged(this, key, old, null);
                                }
                            }
                        } while (!keys.isEmpty());
                    }
                }
            } finally {
                // mark as invalid
                state = State.INVALID;
            }
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

    public boolean isNew() throws IllegalStateException {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return newSession;
        }
    }

    private void notNewSession() {
        this.newSession = false;
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
     * Check that the session can be modified.
     *
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForWrite() throws IllegalStateException {
        checkLocked();

        if (sessionData == null) {
            throw new IllegalStateException("Not valid for write: session " + this);
        }
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
     * Chech that the session data can be read.
     *
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForRead() throws IllegalStateException {
        checkLocked();

        if (sessionData == null) {
            throw new IllegalStateException("Invalid for read: session " + this);
        }
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
