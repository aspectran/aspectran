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
package com.aspectran.undertow.server.servlet;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TowServletContextTest {

    @Test
    void testIsRootContext() {
        TowServletContext context1 = new TowServletContext();
        context1.setContextPath("");
        assertTrue(context1.isRootContext());

        TowServletContext context2 = new TowServletContext();
        context2.setContextPath("/");
        assertTrue(context2.isRootContext());

        TowServletContext context3 = new TowServletContext();
        context3.setContextPath("/demo");
        assertFalse(context3.isRootContext());
    }

    @Test
    void testOrder() {
        TowServletContext context = new TowServletContext();
        assertEquals(0, context.getOrder());

        context.setOrder(100);
        assertEquals(100, context.getOrder());
    }

    @Test
    void testOrderingWithRootFirst() {
        TowServletContext root = new TowServletContext();
        root.setContextPath("/");
        root.setOrder(0);

        TowServletContext demo = new TowServletContext();
        demo.setContextPath("/demo");
        demo.setOrder(0);

        TowServletContext petclinic = new TowServletContext();
        petclinic.setContextPath("/petclinic");
        petclinic.setOrder(-10);

        TowServletContext console = new TowServletContext();
        console.setContextPath("/console");
        console.setOrder(Integer.MAX_VALUE);

        // Put in mixed order
        TowServletContext[] contexts = new TowServletContext[] { console, demo, petclinic, root };

        Comparator<TowServletContext> startupComparator = (c1, c2) -> {
            boolean root1 = c1.isRootContext();
            boolean root2 = c2.isRootContext();
            if (root1 != root2) {
                return root1 ? -1 : 1;
            }
            return Integer.compare(c1.getOrder(), c2.getOrder());
        };

        Arrays.sort(contexts, startupComparator);

        assertEquals("/", contexts[0].getContextPath());
        assertEquals("/petclinic", contexts[1].getContextPath());
        assertEquals("/demo", contexts[2].getContextPath());
        assertEquals("/console", contexts[3].getContextPath());

        // Reverse for shutdown (LIFO)
        Arrays.sort(contexts, startupComparator.reversed());

        assertEquals("/console", contexts[0].getContextPath());
        assertEquals("/demo", contexts[1].getContextPath());
        assertEquals("/petclinic", contexts[2].getContextPath());
        assertEquals("/", contexts[3].getContextPath());
    }

}
