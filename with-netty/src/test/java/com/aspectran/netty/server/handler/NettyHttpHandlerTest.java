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
package com.aspectran.netty.server.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link NettyHttpHandler}.
 *
 * <p>Created: 2026-10-10</p>
 */
class NettyHttpHandlerTest {

    @Test
    void testIsClientAbortException() {
        assertFalse(NettyHttpHandler.isClientAbortException(null));
        assertFalse(NettyHttpHandler.isClientAbortException(new NullPointerException("NPE")));
        assertFalse(NettyHttpHandler.isClientAbortException(new IOException("Disk read failed")));

        assertTrue(NettyHttpHandler.isClientAbortException(new SocketException("Connection reset")));
        assertTrue(NettyHttpHandler.isClientAbortException(new SocketException("Broken pipe")));
        assertTrue(NettyHttpHandler.isClientAbortException(new SocketException("Connection reset by peer")));
        assertTrue(NettyHttpHandler.isClientAbortException(new ClosedChannelException()));
        assertTrue(NettyHttpHandler.isClientAbortException(new IOException("readAddress(..) failed: Connection reset by peer")));
        assertTrue(NettyHttpHandler.isClientAbortException(new IOException("syscall:write(..) : Broken pipe")));
        assertTrue(NettyHttpHandler.isClientAbortException(new IOException("An existing connection was forcibly closed by the remote host")));
        assertTrue(NettyHttpHandler.isClientAbortException(new IOException("Software caused connection abort: socket write error")));
        assertTrue(NettyHttpHandler.isClientAbortException(new SSLException("SSLEngine closed already")));
        assertTrue(NettyHttpHandler.isClientAbortException(new RuntimeException(new SocketException("Connection reset"))));
    }

    @Test
    void testExceptionCaughtClosesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(new NettyHttpHandler(null, null, null, false));
        assertTrue(channel.isOpen());

        channel.pipeline().fireExceptionCaught(new SocketException("Connection reset"));
        assertFalse(channel.isOpen());
    }

}
