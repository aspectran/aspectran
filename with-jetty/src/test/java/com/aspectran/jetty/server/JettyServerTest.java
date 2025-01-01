/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.jetty.server;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.FileCopyUtils;
import com.aspectran.utils.ResourceUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JettyServerTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() throws Exception {
        File root = new File("target/app");
        String basePath = root.getCanonicalPath();
        System.setProperty(BASE_PATH_PROPERTY_NAME, basePath); // for logback
        FileCopyUtils.copyDirectory(ResourceUtils.getResourceAsFile("webroot"), new File(root, "webroot"));

        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);
        aspectranConfig.touchContextConfig()
                .setBasePath(basePath);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        aspectran.translate("jetty start");
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.translate("jetty stop");
            aspectran.release();
        }
    }

    @Test
    void testHello() throws IOException {
        Translet translet = aspectran.translate("hello");
        String result1 = translet.getWrittenResponse();
        String result2;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:8099/hello_jsp");
            result2 = httpClient.execute(request, response -> EntityUtils.toString(response.getEntity()).trim());
        }

        assertEquals(result1, result2);
    }

}
