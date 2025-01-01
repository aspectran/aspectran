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
package com.aspectran.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceUtilsTest {

    @Test
    void toURL() throws MalformedURLException {
        assertEquals("file://mypath/myfile", ResourceUtils.toURL("file://mypath/myfile").toString());
    }

    @Test
    void getResource() throws IOException {
        assertTrue(ResourceUtils.getResource("com/aspectran/utils/ResourceUtilsTest.class").getPath().endsWith("com/aspectran/utils/ResourceUtilsTest.class"));
        System.out.println(ResourceUtils.getResource("com/aspectran/utils/ResourceUtilsTest.class"));
    }

}
