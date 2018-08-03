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
package com.aspectran.with.jetty;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.embed.service.EmbeddedService;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import static junit.framework.TestCase.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JettyServerTest {

    private EmbeddedService service;

    @Before
    public void ready() throws Exception {
        String basePath = new File("target").getCanonicalPath();
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");

        new File(basePath, "logs").mkdirs();
        FileUtils.copyDirectory(ResourceUtils.getResourceAsFile("webapps"), new File(basePath, "webapps"));

        AspectranConfig aspectranConfig = new AspectranConfig(configFile);
        aspectranConfig.updateBasePath(basePath);

        service = EmbeddedService.create(aspectranConfig);
        service.start();
        service.translet("jetty start");
    }

    @After
    public void finish() {
        if (service != null) {
            service.translet("jetty stop");
            service.stop();
        }
    }

    @Test
    public void testHello() throws IOException {
        Translet translet = service.translet("/hello");
        String result1 = translet.getResponseAdapter().getWriter().toString();
        String result2;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("http://127.0.0.1:8099/hello_jsp");
            HttpResponse response = client.execute(request);
            result2 = new BasicResponseHandler().handleResponse(response).trim();
        } catch (SocketException e) {
            // Network is unreachable
            return;
        }

        assertEquals(result1, result2);
    }

}
