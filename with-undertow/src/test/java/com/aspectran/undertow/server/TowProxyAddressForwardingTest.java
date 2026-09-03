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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.undertow.adapter.TowRequestAdapter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ProxyPeerAddressHandler;
import io.undertow.util.Headers;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for proxyAddressForwarding and {@link TowRequestAdapter#getRemoteAddr()}.
 */
class TowProxyAddressForwardingTest {

    @Test
    void testProxyAddressForwarding() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestScheme("http");
        exchange.setSourceAddress(new InetSocketAddress("127.0.0.1", 12345));
        exchange.getRequestHeaders().put(Headers.X_FORWARDED_FOR, "203.0.113.195, 70.41.3.18");
        exchange.getRequestHeaders().put(Headers.X_FORWARDED_PROTO, "https");
        exchange.getRequestHeaders().put(Headers.X_FORWARDED_HOST, "api.example.com");
        exchange.getRequestHeaders().put(Headers.X_FORWARDED_PORT, "8443");

        AtomicReference<TowRequestAdapter> adapterRef = new AtomicReference<>();
        HttpHandler nextHandler = ex -> {
            TowRequestAdapter adapter = new TowRequestAdapter(MethodType.GET, ex);
            adapterRef.set(adapter);
        };

        ProxyPeerAddressHandler proxyHandler = new ProxyPeerAddressHandler(nextHandler);
        proxyHandler.handleRequest(exchange);

        TowRequestAdapter adapter = adapterRef.get();
        assertEquals("203.0.113.195", adapter.getRemoteAddr());
        assertEquals("https", adapter.getScheme());
        assertEquals("api.example.com", adapter.getServerName());
        assertEquals(8443, adapter.getServerPort());
    }

    @Test
    void testWithoutProxyAddressForwarding() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestScheme("http");
        exchange.setSourceAddress(new InetSocketAddress("127.0.0.1", 12345));
        exchange.getRequestHeaders().put(Headers.X_FORWARDED_FOR, "203.0.113.195, 70.41.3.18");
        exchange.getRequestHeaders().put(Headers.X_FORWARDED_PROTO, "https");

        TowRequestAdapter adapter = new TowRequestAdapter(MethodType.GET, exchange);
        assertEquals("127.0.0.1", adapter.getRemoteAddr());
        assertEquals("http", adapter.getScheme());
    }

}
