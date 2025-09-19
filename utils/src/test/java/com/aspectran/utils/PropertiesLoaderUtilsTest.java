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
class PropertiesLoaderUtilsTest {

    private static final String PASSWORD = "encryption-password-for-test";

    @BeforeAll
    void passwordSetting() {
        //System.setProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_KEY, "PBEWithMD5AndTripleDES");
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, PASSWORD);
        String encryptedPassword = PBEncryptionUtils.encrypt("1234", PASSWORD);
        System.out.println(encryptedPassword);
    }

    @Test
    void testLoadProperties() throws IOException {
        System.setProperty(PropertiesLoaderUtils.PROPERTIES_ENCODING_PROPERTY, "UTF-8");
        Properties props = PropertiesLoaderUtils.loadProperties("test.encrypted.properties");
        assertEquals("Aspectran", props.getProperty("name"));
        assertEquals("1234", props.getProperty("passwd"));
        assertEquals("아스펙트란", props.getProperty("name_ko"));
    }

}
