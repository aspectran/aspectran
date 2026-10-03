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
package com.aspectran.web.support.http;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.InstantTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@AspectranTest
class HttpStatusSetterTest {

    private ActivityTester tester;

    @BeforeEach
    void setUp(ActivityContext context) {
        tester = new ActivityTester(context);
    }

    private Translet createTranslet(Activity activity, DefaultResponseAdapter response) {
        ((InstantActivity) activity).setResponseAdapter(response);
        return new InstantTranslet(activity);
    }

    @Test
    void testSetStatus() throws Exception {
        tester.perform(activity -> {
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            Translet translet = createTranslet(activity, response);

            HttpStatusSetter.setStatus(HttpStatus.OK, translet);
            assertEquals(200, response.getStatus());

            HttpStatusSetter.setStatus(204, translet);
            assertEquals(204, response.getStatus());

            return null;
        });
    }

    @Test
    void testSuccessStatuses() throws Exception {
        tester.perform(activity -> {
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            Translet translet = createTranslet(activity, response);

            HttpStatusSetter.ok(translet);
            assertEquals(200, response.getStatus());

            HttpStatusSetter.created(translet);
            assertEquals(201, response.getStatus());
            assertNull(response.getHeader(HttpHeaders.LOCATION));

            HttpStatusSetter.created(translet, "/items/123");
            assertEquals(201, response.getStatus());
            assertEquals("/items/123", response.getHeader(HttpHeaders.LOCATION));

            HttpStatusSetter.accepted(translet);
            assertEquals(202, response.getStatus());

            HttpStatusSetter.noContent(translet);
            assertEquals(204, response.getStatus());

            return null;
        });
    }

    @Test
    void testRedirectionStatuses() throws Exception {
        tester.perform(activity -> {
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            Translet translet = createTranslet(activity, response);

            HttpStatusSetter.movedPermanently(translet, "/new-url");
            assertEquals(301, response.getStatus());
            assertEquals("/new-url", response.getHeader(HttpHeaders.LOCATION));

            HttpStatusSetter.found(translet, "/found-url");
            assertEquals(302, response.getStatus());
            assertEquals("/found-url", response.getHeader(HttpHeaders.LOCATION));

            HttpStatusSetter.seeOther(translet, "/other-url");
            assertEquals(303, response.getStatus());
            assertEquals("/other-url", response.getHeader(HttpHeaders.LOCATION));

            HttpStatusSetter.notModified(translet);
            assertEquals(304, response.getStatus());

            HttpStatusSetter.temporaryRedirect(translet, "/temp-url");
            assertEquals(307, response.getStatus());
            assertEquals("/temp-url", response.getHeader(HttpHeaders.LOCATION));

            HttpStatusSetter.permanentRedirect(translet, "/perm-url");
            assertEquals(308, response.getStatus());
            assertEquals("/perm-url", response.getHeader(HttpHeaders.LOCATION));

            return null;
        });
    }

    @Test
    void testClientErrorStatuses() throws Exception {
        tester.perform(activity -> {
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            Translet translet = createTranslet(activity, response);

            HttpStatusSetter.badRequest(translet);
            assertEquals(400, response.getStatus());

            HttpStatusSetter.unauthorized(translet);
            assertEquals(401, response.getStatus());

            HttpStatusSetter.forbidden(translet);
            assertEquals(403, response.getStatus());

            HttpStatusSetter.notFound(translet);
            assertEquals(404, response.getStatus());

            HttpStatusSetter.methodNotAllowed(translet);
            assertEquals(405, response.getStatus());

            HttpStatusSetter.notAcceptable(translet);
            assertEquals(406, response.getStatus());

            HttpStatusSetter.conflict(translet);
            assertEquals(409, response.getStatus());

            HttpStatusSetter.preconditionFailed(translet);
            assertEquals(412, response.getStatus());

            HttpStatusSetter.unsupportedMediaType(translet);
            assertEquals(415, response.getStatus());

            HttpStatusSetter.unprocessableEntity(translet);
            assertEquals(422, response.getStatus());

            HttpStatusSetter.tooManyRequests(translet);
            assertEquals(429, response.getStatus());

            return null;
        });
    }

    @Test
    void testServerErrorStatuses() throws Exception {
        tester.perform(activity -> {
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            Translet translet = createTranslet(activity, response);

            HttpStatusSetter.internalServerError(translet);
            assertEquals(500, response.getStatus());

            HttpStatusSetter.badGateway(translet);
            assertEquals(502, response.getStatus());

            HttpStatusSetter.serviceUnavailable(translet);
            assertEquals(503, response.getStatus());

            HttpStatusSetter.gatewayTimeout(translet);
            assertEquals(504, response.getStatus());

            return null;
        });
    }

}
