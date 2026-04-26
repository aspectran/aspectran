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
package com.aspectran.test;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.DefaultCoreService;
import org.jspecify.annotations.NonNull;

/**
 * A CoreService implementation designed for integration testing.
 * <p>It extends {@link DefaultCoreService} to provide full service capabilities
 * including automatic scheduler management, similar to how embedded Aspectran works.</p>
 *
 * <p>Created: 2026. 4. 26.</p>
 */
public class TestCoreService extends DefaultCoreService {

    private TestCoreService() {
        super();
    }

    /**
     * Creates and starts a new TestCoreService based on the provided configuration.
     * @param aspectranConfig the Aspectran configuration
     * @return the started TestCoreService
     * @throws Exception if an error occurs during building or starting
     */
    @NonNull
    public static TestCoreService build(AspectranConfig aspectranConfig) throws Exception {
        TestCoreService testCoreService = new TestCoreService();
        testCoreService.configure(aspectranConfig);
        testCoreService.start();
        return testCoreService;
    }

}
