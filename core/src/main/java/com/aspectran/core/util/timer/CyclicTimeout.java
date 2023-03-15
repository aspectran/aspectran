/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.util.timer;

import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.thread.Scheduler;

import javax.security.auth.Destroyable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Long.MAX_VALUE;

/**
 * <p>This class is a clone of org.eclipse.jetty.io.CyclicTimeout</p>
 *
 * <p>An abstract implementation of a timeout.</p>
 * <p>Subclasses should implement {@link #onTimeoutExpired()}.</p>
 * <p>This implementation is optimised assuming that the timeout
 * will mostly be cancelled and then reused with a similar value.</p>
 * <p>This implementation has a {@link CyclicTimeout.Timeout} holding the time
 * at which the scheduled task should fire, and a linked list of
 * {@link CyclicTimeout.Wakeup}, each holding the actual scheduled task.</p>
 * <p>Calling {@link #schedule(long, TimeUnit)} the first time will
 * create a Timeout with an associated Wakeup and submit a task to
 * the scheduler.
 * Calling {@link #schedule(long, TimeUnit)} again with the same or
 * a larger delay will cancel the previous Timeout, but keep the
 * previous Wakeup without submitting a new task to the scheduler,
 * therefore reducing the pressure on the scheduler and avoid it
 * becomes a bottleneck.
 * When the Wakeup task fires, it will see that the Timeout is now
 * in the future and will attach a new Wakeup with the future time
 * to the Timeout, and submit a scheduler task for the new Wakeup.</p>
 */
public abstract class CyclicTimeout implements Destroyable {

    private static final Logger logger = LoggerFactory.getLogger(CyclicTimeout.class);

    private static final Timeout NOT_SET = new Timeout(MAX_VALUE, null);

    private static final Scheduler.Task DESTROYED = () -> false;

    /* The underlying scheduler to use */
    private final Scheduler scheduler;

    /* Reference to the current Timeout and chain of Wakeup */
    private final AtomicReference<Timeout> timeout = new AtomicReference<>(NOT_SET);

