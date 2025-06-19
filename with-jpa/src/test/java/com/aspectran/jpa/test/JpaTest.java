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
package com.aspectran.jpa.test;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() throws Exception {
        File root = new File("target/app");
        String basePath = root.getCanonicalPath();
        System.setProperty(BASE_PATH_PROPERTY_NAME, basePath); // for logback

        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);
        aspectranConfig.touchContextConfig().setBasePath(basePath);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    void vetList() throws IOException {
        Translet translet = aspectran.translate("/vetList");
        String result = translet.getWrittenResponse();
        System.out.println(result);
    }

}
