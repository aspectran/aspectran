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
package com.aspectran.utils.security;

import com.aspectran.utils.PBEncryptionUtils;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link PBTokenIssuer}.
 *
 * <p>Created: 2019/11/25</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PBTokenIssuerTest {

    private static final String DEFAULT_PASSWORD = "encryption-password-for-test";
    private static final String CUSTOM_PASSWORD = "custom-encryption-password";

    @BeforeAll
    static void passwordSetting() {
        // System default password for static methods without explicit password
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, DEFAULT_PASSWORD);
    }

    @Test
    void testStaticPBTokenWithDefaultPassword() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        params.putValue("p2", "v2");
        String token = PBTokenIssuer.createToken(params);
        Parameters params2 = PBTokenIssuer.parseToken(token);
        assertEquals(params.toString(), params2.toString());
    }

    @Test
    void testStaticPBTokenWithCustomPassword() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        params.putValue("p2", "v2");
        String token = PBTokenIssuer.createToken(params, CUSTOM_PASSWORD);
        Parameters params2 = PBTokenIssuer.parseToken(token, CUSTOM_PASSWORD);
        assertEquals(params.toString(), params2.toString());
    }

    @Test
    void testStaticPBTokenWithDifferentDataTypes() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("stringValue", "hello world");
        params.putValue("intValue", 12345);
        params.putValue("longValue(long)", 1234567890L);
        params.putValue("doubleValue", 123.456);
        params.putValue("booleanValue", true);
        List<String> stringList = Arrays.asList("apple", "banana", "cherry");
        params.putValue("stringList", stringList);

        String token = PBTokenIssuer.createToken(params, CUSTOM_PASSWORD);
        Parameters parsedParams = PBTokenIssuer.parseToken(token, CUSTOM_PASSWORD);

        assertEquals("hello world", parsedParams.getString("stringValue"));
        assertEquals(12345, parsedParams.getInt("intValue"));
        assertEquals(1234567890L, parsedParams.getLong("longValue").longValue());
        assertEquals(123.456, parsedParams.getDouble("doubleValue"), 0.0);
        assertTrue(parsedParams.getBoolean("booleanValue"));
        assertEquals(stringList, parsedParams.getStringList("stringList"));
    }

    @Test
    void testStaticPBTokenWithNestedParameters() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");

        Parameters nestedParams = new VariableParameters();
        nestedParams.putValue("n1", "nv1");
        nestedParams.putValue("n2", false);
        params.putValue("nested", nestedParams);

        String token = PBTokenIssuer.createToken(params, CUSTOM_PASSWORD);
        Parameters parsedParams = PBTokenIssuer.parseToken(token, CUSTOM_PASSWORD);

        assertEquals("v1", parsedParams.getString("p1"));
        Parameters parsedNested = parsedParams.getParameters("nested");
        assertNotNull(parsedNested);
        assertEquals("nv1", parsedNested.getString("n1"));
        assertFalse(parsedNested.getBoolean("n2"));
        assertEquals(nestedParams.toString(), parsedNested.toString());
    }

    @Test
    void testStaticEmptyParameters() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        String token = PBTokenIssuer.createToken(params, CUSTOM_PASSWORD);
        Parameters parsedParams = PBTokenIssuer.parseToken(token, CUSTOM_PASSWORD);
        assertTrue(parsedParams.isEmpty());
    }

    @Test
    void testStaticInvalidTokenMalformed() {
        assertThrows(InvalidPBTokenException.class, () -> {
            PBTokenIssuer.parseToken("this-is-not-a-valid-token", CUSTOM_PASSWORD);
        });
    }

    @Test
    void testStaticInvalidTokenWrongPassword() {
        Parameters params = new VariableParameters();
        params.putValue("data", "some-secret-data");

        String token = PBTokenIssuer.createToken(params, "password-one");
        assertThrows(InvalidPBTokenException.class, () -> {
            PBTokenIssuer.parseToken(token, "password-two"); // This should fail
        });
    }

    @Test
    void testStaticValidateToken() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = PBTokenIssuer.createToken(params, CUSTOM_PASSWORD);
        PBTokenIssuer.validate(token, CUSTOM_PASSWORD); // Should not throw exception
    }

    @Test
    void testStaticValidateTokenInvalid() {
        assertThrows(InvalidPBTokenException.class, () -> {
            PBTokenIssuer.validate("invalid-token", CUSTOM_PASSWORD);
        });
    }

}
