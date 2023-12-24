/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 21/10/2018</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PBEncryptionUtilsTest {

    @BeforeAll
    void passwordSetting() {
        // System default
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, "encryption-password-for-test");
    }

    @Test
    void testEncrypt() {
        String original = "1234"; // aW1qbm8rUjFrY1FEQ1gyUkdMdEJWZz09
        String encrypted = PBEncryptionUtils.encrypt(original);
        String decrypted = PBEncryptionUtils.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testEncryptShorten() {
        String original = "1A한";
        //System.out.println(original);
        String encrypted = PBEncryptionUtils.encrypt(original);
        //System.out.println(encrypted);
        String decrypted = PBEncryptionUtils.decrypt(encrypted);
        //System.out.println(decrypted);
        assertEquals(original, decrypted);
    }

}
