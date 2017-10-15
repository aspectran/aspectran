/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

import com.aspectran.core.context.builder.resource.AspectranClassLoader;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Test case for scanning classes.
 */
public class ClassScannerTest {

    @Test
    public void test1() throws IOException {
        System.out.println("--------------------------------");
        System.out.println(" Test case for scanning classes ");
        System.out.println("--------------------------------");

        ClassScanner scanner = new ClassScanner(AspectranClassLoader.getDefaultClassLoader());
        Map<String, Class<?>> map = scanner.scan("com.aspectran.**.*Test");
        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            System.out.println(entry.getValue() + " - " + entry.getKey());
        }
    }

}
