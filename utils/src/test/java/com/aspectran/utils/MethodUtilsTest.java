/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

}
