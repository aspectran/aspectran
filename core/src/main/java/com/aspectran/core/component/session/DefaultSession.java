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

import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.AutoLock;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A default {@link Session} implementation.
 *
 * <p>Created: 2017. 6. 13.</p>
 */
public class DefaultSession implements Session {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSession.class);

    private final AutoLock autoLock = new AutoLock();

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

    protected DefaultSession(SessionData sessionData, SessionHandler sessionHandler, boolean newSession) {
        this.sessionData = sessionData;
        this.sessionHandler = sessionHandler;
        this.newSession = newSession;
        if (newSession) {
            this.sessionData.setDirty(true);
            this.requests = 1;
        }
        this.sessionInactivityTimer = new SessionInactivityTimer(this);
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    @Override
    public String getId() {
        try (AutoLock ignored = autoLock.lock()) {
            return sessionData.getId();
        }
    }

    @Override
    public <T> T getAttribute(String name) {
        try (AutoLock ignored = autoLock.lock()) {
            checkValidForRead();
            return sessionData.getAttribute(name);
        }
    }

    @Override
    public Set<String> getAttributeNames() {
        try (AutoLock ignored = autoLock.lock()) {
            checkValidForRead();
            return sessionData.getKeys();
        }
    }

    @Override
    public Object setAttribute(String name, Object value) {
        try (AutoLock ignored = autoLock.lock()) {
            // if session is not valid, don't accept the set
            checkValidForWrite();
            Object old = sessionData.setAttribute(name, value);
            if (value == null && old == null) {
                return null; // if same as remove attribute but attribute was already removed, no change
            }
            onSessionAttributeUpdate(name, old, value);
            return old;
        }
    }

    @Override
    public Object removeAttribute(String name) {
        return setAttribute(name, null);
    }

    @Override
    public long getCreationTime() {
        try (AutoLock ignored = autoLock.lock()) {
            checkValidForRead();
            return sessionData.getCreated();
        }
    }

    @Override
    public long getLastAccessedTime() {
        try (AutoLock ignored = autoLock.lock()) {
            return sessionData.getLastAccessed();
        }
    }

    @Override
    public int getMaxInactiveInterval() {
        try (AutoLock ignored = autoLock.lock()) {
            if (sessionData.getInactiveInterval() > 0L) {
                return (int)(sessionData.getInactiveInterval() / 1000L);
            } else {
                return -1;
            }
        }
    }

    @Override
    public void setMaxInactiveInterval(int secs) {
        try (AutoLock ignored = autoLock.lock()) {
            sessionData.setInactiveInterval((long)secs * 1000L);
            sessionData.calcAndSetExpiry();
            sessionData.setDirty(true);
            if (logger.isDebugEnabled()) {
                if (secs <= 0) {
                    logger.debug("Session " + sessionData.getId() + " is now immortal (maxInactiveInterval=" + secs + ")");
                } else {
                    logger.debug("Session " + sessionData.getId() + " maxInactiveInterval=" + secs);
                }
            }
        }
    }

    @Override
    public boolean access() {
        try (AutoLock ignored = autoLock.lock()) {
            if (state != State.VALID) {
                return false;
            }

            newSession = false;

            long now = System.currentTimeMillis();
            sessionData.setAccessed(now);
            sessionData.calcAndSetExpiry(now);
            if (isExpiredAt(now)) {
                invalidate();
                return false;
            }

            requests++;

            // temporarily stop the idle timer
            if (logger.isDebugEnabled()) {
                logger.debug("Session " + getId() + " accessed, stopping timer, active requests=" + requests);
            }
            sessionInactivityTimer.cancel();

            return true;
        }
    }

    @Override
    public void complete() {
        try (AutoLock ignored = autoLock.lock()) {
            requests--;

            if (requests < 0) {
                int temp = requests;
                requests = 0;
                throw new IllegalStateException("Incomplete session transaction; requests=" + temp);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Session " + getId() + " complete, active requests=" + requests);
            }

            // start the inactivity timer if necessary
            if (requests == 0) {
                // update the expiry time to take account of the time all requests spent inside of the session
                long now = System.currentTimeMillis();
                sessionData.calcAndSetExpiry(now);
                sessionData.setLastAccessed(sessionData.getAccessed());
                sessionHandler.releaseSession(this);
                sessionInactivityTimer.schedule(calculateInactivityTimeout(now));
            }
        }
    }

    /**
     * Returns the current number of requests that are active in the Session.
     * @return the number of active requests for this session
     */
    protected long getRequests() {
        try (AutoLock ignored = autoLock.lock()) {
            return requests;
        }
    }

    /**
     * Calculate what the session timer setting should be based on:
     * the time remaining before the session expires
     * and any idle eviction time configured.
     * The timer value will be the lesser of the above.
     * @param now the time at which to calculate remaining expiry
     * @return the time remaining before expiry or inactivity timeout
     */
    protected long calculateInactivityTimeout(long now) {
        long time;
        try (AutoLock ignored = autoLock.lock()) {
            long remaining = sessionData.getExpiry() - now;
            long maxInactive = sessionData.getInactiveInterval();
            int evictionIdleSecs = sessionHandler.getSessionCache().getEvictionIdleSecs();
            if (maxInactive <= 0L) {
                // sessions are immortal, they never expire
                if (evictionIdleSecs < SessionCache.EVICT_ON_INACTIVITY) {
                    // we do not want to evict inactive sessions
                    time = -1L;
                    if (logger.isTraceEnabled()) {
                        logger.trace("Session " + getId() + " is immortal && no inactivity eviction");
                    }
                } else {
                    // sessions are immortal but we want to evict after inactivity
                    time = TimeUnit.SECONDS.toMillis(evictionIdleSecs);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Session " + getId() + " is immortal; evict after " + evictionIdleSecs +
                                " sec inactivity");
                    }
                }
            } else {
                // sessions are not immortal
                if (evictionIdleSecs == SessionCache.NEVER_EVICT) {
                    // timeout is the time remaining until its expiry
                    time = Math.max(remaining, 0L);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Session " + getId() + " no eviction");
                    }
                } else if (evictionIdleSecs == SessionCache.EVICT_ON_SESSION_EXIT) {
                    // session will not remain in the cache, so no timeout
                    time = -1L;
                    if (logger.isTraceEnabled()) {
                        logger.trace("Session " + getId() + " evict on exit");
                    }
                } else {
                    // want to evict on idle: timer is the lesser of the session's
                    // expiration remaining and the time to evict
                    time = (remaining > 0L ? Math.min(maxInactive, TimeUnit.SECONDS.toMillis(evictionIdleSecs)) : 0L);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Session " + getId() + " timer set to lesser of maxIdleSeconds=" +
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
        // if the session was not already invalid, or in process of being
        // invalidated, do invalidate
        if (result) {
            try {
                try {
                    // do the invalidation
                    try (AutoLock ignored = autoLock.lock()) {
                        if (getDestroyedReason() == null) {
                            setDestroyedReason(DestroyedReason.INVALIDATED);
                        }
                        sessionHandler.onSessionDestroyed(this);
                    }
                } catch (Exception e) {
                    logger.warn("Error during Session destroy listener", e);
                } finally {
                    // call the attribute removed listeners and finally mark it as invalid
                    finishInvalidate();
                    sessionHandler.removeSession(sessionData.getId(), false);
                }
            } catch (Exception e) {
                logger.warn("Unable to invalidate session " + this, e);
            }
        }
    }

    protected boolean beginInvalidate() {
        boolean result = false;
        try (AutoLock ignored = autoLock.lock()) {
            switch (state) {
                case INVALID:
                    // spec does not allow invalidation of already invalid session
                    throw new IllegalStateException();
                case INVALIDATING:
                    if (logger.isDebugEnabled()) {
                        logger.debug("Session " + sessionData.getId() + " already being invalidated");
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
        try (AutoLock ignored = autoLock.lock()) {
            try {
                if (state == State.VALID || state == State.INVALIDATING) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalidate session id=" + sessionData.getId());
                    }
                    Set<String> keys;
                    do {
                        keys = sessionData.getKeys();
                        for (String key : keys) {
                            Object old = sessionData.setAttribute(key, null);
                            if (old != null) {
                                onSessionAttributeUpdate(key, old, null);
                            }
                        }
                    } while (!keys.isEmpty());
                }
            } finally {
                // mark as invalid
                state = State.INVALID;
                sessionHandler.getStatistics().sessionExpired();
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
    public boolean isValid() {
        try (AutoLock ignored = autoLock.lock()) {
            return (state == State.VALID);
        }
    }

    @Override
    public boolean isNew() {
        try (AutoLock ignored = autoLock.lock()) {
            checkValidForRead();
            return newSession;
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
     * @param time the time since the epoch in ms
     * @return true if expired
     */
    protected boolean isExpiredAt(long time) {
        try (AutoLock ignored = autoLock.lock()) {
            checkValidForRead();
            return sessionData.isExpiredAt(time);
        }
    }

    /**
     * Check if the Session has been idle longer than a number of seconds.
     * @param sec the number of seconds
     * @return true if the session has been idle longer than the interval
     */
    protected boolean isIdleLongerThan(int sec) {
        long now = System.currentTimeMillis();
        try (AutoLock ignored = autoLock.lock()) {
            return ((sessionData.getAccessed() + (sec * 1000L)) <= now);
        }
    }

    /**
     * Call binding and attribute listeners based on the new and old values of
     * the attribute.
     * @param name name of the attribute
     * @param newValue new value of the attribute
     * @param oldValue previous value of the attribute
     * @throws IllegalStateException if no session manager can be find
     */
    protected void onSessionAttributeUpdate(String name, Object oldValue, Object newValue) {
        if (newValue == null || !newValue.equals(oldValue)) {
            if (oldValue != null) {
                unbindValue(name, oldValue);
            }
            if (newValue != null) {
                bindValue(name, newValue);
            }
        }
        sessionHandler.onSessionAttributeUpdate(this, name, oldValue, newValue);
    }

    /**
     * Unbind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueUnbound(Session, String, Object)})
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    protected void unbindValue(String name, Object value) {
        if (value instanceof SessionBindingListener listener) {
            listener.valueUnbound(this, name, value);
        }
    }

    /**
     * Bind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueBound(Session, String, Object)})
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    protected void bindValue(String name, Object value) {
        if (value instanceof SessionBindingListener listener) {
            listener.valueBound(this, name, value);
        }
    }

    /**
     * Check that the session can be modified.
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForWrite() {
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
     * @throws IllegalStateException if the session is invalid
     */
    protected void checkValidForRead() {
        if (state == State.INVALID) {
            throw new IllegalStateException("Invalid for read: session " + this);
        }
        if (state == State.INVALIDATING) {
            return;
        }
        if (!isResident()) {
            throw new IllegalStateException("Invalid for read: id=" + sessionData.getId() + " not resident");
        }
    }

    /**
     * Grab the lock on the session.
     * @return the lock
     */
    protected AutoLock lock() {
        return autoLock.lock();
    }

    @Override
    public String toString() {
        try (AutoLock ignored = autoLock.lock()) {
            ToStringBuilder tsb = new ToStringBuilder(getClass().getSimpleName() + "@" + hashCode());
            tsb.append("id", sessionData.getId());
            tsb.append("state", state);
            tsb.append("requests", requests);
            tsb.appendForce("resident", resident);
            return tsb.toString();
        }
    }

}
