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
package com.aspectran.core.util;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * The Class MethodUtilsTest.
 * 
 * <p>Created: 2016. 2. 29.</p>
 */
public class MethodUtilsTest {

    private static final Log log = LogFactory.getLog(MethodUtilsTest.class);

    @Test
    public void testGetMatchingAccessibleMethod1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { Integer.valueOf(1) };
        Class<?>[] paramTypes = { Integer.class };

        Method method = MethodUtils.getMatchingAccessibleMethod(SampleBean.class, "primitiveArray", null, paramTypes);
        assertNotNull(method);

        log.debug("matched method: " + method);

        SampleBean sampleBean = new SampleBean();
        MethodUtils.invokeMethod(sampleBean, "primitiveArray", args);
    }

    @Test
    public void testGetMatchingAccessibleMethod2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { new Object[] { Integer.valueOf(1), Integer.valueOf(2) } };
        Class<?>[] paramTypes = { Integer[].class };

        Method method = MethodUtils.getMatchingAccessibleMethod(SampleBean.class, "primitiveArray", args, paramTypes);
        assertNotNull(method);

        log.debug("matched method: " + method);

        SampleBean sampleBean = new SampleBean();
        MethodUtils.invokeMethod(sampleBean, "primitiveArray", args);
    }

    @Test
    public void testGetMatchingAccessibleMethod3() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { new SampleBean() };
        Class<?>[] paramTypes = { args[0].getClass() };

        Method method = MethodUtils.getMatchingAccessibleMethod(SampleBean.class, "setSampleBean", args, paramTypes);
        assertNotNull(method);

        log.debug("matched method: " + method);

        SampleBean sampleBean = new SampleBean();
        MethodUtils.invokeSetter(sampleBean, "sampleBean", args);
    }

    @Test
    public void testGetMatchingAccessibleMethod4() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] args = { new Object[] { new SampleBean(), new SampleBean() } };
        Class<?>[] paramTypes = { Object[].class };

        Method method = MethodUtils.getMatchingAccessibleMethod(SampleBean.class, "setSampleBean", args, paramTypes);
        assertNotNull(method);

        log.debug("matched method: " + method);

        SampleBean sampleBean = new SampleBean();
        MethodUtils.invokeSetter(sampleBean, "sampleBean", args);
    }

    @Test
    public void testGetMatchingAccessibleMethod5() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<SampleBean> list = new ArrayList<>();
        list.add(new SampleBean());
        list.add(new SampleBean());
        
        Object[] args = { list };
        Class<?>[] paramTypes = { Object.class };
        
        Method method = MethodUtils.getMatchingAccessibleMethod(SampleBean.class, "setSampleBean", args, paramTypes);
        assertNotNull(method);

        log.debug("matched method: " + method);
        
        SampleBean sampleBean = new SampleBean();
        MethodUtils.invokeSetter(sampleBean, "sampleBean", args);
    }

    @Test
    public void testIsAssignable() {
        Class<?> paramTypes1 = Integer[].class;
        Class<?> paramTypes2 = int[].class;

        boolean result1 = TypeUtils.isAssignable(paramTypes1, paramTypes2);
        assertTrue(result1);

        boolean result2 = TypeUtils.isAssignable(paramTypes2, paramTypes1);
        assertTrue(result2);
    }

    private class SampleBean {

        @SuppressWarnings("unused")
        public void primitiveArray(int num) {
            log.debug("specified args: " + num);
        }

        @SuppressWarnings("unused")
        public void primitiveArray(int[] intArray) {
            log.debug("specified args: " + intArray);
        }

        @SuppressWarnings("unused")
        public String countTo10(Translet translet) {
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i <= 10; i++) {
                sb.append(i).append("\n");
            }

            return sb.toString();
        }
        
        @SuppressWarnings("unused")
        public void setSampleBean(SampleBean sampleBean) {
            log.debug("specified args: " + sampleBean);
        }
        
        @SuppressWarnings("unused")
        public void setSampleBean(SampleBean[] sampleBean) {
            log.debug("specified args: " + sampleBean);
        }

        @SuppressWarnings("unused")
        public void setSampleBean(List<SampleBean> list) {
            log.debug("specified args: " + list);
        }

//        @SuppressWarnings("unused")
//        public void setSampleBean(Object o) {
//            log.debug("specified args: " + o);
//        }
        
    }

    @SuppressWarnings("unused")
    private class ExtendedSampleBean extends SampleBean {
    }
    
}
