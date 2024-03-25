/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathUtilsTest {

    @Test
    void applyRelativePath() {
        assertEquals("mypath/mypath/myfile", PathUtils.applyRelativePath("mypath/myfile", "mypath/myfile"));
        assertEquals("mypath/mypath/mypath/myfile", PathUtils.applyRelativePath("mypath/mypath/", "mypath/myfile"));
        assertEquals("mypath/mypath/", PathUtils.applyRelativePath("mypath/mypath/myfile", ""));
    }

    @Test
    void cleanPath() {
        assertEquals(PathUtils.cleanPath("mypath/myfile"), "mypath/myfile");
        assertEquals(PathUtils.cleanPath("mypath\\myfile"), "mypath/myfile");
        assertEquals(PathUtils.cleanPath("mypath/../mypath/myfile"), "mypath/myfile");
        assertEquals(PathUtils.cleanPath("mypath/myfile/../../mypath/myfile"), "mypath/myfile");
        assertEquals(PathUtils.cleanPath("../mypath/myfile"), "../mypath/myfile");
        assertEquals(PathUtils.cleanPath("../mypath/../mypath/myfile"), "../mypath/myfile");
        assertEquals(PathUtils.cleanPath("mypath/../../mypath/myfile"), "../mypath/myfile");
        assertEquals(PathUtils.cleanPath("/../mypath/myfile"), "/../mypath/myfile");
        assertEquals(PathUtils.cleanPath("/a/:b/../../mypath/myfile"), "/mypath/myfile");
        assertEquals(PathUtils.cleanPath("/"), "/");
        assertEquals(PathUtils.cleanPath("/mypath/../"), "/");
        assertTrue(PathUtils.cleanPath("mypath/..").isEmpty());
        assertTrue(PathUtils.cleanPath("mypath/../.").isEmpty());
        assertEquals(PathUtils.cleanPath("mypath/../"), "./");
        assertEquals(PathUtils.cleanPath("././"), "./");
        assertEquals(PathUtils.cleanPath("./"), "./");
        assertEquals(PathUtils.cleanPath("../"), "../");
        assertEquals(PathUtils.cleanPath("./../"), "../");
        assertEquals(PathUtils.cleanPath(".././"), "../");
        assertEquals(PathUtils.cleanPath("."), "");
        assertEquals(PathUtils.cleanPath("file:/"), "file:/");
        assertEquals(PathUtils.cleanPath("file:/mypath/../"), "file:/");
        assertEquals(PathUtils.cleanPath("file:mypath/.."), "file:");
        assertEquals(PathUtils.cleanPath("file:mypath/../."), "file:");
        assertEquals(PathUtils.cleanPath("file:mypath/../"), "file:./");
        assertEquals(PathUtils.cleanPath("file:././"), "file:./");
        assertEquals(PathUtils.cleanPath("file:./"), "file:./");
        assertEquals(PathUtils.cleanPath("file:../"), "file:../");
        assertEquals(PathUtils.cleanPath("file:./../"), "file:../");
        assertEquals(PathUtils.cleanPath("file:.././"), "file:../");
        assertEquals(PathUtils.cleanPath("file:/mypath/spring.factories"), "file:/mypath/spring.factories");
        assertEquals(PathUtils.cleanPath("file:///c:/some/../path/the%20file.txt"), "file:///c:/path/the%20file.txt");
        assertEquals(PathUtils.cleanPath("jar:file:///c:\\some\\..\\path\\.\\the%20file.txt"), "jar:file:///c:/path/the%20file.txt");
        assertEquals(PathUtils.cleanPath("jar:file:///c:/some/../path/./the%20file.txt"), "jar:file:///c:/path/the%20file.txt");
    }

    @Test
    void pathEquals() {
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
