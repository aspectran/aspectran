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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link SystemUtils}.
 */
class SystemUtilsTest {

    @Test
    void testPropertyAccess() {
        String testKey = "com.aspectran.utils.system.test.key";
        try {
            assertNull(SystemUtils.getProperty(testKey));
            assertEquals("default", SystemUtils.getProperty(testKey, "default"));

            SystemUtils.setProperty(testKey, "value1");
            assertEquals("value1", SystemUtils.getProperty(testKey));

            SystemUtils.setProperty(testKey, "value2");
            assertEquals("value2", SystemUtils.getProperty(testKey));

            SystemUtils.setProperty(testKey, null);
            assertNull(SystemUtils.getProperty(testKey));

            SystemUtils.setProperty(testKey, "value3");
            String cleared = SystemUtils.clearProperty(testKey);
            assertEquals("value3", cleared);
            assertNull(SystemUtils.getProperty(testKey));
        } finally {
            System.clearProperty(testKey);
        }
    }

    @Test
    void testStandardProperties() {
        assertNotNull(SystemUtils.getJavaIoTmpDir());
        assertNotNull(SystemUtils.getUserHome());
        assertNotNull(SystemUtils.getUserDir());
        assertNotNull(SystemUtils.getUserName());
        assertNotNull(SystemUtils.getOsName());
        assertNotNull(SystemUtils.getOsVersion());
        assertNotNull(SystemUtils.getOsArch());
        assertNotNull(SystemUtils.getJavaVersion());
    }

    @Test
    void testOsDetection() {
        String os = SystemUtils.getOsName();
        assertNotNull(os);

        boolean isWin = SystemUtils.isWindows();
        boolean isLinux = SystemUtils.isLinux();
        boolean isMac = SystemUtils.isMac();
        boolean isUnix = SystemUtils.isUnix();

        if (isLinux || isMac) {
            assertTrue(isUnix);
            assertFalse(isWin);
        } else if (isWin) {
            assertFalse(isUnix);
            assertFalse(isLinux);
            assertFalse(isMac);
        }
    }

    @Test
    void testRuntimeAndProcess() {
        assertTrue(SystemUtils.getPid() > 0);
        assertTrue(SystemUtils.getAvailableProcessors() > 0);
        assertTrue(SystemUtils.getMaxMemory() > 0);
        assertTrue(SystemUtils.getTotalMemory() > 0);
        assertTrue(SystemUtils.getFreeMemory() > 0);
    }

    @Test
    void testEnvironment() {
        assertNotNull(SystemUtils.getEnv("PATH", null));
        assertEquals("default", SystemUtils.getEnv("NON_EXISTENT_ENV_12345", "default"));

        String testKey = "com.aspectran.test.env.prop";
        try {
            SystemUtils.setProperty(testKey, "from_prop");
            assertEquals("from_prop", SystemUtils.getPropertyOrEnv(testKey));
        } finally {
            SystemUtils.clearProperty(testKey);
        }

        // Test fallback to PATH via lowercase / dots replacement
        String pathVal = SystemUtils.getPropertyOrEnv("path");
        if (SystemUtils.getEnv("PATH") != null) {
            assertEquals(SystemUtils.getEnv("PATH"), pathVal);
        }
    }

    @Test
    void testNetworking() {
        assertNotNull(SystemUtils.getLocalIP());
        assertFalse(SystemUtils.getLocalIP().isEmpty());

        List<String> allIps = SystemUtils.getAllLocalIPs();
        assertNotNull(allIps);

        assertNotNull(SystemUtils.getHostName());
        assertFalse(SystemUtils.getHostName().isEmpty());
    }

}
