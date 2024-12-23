package com.aspectran.core.component.session;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.AutoLock;
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

    private final DefaultSession session;

    private final CyclicTimeout timer;

    public SessionInactivityTimer(@NonNull DefaultSession session) {
        this.session = session;
        this.timer = new CyclicTimeout(session.getSessionHandler().getScheduler()) {
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

                    // handle what to do with the session after the timer expired
                    boolean expired = session.getSessionHandler().sessionInactivityTimerExpired(session, now);

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
