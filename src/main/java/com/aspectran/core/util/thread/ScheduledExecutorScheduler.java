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
package com.aspectran.core.util.thread;

import com.aspectran.core.component.session.BasicSession;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Created: 2017. 6. 25.</p>
 */
public class ScheduledExecutorScheduler implements Scheduler {

    private final Object lock = new Object();

    private final AtomicBoolean active = new AtomicBoolean();

    private volatile ScheduledThreadPoolExecutor executor;

    public ScheduledExecutorScheduler() {
    }

    public void start() {
        synchronized (lock) {
            if (this.active.get()) {
                throw new IllegalStateException("Failed to start " + this + " because it has already been started");
            }

            executor = new ScheduledThreadPoolExecutor(1);
            executor.setRemoveOnCancelPolicy(true);

            this.active.set(true);
        }
    }

    public void stop() {
        synchronized (lock) {
            if (this.active.compareAndSet(true, false)) {
                executor.shutdownNow();
                executor = null;
            }
        }
    }

    public Task schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor executor = this.executor;
        if (executor == null) {
            return () -> false;
        } else {
            ScheduledFuture<?> result = executor.schedule(task, delay, unit);
            return new ScheduledExecutorScheduler.ScheduledFutureTask(result);
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
