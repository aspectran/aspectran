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
package com.aspectran.netty.server;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.ResourceUtils;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.pkitesting.CertificateBuilder;
import io.netty.pkitesting.X509Bundle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for HTTP/2 support in {@link DefaultNettyServer} (both cleartext h2c and TLS ALPN h2).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NettyHttp2ServerTest {

    private EmbeddedAspectran aspectran;

    private DefaultNettyServer nettyServer;

    private int h2cPort;

    private int h2TlsPort;

    private X509Bundle x509Bundle;

    private File tempDir;

    private File staticFile;

    @BeforeAll
    void ready() throws Exception {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "netty-http2-test-" + System.currentTimeMillis());
        tempDir.mkdirs();
        staticFile = new File(tempDir, "static-sample.txt");
        java.nio.file.Files.writeString(staticFile.toPath(), "Static file content for HTTP/2 streaming test: " + "A".repeat(32768));

        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);
        aspectran = EmbeddedAspectran.run(aspectranConfig);

        nettyServer = aspectran.getBean("netty.server");
        nettyServer.stop();

        com.aspectran.netty.server.handler.resource.NettyResourceHandler resourceHandler =
                new com.aspectran.netty.server.handler.resource.NettyResourceHandler(tempDir);
        resourceHandler.setChunkSize(16384);
        nettyServer.setResourceHandler(resourceHandler);

        // Configure Listener 1: HTTP/2 Cleartext (h2c) on dynamic port
        NettyListenerConfig h2cListener = new NettyListenerConfig("127.0.0.1", 0);
        h2cListener.setHttp2(true);
        h2cListener.setHttp2MaxConcurrentStreams(200);
        h2cListener.setHttp2InitialWindowSize(65535);
        h2cListener.setHttp2MaxFrameSize(16384);
        h2cListener.setReceiveBufferSize(65536);
        h2cListener.setSendBufferSize(65536);

        // Configure Listener 2: HTTP/2 TLS (h2 ALPN) on dynamic port
        x509Bundle = new CertificateBuilder()
                .subject("cn=localhost")
                .setIsCertificateAuthority(true)
                .addSanDnsName("localhost")
                .addSanIpAddress("127.0.0.1")
                .buildSelfSigned();

        ApplicationProtocolConfig apc = new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2,
                ApplicationProtocolNames.HTTP_1_1);
        SslContext sslContext = SslContextBuilder.forServer(x509Bundle.getKeyPair().getPrivate(), x509Bundle.getCertificate())
                .applicationProtocolConfig(apc)
                .build();

        NettyListenerConfig h2TlsListener = new NettyListenerConfig("127.0.0.1", 0);
        h2TlsListener.setSsl(true);
        h2TlsListener.setHttp2(true);
        h2TlsListener.setHttp2MaxConcurrentStreams(200);
        h2TlsListener.setHttp2InitialWindowSize(65535);
        h2TlsListener.setHttp2MaxFrameSize(16384);
        h2TlsListener.setReceiveBufferSize(65536);
        h2TlsListener.setSendBufferSize(65536);
        h2TlsListener.setSslContext(sslContext);

        nettyServer.setListeners(h2cListener, h2TlsListener);
        nettyServer.start();

        h2cPort = h2cListener.getActualPort();
        h2TlsPort = h2TlsListener.getActualPort();

        assertTrue(h2cPort > 0, "h2c port must be positive");
        assertTrue(h2TlsPort > 0, "h2 TLS port must be positive");
    }

    @AfterAll
    void finish() throws Exception {
        if (nettyServer != null && nettyServer.isRunning()) {
            nettyServer.stop();
        }
        if (aspectran != null) {
            aspectran.destroy();
        }
        if (staticFile != null && staticFile.exists()) {
            staticFile.delete();
        }
        if (tempDir != null && tempDir.exists()) {
            tempDir.delete();
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
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/hello"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, getResponse.version(), "Must use HTTP/2 protocol");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Hello Netty World!"), "Response body must contain expected greeting");

        // 2. Subsequent POST request on the multiplexed HTTP/2 connection
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/echo"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("msg=Http2Netty"))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, postResponse.version(), "Must use HTTP/2 protocol");
        assertEquals(200, postResponse.statusCode());
        assertEquals("Echo: Http2Netty", postResponse.body());
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
                .uri(URI.create("https://localhost:" + h2TlsPort + "/hello"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, getResponse.version(), "Must use HTTP/2 protocol via TLS ALPN");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Hello Netty World!"), "Response body must contain expected greeting");

        // 2. POST request over TLS ALPN
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:" + h2TlsPort + "/echo"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("msg=Http2TlsNetty"))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, postResponse.version(), "Must use HTTP/2 protocol via TLS ALPN");
        assertEquals(200, postResponse.statusCode());
        assertEquals("Echo: Http2TlsNetty", postResponse.body());
    }

    @Test
    void testHttp11FallbackOnHttp2CleartextServer() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/hello"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_1_1, response.version(), "Must fallback to HTTP/1.1");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Hello Netty World!"));
    }

    @Test
    void testHttp11FallbackOnHttp2TlsServer() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .sslContext(createInsecureSslContext())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:" + h2TlsPort + "/hello"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_1_1, response.version(), "Must fallback to HTTP/1.1 over TLS");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Hello Netty World!"));
    }

    @Test
    void testHttp2CleartextStaticResourceStreaming() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + h2cPort + "/static-sample.txt"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, response.version(), "Must stream static file over HTTP/2");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("Static file content for HTTP/2 streaming test: "));
        assertEquals(staticFile.length(), response.body().length());
    }

    @Test
    void testHttp2TlsStaticResourceStreaming() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(createInsecureSslContext())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:" + h2TlsPort + "/static-sample.txt"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpClient.Version.HTTP_2, response.version(), "Must stream static file over HTTP/2 TLS");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("Static file content for HTTP/2 streaming test: "));
        assertEquals(staticFile.length(), response.body().length());
    }

}
