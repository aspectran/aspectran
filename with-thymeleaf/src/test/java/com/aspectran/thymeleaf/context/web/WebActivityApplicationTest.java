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
package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.web.service.WebService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link WebActivityApplication}.
 *
 * <p>Created: 2026-09-02</p>
 */
class WebActivityApplicationTest {

    @Test
    void testWebServiceAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("appTitle", "Aspectran Web Application");

        WebService webService = (WebService) Proxy.newProxyInstance(
                WebService.class.getClassLoader(),
                new Class<?>[] { WebService.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> attributes.get(args[0]);
                    case "setAttribute" -> {
                        attributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        attributes.remove(args[0]);
                        yield null;
                    }
                    case "getAttributeNames" -> Collections.unmodifiableSet(attributes.keySet());
                    default -> null;
                }
        );

        ActivityContext activityContext = (ActivityContext) Proxy.newProxyInstance(
                ActivityContext.class.getClassLoader(),
                new Class<?>[] { ActivityContext.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMasterService" -> webService;
                    default -> null;
                }
        );

        WebActivityApplication app = new WebActivityApplication(activityContext, webService);
        assertEquals(1, app.getAttributeCount());
        assertTrue(app.containsAttribute("appTitle"));
        assertEquals("Aspectran Web Application", app.getAttributeValue("appTitle"));
        assertEquals(1, app.getAttributeMap().size());

        app.setAttributeValue("version", "9.7.0");
        assertEquals(2, app.getAttributeCount());
        assertEquals("9.7.0", app.getAttributeValue("version"));

        app.removeAttribute("appTitle");
        assertEquals(1, app.getAttributeCount());
    }

}
