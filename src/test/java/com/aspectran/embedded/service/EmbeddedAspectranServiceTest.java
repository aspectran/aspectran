/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.embedded.service;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.activity.ActivityDataMap;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.ActivityContext;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedAspectranServiceTest {

    private EmbeddedAspectranService aspectranService;

    @Before
    public void ready() throws Exception {
        String rootContextLocation = "classpath:config/embedded/embedded-mode-test-config.xml";
        aspectranService = EmbeddedAspectranService.create(rootContextLocation);
        aspectranService.start();
    }

    @After
    public void finish() {
        if (aspectranService != null) {
            aspectranService.stop();
        }
    }

    @Test
    public void test1() throws IOException {
        ActivityContext activityContext = aspectranService.getActivityContext();
        BeanRegistry beanRegistry = activityContext.getBeanRegistry();
        FirstBean firstBean = beanRegistry.getBean("thirdBean");

        //System.out.println(firstBean);
        //System.out.println(firstBean.getMessage());

        Assert.assertEquals(firstBean.getMessage(), SecondBean.message);

        Translet translet = aspectranService.translet("echo");
        System.out.println(translet.getResponseAdapter().getWriter().toString());

        ParameterMap params = new ParameterMap();
        params.setParameter("id", "0001");
        params.setParameter("name", "aspectran");
        params.setParameter("email", "aspectran@aspectran.com");

        String echo = aspectranService.template("echo", params);
        System.out.println(echo);

        String selectQuery = aspectranService.template("selectQuery", params);
        System.out.println(selectQuery);

        String updateQuery = aspectranService.template("updateQuery", params);
        System.out.println(updateQuery);
    }

    @Test
    public void test2() throws IOException {
        Translet translet = aspectranService.translet("attr-test");
        System.out.println(translet.getResponseAdapter().getWriter().toString());
    }

    @Test
    public void includeTest() throws IOException {
        Translet translet = aspectranService.translet("include-test");
        System.out.println(translet.getResponseAdapter().getWriter().toString());
    }

    @Test
    public void actionCallTest() throws IOException {
        Translet translet = aspectranService.translet("add-up");
        ActivityDataMap dataMap = translet.getActivityDataMap();
        //System.out.println("Result: " + dataMap.get("result"));
        assertEquals(dataMap.get("result"), 10);
    }

}