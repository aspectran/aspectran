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
package com.aspectran.daemon.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link DefaultDaemonService}.
 */
class DefaultDaemonServiceTest {

    @Test
    void testDaemonServiceBuild() throws Exception {
        // Given: A base service is already running
        AspectranConfig aspectranConfig = new AspectranConfig();
        EmbeddedAspectran aspectran = EmbeddedAspectran.run(aspectranConfig);

        try {
            // When: No active DaemonService found, starting a new one based on root service
            DefaultDaemonService daemonService = DefaultDaemonServiceBuilder.build((CoreService)aspectran);

            // Then: The daemonService is added to baseService's sub-services during construction.
            // If the baseService is already active, the new daemonService is considered an
            // orphan and must be started manually.
            if (daemonService.getServiceLifeCycle().isOrphan()) {
                daemonService.start();
            }

            assertNotNull(daemonService);
            assertTrue(daemonService.isActive());
            assertTrue(CoreServiceHolder.getAllServices().contains(daemonService));

            daemonService.stop();
        } finally {
            aspectran.destroy();
        }
    }

}
