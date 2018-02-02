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
package com.aspectran.core.util.thread;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Created: 2017. 6. 25.</p>
 */
public class ScheduledExecutorScheduler implements Scheduler {

    private static final Log log = LogFactory.getLog(ScheduledExecutorScheduler.class);

    private final AtomicBoolean active = new AtomicBoolean();

    private volatile ScheduledThreadPoolExecutor executor;

    @Override
    public void start() {
        if (this.active.compareAndSet(false, true)) {
            if (log.isDebugEnabled()) {
                log.debug("Starting " + this);
            }

            executor = new ScheduledThreadPoolExecutor(1);
            executor.setRemoveOnCancelPolicy(true);
        } else {
            if (log.isDebugEnabled()) {
                log.warn("Already Started " + this);
            }
        }
    }

    @Override
    public void stop() {
        if (this.active.compareAndSet(true, false)) {
            if (log.isDebugEnabled()) {
                log.debug("Stopping " + this);
            }

            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public Task schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor executor = this.executor;
        if (executor == null) {
            return () -> false;
        } else {
            ScheduledFuture<?> result = executor.schedule(task, delay, unit);
            return new ScheduledFutureTask(result);
        }
    }

    private static class ScheduledFutureTask implements Task {

        private final ScheduledFuture<?> scheduledFuture;

        ScheduledFutureTask(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public boolean cancel() {
            return scheduledFuture.cancel(false);
        }

    }

}
