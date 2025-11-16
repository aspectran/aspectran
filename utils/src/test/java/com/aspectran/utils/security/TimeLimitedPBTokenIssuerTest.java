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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link TimeLimitedPBTokenIssuer}.
 *
 * <p>Created: 2019/11/25</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimeLimitedPBTokenIssuerTest {

    private static final String DEFAULT_PASSWORD = "encryption-password-for-test";
    private static final String CUSTOM_PASSWORD = "custom-encryption-password";

    @BeforeAll
    static void passwordSetting() {
        // System default password for static methods without explicit password
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, DEFAULT_PASSWORD);
    }

    @Test
    void testStaticTimeLimitedPBTokenWithDefaultPassword() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 1000L); // 1 second validity
        Parameters params2 = TimeLimitedPBTokenIssuer.parseToken(token);
        assertEquals(params.toString(), params2.toString());
    }

    @Test
    void testStaticTimeLimitedPBTokenExpirationWithDefaultPassword() throws InterruptedException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 100L); // 100ms validity
        Thread.sleep(200); // Wait for token to expire
        assertThrows(ExpiredPBTokenException.class, () -> {
            TimeLimitedPBTokenIssuer.parseToken(token);
        });
    }

    @Test
    void testStaticTimeLimitedPBTokenWithCustomPassword() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 1000L, CUSTOM_PASSWORD); // 1 second validity
        Parameters params2 = TimeLimitedPBTokenIssuer.parseToken(token, CUSTOM_PASSWORD);
        assertEquals(params.toString(), params2.toString());
    }

    @Test
    void testStaticTimeLimitedPBTokenExpirationWithCustomPassword() throws InterruptedException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 100L, CUSTOM_PASSWORD); // 100ms validity
        Thread.sleep(200); // Wait for token to expire
        assertThrows(ExpiredPBTokenException.class, () -> {
            TimeLimitedPBTokenIssuer.parseToken(token, CUSTOM_PASSWORD);
        });
    }

    @Test
    void testStaticTimeLimitedPBTokenInvalidPassword() {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 1000L, "password-one");
        assertThrows(InvalidPBTokenException.class, () -> {
            TimeLimitedPBTokenIssuer.parseToken(token, "password-two");
        });
    }

    @Test
    void testStaticTimeLimitedPBTokenValidateWithDefaultPassword() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 1000L);
        TimeLimitedPBTokenIssuer.validate(token); // Should not throw exception
    }

    @Test
    void testStaticTimeLimitedPBTokenValidateWithCustomPassword() throws InvalidPBTokenException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 1000L, CUSTOM_PASSWORD);
        TimeLimitedPBTokenIssuer.validate(token, CUSTOM_PASSWORD); // Should not throw exception
    }

    @Test
    void testStaticTimeLimitedPBTokenValidateExpired() throws InterruptedException {
        Parameters params = new VariableParameters();
        params.putValue("p1", "v1");
        String token = TimeLimitedPBTokenIssuer.createToken(params, 100L, CUSTOM_PASSWORD);
        Thread.sleep(200);
        assertThrows(ExpiredPBTokenException.class, () -> {
            TimeLimitedPBTokenIssuer.validate(token, CUSTOM_PASSWORD);
        });
    }

}
