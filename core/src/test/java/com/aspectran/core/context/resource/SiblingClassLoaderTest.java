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
package com.aspectran.core.context.resource;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiblingClassLoaderTest {

    @Test
    void testRootClassLoader() throws InvalidResourceException {
        SiblingClassLoader root = new SiblingClassLoader("test-root");
        assertTrue(root.isRoot());
        assertTrue(root.isFirstborn());
        assertEquals("test-root", root.getName());
        assertEquals(1000, root.getId());
        assertNotNull(root.getResourceManager());
        assertFalse(root.hasSiblings());

        List<SiblingClassLoader> list = new ArrayList<>();
        root.getAllSiblings().forEachRemaining(list::add);
        assertEquals(1, list.size());
        assertEquals(root, list.getFirst());
    }

    @Test
    void testSiblingHierarchy() throws InvalidResourceException {
        String[] locations = new String[] {"/dummy/path1", "/dummy/path2"};
        SiblingClassLoader root = new SiblingClassLoader("hierarchy-root", null, locations);

        assertTrue(root.isRoot());
        List<SiblingClassLoader> allSiblings = new ArrayList<>();
        Iterator<SiblingClassLoader> iter = root.getAllSiblings();
        while (iter.hasNext()) {
            allSiblings.add(iter.next());
        }

        // root + 2 children
        assertEquals(3, allSiblings.size());
        assertEquals(root, allSiblings.getFirst());

        // Test reload
        root.reload();
        assertNotNull(root.getResourceManager());
    }

    @Test
    void testExclusions() throws InvalidResourceException {
        SiblingClassLoader root = new SiblingClassLoader("test-exclusions");
        root.excludePackage("java.lang", "com.example");
        root.excludeClass("com.test.SpecificClass");

        assertNotNull(root.toString());
    }

}
