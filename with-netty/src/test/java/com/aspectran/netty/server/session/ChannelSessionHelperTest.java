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
package com.aspectran.netty.server.session;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test case for {@link ChannelSessionHelper}.
 */
class ChannelSessionHelperTest {

    @Test
    void testSessionIdLifecycle() {
        EmbeddedChannel channel = new EmbeddedChannel();

        assertNull(ChannelSessionHelper.getSessionId(channel));

        ChannelSessionHelper.setSessionId(channel, "sess-12345");
        assertEquals("sess-12345", ChannelSessionHelper.getSessionId(channel));

        String removed = ChannelSessionHelper.getAndRemoveSessionId(channel);
        assertEquals("sess-12345", removed);
        assertNull(ChannelSessionHelper.getSessionId(channel));

        ChannelSessionHelper.setSessionId(channel, "sess-67890");
        ChannelSessionHelper.clearSessionId(channel);
        assertNull(ChannelSessionHelper.getSessionId(channel));
    }

}
