/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.context.rule.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2023/06/18</p>
 */
class NamespaceTest {

    @Test
    void splitNamespace() {
        String namespace = ".abc.def.ghi.";
        String[] nameArray = Namespace.splitNamespace(namespace);
        assertArrayEquals(new String[] {"abc", "def", "ghi"}, nameArray);
    }

    @Test
    void applyNamespace() {
        String namespace = "abc.def.ghi";
        String[] nameArray = Namespace.splitNamespace(namespace);
        String result = Namespace.applyNamespace(nameArray, "jkl.mno");
        assertEquals("abc.def.ghi.jkl.mno", result);
    }

    @Test
    void applyNamespaceForTranslet() {
        String namespace = "abc.def.ghi";
        String[] nameArray = Namespace.splitNamespace(namespace);
        String result = Namespace.applyNamespaceForTranslet(nameArray, "/jkl/mno");
        assertEquals("abc/def/ghi/jkl/mno", result);
    }

    @Test
    void applyNamespaceForTranslet2() {
        String namespace = "/abc/def.ghi";
        String[] nameArray = Namespace.splitNamespace(namespace);
        String result = Namespace.applyNamespaceForTranslet(nameArray, "/jkl/mno/");
        assertEquals("/abc/def/ghi/jkl/mno/", result);
    }

    @Test
    void applyNamespaceForTranslet3() {
        String namespace = "/abc/def.ghi/";
        String[] nameArray = Namespace.splitNamespace(namespace);
        String result = Namespace.applyNamespaceForTranslet(nameArray, "");
        assertEquals("/abc/def/ghi/", result);
    }

    @Test
    void applyTransletNamePattern() {
        String prefix = "/abc/def";
        String name = "/ghi";
        String suffix = ".apon";
        String result = Namespace.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("/abc/def/ghi.apon", result);
    }

    @Test
    void applyTransletNamePattern2() {
        String prefix = "abc/def_";
        String name = "ghi/jkl";
        String suffix = ".apon";
        String result = Namespace.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("abc/def_ghi/jkl.apon", result);
    }

    @Test
    void applyTransletNamePattern3() {
        String prefix = "/abc/def";
        String name = "/ghi";
        String suffix = "/jkl";
        String result = Namespace.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("/abc/def/ghi/jkl", result);
    }

    @Test
    void applyTransletNamePattern4() {
        String prefix = "//abc//def";
        String name = "//ghi";
        String suffix = "//jkl//";
        String result = Namespace.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("//abc//def//ghi//jkl//", result);
    }

}
