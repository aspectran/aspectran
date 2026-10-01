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
package com.aspectran.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathUtilsTest {

    @Test
    void applyRelativePath() {
        assertEquals("mypath/mypath/myfile", PathUtils.applyRelativePath("mypath/myfile", "mypath/myfile"));
        assertEquals("mypath/mypath/mypath/myfile", PathUtils.applyRelativePath("mypath/mypath/", "mypath/myfile"));
        assertEquals("mypath/mypath/", PathUtils.applyRelativePath("mypath/mypath/myfile", ""));
        assertEquals("mypath/mypath/myfile", PathUtils.applyRelativePath("mypath\\myfile", "mypath\\myfile"));
        assertEquals("mypath/sub/otherfile", PathUtils.applyRelativePath("mypath\\sub\\myfile", "otherfile"));
    }

    @Test
    void cleanPath() {
        assertNull(PathUtils.cleanPath(null));
        assertEquals("mypath/myfile", PathUtils.cleanPath("mypath/myfile"));
        assertEquals("mypath/myfile", PathUtils.cleanPath("mypath\\myfile"));
        assertEquals("mypath/myfile", PathUtils.cleanPath("mypath/../mypath/myfile"));
        assertEquals("mypath/myfile", PathUtils.cleanPath("mypath/myfile/../../mypath/myfile"));
        assertEquals("../mypath/myfile", PathUtils.cleanPath("../mypath/myfile"));
        assertEquals("../mypath/myfile", PathUtils.cleanPath("../mypath/../mypath/myfile"));
        assertEquals("../mypath/myfile", PathUtils.cleanPath("mypath/../../mypath/myfile"));
        assertEquals("/../mypath/myfile", PathUtils.cleanPath("/../mypath/myfile"));
        assertEquals("/mypath/myfile", PathUtils.cleanPath("/a/:b/../../mypath/myfile"));
        assertEquals("/", PathUtils.cleanPath("/"));
        assertEquals("/", PathUtils.cleanPath("/mypath/../"));
        assertTrue(PathUtils.cleanPath("mypath/..").isEmpty());
        assertTrue(PathUtils.cleanPath("mypath/../.").isEmpty());
        assertEquals("./", PathUtils.cleanPath("mypath/../"));
        assertEquals("./", PathUtils.cleanPath("././"));
        assertEquals("./", PathUtils.cleanPath("./"));
        assertEquals("../", PathUtils.cleanPath("../"));
        assertEquals("../", PathUtils.cleanPath("./../"));
        assertEquals("../", PathUtils.cleanPath(".././"));
        assertEquals("", PathUtils.cleanPath("."));
        assertEquals("file:/", PathUtils.cleanPath("file:/"));
        assertEquals("file:/", PathUtils.cleanPath("file:/mypath/../"));
        assertEquals("file:", PathUtils.cleanPath("file:mypath/.."));
        assertEquals("file:", PathUtils.cleanPath("file:mypath/../."));
        assertEquals("file:./", PathUtils.cleanPath("file:mypath/../"));
        assertEquals("file:./", PathUtils.cleanPath("file:././"));
        assertEquals("file:./", PathUtils.cleanPath("file:./"));
        assertEquals("file:../", PathUtils.cleanPath("file:../"));
        assertEquals("file:../", PathUtils.cleanPath("file:./../"));
        assertEquals("file:../", PathUtils.cleanPath("file:.././"));
        assertEquals("file:/mypath/spring.factories", PathUtils.cleanPath("file:/mypath/spring.factories"));
        assertEquals("file:///c:/path/the%20file.txt", PathUtils.cleanPath("file:///c:/some/../path/the%20file.txt"));
        assertEquals("jar:file:///c:/path/the%20file.txt", PathUtils.cleanPath("jar:file:///c:\\some\\..\\path\\.\\the%20file.txt"));
        assertEquals("jar:file:///c:/path/the%20file.txt", PathUtils.cleanPath("jar:file:///c:/some/../path/./the%20file.txt"));
    }

    @Test
    void pathEquals() {
        assertTrue(PathUtils.pathEquals(null, null));
        assertFalse(PathUtils.pathEquals(null, "/dummy"));
        assertFalse(PathUtils.pathEquals("/dummy", null));
        assertTrue(PathUtils.pathEquals("/dummy1/dummy2/dummy3", "/dummy1/dummy2/dummy3"));
        assertTrue(PathUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3", "C:\\dummy1\\dummy2\\dummy3"));
        assertTrue(PathUtils.pathEquals("/dummy1/bin/../dummy2/dummy3", "/dummy1/dummy2/dummy3"));
        assertTrue(PathUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3", "C:\\dummy1\\bin\\..\\dummy2\\dummy3"));
        assertTrue(PathUtils.pathEquals("/dummy1/bin/../dummy2/bin/../dummy3", "/dummy1/dummy2/dummy3"));
        assertTrue(PathUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3", "C:\\dummy1\\bin\\..\\dummy2\\bin\\..\\dummy3"));
        assertTrue(PathUtils.pathEquals("/dummy1/bin/tmp/../../dummy2/dummy3", "/dummy1/dummy2/dummy3"));
        assertTrue(PathUtils.pathEquals("/dummy1/dummy2/dummy3", "/dummy1/dum/dum/../../dummy2/dummy3"));
        assertTrue(PathUtils.pathEquals("./dummy1/dummy2/dummy3", "dummy1/dum/./dum/../../dummy2/dummy3"));
        assertTrue(PathUtils.pathEquals(".", ""));
        assertFalse(PathUtils.pathEquals("./dummy1/dummy2/dummy3", "/dummy1/dum/./dum/../../dummy2/dummy3"));
        assertFalse(PathUtils.pathEquals("/dummy1/dummy2/dummy3", "/dummy1/dummy4/dummy3"));
        assertFalse(PathUtils.pathEquals("/dummy1/bin/tmp/../dummy2/dummy3", "/dummy1/dummy2/dummy3"));
        assertFalse(PathUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3", "C:\\dummy1\\bin\\tmp\\..\\dummy2\\dummy3"));
        assertFalse(PathUtils.pathEquals("/dummy1/bin/../dummy2/dummy3", "/dummy1/dummy2/dummy4"));
    }

}
