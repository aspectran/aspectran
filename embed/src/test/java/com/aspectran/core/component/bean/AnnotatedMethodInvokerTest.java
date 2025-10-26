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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnnotatedMethodInvokerTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.newContextConfig()
                .addBasePackage("com.aspectran.core.component.bean.sample");

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void testParameterBindingFailure() {
        // This test triggers the improved debug log for a parameter binding failure.
        // The action method expects an int but receives a non-numeric string.
        // Since the parameter is not marked @Required, no exception should be thrown.
        assertDoesNotThrow(() -> aspectran.translate("/parameter-binding-failure"));
    }

    @Test
    void testModelBindingFailure() {
        // This test triggers the improved debug log for a model property binding failure.
        // The 'number' property of TestModel expects an int, but we pass a non-numeric string.
        ParameterMap params = new ParameterMap();
        params.setParameter("number", "invalid-number");
        assertDoesNotThrow(() -> aspectran.translate("/model-binding-failure", params));
    }

    @Test
    void testRequiredSetterIsIgnoredOnModelBinding() {
        // This test verifies that @Required on a model's setter is correctly ignored
        // during request-to-model binding. Before the fix, this would throw an
        // IllegalArgumentException because the 'name' parameter was missing.
        // Now, it should complete without error.
        assertDoesNotThrow(() -> aspectran.translate("/required-setter-missing"));
    }

    @Test
    void testBindModelFeatures() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("memberAge", "30"); // For @Qualifier("memberAge")
        params.setParameter("joinDate", "2025-10-26"); // For @Format("yyyy-MM-dd")
        params.setParameterValues("hobbies", new String[]{"coding", "reading"});

        assertDoesNotThrow(() -> aspectran.translate("/bind-model-features", params));
    }

}
