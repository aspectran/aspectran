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
package com.aspectran.web.support.rest.response;

import com.aspectran.web.activity.response.DefaultRestResponse;
import com.aspectran.web.activity.response.RestResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ResponsePayload}, {@link SuccessResponse},
 * and {@link FailureResponse} protection mechanisms.
 *
 * <p>Created: 2026-10-23</p>
 */
class ResponsePayloadTest {

    @Test
    void testResponsePayloadProtect() {
        ResponsePayload payload = new ResponsePayload(true, "secret-token");
        assertFalse(payload.isProtected());
        assertEquals("{success=true, data=secret-token}", payload.toString());

        payload.protect();
        assertTrue(payload.isProtected());
        assertEquals("{success=true, data=[PROTECTED]}", payload.toString());

        // Ensure actual data is intact in toEntity()
        Map<String, Object> entity = payload.toEntity();
        assertEquals("secret-token", entity.get("data"));

        payload.protect(false);
        assertFalse(payload.isProtected());
        assertEquals("{success=true, data=secret-token}", payload.toString());
    }

    @Test
    void testSuccessResponseProtect() {
        SuccessResponse response = new SuccessResponse("sensitive-user-info");
        assertFalse(response.isProtected());
        assertEquals("{success=true, data=sensitive-user-info}", response.toString());

        response.protect();
        assertTrue(response.isProtected());
        assertEquals("{success=true, data=[PROTECTED]}", response.toString());

        // Verify that underlying payload is protected
        assertTrue(response.getData().isProtected());
        assertEquals("sensitive-user-info", response.getData().toEntity().get("data"));

        // When data is updated after protect()
        response.setData("new-sensitive-info");
        assertTrue(response.isProtected());
        assertEquals("{success=true, data=[PROTECTED]}", response.toString());
        assertEquals("new-sensitive-info", response.getData().toEntity().get("data"));
    }

    @Test
    void testFailureResponseProtect() {
        FailureResponse response = new FailureResponse("AUTH_FAILED", "Authentication failed");
        response.setData("user-password-attempt");
        assertFalse(response.isProtected());
        assertTrue(response.toString().contains("data=user-password-attempt"));

        response.protect();
        assertTrue(response.isProtected());
        assertTrue(response.toString().contains("data=[PROTECTED]"));
        assertTrue(response.toString().contains("code=AUTH_FAILED"));
        assertEquals("user-password-attempt", response.getData().toEntity().get("data"));
    }

    @Test
    void testDefaultRestResponseProtect() {
        RestResponse response = new DefaultRestResponse("secret-data");
        assertFalse(response.isProtected());
        assertTrue(response.toString().contains("data=secret-data"));

        response.protect();
        assertTrue(response.isProtected());
        assertTrue(response.toString().contains("data=[PROTECTED]"));
        assertFalse(response.toString().contains("secret-data"));
        assertEquals("secret-data", response.getData());
    }

}
