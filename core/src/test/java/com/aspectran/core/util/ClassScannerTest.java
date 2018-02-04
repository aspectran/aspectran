/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util;

import com.aspectran.core.context.resource.AspectranClassLoader;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

/**
 * Test case for scanning classes.
 */
public class ClassScannerTest {

    @Test
    public void testScanClass() throws IOException {
        ClassScanner scanner = new ClassScanner(AspectranClassLoader.getDefaultClassLoader());
        Map<String, Class<?>> map = scanner.scan("com.aspectran.**.util.*Test");
        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + " - " + entry.getValue().getName());
            assertTrue(entry.getKey().endsWith(entry.getValue().getName().replace('.', '/') + ".class"));
        }
    }

}
