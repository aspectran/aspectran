/*
 * Copyright 2008-2017 Juho Jeong
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

import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Test case for scanning files.
 */
public class FileScannerTest {

    @Test
    public void test1() {
        System.out.println("------------------------------");
        System.out.println(" Test case for scanning files ");
        System.out.println("------------------------------");

        FileScanner scanner = new FileScanner("./target/test-classes");
        Map<String, File> map = scanner.scan("**/*Test.class");
        for (Map.Entry<String, File> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

}
