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
package com.aspectran.undertow.server;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.FileCopyUtils;
import com.aspectran.utils.ResourceUtils;
import io.netty.pkitesting.CertificateBuilder;
import io.netty.pkitesting.X509Bundle;
import io.undertow.Undertow;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for HTTP/2 support in {@link DefaultTowServer} (both cleartext h2c and TLS ALPN h2).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TowHttp2ServerTest {

    private EmbeddedAspectran aspectran;

    private DefaultTowServer towServer;

    private int h2cPort;

    private int h2TlsPort;

    private X509Bundle x509Bundle;

    @BeforeAll
    void ready() throws Exception {
        File root = new File("target/app");
        String basePath = root.getCanonicalPath();
        System.setProperty(BASE_PATH_PROPERTY, basePath);
        FileCopyUtils.copyDirectory(ResourceUtils.getResourceAsFile("webroot"), new File(root, "webroot"));

        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);
        aspectranConfig.touchContextConfig().setBasePath(basePath);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        towServer = aspectran.getBean("tow.server");

        // Enable HTTP/2 on Undertow server options
        TowOptions serverOptions = new TowOptions();
        serverOptions.setEnableHttp2(true);
        towServer.setServerOptions(serverOptions);

        // Configure Listener 1: HTTP/2 Cleartext (h2c) on dynamic port
        HttpListenerConfig httpListener = new HttpListenerConfig();
        httpListener.setHost("127.0.0.1");
        httpListener.setPort(0);

        // Configure Listener 2: HTTP/2 TLS (h2 ALPN) on dynamic port
        x509Bundle = new CertificateBuilder()
                .subject("cn=localhost")
                .setIsCertificateAuthority(true)
                .addSanDnsName("localhost")
                .addSanIpAddress("127.0.0.1")
                .buildSelfSigned();

        HttpsListenerConfig httpsListener = new HttpsListenerConfig();
        httpsListener.setHost("127.0.0.1");
        httpsListener.setPort(0);
        httpsListener.setKeyManagers(x509Bundle.toKeyManagerFactory().getKeyManagers());

        towServer.setHttpListeners(httpListener);
        towServer.setHttpsListeners(httpsListener);
        towServer.start();

        // Retrieve active listener ports
        Undertow undertow = towServer.getUndertow();
        for (Undertow.ListenerInfo listenerInfo : undertow.getListenerInfo()) {
            if (listenerInfo.getAddress() instanceof InetSocketAddress inetSocketAddress) {
                if ("http".equals(listenerInfo.getProtcol())) {
                    h2cPort = inetSocketAddress.getPort();
                } else if ("https".equals(listenerInfo.getProtcol())) {
                    h2TlsPort = inetSocketAddress.getPort();
                }
            }
        }

        assertTrue(h2cPort > 0, "h2c port must be positive");
        assertTrue(h2TlsPort > 0, "h2 TLS port must be positive");
    }

    @AfterAll
    void finish() throws Exception {
        if (towServer != null && towServer.isRunning()) {
            towServer.stop();
        }
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    private SSLContext createInsecureSslContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { x509Bundle.toTrustManager() }, new SecureRandom());
        return sslContext;
    }

    @Test
    void testHttp2CleartextGetAndPost() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // 1. Initial GET request upgrades cleartext connection to HTTP/2
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/hello_jsp"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, getResponse.version(), "Must use HTTP/2 protocol");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("world"), "Response body must contain expected greeting");

        // 2. Subsequent POST request on the multiplexed HTTP/2 connection
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/hello_jsp"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("name=Aspectran"))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, postResponse.version(), "Must use HTTP/2 protocol");
        assertEquals(200, postResponse.statusCode());
        assertTrue(postResponse.body().contains("world"), "Response body must contain expected greeting");
    }

    @Test
    void testHttp2TlsGetAndPost() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(createInsecureSslContext())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // 1. GET request over TLS ALPN
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:" + h2TlsPort + "/hello_jsp"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, getResponse.version(), "Must use HTTP/2 protocol via TLS ALPN");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("world"), "Response body must contain expected greeting");

        // 2. POST request over TLS ALPN
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:" + h2TlsPort + "/hello_jsp"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("name=Aspectran"))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, postResponse.version(), "Must use HTTP/2 protocol via TLS ALPN");
        assertEquals(200, postResponse.statusCode());
        assertTrue(postResponse.body().contains("world"), "Response body must contain expected greeting");
    }

    @Test
    void testHttp11FallbackOnHttp2CleartextServer() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/hello_jsp"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_1_1, response.version(), "Must fallback to HTTP/1.1");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("world"));
    }

    @Test
    void testHttp11FallbackOnHttp2TlsServer() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .sslContext(createInsecureSslContext())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:" + h2TlsPort + "/hello_jsp"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_1_1, response.version(), "Must fallback to HTTP/1.1 over TLS");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("world"));
    }

}
