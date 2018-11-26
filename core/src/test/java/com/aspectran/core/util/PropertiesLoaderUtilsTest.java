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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 21/10/2018</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PropertiesLoaderUtilsTest {

    private String oldPassword;

    @BeforeAll
    public void saveProperties() {
        oldPassword = System.getProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY);
    }

    @AfterAll
    public void restoreProperties() {
        if (oldPassword == null) {
            System.clearProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY);
        } else {
            System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, oldPassword);
        }
    }

    @Test
    public void testLoadProperties() throws IOException {
        String password = "abcd1234()";
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, password);
        Properties props = PropertiesLoaderUtils.loadProperties("test.encrypted.properties");
        assertEquals(props.getProperty("name"), "Aspectran");
        assertEquals(props.getProperty("passwd"), "1234");
    }

}