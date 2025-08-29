/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-20.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils.timer;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.scheduling.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Destroyable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Long.MAX_VALUE;

/**
 * An abstract implementation of a cyclic timeout mechanism.
 * <p>This class is a clone of {@code org.eclipse.jetty.io.CyclicTimeout}.</p>
 * <p>Subclasses must implement {@link #onTimeoutExpired()} to define the action to be performed when the timeout expires.</p>
 * <p>This implementation is optimized for scenarios where timeouts are frequently canceled and then
 * rescheduled with similar values, reducing pressure on the underlying {@link Scheduler}.</p>
 * <p>It uses a {@link Timeout} object to hold the scheduled time and a linked list of {@link Wakeup} tasks.
 * When {@link #schedule(long, TimeUnit)} is called, it either creates a new {@code Wakeup} and schedules it,
 * or reuses an existing one if the new timeout is later than the current one, thus avoiding unnecessary
 * scheduler submissions.</p>
 */
public abstract class CyclicTimeout implements Destroyable {

    private static final Logger logger = LoggerFactory.getLogger(CyclicTimeout.class);

    private static final Timeout NOT_SET = new Timeout(MAX_VALUE, null);

    private static final Scheduler.Task DESTROYED = () -> false;

    /** The underlying scheduler to use. */
    private final Scheduler scheduler;

    /** Reference to the current Timeout and chain of Wakeup. */
    private final AtomicReference<Timeout> timeout = new AtomicReference<>(NOT_SET);

    /**
     * Creates a new CyclicTimeout instance.
     * @param scheduler the {@link Scheduler} used to schedule wakeups
     */
    public CyclicTimeout(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Returns the scheduler used by this CyclicTimeout.
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Schedules a timeout. If a timeout is already set, it will be canceled and replaced
     * by the new one. This method is optimized to reduce scheduler pressure by reusing
     * existing wakeup tasks when possible.
     * @param delay the period of time before the timeout expires
     * @param units the time unit of the period
     * @return {@code true} if the timeout was already set (and thus replaced), {@code false} otherwise
     */
    public boolean schedule(long delay, @NonNull TimeUnit units) {
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
                    logger.trace("Installed timeout in {} ms, {} waking up in {} ms",
                            units.toMillis(delay),
                            (newWakeup != null ? "new" : "existing"),
                            TimeUnit.NANOSECONDS.toMillis(wakeup.at - now));
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
     * Cancels this CyclicTimeout so that it won't expire.
     * After being canceled, this CyclicTimeout can be scheduled again.
     * @return {@code true} if this CyclicTimeout was scheduled to expire and was successfully canceled,
     *         {@code false} otherwise
     */
    public boolean cancel() {
        boolean result;
        while (true) {
            Timeout timeout = this.timeout.get();
            result = (timeout.at != MAX_VALUE);
            Wakeup wakeup = timeout.wakeup;
            Timeout newTimeout = (wakeup == null ? NOT_SET : new Timeout(MAX_VALUE, wakeup));
            if (this.timeout.compareAndSet(timeout, newTimeout)) {
                break;
            }
        }
        return result;
    }

    /**
     * Invoked when the timeout expires.
     * Subclasses must implement this method to define the action to be performed.
     */
    public abstract void onTimeoutExpired();

    /**
     * Destroys this CyclicTimeout.
     * After being destroyed, this CyclicTimeout is not used anymore.
     * It cancels any pending tasks and cleans up resources.
     */
    @Override
    public void destroy() {
        Timeout timeout = this.timeout.getAndSet(NOT_SET);
        Wakeup wakeup = (timeout != null ? timeout.wakeup : null);
        while (wakeup != null) {
            wakeup.destroy();
            wakeup = wakeup.next;
        }
    }

    /**
     * Represents a specific timeout time and links to a chain of {@link Wakeup} tasks.
     */
    private static class Timeout {
        private final long at;
        private final Wakeup wakeup;

        private Timeout(long timeoutAt, Wakeup wakeup) {
            this.at = timeoutAt;
            this.wakeup = wakeup;
        }

        @Override
        @NonNull
        public String toString() {
            return String.format("%s@%x:%dms,%s",
                getClass().getSimpleName(),
                hashCode(),
                TimeUnit.NANOSECONDS.toMillis(at),
                wakeup);
        }
    }

    /**
     * Represents a wakeup task in a chain, which is a {@link Runnable} that gets scheduled
     * by the {@link Scheduler}.
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
                    if (wakeup == null || wakeup.at > timeout.at) {
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
        @NonNull
        public String toString() {
            return String.format("%s@%x:%dms->%s",
                getClass().getSimpleName(),
                hashCode(),
                (at == MAX_VALUE ? at : TimeUnit.NANOSECONDS.toMillis(at)),
                next);
        }
    }

}