    /**
     * @param scheduler A scheduler used to schedule wakeups
     */
    public CyclicTimeout(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * <p>Schedules a timeout, even if already set, cancelled or expired.</p>
     * <p>If a timeout is already set, it will be cancelled and replaced
     * by the new one.</p>
     *
     * @param delay The period of time before the timeout expires.
     * @param units The unit of time of the period.
     * @return true if the timeout was already set.
     */
    public boolean schedule(long delay, TimeUnit units) {
        long now = System.nanoTime();
        long newTimeoutAt = now + units.toNanos(delay);

        Wakeup newWakeup = null;
        boolean result;
        while (true) {
            Timeout timeout = this.timeout.get();
            result = (timeout.at != MAX_VALUE);

            // Is the current wakeup good to use? ie before our timeout time?
            Wakeup wakeup = timeout.wakeup;
            if (wakeup == null || wakeup.at > newTimeoutAt) {
                // No, we need an earlier wakeup.
                wakeup = newWakeup = new Wakeup(newTimeoutAt, wakeup);
            }

            if (this.timeout.compareAndSet(timeout, new Timeout(newTimeoutAt, wakeup))) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Installed timeout in " + units.toMillis(delay) + " ms, waking up in " +
                        TimeUnit.NANOSECONDS.toMillis(wakeup.at - now) + " ms");
                }
                break;
            }
        }

        // If we created a new wakeup, we need to actually schedule it.
        // Any wakeup that is created and discarded by the failed CAS will not be
        // in the wakeup chain, will not have a scheduler task set and will be GC'd.
        if (newWakeup != null) {
            newWakeup.schedule(now);
        }

        return result;
    }

    /**
     * <p>Cancels this CyclicTimeout so that it won't expire.</p>
     * <p>After being cancelled, this CyclicTimeout can be scheduled again.</p>
     *
     * @return true if this CyclicTimeout was scheduled to expire
     * @see #destroy()
     */
    public boolean cancel() {
        boolean result;
        while (true) {
            Timeout timeout = this.timeout.get();
            result = (timeout.at != MAX_VALUE);
            Wakeup wakeup = timeout.wakeup;
            Timeout new_timeout = (wakeup == null ? NOT_SET : new Timeout(MAX_VALUE, wakeup));
            if (this.timeout.compareAndSet(timeout, new_timeout)) {
                break;
            }
        }
        return result;
    }

    /**
     * <p>Invoked when the timeout expires.</p>
     */
    public abstract void onTimeoutExpired();

    /**
     * <p>Destroys this CyclicTimeout.</p>
     * <p>After being destroyed, this CyclicTimeout is not used anymore.</p>
     */
    @Override
    public void destroy() {
        Timeout timeout = this.timeout.getAndSet(NOT_SET);
        Wakeup wakeup = timeout == null ? null : timeout.wakeup;
        while (wakeup != null) {
            wakeup.destroy();
            wakeup = wakeup.next;
        }
    }

    /**
     * A timeout time with a link to a Wakeup chain.
     */
    private static class Timeout {
        private final long at;
        private final Wakeup wakeup;

        private Timeout(long timeoutAt, Wakeup wakeup) {
            this.at = timeoutAt;
            this.wakeup = wakeup;
        }

        @Override
        public String toString() {
            return String.format("%s@%x:%dms,%s",
                getClass().getSimpleName(),
                hashCode(),
                TimeUnit.NANOSECONDS.toMillis(at - System.nanoTime()),
                wakeup);
        }
    }

    /**
     * A Wakeup chain of real scheduler tasks.
     */
    private class Wakeup implements Runnable {
        private final AtomicReference<Scheduler.Task> task = new AtomicReference<>();
        private final long at;
        private final Wakeup next;

        private Wakeup(long wakeupAt, Wakeup next) {
            this.at = wakeupAt;
            this.next = next;
        }

        private void schedule(long now) {
            task.compareAndSet(null, scheduler.schedule(this, at - now, TimeUnit.NANOSECONDS));
        }

        private void destroy() {
            Scheduler.Task task = this.task.getAndSet(DESTROYED);
            if (task != null) {
                task.cancel();
            }
        }

        @Override
        public void run() {
            long now = System.nanoTime();
            Wakeup newWakeup = null;
            boolean hasExpired = false;
            while (true) {
                Timeout timeout = CyclicTimeout.this.timeout.get();

                // We must look for ourselves in the current wakeup list.
                // If we find ourselves, then we act and we use our tail for any new
                // wakeup list, effectively removing any wakeup before us in the list (and making them no-ops).
                // If we don't find ourselves, then a wakeup that should have expired after us has already run
                // and removed us from the list, so we become a noop.

                Wakeup wakeup = timeout.wakeup;
                while (wakeup != null) {
                    if (wakeup == this) {
                        break;
                    }
                    // Not us, so look at next wakeup in the list.
                    wakeup = wakeup.next;
                }
                if (wakeup == null) {
                    // Not found, we become a noop.
                    return;
                }

                // We are in the wakeup list! So we have to act and we know our
                // tail has not expired (else it would have removed us from the list).
                // Remove ourselves (and any prior Wakeup) from the wakeup list.
                wakeup = wakeup.next;

                Timeout newTimeout;
                if (timeout.at <= now) {
                    // We have timed out!
                    hasExpired = true;
                    newTimeout = (wakeup == null ? NOT_SET : new Timeout(MAX_VALUE, wakeup));
                } else if (timeout.at != MAX_VALUE) {
                    // We have not timed out, but we are set to!
                    // Is the current wakeup good to use? ie before our timeout time?
                    if (wakeup == null || wakeup.at >= timeout.at) {
                        // No, we need an earlier wakeup.
                        wakeup = newWakeup = new Wakeup(timeout.at, wakeup);
                    }
                    newTimeout = new Timeout(timeout.at, wakeup);
                } else {
                    // We don't timeout, preserve scheduled chain.
                    newTimeout = (wakeup == null ? NOT_SET : new Timeout(MAX_VALUE, wakeup));
                }

                // Loop until we succeed in changing state or we are a noop!
                if (CyclicTimeout.this.timeout.compareAndSet(timeout, newTimeout)) {
                    break;
                }
            }

            // If we created a new wakeup, we need to actually schedule it.
            if (newWakeup != null) {
                newWakeup.schedule(now);
            }

            // If we expired, then do the callback.
            if (hasExpired) {
                onTimeoutExpired();
            }
        }

        @Override
        public String toString() {
            return String.format("%s@%x:%dms->%s",
                getClass().getSimpleName(),
                hashCode(),
                at == MAX_VALUE ? at : TimeUnit.NANOSECONDS.toMillis(at - System.nanoTime()),
                next);
        }

    }

}
