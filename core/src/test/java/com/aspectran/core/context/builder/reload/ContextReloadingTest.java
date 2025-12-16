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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.FlashMapManager;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.DefaultApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceLifeCycle;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for context reloading feature.
 * <p>Created: 2017. 4. 24.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContextReloadingTest {

    @TempDir
    Path tempDir;

    private HybridActivityContextBuilder builder;

    private ReloadTestLifeCycle lifeCycle;

    private CountDownLatch restartLatch;

    final String beanId = "sampleBean";

    final String propertyName = "message";

    final String initialMessage = "Hello, Aspectran!";

    final String updatedMessage = "Context Reloaded!";

    @BeforeEach
    void setup() {
        restartLatch = new CountDownLatch(1);
        lifeCycle = new ReloadTestLifeCycle(restartLatch);

        MockCoreService mockCoreService = new MockCoreService(lifeCycle, tempDir.toString());
        builder = new HybridActivityContextBuilder(mockCoreService);
        builder.setHardReload(true);
        builder.setAutoReloadEnabled(true);

        lifeCycle.setBuilder(builder);
    }

    @AfterEach
    void cleanup() {
        if (builder != null && builder.isActive()) {
            builder.destroy();
        }
    }

    private void writeMainAppContext(String content) throws IOException {
        File appContextFile = new File(tempDir.toFile(), "app-context.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE aspectran PUBLIC \"-//ASPECTRAN//DTD Aspectran 9.0//EN\"\n" +
                "        \"https://aspectran.com/dtd/aspectran-9.dtd\">\n" +
                "<aspectran>\n" +
                content +
                "</aspectran>";
        Files.writeString(appContextFile.toPath(), xml);
    }

    private void writeSecondAppContext(String content) throws IOException {
        File appContextFile = new File(tempDir.toFile(), "second-context.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE aspectran PUBLIC \"-//ASPECTRAN//DTD Aspectran 9.0//EN\"\n" +
                "        \"https://aspectran.com/dtd/aspectran-9.dtd\">\n" +
                "<aspectran>\n" +
                content +
                "</aspectran>";
        Files.writeString(appContextFile.toPath(), xml);
    }

    @NonNull
    private String createBeanRule(String message) {
        return "  <bean id=\"" + beanId + "\" class=\"com.aspectran.core.context.builder.reload.SampleBean\">\n" +
                "    <properties>\n" +
                "      <item name=\"" + propertyName + "\">" + message + "</item>\n" +
                "    </properties>\n" +
                "  </bean>";
    }

    @Test
    void testContextReloading() throws Exception {
        builder.setScanIntervalSeconds(1);
        builder.setContextRules(new String[] {"/app-context.xml"});

        // 1. Write initial config files
        writeMainAppContext(createBeanRule(initialMessage));

        // 2. Initial build and verify
        ActivityContext context1 = builder.build();
        lifeCycle.setContext(context1); // Set initial context on the lifecycle holder
        assertNotNull(context1);
        SampleBean bean1 = context1.getBeanRegistry().getBean(beanId);
        assertEquals(initialMessage, bean1.getMessage());

        // 3. Modify the included file
        writeMainAppContext(createBeanRule(updatedMessage));

        // 4. Wait for reload (triggered by timer, calls lifeCycle.restart())
        assertTrue(restartLatch.await(5, TimeUnit.SECONDS), "Context did not reload within 5 seconds");

        // 5. Verify reloaded state from the new context held by the lifecycle
        ActivityContext context2 = lifeCycle.getContext();
        assertNotNull(context2);
        SampleBean bean2 = context2.getBeanRegistry().getBean(beanId);
        assertEquals(updatedMessage, bean2.getMessage());
    }

    @Test
    void testContextReloadingOnAppendedFileChange() throws Exception {
        builder.setScanIntervalSeconds(1);
        builder.setContextRules(new String[] {"/app-context.xml"});

        // 1. Write initial config files
        writeMainAppContext("  <append file=\"/second-context.xml\"/>");
        writeSecondAppContext(createBeanRule(initialMessage));

        // 2. Initial build and verify
        ActivityContext context1 = builder.build();
        lifeCycle.setContext(context1);
        assertNotNull(context1);
        SampleBean bean1 = context1.getBeanRegistry().getBean(beanId);
        assertEquals(initialMessage, bean1.getMessage());

        // 3. Modify the appended file
        writeSecondAppContext(createBeanRule(updatedMessage));

        // 4. Wait for reload
        assertTrue(restartLatch.await(5, TimeUnit.SECONDS), "Context did not reload on appended file change");

        // 5. Verify reloaded state
        ActivityContext context2 = lifeCycle.getContext();
        assertNotNull(context2);
        SampleBean bean2 = context2.getBeanRegistry().getBean(beanId);
        assertEquals(updatedMessage, bean2.getMessage());
    }

    @Test
    void testInvalidScanInterval() throws Exception {
        builder.setContextRules(new String[] {"/app-context.xml"});
        writeMainAppContext(createBeanRule(initialMessage));

        // 1. Test with scanInterval = 0
        builder.setScanIntervalSeconds(0);
        ActivityContext context1 = builder.build(); // Should not throw
        assertNotNull(context1);
        assertFalse(builder.isReloadingTimerRunning(), "Timer should not be running for scanInterval <= 0");

        builder.destroy(); // Clean up for the next part of the test

        // 2. Test with scanInterval = -1
        builder.setScanIntervalSeconds(-1);
        ActivityContext context2 = builder.build(); // Should not throw
        assertNotNull(context2);
        assertFalse(builder.isReloadingTimerRunning(), "Timer should not be running for scanInterval <= 0");
    }

    @Test
    void testContextReloadingOnFileDeletion() throws Exception {
        builder.setScanIntervalSeconds(1);
        builder.setContextRules(new String[] {"/app-context.xml"});

        // 1. Write initial config files
        writeMainAppContext(createBeanRule(initialMessage));

        // 2. Initial build and verify
        ActivityContext context1 = builder.build();
        lifeCycle.setContext(context1);
        assertNotNull(context1);

        // 3. Delete the config file
        File appContextFile = new File(tempDir.toFile(), "app-context.xml");
        assertTrue(appContextFile.delete());

        // 4. Wait for reload attempt
        assertTrue(restartLatch.await(5, TimeUnit.SECONDS), "Context reload was not triggered on file deletion");

        // 5. Verify that the restart failed
        assertNotNull(lifeCycle.getRestartException());
        assertEquals(ActivityContextBuilderException.class, lifeCycle.getRestartException().getClass());
    }

    /**
     * Mock implementation of {@link ServiceLifeCycle} for testing context reloading.
     * It uses a {@link CountDownLatch} to signal when a restart has been triggered
     * and captures any exceptions that occur during the restart.
     */
    private static class ReloadTestLifeCycle implements ServiceLifeCycle {

        private final CountDownLatch latch;

        private HybridActivityContextBuilder builder;

        private volatile ActivityContext context;

        private volatile Exception restartException;

        ReloadTestLifeCycle(CountDownLatch latch) {
            this.latch = latch;
        }

        void setBuilder(HybridActivityContextBuilder builder) {
            this.builder = builder;
        }

        void setContext(ActivityContext context) {
            this.context = context;
        }

        ActivityContext getContext() {
            return context;
        }

        Exception getRestartException() {
            return restartException;
        }

        @Override
        public String getServiceName() {
            return ObjectUtils.simpleIdentityToString(this);
        }

        @Override
        public void restart(String message) {
            try {
                builder.destroy();
                this.context = builder.build();
            } catch (Exception e) {
                this.restartException = e;
            } finally {
                latch.countDown();
            }
        }

        // The following methods are not used in this test

        @Override
        public void restart() throws Exception {
            restart(null);
        }

        @Override
        @Nullable
        public CoreService getRootService() {
            return null;
        }

        @Override
        @Nullable
        public CoreService getParentService() {
            return null;
        }

        @Override
        public boolean isRootService() {
            return true;
        }

        @Override
        public boolean isOrphan() {
            return false;
        }

        @Override
        public boolean isDerived() {
            return false;
        }

        @Override
        public void setServiceStateListener(ServiceStateListener serviceStateListener) {
        }

        @Override
        public void addService(ServiceLifeCycle serviceLifeCycle) {
        }

        @Override
        public void removeService(ServiceLifeCycle serviceLifeCycle) {
        }

        @Override
        public void withdraw() {
        }

        @Override
        public void start() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean isBusy() {
            return false;
        }

        @Override
        public void pause() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void pause(long timeout) {
        }

        @Override
        public void resume() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Mock implementation of {@link CoreService} for testing.
     * It provides the minimum necessary components (LifeCycle, ApplicationAdapter)
     * to the {@link HybridActivityContextBuilder}.
     */
    private static class MockCoreService implements CoreService {

        private final ServiceLifeCycle lifeCycle;

        private final ApplicationAdapter applicationAdapter;

        MockCoreService(ServiceLifeCycle lifeCycle, String basePath) {
            this.lifeCycle = lifeCycle;
            this.applicationAdapter = new DefaultApplicationAdapter(basePath);
        }

        @Override
        public ServiceLifeCycle getServiceLifeCycle() {
            return lifeCycle;
        }

        @Override
        public String getBasePath() {
            return applicationAdapter.getBasePathString();
        }

        @Override
        public ApplicationAdapter getApplicationAdapter() {
            return applicationAdapter;
        }

        @Override
        public ActivityContext getActivityContext() {
            return ((ReloadTestLifeCycle)lifeCycle).getContext();
        }

        // The following methods are not used in this test

        @Override
        public String getContextName() {
            return "reload-test-context";
        }

        @Override
        public boolean hasServiceClassLoader() {
            return false;
        }

        @Override
        public ClassLoader getServiceClassLoader() {
            return null;
        }

        @Override
        public ClassLoader getAltClassLoader() {
            return null;
        }

        @Override
        public Activity getDefaultActivity() {
            return null;
        }

        @Override
        public SchedulerService getSchedulerService() {
            return null;
        }

        @Override
        public boolean isRequestAcceptable(String requestName) {
            return false;
        }

        @Override
        public FlashMapManager getFlashMapManager() {
            return null;
        }

        @Override
        public LocaleResolver getLocaleResolver() {
            return null;
        }

        @Override
        public AspectranConfig getAspectranConfig() {
            return null;
        }

        @Override
        public CoreService getRootService() {
            return this;
        }

        @Override
        public CoreService getParentService() {
            return null;
        }

        @Override
        public boolean isRootService() {
            return true;
        }

        @Override
        public boolean isOrphan() {
            return false;
        }

        @Override
        public boolean isDerived() {
            return false;
        }

    }

}
