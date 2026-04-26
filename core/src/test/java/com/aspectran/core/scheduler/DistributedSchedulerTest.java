/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.core.scheduler;

import com.aspectran.core.component.schedule.ScheduledJobLockProvider;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.test.AspectranConfigProvider;
import com.aspectran.test.AspectranTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test for Scheduler and distributed job locking.
 */
@AspectranTest(configProvider = DistributedSchedulerTest.class)
public class DistributedSchedulerTest implements AspectranConfigProvider {

    private static final TestLockProvider lockProvider = new TestLockProvider();

    @Override
    public AspectranConfig getConfig() {
        AspectranConfig config = new AspectranConfig();
        config.touchContextConfig().setContextRules(new String[] {
            "classpath:com/aspectran/core/scheduler/scheduler-test-context.xml"
        });
        config.touchSchedulerConfig()
                .setEnabled(true)
                .setStartDelaySeconds(1)
                .setWaitOnShutdown(true);
        return config;
    }

    @BeforeEach
    void setup() {
        lockProvider.reset();
        CoreServiceHolder.setJobLockProvider(lockProvider);
    }

    @AfterEach
    void tearDown() {
        CoreServiceHolder.setJobLockProvider(null);
    }

    @Test
    void testDistributedLockSuccess(CoreService coreService) {
        lockProvider.setShouldSucceed(true);

        // Wait for the scheduler to trigger the job at least once
        await().until(() -> lockProvider.getLockCallCount() > 0);

        assertEquals(lockProvider.getLockCallCount(), lockProvider.getUnlockCallCount());
    }

    @Test
    void testDistributedLockFailure(CoreService coreService) {
        lockProvider.setShouldSucceed(false);

        // Wait for the scheduler to attempt to trigger the job
        await().until(() -> lockProvider.getLockCallCount() > 0);

        // Since lock failed, unlock should not be called and job execution should be skipped
        assertEquals(0, lockProvider.getUnlockCallCount());
    }

    public static class TestBean {
        public void doWork() {
            System.out.println("TestBean.doWork()");
        }
    }

    private static class TestLockProvider implements ScheduledJobLockProvider {
        private final AtomicInteger lockCallCount = new AtomicInteger();
        private final AtomicInteger unlockCallCount = new AtomicInteger();
        private final AtomicBoolean shouldSucceed = new AtomicBoolean(true);

        public void setShouldSucceed(boolean succeed) {
            this.shouldSucceed.set(succeed);
        }

        public void reset() {
            lockCallCount.set(0);
            unlockCallCount.set(0);
            shouldSucceed.set(true);
        }

        public int getLockCallCount() {
            return lockCallCount.get();
        }

        public int getUnlockCallCount() {
            return unlockCallCount.get();
        }

        @Override
        public boolean lock(String lockKey) {
            lockCallCount.incrementAndGet();
            return shouldSucceed.get();
        }

        @Override
        public void unlock(String lockKey) {
            unlockCallCount.incrementAndGet();
        }
    }

}
