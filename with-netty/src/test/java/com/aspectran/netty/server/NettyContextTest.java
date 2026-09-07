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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NettyContextTest {

    @Test
    void testIsRootContext() {
        NettyContext context1 = new NettyContext();
        context1.setContextPath("");
        assertTrue(context1.isRootContext());

        NettyContext context2 = new NettyContext();
        context2.setContextPath("/");
        assertTrue(context2.isRootContext());

        NettyContext context3 = new NettyContext();
        context3.setContextPath("/demo");
        assertFalse(context3.isRootContext());
    }

    @Test
    void testOrder() {
        NettyContext context = new NettyContext();
        assertEquals(0, context.getOrder());

        context.setOrder(100);
        assertEquals(100, context.getOrder());
    }

    @Test
    void testOrderingWithRootFirst() {
        NettyContext root = new NettyContext("/");
        root.setOrder(0);

        NettyContext demo = new NettyContext("/demo");
        demo.setOrder(0);

        NettyContext petclinic = new NettyContext("/petclinic");
        petclinic.setOrder(-10);

        NettyContext console = new NettyContext("/console");
        console.setOrder(Integer.MAX_VALUE);

        List<NettyContext> contexts = new ArrayList<>(Arrays.asList(console, demo, petclinic, root));

        Comparator<NettyContext> startupComparator = (c1, c2) -> {
            boolean root1 = c1.isRootContext();
            boolean root2 = c2.isRootContext();
            if (root1 != root2) {
                return root1 ? -1 : 1;
            }
            return Integer.compare(c1.getOrder(), c2.getOrder());
        };

        contexts.sort(startupComparator);

        assertEquals("", contexts.get(0).getContextPath()); // normalized root is ""
        assertEquals("/petclinic", contexts.get(1).getContextPath());
        assertEquals("/demo", contexts.get(2).getContextPath());
        assertEquals("/console", contexts.get(3).getContextPath());

        // Reverse for shutdown (LIFO)
        contexts.sort(startupComparator.reversed());

        assertEquals("/console", contexts.get(0).getContextPath());
        assertEquals("/demo", contexts.get(1).getContextPath());
        assertEquals("/petclinic", contexts.get(2).getContextPath());
        assertEquals("", contexts.get(3).getContextPath());
    }

}
