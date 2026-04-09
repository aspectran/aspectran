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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for {@link AnnotatedMethodInvoker}.
 */
@AspectranTest(
    rules = "/config/activity/annotated-method-invoker-test.xml"
)
class AnnotatedMethodInvokerTest {

    @Test
    void testInvokeSimple(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("name", "tester");
        parameterMap.setParameter("age", "20");
        activity.setParameterMap(parameterMap);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("simple", String.class, int.class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("name");
            pbr1.setType(String.class);

            ParameterBindingRule pbr2 = new ParameterBindingRule();
            pbr2.setName("age");
            pbr2.setType(int.class);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1, pbr2 };

            Object result = AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            assertEquals("tester:20", result);
            return null;
        });
    }

    @Test
    void testInvokeWithTranslet(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("msg", "hello");
        activity.setParameterMap(parameterMap);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("withTranslet", Translet.class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("translet");
            pbr1.setType(Translet.class);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1 };

            Object result = AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            assertEquals("hello", result);
            return null;
        });
    }

    @Test
    void testInvokeWithArray(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameterValues("items", new String[] {"a", "b", "c"});
        activity.setParameterMap(parameterMap);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("withArray", String[].class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("items");
            pbr1.setType(String[].class);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1 };

            Object result = AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            assertEquals("a,b,c", result);
            return null;
        });
    }

    @Test
    void testInvokeWithList(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameterValues("items", new String[] {"a", "b", "c"});
        activity.setParameterMap(parameterMap);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("withList", List.class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("items");
            pbr1.setType(List.class);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1 };

            Object result = AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            assertEquals("a-b-c", result);
            return null;
        });
    }

    @Test
    void testInvokeWithModel(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("name", "tester");
        parameterMap.setParameter("age", "30");
        activity.setParameterMap(parameterMap);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("withModel", TestModel.class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("model");
            pbr1.setType(TestModel.class);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1 };

            Object result = AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            assertEquals("tester:30", result.toString());
            return null;
        });
    }

    @Test
    void testInvokeRequiredMissing(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("simple", String.class, int.class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("name");
            pbr1.setType(String.class);
            pbr1.setRequired(true);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1, pbr1 }; // dummy second pbr

            assertThrows(ParameterBindingException.class, () -> {
                AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            });
            return null;
        });
    }

    @Test
    void testInvokeConversionErrorWithPrimitiveDefault(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("age", "not-a-number");
        activity.setParameterMap(parameterMap);
        activity.prepare("/test");

        activity.perform(() -> {
            MockAction action = new MockAction();
            Method method = MockAction.class.getMethod("simple", String.class, int.class);

            ParameterBindingRule pbr1 = new ParameterBindingRule();
            pbr1.setName("name");
            pbr1.setType(String.class);

            ParameterBindingRule pbr2 = new ParameterBindingRule();
            pbr2.setName("age");
            pbr2.setType(int.class);

            ParameterBindingRule[] pbrs = new ParameterBindingRule[] { pbr1, pbr2 };

            Object result = AnnotatedMethodInvoker.invoke(activity, action, method, pbrs);
            assertEquals("null:0", result); // age defaults to 0 on conversion error
            return null;
        });
    }

    public static class MockAction {
        public String simple(String name, int age) {
            return name + ":" + age;
        }

        public String withTranslet(@NonNull Translet translet) {
            return translet.getParameter("msg");
        }

        public String withArray(String[] items) {
            return (items != null ? String.join(",", items) : "null");
        }

        public String withList(List<String> items) {
            return (items != null ? String.join("-", items) : "null");
        }

        public TestModel withModel(TestModel model) {
            return model;
        }
    }

    public static class TestModel {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return name + ":" + age;
        }
    }

}
