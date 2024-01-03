/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.embed.service;

import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.utils.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmbeddedAspectranTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        //String rootFile = "classpath:config/embedded/embedded-aspectran-context.xml";
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.newContextConfig().setContextRules(new String[] {"./target/test-classes/config/embedded/embedded-aspectran-context.xml"});
        aspectranConfig.newEmbedConfig().newSessionManagerConfig().setEnabled(true);
        System.setProperty(ActivityContextBuilder.DEBUG_MODE_PROPERTY_NAME, "true");
        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    void test1() {
        FirstBean firstBean = aspectran.getBean("thirdBean");

        //System.out.println(firstBean);
        //System.out.println(firstBean.getMessage());

        assertEquals(firstBean.getMessage(), SecondBean.message);

        Translet translet = aspectran.translate("echo");
        System.out.println(translet);

        ParameterMap params = new ParameterMap();
        params.setParameter("id", "0001");
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String echo = aspectran.render("echo", params);
        System.out.println(echo);
    }

    @Test
    void test2() {
        Translet translet = aspectran.translate("attr-test");
        assertEquals("abc123", translet.toString());
    }

    @Test
    void includeTest() {
        Translet translet = aspectran.translate("include-test");
        assertEquals("abc123-param1 :: This is the second bean.test", translet.toString());
    }

    @Test
    void forwardTest() {
        Translet translet = aspectran.translate("forward-test");
        assertEquals("abc123-param1 :: This is the second bean.test", translet.toString());
    }

    @Test
    void instantActivityTest() {
        String result = aspectran.execute(() -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void actionCallTest() {
        Translet translet = aspectran.translate("add-up");
        ActivityData activityData = translet.getActivityData();
        //System.out.println("Result: " + activityData.get("result"));
        assertEquals(activityData.get("result"), 10);
    }

    @Test
    void testEcho123() {
        Translet translet = aspectran.translate("echo123");
        assertEquals("123==123", translet.toString().trim());
    }

    @Test
    void testChooseWhen() {
        String mode = "case2-2";

        ParameterMap params = new ParameterMap();
        params.setParameter("mode", mode);

        Translet translet = aspectran.translate("chooseWhenTest", params);
        ActivityData activityData = translet.getActivityData();
        String response = translet.toString();

        System.out.println("Mode: " + mode);
        System.out.println("Action Result: " + activityData.get(mode));
        System.out.println("Response: " + response);

        assertEquals(mode, activityData.get(mode));
        assertEquals("Case 2-2: case2-2, Case 2-4: case2-2".trim(), response.trim());
    }

    @Test
    void thrown1Test() {
        String mode = "thrown1";

        ParameterMap params = new ParameterMap();
        params.setParameter("mode", mode);

        Translet translet = aspectran.translate("thrownTest", params);
        String response = translet.toString();

        assertEquals("thrown1 - thrown NullPointerException", response);
    }

    @Test
    void thrown2Test() {
        String mode = "thrown2";

        ParameterMap params = new ParameterMap();
        params.setParameter("mode", mode);

        Translet translet = aspectran.translate("thrownTest", params);
        String response = translet.toString();

        assertEquals("thrown2 - thrown IllegalArgumentException", response);
    }

    @Test
    void thrown3Test() {
        String mode = "thrown3";

        ParameterMap params = new ParameterMap();
        params.setParameter("mode", mode);

        Translet translet = aspectran.translate("thrownTest", params);
        String response = translet.toString();

        assertEquals("thrown3 - thrown UnsupportedOperationException", response);
    }

    @Test
    void fieldCallTest() {
        String message = aspectran.render("fieldCallTest");
        assertEquals("staticfieldstaticfieldstaticfield", StringUtils.trimAllWhitespace(message));
    }

}
