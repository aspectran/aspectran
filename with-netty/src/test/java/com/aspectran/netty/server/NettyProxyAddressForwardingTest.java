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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.netty.adapter.NettyRequestAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;

import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_FOR;
import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_HOST;
import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_PORT;
import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_PROTO;
import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_SSL;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for proxyAddressForwarding in {@link NettyRequestAdapter}.
 */
class NettyProxyAddressForwardingTest {

    @Test
    void testForwardedHeadersWhenEnabled() {
        EmbeddedChannel channel = new EmbeddedChannel();
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test");
        req.headers().set(X_FORWARDED_FOR, "203.0.113.195, 70.41.3.18");
        req.headers().set(X_FORWARDED_PROTO, "https");
        req.headers().set(X_FORWARDED_PORT, "8443");
        req.headers().set(X_FORWARDED_HOST, "api.example.com:8443");

        NettyRequestAdapter adapter = new NettyRequestAdapter(
                MethodType.GET, req, channel.pipeline().firstContext(), "/", true);

        assertEquals("203.0.113.195", adapter.getRemoteAddr());
        assertEquals("https", adapter.getScheme());
        assertEquals(8443, adapter.getServerPort());
        assertEquals("api.example.com", adapter.getServerName());
    }

    @Test
    void testForwardedHeadersWhenDisabled() {
        EmbeddedChannel channel = new EmbeddedChannel();
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test");
        req.headers().set(X_FORWARDED_FOR, "203.0.113.195, 70.41.3.18");
        req.headers().set(X_FORWARDED_PROTO, "https");
        req.headers().set(X_FORWARDED_PORT, "8443");
        req.headers().set(X_FORWARDED_HOST, "api.example.com:8443");

        NettyRequestAdapter adapter = new NettyRequestAdapter(
                MethodType.GET, req, channel.pipeline().firstContext(), "/", false);

        // When disabled, X-Forwarded-* headers should NOT override scheme or remote address
        assertEquals("http", adapter.getScheme());
        assertEquals(80, adapter.getServerPort());
    }

    @Test
    void testForwardedSslHeader() {
        EmbeddedChannel channel = new EmbeddedChannel();
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test");
        req.headers().set(X_FORWARDED_SSL, "on");

        NettyRequestAdapter adapter = new NettyRequestAdapter(
                MethodType.GET, req, channel.pipeline().firstContext(), "/", true);

        assertEquals("https", adapter.getScheme());
    }

}
