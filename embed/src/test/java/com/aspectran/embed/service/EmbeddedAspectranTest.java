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
package com.aspectran.embed.service;

import com.aspectran.core.activity.ActivityDataMap;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.ActivityContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmbeddedAspectranTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    public void ready() {
        String rootConfigFile = "classpath:config/embedded/embedded-aspectran-config.xml";
        aspectran = EmbeddedAspectran.run(rootConfigFile);
    }

    @AfterAll
    public void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    public void test1() throws IOException {
        ActivityContext activityContext = aspectran.getActivityContext();
        BeanRegistry beanRegistry = activityContext.getBeanRegistry();
        FirstBean firstBean = beanRegistry.getBean("thirdBean");

        //System.out.println(firstBean);
        //System.out.println(firstBean.getMessage());

        assertEquals(firstBean.getMessage(), SecondBean.message);

        Translet translet = aspectran.translate("echo");
        System.out.println(translet.getResponseAdapter().getWriter().toString());

        ParameterMap params = new ParameterMap();
        params.setParameter("id", "0001");
        params.setParameter("name", "aspectran");
        params.setParameter("email", "aspectran@aspectran.com");

        String echo = aspectran.template("echo", params);
        System.out.println(echo);

        String selectQuery = aspectran.template("selectQuery", params);
        System.out.println(selectQuery);

        String updateQuery = aspectran.template("updateQuery", params);
        System.out.println(updateQuery);
    }

    @Test
    public void test2() throws IOException {
        Translet translet = aspectran.translate("attr-test");
        System.out.println(translet.getResponseAdapter().getWriter().toString());
    }

    @Test
    public void includeTest() throws IOException {
        Translet translet = aspectran.translate("include-test");
        System.out.println(translet.getResponseAdapter().getWriter().toString());
    }

    @Test
    public void actionCallTest() {
        Translet translet = aspectran.translate("add-up");
        ActivityDataMap dataMap = translet.getActivityDataMap();
        //System.out.println("Result: " + dataMap.get("result"));
        assertEquals(dataMap.get("result"), 10);
    }

    @Test
    public void testEcho123() throws IOException {
        Translet translet = aspectran.translate("echo123");
        System.out.println(translet.getResponseAdapter().getWriter().toString());
    }

}