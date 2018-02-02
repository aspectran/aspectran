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

import org.junit.Test;

import java.io.File;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

/**
 * Test case for scanning files.
 */
public class FileScannerTest {

    @Test
    public void testFileScan() {
        FileScanner scanner = new FileScanner("./target/test-classes");
        Map<String, File> map = scanner.scan("**/util/*Test.class");
        for (Map.Entry<String, File> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + " - " + entry.getValue());
            assertTrue(entry.getValue().toString().replace(File.separatorChar, '/').endsWith(entry.getKey()));
        }
    }

}
