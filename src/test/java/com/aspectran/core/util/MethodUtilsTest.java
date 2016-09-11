/**
 * Copyright 2008-2016 Juho Jeong
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

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertNotNull;

import java.lang.reflect.Method;

import org.junit.Test;

import com.aspectran.core.activity.Translet;

/**
 * <p>Created: 2016. 2. 29.</p>
 */
public class MethodUtilsTest {

    @Test
    public void getMatchingAccessibleMethod() {
        Class<?>[] paramTypes = { Integer[].class };
        Method method = MethodUtils.getMatchingAccessibleMethod(SampleAction.class, "primitiveArray", paramTypes);
        assertNotNull(method);
    }

    @Test
    public void isAssignable() {
        Class<?> paramTypes1 = Integer[].class;
        Class<?> paramTypes2 = int[].class;
        boolean result = ClassUtils.isAssignable(paramTypes1, paramTypes2);
        assertTrue(result);
    }

    private class SampleAction {

        @SuppressWarnings("unused")
		public void primitiveArray(int[] intArray) {
        }

        @SuppressWarnings("unused")
		public String countTo10(Translet translet) {
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i <= 10; i++) {
                sb.append(i).append("\n");
            }

            return sb.toString();
        }

    }

}
