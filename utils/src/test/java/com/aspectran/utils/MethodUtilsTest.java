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
package com.aspectran.utils;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Class MethodUtilsTest.
 *
 * <p>Created: 2016. 2. 29.</p>
 */
class MethodUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodUtilsTest.class);

    @Test
    void testGetMatchingAccessibleMethod1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = {1};
        Class<?>[] paramTypes = { Integer.class };

        Method method = MethodUtils.getMatchingAccessibleMethod(MethodUtilsTestBean.class, "primitiveArray", null, paramTypes);
        assertNotNull(method);

        logger.debug("matched method: {}", method);

        MethodUtilsTestBean sampleBean = new MethodUtilsTestBean();
        MethodUtils.invokeMethod(sampleBean, "primitiveArray", args);
    }

    @Test
    void testGetMatchingAccessibleMethod2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { new Object[] {1, 2} };
        Class<?>[] paramTypes = { Integer[].class };

        Method method = MethodUtils.getMatchingAccessibleMethod(MethodUtilsTestBean.class, "primitiveArray", args, paramTypes);
        assertNotNull(method);

        logger.debug("matched method: {}", method);

        MethodUtilsTestBean sampleBean = new MethodUtilsTestBean();
        MethodUtils.invokeMethod(sampleBean, "primitiveArray", args);
    }

    @Test
    void testGetMatchingAccessibleMethod3() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { new MethodUtilsTestBean() };
        Class<?>[] paramTypes = { args[0].getClass() };

        Method method = MethodUtils.getMatchingAccessibleMethod(MethodUtilsTestBean.class, "setSampleBean", args, paramTypes);
        assertNotNull(method);

        logger.debug("matched method: {}", method);

        MethodUtilsTestBean sampleBean = new MethodUtilsTestBean();
        MethodUtils.invokeSetter(sampleBean, "sampleBean", args);
    }

    @Test
    void testGetMatchingAccessibleMethod4() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { new Object[] { new MethodUtilsTestBean(), new MethodUtilsTestBean() } };
        Class<?>[] paramTypes = { Object[].class };

        Method method = MethodUtils.getMatchingAccessibleMethod(MethodUtilsTestBean.class, "setSampleBean", args, paramTypes);
        assertNotNull(method);

        logger.debug("matched method: {}", method);

        MethodUtilsTestBean sampleBean = new MethodUtilsTestBean();
        MethodUtils.invokeSetter(sampleBean, "sampleBean", args);
    }

    @Test
    void testGetMatchingAccessibleMethod5() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<MethodUtilsTestBean> list = new ArrayList<>();
        list.add(new MethodUtilsTestBean());
        list.add(new MethodUtilsTestBean());

        Object[] args = { list };
        Class<?>[] paramTypes = { Object.class };

        Method method = MethodUtils.getMatchingAccessibleMethod(MethodUtilsTestBean.class, "setSampleBean", args, paramTypes);
        assertNotNull(method);

        logger.debug("matched method: {}", method);

        MethodUtilsTestBean sampleBean = new MethodUtilsTestBean();
        MethodUtils.invokeSetter(sampleBean, "sampleBean", args);
    }

    @Test
    void testIsAssignable() {
        Class<?> paramTypes1 = Integer[].class;
        Class<?> paramTypes2 = int[].class;

        boolean result1 = TypeUtils.isAssignable(paramTypes1, paramTypes2);
        assertTrue(result1);

        boolean result2 = TypeUtils.isAssignable(paramTypes2, paramTypes1);
        assertTrue(result2);
    }

    @Test
    void testInvokeStaticMethodWithNull() throws Exception {
        Object result = MethodUtils.invokeStaticMethod(MethodUtilsTestBean.class, "staticEcho", new Object[] { null });
        assertNull(result);

        Object result2 = MethodUtils.invokeStaticMethod(MethodUtilsTestBean.class, "staticEcho", new Object[] { "hello" });
        assertEquals("hello", result2);
    }

    @Test
    void testInvokeSetterAndGetter() throws Exception {
        MethodUtilsTestBean bean = new MethodUtilsTestBean();
        MethodUtils.invokeSetter(bean, "sampleName", "Aspectran");
        Object value = MethodUtils.invokeGetter(bean, "sampleName");
        assertEquals("Aspectran", value);

        MethodUtils.invokeSetter(bean, "setSampleName", "Aspectran2");
        Object value2 = MethodUtils.invokeGetter(bean, "getSampleName");
        assertEquals("Aspectran2", value2);
    }

    @Test
    void testInvokeNestedSetterAndGetter() throws Exception {
        MethodUtilsTestBean root = new MethodUtilsTestBean();
        MethodUtilsTestBean nested = new MethodUtilsTestBean();
        root.setNestedBean(nested);

        MethodUtils.invokeSetter(root, "nestedBean.sampleName", "NestedValue");
        Object value = MethodUtils.invokeGetter(root, "nestedBean.sampleName");
        assertEquals("NestedValue", value);
    }

    @Test
    void testInvokeBooleanGetter() throws Exception {
        MethodUtilsTestBean bean = new MethodUtilsTestBean();
        bean.setActive(true);

        Object value1 = MethodUtils.invokeGetter(bean, "active");
        assertEquals(Boolean.TRUE, value1);

        Object value2 = MethodUtils.invokeGetter(bean, "isActive");
        assertEquals(Boolean.TRUE, value2);
    }

    @Test
    void testInvokeMethodOverloading() throws Exception {
        MethodUtilsTestBean bean = new MethodUtilsTestBean();

        // Specific String overload preferred
        Object res1 = MethodUtils.invokeMethod(bean, "echo", "text");
        assertEquals("String:text", res1);

        // Specific Integer overload preferred
        Object res2 = MethodUtils.invokeMethod(bean, "echo", 123);
        assertEquals("Integer:123", res2);

        // Fallback to Object overload
        Object res3 = MethodUtils.invokeMethod(bean, "echo", new Object());
        assertTrue(res3.toString().startsWith("Object:"));
    }

    @Test
    void testInvokeExactMethod() throws Exception {
        MethodUtilsTestBean bean = new MethodUtilsTestBean();

        Object res = MethodUtils.invokeExactMethod(bean, "echo", new Object[] { "exact" }, new Class<?>[] { String.class });
        assertEquals("String:exact", res);

        // Exact match with Object.class should invoke echo(Object)
        Object resObj = MethodUtils.invokeExactMethod(bean, "echo", new Object[] { "exact" }, new Class<?>[] { Object.class });
        assertEquals("Object:exact", resObj);
    }

    @Test
    void testInvokeStaticMethodVariants() throws Exception {
        Object res1 = MethodUtils.invokeStaticMethod(MethodUtilsTestBean.class, "staticNoArg");
        assertEquals("staticNoArg", res1);

        Object res2 = MethodUtils.invokeExactStaticMethod(MethodUtilsTestBean.class, "staticNoArg");
        assertEquals("staticNoArg", res2);

        Object res3 = MethodUtils.invokeExactStaticMethod(MethodUtilsTestBean.class, "staticOverload", new Object[] { "test" }, new Class<?>[] { String.class });
        assertEquals("String:test", res3);
    }

    @Test
    void testGetAccessibleMethodWithNullParamTypes() {
        Method method = MethodUtils.getAccessibleMethod(MethodUtilsTestBean.class, "primitiveArray", new Class<?>[] { null });
        assertNull(method);
    }

    @Test
    void testExceptions() {
        MethodUtilsTestBean bean = new MethodUtilsTestBean();

        // Empty setter/getter name
        assertThrows(IllegalArgumentException.class, () ->
                MethodUtils.invokeSetter(bean, "", "value"));
        assertThrows(IllegalArgumentException.class, () ->
                MethodUtils.invokeGetter(bean, ""));

        // Non-existent method
        assertThrows(NoSuchMethodException.class, () ->
                MethodUtils.invokeMethod(bean, "nonExistentMethod"));
        assertThrows(NoSuchMethodException.class, () ->
                MethodUtils.invokeStaticMethod(MethodUtilsTestBean.class, "nonExistentStaticMethod"));
    }

    @Test
    void testClearCache() {
        MethodUtilsTestBean bean = new MethodUtilsTestBean();
        try {
            MethodUtils.invokeMethod(bean, "countTo10");
        } catch (Exception ignored) {
        }
        int cleared = MethodUtils.clearCache();
        assertTrue(cleared > 0);
    }

}
