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

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceEntriesTest {

    @Test
    void testPutAndGetNormalization() throws Exception {
        ResourceEntries entries = new ResourceEntries();
        URL testUrl = new URI("file:///test/path").toURL();

        entries.put("com/example/MyClass.class", testUrl);

        // Normalized access with forward slashes, backslashes, leading slash, trailing slash
        assertNotNull(entries.get("com/example/MyClass.class"));
        assertNotNull(entries.get("com\\example\\MyClass.class"));
        assertNotNull(entries.get("/com/example/MyClass.class"));
        assertNotNull(entries.get("/com/example/MyClass.class/"));

        assertTrue(entries.containsKey("com\\example\\MyClass.class"));
        assertTrue(entries.containsKey("/com/example/MyClass.class"));

        assertEquals(testUrl, entries.get("com/example/MyClass.class"));

        assertNull(entries.get(null));
        assertFalse(entries.containsKey(null));
        assertNull(entries.remove(null));

        URL removed = entries.remove("/com\\example/MyClass.class");
        assertEquals(testUrl, removed);
        assertFalse(entries.containsKey("com/example/MyClass.class"));
    }

}
